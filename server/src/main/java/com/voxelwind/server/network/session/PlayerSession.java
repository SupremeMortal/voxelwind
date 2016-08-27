package com.voxelwind.server.network.session;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.spotify.futures.CompletableFutures;
import com.voxelwind.api.game.inventories.PlayerInventory;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.level.Chunk;
import com.voxelwind.api.game.util.TextFormat;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.Skin;
import com.voxelwind.api.server.command.CommandException;
import com.voxelwind.api.server.command.CommandNotFoundException;
import com.voxelwind.api.server.event.player.PlayerSpawnEvent;
import com.voxelwind.api.server.player.GameMode;
import com.voxelwind.api.server.util.TranslatedMessage;
import com.voxelwind.server.game.inventories.InventoryObserver;
import com.voxelwind.server.game.inventories.VoxelwindBaseInventory;
import com.voxelwind.server.game.inventories.VoxelwindBasePlayerInventory;
import com.voxelwind.server.game.level.VoxelwindLevel;
import com.voxelwind.server.game.level.chunk.VoxelwindChunk;
import com.voxelwind.server.game.entities.*;
import com.voxelwind.server.game.level.util.Attribute;
import com.voxelwind.server.network.handler.NetworkPacketHandler;
import com.voxelwind.server.network.mcpe.packets.*;
import com.voxelwind.api.util.Rotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerSession extends LivingEntity implements Player, InventoryObserver {
    private static final int REQUIRED_TO_SPAWN = 56;
    private static final Logger LOGGER = LogManager.getLogger(PlayerSession.class);

    private final McpeSession session;
    private final Set<Vector2i> sentChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Long> isViewing = new HashSet<>();
    private GameMode gameMode = GameMode.SURVIVAL;
    private boolean spawned = false;
    private int viewDistance = 5;
    private final AtomicInteger windowIdGenerator = new AtomicInteger();
    private final BiMap<Integer, VoxelwindBaseInventory> openWindows = HashBiMap.create();
    private int openInventoryId = -1;
    private final PlayerInventory playerInventory = new VoxelwindBasePlayerInventory(this);

    public PlayerSession(McpeSession session, VoxelwindLevel level) {
        super(EntityTypeData.PLAYER, level, level.getSpawnLocation(), 20f);
        this.session = session;
    }

    @Override
    public boolean onTick() {
        if (!spawned) {
            // Don't tick until the player has truly been spawned into the world.
            return true;
        }

        if (!super.onTick()) {
            return false;
        }

        // If the upstream session is closed, the player session should no longer be alive.
        if (session.isClosed()) {
            return false;
        }

        return true;
    }

    @Override
    protected void setPosition(Vector3f position) {
        setPosition(position, false);
    }

    private void setPosition(Vector3f position, boolean internal) {
        Vector3f oldPosition = getPosition();
        super.setPosition(position);

        if (!internal) {
            sendMovePlayerPacket();
            if (hasSubstantiallyMoved(oldPosition, position)) {
                sendNewChunks();
                updateViewableEntities();
            }
        }
    }

    @Override
    public void setRotation(@Nonnull Rotation rotation) {
        setRotation(rotation, false);
    }

    private void setRotation(Rotation rotation, boolean internal) {
        super.setRotation(rotation);

        if (!internal) {
            sendMovePlayerPacket();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Do not use remove() on player sessions. Use disconnect() instead.");
    }

    @Override
    public boolean isRemoved() {
        return session.isClosed();
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);

        sendAttributes();
    }

    @Override
    protected void doDeath() {
        McpeEntityEvent event = new McpeEntityEvent();
        event.setEntityId(getEntityId());
        event.setEvent((byte) 3);
        getLevel().getPacketManager().queuePacketForViewers(this, event);

        Vector3f respawnLocation = getLevel().getSpawnLocation();

        McpeRespawn respawn = new McpeRespawn();
        respawn.setPosition(respawnLocation);
        session.addToSendQueue(respawn);

        setPosition(respawnLocation, true);
    }

    private void sendAttributes() {
        // Supported by MiNET:
        // - generic.health
        // - player.hunger
        // - player.level
        // - player.experience
        // - generic.movementSpeed
        // - generic.absorption
        Attribute health = new Attribute("generic.health", 0f, getMaximumHealth(), getHealth());
        Attribute hunger = new Attribute("player.hunger", 0f, 20f, 20f); // TODO: Implement hunger
        // TODO: Implement levels, movement speed, and absorption.

        McpeUpdateAttributes packet = new McpeUpdateAttributes();
        packet.getAttributes().add(health);
        packet.getAttributes().add(hunger);
        session.addToSendQueue(packet);
    }

    private void sendMovePlayerPacket() {
        McpeMovePlayer movePlayerPacket = new McpeMovePlayer();
        movePlayerPacket.setEntityId(getEntityId());
        movePlayerPacket.setPosition(getGamePosition());
        movePlayerPacket.setRotation(getRotation());
        movePlayerPacket.setMode(isTeleported());
        movePlayerPacket.setOnGround(isOnGround());
        session.addToSendQueue(movePlayerPacket);
    }

    public void doInitialSpawn() {
        // Fire PlayerSpawnEvent.
        // TODO: Fill this in from known player data.
        PlayerSpawnEvent event = new PlayerSpawnEvent(this, getLevel().getSpawnLocation(), getLevel(), Rotation.ZERO);
        session.getServer().getEventManager().fire(event);

        teleport(event.getSpawnLevel(), event.getSpawnLocation(), event.getRotation());
        resetStale(); // We haven't sent packets for other players yet, so staleness is superfluous

        // Send packets to spawn the player.
        McpeStartGame startGame = new McpeStartGame();
        startGame.setSeed(-1);
        startGame.setDimension((byte) 0);
        startGame.setGenerator(1);
        startGame.setGamemode(gameMode.ordinal());
        startGame.setEntityId(getEntityId());
        startGame.setSpawnLocation(getPosition().toInt());
        startGame.setPosition(getGamePosition());
        session.addToSendQueue(startGame);

        McpeAdventureSettings settings = new McpeAdventureSettings();
        settings.setPlayerPermissions(3);
        session.addToSendQueue(settings);

        McpeSetSpawnPosition spawnPosition = new McpeSetSpawnPosition();
        spawnPosition.setPosition(getLevel().getSpawnLocation().toInt());
        session.addToSendQueue(spawnPosition);
    }

    public McpeSession getUserSession() {
        return session;
    }

    NetworkPacketHandler getPacketHandler() {
        return new PlayerSessionNetworkPacketHandler();
    }

    private CompletableFuture<List<Chunk>> getChunksForRadius(int radius, boolean updateSent) {
        // Get current player's position in chunks.
        Vector3i positionAsInt = getPosition().toInt();
        int chunkX = positionAsInt.getX() >> 4;
        int chunkZ = positionAsInt.getZ() >> 4;

        // Now get and send chunk data.
        Set<Vector2i> chunksForRadius = new HashSet<>();
        List<CompletableFuture<Chunk>> completableFutures = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int newChunkX = chunkX + x, newChunkZ = chunkZ + z;
                Vector2i chunkCoords = new Vector2i(newChunkX, newChunkZ);
                chunksForRadius.add(chunkCoords);

                if (updateSent) {
                    if (!sentChunks.add(chunkCoords)) {
                        // Already sent, don't need to resend.
                        continue;
                    }
                }

                completableFutures.add(getLevel().getChunkProvider().get(newChunkX, newChunkZ));
            }
        }

        if (updateSent) {
            sentChunks.retainAll(chunksForRadius);
        }

        return CompletableFutures.allAsList(completableFutures);
    }

    public void disconnect(@Nonnull String reason) {
        session.disconnect(reason);
    }

    public void sendMessage(@Nonnull String message) {
        McpeText text = new McpeText();
        text.setType(McpeText.TextType.RAW);
        text.setMessage(message);
        session.addToSendQueue(text);
    }

    public void updateViewableEntities() {
        synchronized (isViewing) {
            Collection<BaseEntity> inView = getLevel().getEntityManager().getEntitiesInDistance(getPosition(), 64);
            Collection<Long> mustRemove = new ArrayList<>();
            Collection<BaseEntity> mustAdd = new ArrayList<>();
            for (Long id : isViewing) {
                Optional<BaseEntity> optional = getLevel().getEntityManager().findEntityById(id);
                if (optional.isPresent()) {
                    if (!inView.contains(optional.get())) {
                        mustRemove.add(id);
                    }
                } else {
                    mustRemove.add(id);
                }
            }

            for (BaseEntity entity : inView) {
                if (entity.getEntityId() == getEntityId()) {
                    continue;
                }

                if (isViewing.add(entity.getEntityId())) {
                    mustAdd.add(entity);
                }
            }
            isViewing.removeAll(mustRemove);

            for (Long id : mustRemove) {
                McpeRemoveEntity entity = new McpeRemoveEntity();
                entity.setEntityId(id);
                session.addToSendQueue(entity);
            }

            for (BaseEntity entity : mustAdd) {
                session.addToSendQueue(entity.createAddEntityPacket());
            }
        }
    }

    @Nonnull
    @Override
    public UUID getUniqueId() {
        return session.getAuthenticationProfile().getIdentity();
    }

    @Override
    public boolean isXboxAuthenticated() {
        return session.getAuthenticationProfile().getXuid() != null;
    }

    @Nonnull
    @Override
    public OptionalLong getXuid() {
        return session.getAuthenticationProfile().getXuid() == null ? OptionalLong.empty() :
                OptionalLong.of(session.getAuthenticationProfile().getXuid());
    }

    @Nonnull
    @Override
    public String getName() {
        return session.getAuthenticationProfile().getDisplayName();
    }

    @Nonnull
    @Override
    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    private void sendNewChunks() {
        getChunksForRadius(viewDistance, true).whenComplete((chunks, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Unable to load chunks for " + getUserSession().getAuthenticationProfile().getDisplayName(), throwable);
                disconnect("Internal server error");
                return;
            }

            for (Chunk chunk : chunks) {
                session.sendImmediatePackage(((VoxelwindChunk) chunk).getChunkDataPacket());
            }
        });
    }

    @Override
    public Skin getSkin() {
        return new Skin(session.getClientData().getSkinId(), session.getClientData().getSkinData());
    }

    @Nonnull
    @Override
    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public void setGameMode(@Nonnull GameMode mode) {
        GameMode oldGameMode = gameMode;
        gameMode = Preconditions.checkNotNull(mode, "mode");

        if (oldGameMode != gameMode && spawned) {
            McpeSetPlayerGameMode packet = new McpeSetPlayerGameMode();
            packet.setGamemode(mode.ordinal());
            session.addToSendQueue(packet);
        }
    }

    @Override
    public void sendTranslatedMessage(@Nonnull TranslatedMessage message) {
        Preconditions.checkNotNull(message, "message");
        McpeText text = new McpeText();
        text.setType(McpeText.TextType.TRANSLATE);
        text.setTranslatedMessage(message);
        session.addToSendQueue(text);
    }

    @Override
    public PlayerInventory getInventory() {
        return playerInventory;
    }

    public int getNextWindowId() {
        return windowIdGenerator.incrementAndGet() % 2;
    }

    @Override
    public void onInventoryChange(int slot, @Nullable ItemStack oldItem, @Nullable ItemStack newItem, VoxelwindBaseInventory inventory, @Nullable PlayerSession session) {
        Integer windowId = openWindows.inverse().get(inventory);
        if (windowId == null) {
            return;
        }

        if (session != null) {
            McpeContainerSetSlot packet = new McpeContainerSetSlot();
            packet.setSlot((short) slot);
            packet.setStack(newItem);
            packet.setWindowId(windowId.byteValue());
            this.session.addToSendQueue(packet);
        }
    }

    @Override
    public void onInventoryContentsReplacement(Map<Integer, ItemStack> newItems, VoxelwindBaseInventory inventory) {
        Integer windowId = openWindows.inverse().get(inventory);
        if (windowId == null) {
            return;
        }

        McpeContainerSetContents packet = new McpeContainerSetContents();
        packet.setWindowId(windowId.byteValue());
        packet.getStacks().putAll(newItems);
        session.addToSendQueue(packet);
    }

    private class PlayerSessionNetworkPacketHandler implements NetworkPacketHandler {
        @Override
        public void handle(McpeLogin packet) {
            throw new IllegalStateException("Login packet received but player session is currently active!");
        }

        @Override
        public void handle(McpeClientMagic packet) {
            throw new IllegalStateException("Client packet received but player session is currently active!");
        }

        @Override
        public void handle(McpeRequestChunkRadius packet) {
            int radius = Math.max(5, Math.min(16, packet.getRadius()));
            McpeChunkRadiusUpdated updated = new McpeChunkRadiusUpdated();
            updated.setRadius(radius);
            session.addToSendQueue(updated);
            viewDistance = radius;

            getChunksForRadius(radius, true).whenComplete((chunks, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Unable to load chunks for " + getUserSession().getAuthenticationProfile().getDisplayName(), throwable);
                    disconnect("Internal server error");
                    return;
                }

                // Sort the chunks to be sent by whichever is closest to the spawn chunk for smoother loading.
                Vector3f spawnPosition = getPosition();
                int spawnChunkX = spawnPosition.getFloorX() >> 4;
                int spawnChunkZ = spawnPosition.getFloorZ() >> 4;
                Vector2i originCoord = new Vector2i(spawnChunkX, spawnChunkZ);
                Collections.sort(chunks, (o1, o2) -> {
                    Vector2i o1Coord = new Vector2i(o1.getX(), o1.getZ());
                    Vector2i o2Coord = new Vector2i(o2.getX(), o2.getZ());

                    // Use whichever is closest to the origin.
                    return Integer.compare(o1Coord.distanceSquared(originCoord),
                            o2Coord.distanceSquared(originCoord));
                });

                int sent = 0;

                for (Chunk chunk : chunks) {
                    session.sendImmediatePackage(((VoxelwindChunk) chunk).getChunkDataPacket());
                    sent++;

                    if (!spawned && sent >= REQUIRED_TO_SPAWN) {
                        McpePlayStatus status = new McpePlayStatus();
                        status.setStatus(McpePlayStatus.Status.PLAYER_SPAWN);
                        session.sendImmediatePackage(status);

                        McpeSetTime setTime = new McpeSetTime();
                        setTime.setTime(getLevel().getTime());
                        setTime.setRunning(true);
                        session.sendImmediatePackage(setTime);

                        spawned = true;

                        McpeRespawn respawn = new McpeRespawn();
                        respawn.setPosition(getPosition());
                        session.sendImmediatePackage(respawn);

                        updateViewableEntities();
                        sendAttributes();
                    }
                }
            });
        }

        @Override
        public void handle(McpePlayerAction packet) {
            switch (packet.getAction()) {
                case ACTION_START_BREAK:
                    // Fire interact
                    break;
                case ACTION_ABORT_BREAK:
                    // No-op
                    break;
                case ACTION_STOP_BREAK:
                    // No-op
                    break;
                case ACTION_RELEASE_ITEM:
                    // Drop item, shoot bow, or dump bucket?
                    break;
                case ACTION_STOP_SLEEPING:
                    // Stop sleeping
                    break;
                case ACTION_SPAWN_SAME_DIMENSION:
                    // Clean up attributes?
                    break;
                case ACTION_JUMP:
                    // No-op
                    break;
                case ACTION_START_SPRINT:
                    sprinting = true;
                    sendAttributes();
                    break;
                case ACTION_STOP_SPRINT:
                    sprinting = false;
                    sendAttributes();
                    break;
                case ACTION_START_SNEAK:
                    sneaking = true;
                    sendAttributes();
                    break;
                case ACTION_STOP_SNEAK:
                    sneaking = false;
                    sendAttributes();
                    break;
                case ACTION_SPAWN_OVERWORLD:
                    // Clean up attributes?
                    break;
                case ACTION_SPAWN_NETHER:
                    // Clean up attributes?
                    break;
            }

            McpeSetEntityData dataPacket = new McpeSetEntityData();
            dataPacket.getMetadata().put(0, getFlagValue());
            getLevel().getPacketManager().queuePacketForViewers(PlayerSession.this, dataPacket);
        }

        @Override
        public void handle(McpeAnimate packet) {
            getLevel().getPacketManager().queuePacketForPlayers(packet);
        }

        @Override
        public void handle(McpeText packet) {
            // Debugging commands.
            if (packet.getMessage().startsWith("/")) {
                String command = packet.getMessage().substring(1);
                try {
                    session.getServer().getCommandManager().executeCommand(PlayerSession.this, command);
                } catch (CommandNotFoundException e) {
                    sendMessage(TextFormat.RED + "No such command found.");
                } catch (CommandException e) {
                    LOGGER.error("Error while running command '{}' for {}", command, getName(), e);
                    sendMessage(TextFormat.RED + "An error has occurred while running the command.");
                }
                return;
            }

            // By default, queue this packet for all players in the world.
            getLevel().getPacketManager().queuePacketForPlayers(packet);
        }

        @Override
        public void handle(McpeMovePlayer packet) {
            // TODO: We may do well to perform basic anti-cheat
            Vector3f originalPosition = getPosition();
            Vector3f newPosition = packet.getPosition().sub(0, 1.62, 0);

            setPosition(newPosition, true);
            setRotation(packet.getRotation(), true);

            // If we haven't moved in the X or Z axis, don't update viewable entities or try updating chunks - they haven't changed.
            if (hasSubstantiallyMoved(originalPosition, newPosition)) {
                updateViewableEntities();
                sendNewChunks();
            }
        }

        @Override
        public void handle(McpeContainerClose packet) {

        }

        @Override
        public void handle(McpeContainerSetSlot packet) {
            VoxelwindBaseInventory window = null;
            if (openInventoryId < 0 || openInventoryId != packet.getWindowId()) {
                // There's no inventory open, so it's probably the player inventory.
                if (packet.getWindowId() == 0) {
                    window = (VoxelwindBaseInventory) playerInventory;
                } else if (packet.getWindowId() == 0x78) {
                    // It's the armor inventory.
                    switch (packet.getSlot()) {
                        case 0:
                            getEquipment().setHelmet(packet.getStack());
                            break;
                        case 1:
                            getEquipment().setChestplate(packet.getStack());
                            break;
                        case 2:
                            getEquipment().setLeggings(packet.getStack());
                            break;
                        case 3:
                            getEquipment().setBoots(packet.getStack());
                            break;
                    }
                    return;
                }
            } else {
                window = openWindows.get((int) packet.getWindowId());
            }

            if (window == null) {
                return;
            }

            window.setItem(packet.getSlot(), packet.getStack(), PlayerSession.this);
        }
    }

    private static boolean hasSubstantiallyMoved(Vector3f oldPos, Vector3f newPos) {
        return (Float.compare(oldPos.getX(), newPos.getX()) != 0 || Float.compare(oldPos.getZ(), newPos.getZ()) != 0);
    }
}
