package com.voxelwind.server.network.session;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.spotify.futures.CompletableFutures;
import com.voxelwind.api.game.entities.Entity;
import com.voxelwind.api.game.entities.components.*;
import com.voxelwind.api.game.entities.components.system.System;
import com.voxelwind.api.game.entities.components.system.SystemRunner;
import com.voxelwind.api.game.entities.misc.DroppedItem;
import com.voxelwind.api.game.inventories.Inventory;
import com.voxelwind.api.game.inventories.OpenableInventory;
import com.voxelwind.api.game.inventories.PlayerInventory;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.level.Chunk;
import com.voxelwind.api.game.level.Level;
import com.voxelwind.api.game.level.block.Block;
import com.voxelwind.api.game.level.block.BlockType;
import com.voxelwind.api.game.level.block.BlockTypes;
import com.voxelwind.api.game.util.TextFormat;
import com.voxelwind.api.game.util.data.BlockFace;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.command.CommandException;
import com.voxelwind.api.server.command.CommandNotFoundException;
import com.voxelwind.api.server.event.block.BlockReplaceEvent;
import com.voxelwind.api.server.event.player.PlayerJoinEvent;
import com.voxelwind.api.server.event.player.PlayerSpawnEvent;
import com.voxelwind.api.server.player.GameMode;
import com.voxelwind.api.server.player.PlayerMessageDisplayType;
import com.voxelwind.api.server.player.PopupMessage;
import com.voxelwind.api.server.player.TranslatedMessage;
import com.voxelwind.api.util.Rotation;
import com.voxelwind.server.VoxelwindServer;
import com.voxelwind.server.command.VoxelwindCommandManager;
import com.voxelwind.server.game.entities.BaseEntity;
import com.voxelwind.server.game.entities.EntityTypeData;
import com.voxelwind.server.game.entities.LivingEntity;
import com.voxelwind.server.game.entities.components.HealthComponent;
import com.voxelwind.server.game.entities.components.PlayerDataComponent;
import com.voxelwind.server.game.entities.misc.VoxelwindDroppedItem;
import com.voxelwind.server.game.entities.systems.DeathSystem;
import com.voxelwind.server.game.inventories.*;
import com.voxelwind.server.game.inventories.transaction.*;
import com.voxelwind.server.game.inventories.transaction.record.TransactionRecord;
import com.voxelwind.server.game.inventories.transaction.record.WorldInteractionTransactionRecord;
import com.voxelwind.server.game.item.VoxelwindItemStack;
import com.voxelwind.server.game.level.VoxelwindLevel;
import com.voxelwind.server.game.level.block.BasicBlockState;
import com.voxelwind.server.game.level.block.BlockBehavior;
import com.voxelwind.server.game.level.block.BlockBehaviors;
import com.voxelwind.server.game.level.block.behaviors.BehaviorUtils;
import com.voxelwind.server.game.level.chunk.util.FullChunkPacketCreator;
import com.voxelwind.server.game.level.util.BoundingBox;
import com.voxelwind.server.game.level.util.Gamerule;
import com.voxelwind.server.game.level.util.PlayerAttribute;
import com.voxelwind.server.game.permissions.PermissionLevel;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.packets.*;
import com.voxelwind.server.network.mcpe.util.ActionPermissionFlag;
import com.voxelwind.server.network.raknet.handler.NetworkPacketHandler;
import com.voxelwind.server.network.session.auth.PlayerRecord;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class PlayerSession extends LivingEntity implements Player, InventoryObserver, DeathSystem.CustomDeath {
    private final McpeSession session;
    private final Set<Vector2i> sentChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final TLongSet isViewing = new TLongHashSet();
    private boolean spawned = false;
    private int viewDistance = 5;
    private final AtomicInteger windowIdGenerator = new AtomicInteger();
    private byte openInventoryId = -1;
    private boolean hasMoved = false;
    private final VoxelwindServer vwServer;
    private final VoxelwindBasePlayerInventory playerInventory = new VoxelwindBasePlayerInventory(this);
    private final Set<UUID> playersSentForList = new HashSet<>();
    private final List<Gamerule> gamerules = new ArrayList<>();
    private Inventory openedInventory;
    private boolean flying;

    public PlayerSession(McpeSession session, VoxelwindLevel level) {
        super(EntityTypeData.PLAYER, level, level.getSpawnLocation(), session.getServer(), 20);
        this.session = session;
        this.vwServer = session.getServer();

        this.registerComponent(PlayerData.class, new PlayerDataComponent(this));
    }

    @Override
    public NetworkPackage createAddEntityPacket() {
        McpeAddPlayer addPlayer = new McpeAddPlayer();
        addPlayer.setUuid(session.getAuthenticationProfile().getIdentity());
        addPlayer.setUsername(session.getAuthenticationProfile().getDisplayName());
        addPlayer.setEntityId(getEntityId());
        addPlayer.setRuntimeEntityId(getEntityId());
        addPlayer.setPosition(getPosition());
        addPlayer.setVelocity(getMotion());
        addPlayer.setRotation(getRotation());
        addPlayer.setHeld(playerInventory.getStackInHand().orElse(null));
        addPlayer.getMetadata().putAll(getMetadata());
        addPlayer.setFlags(0);
        addPlayer.setCommandPermission(1);
        addPlayer.setActionPermissions(ActionPermissionFlag.DEFAULT, true);
        addPlayer.setPermissionLevel(PermissionLevel.OPERATOR);
        addPlayer.setCustomPermissions(0);
        addPlayer.setUserId(getEntityId());
        return addPlayer;
    }

    private void pickupAdjacent() {
        BoundingBox box = getBoundingBox().grow(0.5f, 0.25f, 0.5f);
        for (BaseEntity entity : getLevel().getEntityManager().getEntitiesInBounds(box)) {
            Optional<PickupDelay> delay = entity.get(PickupDelay.class);
            if (delay.isPresent()) {
                if (delay.get().canPickup()) {
                    if (entity instanceof DroppedItem) {
                        ContainedItem item = entity.ensureAndGet(ContainedItem.class);
                        if (playerInventory.addItem(item.getItemStack())) {
                            McpeTakeItemEntity packetBroadcast = new McpeTakeItemEntity();
                            packetBroadcast.setItemEntityId(entity.getEntityId());
                            packetBroadcast.setPlayerEntityId(getEntityId());
                            getLevel().getPacketManager().queuePacketForViewers(this, packetBroadcast);

                            McpeTakeItemEntity packetSelf = new McpeTakeItemEntity();
                            packetSelf.setItemEntityId(entity.getEntityId());
                            packetSelf.setPlayerEntityId(0);
                            session.addToSendQueue(packetSelf);

                            entity.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void setPosition(Vector3f position) {
        super.setPosition(position);
        hasMoved = true;
    }

    @Override
    public void setRotation(@Nonnull Rotation rotation) {
        super.setRotation(rotation);
        hasMoved = true;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Do not use remove() on player sessions. Use disconnect() instead.");
    }

    void removeInternal() {
        super.remove();
    }

    @Override
    public void doDeath() {
        sendAttributes(); // this will trigger client-side death
    }

    private void sendAttributes() {
        Health healthComponent = ensureAndGet(Health.class);
        PlayerData playerDataComponent = ensureAndGet(PlayerData.class);
        PlayerAttribute health = new PlayerAttribute("minecraft:health", 0f, healthComponent.getMaximumHealth(),
                Math.max(0, healthComponent.getHealth()), healthComponent.getMaximumHealth());
        PlayerAttribute hunger = new PlayerAttribute("minecraft:player.hunger", 0f, 20f, playerDataComponent.getHunger(), 20f); // TODO: Implement hunger
        float effectiveSpeed = sprinting ? (float) Math.min(0.5f, playerDataComponent.getBaseSpeed() * 1.3) : playerDataComponent.getBaseSpeed();
        PlayerAttribute speed = new PlayerAttribute("minecraft:movement", 0, 0.5f, effectiveSpeed, 0.1f);
        // TODO: Implement levels, movement speed, and absorption.

        McpeUpdateAttributes packet = new McpeUpdateAttributes();
        packet.setRuntimeEntityId(getEntityId());
        packet.getAttributes().add(health);
        packet.getAttributes().add(hunger);
        packet.getAttributes().add(speed);
        session.addToSendQueue(packet);
    }

    private void sendMovePlayerPacket() {
        McpeMovePlayer movePlayerPacket = new McpeMovePlayer();
        movePlayerPacket.setRuntimeEntityId(getEntityId());
        movePlayerPacket.setPosition(getGamePosition());
        movePlayerPacket.setRotation(getRotation());
        movePlayerPacket.setMode((isTeleported() ? McpeMovePlayer.Mode.TELEPORT : McpeMovePlayer.Mode.NORMAL));
        if (isTeleported()) {
            movePlayerPacket.setTeleportationCause(McpeMovePlayer.TeleportationCause.UNKNOWN);
        }
        movePlayerPacket.setOnGround(isOnGround());
        movePlayerPacket.setRidingEntityId(0);
        session.addToSendQueue(movePlayerPacket);
    }

    private void doInitialSpawn() {
        log.info("{} ({}) has logged in.", getName(), getRemoteAddress().map(Object::toString).orElse("UNKNOWN"));

        // Fire PlayerSpawnEvent.
        // TODO: Fill this in of known player data.
        PlayerSpawnEvent event = new PlayerSpawnEvent(this, getLevel().getSpawnLocation(), getLevel(), Rotation.ZERO);
        session.getServer().getEventManager().fire(event);

        if (getLevel() != event.getSpawnLevel()) {
            getLevel().getEntityManager().unregister(this);
            ((VoxelwindLevel) event.getSpawnLevel()).getEntityManager().register(this);
            setEntityId(((VoxelwindLevel) event.getSpawnLevel()).getEntityManager().allocateEntityId());
        }
        setPosition(event.getSpawnLocation());
        setRotation(event.getRotation());
        hasMoved = false; // don't send duplicated packets

        PlayerData playerDataComponent = ensureAndGet(PlayerData.class);

        initializeGamerules();
        // Send packets to spawn the player
        McpeStartGame startGame = new McpeStartGame();
        startGame.setEntityId(getEntityId());
        startGame.setRuntimeEntityId(getEntityId());
        startGame.setPlayerGamemode(playerDataComponent.getGameMode().ordinal());
        startGame.setSpawn(getGamePosition());
        startGame.setPitch(event.getRotation().getPitch());
        startGame.setYaw(event.getRotation().getYaw());
        startGame.setSeed((int)getLevel().getSeed());
        startGame.setDimension(0);
        startGame.setGenerator(1);
        startGame.setWorldGamemode(0);
        startGame.setDifficulty(1);
        startGame.setWorldSpawn(getLevel().getSpawnLocation().toInt());
        startGame.setHasAchievementsDisabled(true);
        startGame.setDayCycleStopTime(getLevel().getTime());
        startGame.setEduMode(getMcpeSession().getClientData().isEduMode());
        startGame.setRainLevel(0F);
        startGame.setLightingLevel(0F);
        startGame.setMultiplayer(true);
        startGame.setBroadcastToLan(true);
        startGame.setBroadcastToXbl(true);
        startGame.setEnableCommands(true);
        startGame.setTexturepacksRequired(false);
        startGame.getGameRules().addAll(getGameRules());
        startGame.setBonusChest(false);
        startGame.setMapEnabled(true);
        startGame.setTrustPlayers(true);
        startGame.setPermissionLevel(PermissionLevel.CUSTOM);
        startGame.setGamePublishSettings(3);
        startGame.setLevelId("SECRET");
        startGame.setWorldName(getLevel().getName());
        startGame.setPremiumWorldTemplateId("");
        startGame.setTrial(false);
        startGame.setCurrentTick(0);
        startGame.setEnchantmentSeed(0);
        session.addToSendQueue(startGame);

        McpeSetTime setTime = new McpeSetTime();
        setTime.setTime(getLevel().getTime());
        session.addToSendQueue(setTime);

        sendAttributes();
        sendPlayerInventory();
    }

    public McpeSession getMcpeSession() {
        return session;
    }

    NetworkPacketHandler getPacketHandler() {
        return new PlayerSessionNetworkPacketHandler();
    }

    private CompletableFuture<List<Chunk>> getChunksForRadius(int radius) {
        // Get current player's position in chunk coordinates.
        int chunkX = getPosition().getFloorX() >> 4;
        int chunkZ = getPosition().getFloorZ() >> 4;

        // Now get and send chunk data.
        Set<Vector2i> chunksForRadius = new HashSet<>();
        List<CompletableFuture<Chunk>> completableFutures = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int newChunkX = chunkX + x, newChunkZ = chunkZ + z;
                Vector2i chunkCoords = new Vector2i(newChunkX, newChunkZ);
                chunksForRadius.add(chunkCoords);

                if (!sentChunks.add(chunkCoords)) {
                    // Already sent, don't need to resend.
                    continue;
                }

                completableFutures.add(getLevel().getChunk(newChunkX, newChunkZ));
            }
        }

        sentChunks.retainAll(chunksForRadius);

        return CompletableFutures.allAsList(completableFutures);
    }

    private void sendAdventureSettings() {
        PlayerData playerData = ensureAndGet(PlayerData.class);
        McpeAdventureSettings settings = new McpeAdventureSettings();
        boolean spectator = (playerData.getGameMode() == GameMode.SPECTATOR);

        settings.setFlags(McpeAdventureSettings.Flag.ALLOW_FLIGHT, playerData.isAllowedToFly());
        settings.setFlags(McpeAdventureSettings.Flag.FLYING, playerData.isFlying());
        settings.setFlags(McpeAdventureSettings.Flag.NO_CLIP, playerData.canNoClip());
        settings.setFlags(McpeAdventureSettings.Flag.AUTO_JUMP, playerData.canAutoJump());
        settings.setFlags(McpeAdventureSettings.Flag.IMMUTABLE_WORLD, playerData.isImmutableWorld());
        settings.setFlags(McpeAdventureSettings.Flag.MUTED, playerData.isMuted());
        settings.setFlags(McpeAdventureSettings.Flag.WORLD_BUILDER, playerData.isWorldBuilder());
        settings.setFlags(McpeAdventureSettings.Flag.NO_PVP, !playerData.canPvP() || spectator);
        settings.setFlags(McpeAdventureSettings.Flag.NO_MVP, !playerData.canPvP() || spectator);
        settings.setFlags(McpeAdventureSettings.Flag.NO_PVM, !playerData.canPvP() || spectator);
        settings.setCommandPermissions(1);
        settings.setPermissionLevel(PermissionLevel.OPERATOR);
        settings.setCustomStoredPermissions(0);
        settings.setActionPermissions(ActionPermissionFlag.DEFAULT, true);
        settings.setUserId(getEntityId());

        session.addToSendQueue(settings);
    }

    private void initializeGamerules() {
        gamerules.add(new Gamerule<>("drowningdamage", true));
        gamerules.add(new Gamerule<>("dotiledrops", true));
        gamerules.add(new Gamerule<>("commandblockoutput", true));
        gamerules.add(new Gamerule<>("domobloot", true));
        gamerules.add(new Gamerule<>("dodaylightcycle", true));
        gamerules.add(new Gamerule<>("keepinventory", false));
        gamerules.add(new Gamerule<>("domobspawning", true));
        gamerules.add(new Gamerule<>("doentitydrops", true));
        gamerules.add(new Gamerule<>("dofiretick", true));
        gamerules.add(new Gamerule<>("doweathercycle", true));
        gamerules.add(new Gamerule<>("falldamage", true));
        gamerules.add(new Gamerule<>("pvp", true));
        gamerules.add(new Gamerule<>("firedamage", true));
        gamerules.add(new Gamerule<>("mobgriefing", true));
        gamerules.add(new Gamerule<>("sendcommandfeedback", true));
        gamerules.add(new Gamerule<>("showcoordinates", true));
        gamerules.add(new Gamerule<>("donaturalregeneration", true));
        if (!getMcpeSession().getClientData().isEduMode()) return; // No point in sending useless gamerules.
        gamerules.add(new Gamerule<>("globalmute", false));
        gamerules.add(new Gamerule<>("allowdestructiveobjects", true));
        gamerules.add(new Gamerule<>("allowmobs", true));
    }

    public List<Gamerule> getGameRules() {
        return gamerules;
    }

    @Override
    public void disconnect(@Nonnull String reason) {
        session.disconnect(reason);
    }

    @Override
    public void sendMessage(@Nonnull String message) {
        Preconditions.checkNotNull(message, "message");
        McpeText text = new McpeText();
        text.setType(McpeText.TextType.RAW);
        text.setXuid(Long.toString(session.getAuthenticationProfile().getXuid()));
        text.setMessage(message);
        session.addToSendQueue(text);
    }

    public void updateViewableEntities() {
        synchronized (isViewing) {
            Collection<BaseEntity> inView = getLevel().getEntityManager().getEntitiesInDistance(getPosition(), 64);
            TLongSet mustRemove = new TLongHashSet();
            Collection<BaseEntity> mustAdd = new ArrayList<>();

            isViewing.forEach(id -> {
                Optional<BaseEntity> optional = getLevel().getEntityManager().findEntityById(id);
                if (optional.isPresent()) {
                    if (!inView.contains(optional.get())) {
                        mustRemove.add(id);
                    }
                } else {
                    mustRemove.add(id);
                }
                return true;
            });

            for (BaseEntity entity : inView) {
                if (entity.getEntityId() == getEntityId()) {
                    continue;
                }

                // Check if user has loaded the chunk, otherwise the client will crash
                Vector2i chunkVector = new Vector2i(entity.getPosition().getFloorX() >> 4, entity.getPosition().getFloorZ() >> 4);
                if (sentChunks.contains(chunkVector) && isViewing.add(entity.getEntityId())) {
                    mustAdd.add(entity);
                }
            }

            isViewing.removeAll(mustRemove);

            mustRemove.forEach(id -> {
                McpeRemoveEntity entity = new McpeRemoveEntity();
                entity.setEntityId(id);
                session.addToSendQueue(entity);
                return true;
            });

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
    public Optional<InetSocketAddress> getRemoteAddress() {
        return session.getRemoteAddress();
    }

    private CompletableFuture<List<Chunk>> sendNewChunks() {
        return getChunksForRadius(viewDistance).whenComplete((chunks, throwable) -> {
            if (throwable != null) {
                log.error("Unable to load chunks for " + getMcpeSession().getAuthenticationProfile().getDisplayName(), throwable);
                disconnect("Internal server error");
                return;
            }

            // Sort by whichever chunks are closest to the player for smoother loading
            Vector3f currentPosition = getPosition();
            int currentChunkX = currentPosition.getFloorX() >> 4;
            int currentChunkZ = currentPosition.getFloorZ() >> 4;
            chunks.sort(new AroundPointComparator(currentChunkX, currentChunkZ));

            for (Chunk chunk : chunks) {
                session.sendImmediatePackage(((FullChunkPacketCreator) chunk).toFullChunkData());
            }
        });
    }

    @Override
    public void sendMessage(@Nonnull String message, @Nonnull PlayerMessageDisplayType type) {
        Preconditions.checkNotNull(message, "message");
        Preconditions.checkNotNull(type, "type");
        McpeText text = new McpeText();
        switch (type) {
            case CHAT:
                text.setType(McpeText.TextType.RAW);
                break;
            case TIP:
                text.setType(McpeText.TextType.TIP);
                break;
            case POPUP:
                text.setType(McpeText.TextType.POPUP);
                text.setSource(""); // TODO: Is it worth adding a caption for this?
                break;
        }
        text.setMessage(message);
        text.setXuid(Long.toString(session.getAuthenticationProfile().getXuid()));
        session.addToSendQueue(text);
    }

    @Override
    public void sendTranslatedMessage(@Nonnull TranslatedMessage message) {
        Preconditions.checkNotNull(message, "message");
        McpeText text = new McpeText();
        text.setType(McpeText.TextType.TRANSLATION);
        text.setTranslatedMessage(message);
        session.addToSendQueue(text);
    }

    @Override
    public void sendPopupMessage(@Nonnull PopupMessage message) {
        Preconditions.checkNotNull(message, "message");
        McpeText text = new McpeText();
        text.setType(McpeText.TextType.POPUP);
        text.setSource(message.getCaption());
        text.setMessage(message.getMessage());
        session.addToSendQueue(text);
    }

    @Override
    public PlayerInventory getInventory() {
        return playerInventory;
    }

    @Override
    public Optional<Inventory> getOpenedInventory() {
        return Optional.ofNullable(openedInventory);
    }


    @Override
    public void openInventory(Inventory inventory) {
        Preconditions.checkNotNull(inventory, "inventory");
        Preconditions.checkArgument(inventory instanceof VoxelwindBaseOpenableInventory, "inventory is not a valid type that can be opened");
        Preconditions.checkState(openedInventory == null, "inventory already opened");

        VoxelwindInventoryType internalType = VoxelwindInventoryType.fromApi(inventory.getInventoryType());
        byte windowId = internalType.getWindowId(this);
        openedInventory = inventory;
        openInventoryId = windowId;

        McpeContainerOpen openPacket = new McpeContainerOpen();
        openPacket.setWindowId(windowId);
        openPacket.setType(internalType.getType());
        openPacket.setPosition(((OpenableInventory) inventory).getPosition());
        openPacket.setRuntimeEntityId(-1);
        session.addToSendQueue(openPacket);

        McpeInventoryContent contents = new McpeInventoryContent();
        contents.setInventoryId(windowId);
        contents.setStacks(inventory.getAllContents());

        McpeWrapper contentsWrapper = new McpeWrapper();
        contentsWrapper.getPackets().add(contents);
        session.addToSendQueue(contentsWrapper);

        ((VoxelwindBaseInventory) openedInventory).getObserverList().add(this);
    }

    @Override
    public void closeInventory() {
        Preconditions.checkState(openedInventory != null, "inventory not opened");
        McpeContainerClose close = new McpeContainerClose();
        close.setWindowId(openInventoryId);
        session.addToSendQueue(close);

        ((VoxelwindBaseInventory) openedInventory).getObserverList().remove(this);
        openedInventory = null;
        openInventoryId = -1;
    }

    public byte getNextWindowId() {
        return (byte) (1 + (windowIdGenerator.incrementAndGet() % 2));
    }

    public boolean isChunkInView(int x, int z) {
        return sentChunks.contains(new Vector2i(x, z));
    }

    @Override
    public void onInventoryChange(int slot, @Nullable ItemStack oldItem, @Nullable ItemStack newItem, VoxelwindBaseInventory inventory, @Nullable PlayerSession session) {
        byte windowId;
        if (inventory == openedInventory) {
            windowId = openInventoryId;
        } else if (inventory instanceof PlayerInventory) {
            windowId = 0x00;
        } else {
            return;
        }

        if (session != this) {
            McpeInventorySlot packet = new McpeInventorySlot();
            packet.setSlot((short) slot);
            packet.setStack(newItem);
            packet.setInventoryId(windowId);
            this.session.addToSendQueue(packet);
        }
    }

    private void breakBlock(Vector3i position) {
        PlayerData playerData = ensureAndGet(PlayerData.class);
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;

        Optional<Chunk> chunkOptional = getLevel().getChunkIfLoaded(chunkX, chunkZ);
        if (!chunkOptional.isPresent()) {
            // Chunk not loaded, danger ahead!
            log.error("{} tried to remove block at unloaded chunk ({}, {})", getName(), chunkX, chunkZ);
            return;
        }

        int inChunkX = position.getX() & 0x0f;
        int inChunkZ = position.getZ() & 0x0f;

        Block block = chunkOptional.get().getBlock(inChunkX, position.getY(), inChunkZ);
        // Call BlockReplaceEvent.
        BlockReplaceEvent event = new BlockReplaceEvent(block, block.getBlockState(), new BasicBlockState(BlockTypes.AIR, null, null),
                PlayerSession.this, BlockReplaceEvent.ReplaceReason.PLAYER_BREAK);
        getServer().getEventManager().fire(event);
        if (event.getResult() == BlockReplaceEvent.Result.CONTINUE) {
            if (playerData.getGameMode() != GameMode.CREATIVE) {
                BlockBehavior blockBehavior = BlockBehaviors.getBlockBehavior(block.getBlockState().getBlockType());
                if (!blockBehavior.handleBreak(getServer(), PlayerSession.this, block, playerInventory.getStackInHand().orElse(null))) {
                    Collection<ItemStack> drops = blockBehavior.getDrops(getServer(), PlayerSession.this, block, playerInventory.getStackInHand().orElse(null));
                    for (ItemStack drop : drops) {
                        DroppedItem item = getLevel().dropItem(drop, block.getLevelLocation().toFloat().add(0.5, 0.5, 0.5));
                        item.ensureAndGet(PickupDelay.class).setDelayPickupTicks(5);
                    }
                    chunkOptional.get().setBlock(inChunkX, position.getY(), inChunkZ, new BasicBlockState(BlockTypes.AIR, null, null));
                }
            } else {
                chunkOptional.get().setBlock(inChunkX, position.getY(), inChunkZ, new BasicBlockState(BlockTypes.AIR, null, null));
            }
        }

        getLevel().broadcastBlockUpdate(position);
    }

    @Override
    public void onInventoryContentsReplacement(ItemStack[] newItems, VoxelwindBaseInventory inventory) {
        byte windowId;
        if (inventory == openedInventory) {
            windowId = openInventoryId;
        } else if (inventory instanceof PlayerInventory) {
            windowId = 0x00;
        } else {
            return;
        }

        McpeInventoryContent packet = new McpeInventoryContent();
        packet.setInventoryId(windowId);
        packet.setStacks(newItems);

        McpeWrapper contentsWrapper = new McpeWrapper();
        contentsWrapper.getPackets().add(packet);
        session.addToSendQueue(contentsWrapper);
    }

    private void broadcastSetEntityData() {
        McpeSetEntityData dataPacket = new McpeSetEntityData();
        dataPacket.setRuntimeEntityId(getEntityId());
        dataPacket.getMetadata().put(0, getFlagValue());
        getLevel().getPacketManager().queuePacketForViewers(PlayerSession.this, dataPacket);
    }

    private void sendSetEntityData() {
        McpeSetEntityData dataPacket = new McpeSetEntityData();
        dataPacket.setRuntimeEntityId(getEntityId());
        dataPacket.getMetadata().putAll(getMetadata());
        session.sendImmediatePackage(dataPacket);
    }

    private void sendPlayerInventory() {
        McpeInventoryContent initContents = new McpeInventoryContent();
        initContents.setInventoryId(0x7b);
        initContents.setStacks(new ItemStack[]{});
        session.addToSendQueue(initContents);

        // Because MCPE is stupid, we have to add 9 more slots. The rest will be filled in as air.
        McpeInventoryContent inventoryContents = new McpeInventoryContent();
        inventoryContents.setInventoryId(0x00);
        inventoryContents.setStacks(Arrays.copyOf(playerInventory.getAllContents(), playerInventory.getInventoryType().getInventorySize() + 9));

        session.addToSendQueue(inventoryContents);

        ArmorEquipment armorEquipment = ensureAndGet(ArmorEquipment.class);
        McpeInventoryContent armorContent = new McpeInventoryContent();
        armorContent.setInventoryId(0x78);
        armorContent.setStacks(new ItemStack[]{
                armorEquipment.getHelmet().orElse(null),
                armorEquipment.getChestplate().orElse(null),
                armorEquipment.getLeggings().orElse(null),
                armorEquipment.getBoots().orElse(null),
        });

        session.addToSendQueue(armorContent);

        McpeMobEquipment mobEquipment = new McpeMobEquipment();
        mobEquipment.setRuntimeEntityId(getEntityId());
        mobEquipment.setStack(getInventory().getStackInHand().orElse(null));
        mobEquipment.setInventorySlot((byte) getInventory().getHeldInventorySlot());

        session.addToSendQueue(mobEquipment);
    }

    @Override
    public void teleport(@Nonnull Level level, @Nonnull Vector3f position, @Nonnull Rotation rotation) {
        Level oldLevel = getLevel();
        super.teleport(level, position, rotation);

        if (oldLevel != level) {
            doDimensionChange();
        }
    }

    private void doDimensionChange() {
        // Reset spawned status
        spawned = false;
        sentChunks.clear();

        // Create the packets we will send to do the dimension change
        McpeChangeDimension changeDim0 = new McpeChangeDimension();
        changeDim0.setPosition(getGamePosition());
        changeDim0.setDimension(0);

        McpeChangeDimension changeDim1 = new McpeChangeDimension();
        changeDim1.setPosition(getGamePosition());
        changeDim1.setDimension(1);

        McpePlayStatus doRespawnPacket = new McpePlayStatus();
        doRespawnPacket.setStatus(McpePlayStatus.Status.PLAYER_SPAWN);

        // Send in order: DIM0, respawn, DIM1, respawn, empty chunks, DIM1, respawn, DIM0, respawn, actual chunks, McpeRespawn
        session.sendImmediatePackage(changeDim0);
        session.sendImmediatePackage(doRespawnPacket);
        session.sendImmediatePackage(changeDim1);
        session.sendImmediatePackage(doRespawnPacket);

        // Send a bunch of empty chunks around the new position.
        int chunkX = getPosition().getFloorX() >> 4;
        int chunkZ = getPosition().getFloorX() >> 4;

        for (int x = -3; x < 3; x++) {
            for (int z = -3; z < 3; z++) {
                McpeFullChunkData data = new McpeFullChunkData();
                data.setChunkX(chunkX + x);
                data.setChunkZ(chunkZ + z);
                data.setData(new byte[0]);
                session.sendImmediatePackage(data);
            }
        }

        // Finish sending the dimension change and respawn packets.
        session.sendImmediatePackage(changeDim1);
        session.sendImmediatePackage(doRespawnPacket);
        session.sendImmediatePackage(changeDim0);
        session.sendImmediatePackage(doRespawnPacket);

        // Now send the real chunks and then use McpeRespawn.
        sendNewChunks().whenComplete((chunks, throwable) -> {
            // Chunks sent, respawn player.
            McpeRespawn respawn = new McpeRespawn();
            respawn.setPosition(getPosition());
            session.sendImmediatePackage(respawn);
            spawned = true;

            updatePlayerList();
        });
    }

    private void placeBlock(ItemUseTransaction itemUseTransaction) {
        ItemStack clientInHand = itemUseTransaction.getItem();

        // Sanity check:
        Optional<ItemStack> actuallyInHand = playerInventory.getStackInHand();
        if (log.isDebugEnabled()) {
            log.debug("Held: {}, slot: {}", actuallyInHand, playerInventory.getHeldHotbarSlot());
        }

        if ((actuallyInHand.isPresent() && actuallyInHand.get().getItemType() != clientInHand.getItemType()) ||
                !actuallyInHand.isPresent() && clientInHand.getItemType() != BlockTypes.AIR) {
            // Not actually the same item.
            sendPlayerInventory();
            return;
        }

        // What block is this item being used against?
        Optional<Block> usedAgainst = getLevel().getBlockIfChunkLoaded(itemUseTransaction.getPosition());
        if (!usedAgainst.isPresent()) {
            // Not loaded into memory.
            return;
        }

        // Ask the block being checked.
        ItemStack serverInHand = actuallyInHand.orElse(new VoxelwindItemStack(BlockTypes.AIR, 1, null));
        BlockFace face = BlockFace.values()[itemUseTransaction.getFace()];
        BlockBehavior againstBehavior = BlockBehaviors.getBlockBehavior(usedAgainst.get().getBlockState().getBlockType());
        switch (againstBehavior.handleItemInteraction(getServer(), PlayerSession.this, itemUseTransaction.getPosition(), face, serverInHand)) {
            case NOTHING:
                // Update inventory
                sendPlayerInventory();
                return;
            case PLACE_BLOCK_AND_REMOVE_ITEM:
                Preconditions.checkState(serverInHand.getItemType() instanceof BlockType, "Tried to place air or non-block.");
                if (!BehaviorUtils.setBlockState(PlayerSession.this, itemUseTransaction.getPosition().add(face.getOffset()), BehaviorUtils.createBlockState(usedAgainst.get().getLevelLocation(), face, serverInHand))) {
                    sendPlayerInventory();
                    break;
                }
                // This will fall through
            case REMOVE_ONE_ITEM:
                int newItemAmount = serverInHand.getAmount() - 1;
                if (newItemAmount <= 0) {
                    playerInventory.clearItem(playerInventory.getHeldInventorySlot());
                } else {
                    playerInventory.setItem(playerInventory.getHeldInventorySlot(), serverInHand.toBuilder().amount(newItemAmount).build());
                }
                break;
            case REDUCE_DURABILITY:
                // TODO: Implement
                break;
        }
    }

    public void handledropItem(WorldInteractionTransactionRecord record) {

        if (record.getOldItem().getItemType() == BlockTypes.AIR) {
            return;
        }

        // TODO: Events
        Optional<ItemStack> stackOptional = playerInventory.getStackInHand();
        if (!stackOptional.isPresent()) {
            sendPlayerInventory();
            return;
        }

        DroppedItem item = new VoxelwindDroppedItem(getLevel(), getPosition().add(0, 1.3, 0), getServer(), stackOptional.get());
        item.setMotion(getDirectionVector().mul(0.4));
        playerInventory.clearItem(playerInventory.getHeldInventorySlot());
    }

    public boolean isSpawned() {
        return spawned;
    }

    private class PlayerSessionNetworkPacketHandler implements NetworkPacketHandler {
        @Override
        public void handle(McpeLogin packet) {
            throw new IllegalStateException("Login packet received but player session is currently active!");
        }

        @Override
        public void handle(McpeSubClientLogin packet) {
            throw new IllegalStateException("Login packet received but player session is currently active!");
        }

        @Override
        public void handle(McpeClientToServerHandshake packet) {
            throw new IllegalStateException("Client packet received but player session is currently active!");
        }

        @Override
        public void handle(McpeRequestChunkRadius packet) {
            int radius = Math.max(5, Math.min(vwServer.getConfiguration().getMaximumViewDistance(), packet.getRadius()));
            McpeChunkRadiusUpdate updated = new McpeChunkRadiusUpdate();
            updated.setRadius(radius);
            session.sendImmediatePackage(updated);
            viewDistance = radius;

            CompletableFuture<List<Chunk>> sendChunksFuture = sendNewChunks();
            sendChunksFuture.whenComplete((chunks, throwable) -> {
                if (!spawned) {
                    sendSetEntityData();

                    McpePlayStatus status = new McpePlayStatus();
                    status.setStatus(McpePlayStatus.Status.PLAYER_SPAWN);
                    session.sendImmediatePackage(status);

                    McpeSetTime setTime = new McpeSetTime();
                    setTime.setTime(getLevel().getTime());
                    session.sendImmediatePackage(setTime);

                    updateViewableEntities();
                    sendMovePlayerPacket();
                    updatePlayerList();

                    McpeAvailableCommands availableCommands = ((VoxelwindCommandManager) vwServer.getCommandManager())
                            .generateAvailableCommandsPacket();

                    McpeWrapper availableCommandsWrapper = new McpeWrapper();
                    availableCommandsWrapper.getPackets().add(availableCommands);
                    session.sendImmediatePackage(availableCommandsWrapper);

                    spawned = true;

                    log.info("{} ({}) has been spawned at {} ({})", getName(), getRemoteAddress().map(Object::toString).orElse("UNKNOWN"),
                            getPosition(), getLevel().getName());

                    PlayerJoinEvent event = new PlayerJoinEvent(PlayerSession.this, TextFormat.YELLOW + getName() + " joined the game.");
                    session.getServer().getEventManager().fire(event);
                }
            });
        }

        @Override
        public void handle(McpePlayerAction packet) {
            switch (packet.getAction()) {
                case START_BREAK:
                    // Fire interact
                    break;
                case ABORT_BREAK:
                    // No-op
                    break;
                case STOP_BREAK:
                    // No-op
                    break;
                case DROP_ITEM:
                    // Drop item, shoot bow, or dump bucket?
                    break;
                case STOP_SLEEPING:
                    // Stop sleeping
                    break;
                case RESPAWN:
                    // Clean up attributes?
                    Health health = ensureAndGet(Health.class);
                    if (!(spawned && health.isDead())) {
                        return;
                    }

                    setSprinting(false);
                    setSneaking(false);
                    health.setHealth(health.getMaximumHealth());
                    sendHealthPacket();
                    sendPlayerInventory();
                    teleport(getLevel(), getLevel().getSpawnLocation());
                    sendAttributes();

                    McpeRespawn respawn = new McpeRespawn();
                    respawn.setPosition(getLevel().getSpawnLocation());
                    session.addToSendQueue(respawn);
                    break;
                case JUMP:
                    // No-op
                    break;
                case START_SPRINT:
                    sprinting = true;
                    sendAttributes();
                    break;
                case STOP_SPRINT:
                    sprinting = false;
                    sendAttributes();
                    break;
                case START_SNEAK:
                    sneaking = true;
                    sendAttributes();
                    break;
                case STOP_SNEAK:
                    sneaking = false;
                    sendAttributes();
                    break;
                case BREAKING:
                    // In process of breaking block.
                    break;
            }
            broadcastSetEntityData();
        }

        @Override
        public void handle(McpeAnimate packet) {
            getLevel().getPacketManager().queuePacketForPlayers(packet);
        }

        @Override
        public void handle(McpeText packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            Preconditions.checkArgument(packet.getType() == McpeText.TextType.CHAT, "Text packet type from client is not CHAT");
            Preconditions.checkArgument(!packet.getMessage().contains("\0"), "Text packet from client contains a null byte");
            Preconditions.checkArgument(!packet.getMessage().trim().isEmpty(), "Text packet from client is effectively empty");

            if (packet.getMessage().startsWith("/")) {
                String command = packet.getMessage().substring(1);
                try {
                    session.getServer().getCommandManager().executeCommand(PlayerSession.this, command);
                } catch (CommandNotFoundException e) {
                    sendMessage(TextFormat.RED + "No such command found.");
                } catch (CommandException e) {
                    log.error("Error while running command '{}' for {}", command, getName(), e);
                    sendMessage(TextFormat.RED + "An error has occurred while running the command.");
                }
                return;
            }

            packet.setSource(getName());
            packet.setNeedsTranslation(false); // Deal with this later.
            packet.setXuid(Long.toString(session.getAuthenticationProfile().getXuid()));
            // By default, queue this packet for all players in the world.
            getLevel().getPacketManager().queuePacketForPlayers(packet);
        }

        @Override
        public void handle(McpeMovePlayer packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            // TODO: We may do well to perform basic anti-cheat
            Vector3f originalPosition = getPosition();
            Vector3f newPosition = packet.getPosition().sub(0, 1.62, 0);

            // Reject moves that are obviously too fast. (>=100 blocks)
            if (newPosition.distanceSquared(newPosition) >= 10000) {
                setPosition(originalPosition);
                setRotation(packet.getRotation());
                return;
            }

            if (flying && packet.isOnGround()) {
                PlayerData data = ensureAndGet(PlayerData.class);
                data.setFlying(false);
            }

            setPosition(newPosition);
            setRotation(packet.getRotation());
            // If we haven't moved in the X or Z axis, don't update viewable entities or try updating chunks - they haven't changed.
            if (hasSubstantiallyMoved(originalPosition, newPosition)) {
                updateViewableEntities();
                sendNewChunks().exceptionally(throwable -> {
                    log.error("Unable to send chunks", throwable);
                    disconnect("Internal server error");
                    return null;
                });
            }
        }

        @Override
        public void handle(McpeContainerClose packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            if (openedInventory != null) {
                ((VoxelwindBaseInventory) openedInventory).getObserverList().remove(PlayerSession.this);
                openedInventory = null;
                openInventoryId = -1;
            }
        }

        @Override
        public void handle(McpeInventorySlot packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            VoxelwindBaseInventory window = null;
            if (openInventoryId < 0 || openInventoryId != packet.getInventoryId()) {
                // There's no inventory open, so it's probably the player inventory.
                if (packet.getInventoryId() == 0) {
                    window = playerInventory;
                } else if (packet.getInventoryId() == 0x78) {
                    // It's the armor inventory. Handle it here.
                    ArmorEquipment equipment = ensureAndGet(ArmorEquipment.class);
                    switch (packet.getSlot()) {
                        case 0:
                            equipment.setHelmet(packet.getStack());
                            break;
                        case 1:
                            equipment.setChestplate(packet.getStack());
                            break;
                        case 2:
                            equipment.setLeggings(packet.getStack());
                            break;
                        case 3:
                            equipment.setBoots(packet.getStack());
                            break;
                    }
                    return;
                }
            } else {
                window = (VoxelwindBaseInventory) openedInventory;
            }

            if (window == null) {
                return;
            }

            window.setItem(packet.getSlot(), packet.getStack(), PlayerSession.this);
        }

        @Override
        public void handle(McpeMobEquipment packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            // Basic sanity check:
            if (packet.getHotbarSlot() < 0 || packet.getHotbarSlot() >= 9) {
                throw new IllegalArgumentException("Specified hotbar slot " + packet.getHotbarSlot() + " isn't valid.");
            }

            int correctedInventorySlot = packet.getInventorySlot() - 9;
            int finalSlot = correctedInventorySlot < 0 || correctedInventorySlot >= playerInventory.getInventoryType().getInventorySize() ?
                    -1 : correctedInventorySlot;

            playerInventory.setHotbarLink(packet.getHotbarSlot(), finalSlot);
            playerInventory.setHeldHotbarSlot(packet.getHotbarSlot(), false);
        }

        @Override
        public void handle(McpeAdventureSettings packet) {
            int flags = packet.getFlags();
            flying = ((flags & 0x200) == 0x200);
        }

        @Override
        public void handle(McpeInventoryTransaction packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            packet.getTransaction().handle(session);
        }

        @Override
        public void handle(NormalTransaction transaction) {
            for (TransactionRecord transactionRecord : transaction.getRecords()) {
                transactionRecord.execute(PlayerSession.this);
            }
        }

        @Override
        public void handle(InventoryMismatchTransaction transaction) {

        }

        @Override
        public void handle(ItemUseTransaction transaction) {
            switch (transaction.getAction()) {
                case USE:
                    break;
                case PLACE:
                    placeBlock(transaction);
                    break;
                case BREAK:
                    breakBlock(transaction.getPosition());
                    break;
            }
        }

        @Override
        public void handle(ItemUseOnEntityTransaction transaction) {

        }

        @Override
        public void handle(ItemReleaseTransaction transaction) {

        }

        @Override
        public void handle(McpeResourcePackClientResponse packet) {
            switch(packet.getResponseStatus()){
                case 2:
                    //TODO: Send ResourcePackDataInfo
                    return;
                case 3:
                    McpeResourcePackStack stack = new McpeResourcePackStack();
                    session.sendImmediatePackage(stack);
                    return;
                case 4:
                    doInitialSpawn();
            }
        }

        @Override
        public void handle(McpeCommandRequest packet) {
            Health health = ensureAndGet(Health.class);
            if (!spawned || health.isDead()) {
                return;
            }

            // This is essentially a hack at the moment.
            // TODO: Replace with nicer command API
            String rawCommand = packet.getCommand();
            if (rawCommand == null) {
                log.debug("Unable to reconstruct command for packet {}", packet);
                sendMessage(TextFormat.RED + "An error has occurred while running the command.");
                return;
            }

            if (rawCommand.startsWith("/")) {
                rawCommand = rawCommand.substring(1);
            }
            /*if (argsNode.getNodeType() == JsonNodeType.NULL) {
                command = packet.getCommand();
            } else if (argsNode.getNodeType() == JsonNodeType.OBJECT) {
                JsonNode innerArgs = argsNode.get("args");
                if (innerArgs.getNodeType() == JsonNodeType.STRING) {
                    command = packet.getCommand() + " " + innerArgs.asText();
                } else if (innerArgs.getNodeType() == JsonNodeType.ARRAY) {
                    StringBuilder reconstructedCommand = new StringBuilder(packet.getCommand());
                    ArrayNode innerArgsArray = (ArrayNode) innerArgs;
                    for (JsonNode node : innerArgsArray) {
                        reconstructedCommand.append(' ').append(node.asText());
                    }
                    command = reconstructedCommand.toString();
                }
            }*/

            try {
                session.getServer().getCommandManager().executeCommand(PlayerSession.this, rawCommand.trim());
            } catch (CommandNotFoundException e) {
                sendMessage(TextFormat.RED + "No such command found.");
            } catch (CommandException e) {
                log.error("Error while running command '{}' for {}", rawCommand, getName(), e);
                sendMessage(TextFormat.RED + "An error has occurred while running the command.");
            }
        }
    }

    private void handleHunger(boolean sendAttributes) {
        // TODO: Not finished yet.
        /*Health health = ensureAndGet(Health.class);
        PlayerData playerData = ensureAndGet(PlayerData.class);

        // http://minecraft.gamepedia.com/Hunger#Effects
        if (playerData.getHunger() >= 18) {
            if (health.getHealth() < health.getMaximumHealth()) {
                if (playerData.getHunger() == 20 && Float.compare(playerData.getSaturation(), 0) >= 0) {
                    if (tickCreated % 10 == 0) {
                        playerData.setExhaustion(playerData.getExhaustion() + 4);
                        health.setHealth(Math.min(health.getHealth() + 1, health.getMaximumHealth()));
                    }
                } else {
                    if (tickCreated % 80 == 0) {
                        playerData.setExhaustion(playerData.getExhaustion() + 4);
                        health.setHealth(Math.min(health.getHealth() + 1, health.getMaximumHealth()));
                    }
                }
            }
        } else if (playerData.getHunger() == 0) {

        }

        if (sendAttributes) {
            sendAttributes();
        }*/
    }

    private void sendHealthPacket() {
        Health health = ensureAndGet(Health.class);
        McpeSetHealth packet = new McpeSetHealth();
        packet.setHealth(health.getHealth());
        session.addToSendQueue(packet);
    }

    private void updatePlayerList() {
        synchronized (playersSentForList) {
            Set<Player> toAdd = new HashSet<>();
            Set<UUID> toRemove = new HashSet<>();
            Map<UUID, PlayerSession> availableSessions = new HashMap<>();
            for (PlayerSession session : getLevel().getEntityManager().getPlayers()) {
                if (session == this) continue;
                availableSessions.put(session.getUniqueId(), session);
            }

            for (Player player : availableSessions.values()) {
                if (playersSentForList.add(player.getUniqueId())) {
                    toAdd.add(player);
                }
            }

            for (UUID uuid : playersSentForList) {
                if (!availableSessions.containsKey(uuid)) {
                    toRemove.add(uuid);
                }
            }

            if (!toAdd.isEmpty()) {
                McpePlayerList list = new McpePlayerList();
                list.setType((byte) 0);
                for (Player player : toAdd) {
                    PlayerData data = ensureAndGet(PlayerData.class);
                    PlayerRecord record = new PlayerRecord(player.getUniqueId());
                    record.setEntityId(player.getEntityId());
                    record.setSkin(data.getSkin());
                    record.setName(player.getName());
                    record.setXuid(session.getAuthenticationProfile().getXuid());
                    list.getRecords().add(record);
                }
                session.addToSendQueue(list);
            }

            if (!toRemove.isEmpty()) {
                playersSentForList.removeAll(toRemove);

                McpePlayerList list = new McpePlayerList();
                list.setType((byte) 1);
                for (UUID uuid : toRemove) {
                    list.getRecords().add(new PlayerRecord(uuid));
                }
                session.addToSendQueue(list);
            }
        }
    }

    private static class AroundPointComparator implements Comparator<Chunk> {
        private final int spawnX;
        private final int spawnZ;

        private AroundPointComparator(int spawnX, int spawnZ) {
            this.spawnX = spawnX;
            this.spawnZ = spawnZ;
        }

        @Override
        public int compare(Chunk o1, Chunk o2) {
            // Use whichever is closest to the origin.
            return Integer.compare(distance(o1.getX(), o1.getZ()), distance(o2.getX(), o2.getZ()));
        }

        private int distance(int x, int z) {
            int dx = spawnX - x;
            int dz = spawnZ - z;
            return dx * dx + dz * dz;
        }
    }

    public static final System PLAYER_SYSTEM = System.builder()
            .expectComponents(Health.class, PlayerData.class)
            .runner(new PlayerTickSystemRunner())
            .build();

    private static class PlayerTickSystemRunner implements SystemRunner {
        @Override
        public void run(Entity entity) {
            Verify.verify(entity instanceof PlayerSession, "Invalid entity type (need PlayerSession)");
            PlayerSession session = (PlayerSession) entity;

            Health health = entity.ensureAndGet(Health.class);
            PlayerData playerData = entity.ensureAndGet(PlayerData.class);

            if (!session.isSpawned() || health.isDead()) {
                // Don't tick until the player has truly been spawned into the world.
                return;
            }

            // If the upstream session is closed, the player session should no longer be alive.
            if (session.getMcpeSession().isClosed()) {
                // We don't remove the entity, McpeSession handles this for us.
                //session.removeInternal();
                return;
            }

            if (session.hasMoved) {
                session.hasMoved = false;
                if (session.isTeleported()) {
                    session.sendMovePlayerPacket();
                }
                session.updateViewableEntities();
                session.sendNewChunks().exceptionally(throwable -> {
                    log.error("Unable to send chunks", throwable);
                    session.disconnect("Internal server error");
                    return null;
                });
            }

            // Check for items on the ground.
            // TODO: This should be its own system
            session.pickupAdjacent();

            // Update player list.
            session.updatePlayerList();
            boolean hungerTouched = ((PlayerDataComponent) playerData).hungerTouched();
            boolean attributesTouched = ((PlayerDataComponent) playerData).attributesTouched();
            boolean healthTouched = ((HealthComponent) health).needsUpdate();
            boolean sendAttributes = hungerTouched || attributesTouched || healthTouched;

            if (sendAttributes) {
                session.sendAttributes();
            }

            if (((PlayerDataComponent) playerData).gamemodeTouched()) {
                //Set gamemode's default adventure settings.
                GameMode gameMode = playerData.getGameMode();
                playerData.setAllowedToFly(gameMode.isAllowedToFly());
                playerData.setImmutableWorld(gameMode.isImmutableWorld());
                playerData.setNoClip(gameMode.isNoClip());

                McpeSetPlayerGameType setGameMode = new McpeSetPlayerGameType();
                setGameMode.setGamemode(gameMode.ordinal());
                session.getMcpeSession().addToSendQueue(setGameMode);
            }

            if (((PlayerDataComponent) playerData).adventureSettingsTouched()) {
                session.sendAdventureSettings();
            }
        }
    }

    private static boolean hasSubstantiallyMoved(Vector3f oldPos, Vector3f newPos) {
        return (Float.compare(oldPos.getX(), newPos.getX()) != 0 || Float.compare(oldPos.getZ(), newPos.getZ()) != 0);
    }
}
