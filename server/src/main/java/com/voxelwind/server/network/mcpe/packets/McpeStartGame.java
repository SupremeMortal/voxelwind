package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.level.util.Gamerule;
import com.voxelwind.server.game.permissions.PermissionLevel;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeStartGame implements NetworkPackage {
    private long entityId; // = null;
    private long runtimeEntityId; // = null;
    private int playerGamemode;
    private Vector3f spawn; // = null;
    private float pitch; // = null;
    private float yaw;
    private int seed; // = null;
    private int dimension; // = null;
    private int generator; // = null;
    private int worldGamemode; // = null;
    private int difficulty; // = null;
    private Vector3i worldSpawn; // = null;
    private boolean hasAchievementsDisabled; // = null;
    private int dayCycleStopTime; // = null;
    private boolean eduMode; // = null;
    private float rainLevel; // = null;
    private float lightingLevel; // = null;
    private boolean multiplayer;
    private boolean broadcastToLan;
    private boolean broadcastToXbl;
    private boolean enableCommands; // = null;
    private boolean isTexturepacksRequired; // = null;
    private Gamerule[] gameRules; // TODO
    private boolean bonusChest;
    private boolean mapEnabled;
    private boolean trustPlayers;
    private PermissionLevel permissionLevel;
    private int gamePublishSettings;
    private String levelId; // = null;
    private String worldName; // = null;
    private String premiumWorldTemplateId = "";
    private boolean unknown0;
    private long currentTick;
    private int enchantmentSeed;

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSignedLong(buffer, entityId);
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        Varints.encodeSigned(buffer, playerGamemode);
        McpeUtil.writeVector3f(buffer, spawn);
        buffer.writeFloat(pitch);
        buffer.writeFloat(yaw);
        Varints.encodeSigned(buffer, seed);
        Varints.encodeSigned(buffer, dimension);
        Varints.encodeSigned(buffer, generator);
        Varints.encodeSigned(buffer, worldGamemode);
        Varints.encodeSigned(buffer, difficulty);
        McpeUtil.writeBlockCoords(buffer, worldSpawn);
        buffer.writeBoolean(hasAchievementsDisabled);
        Varints.encodeSigned(buffer, dayCycleStopTime);
        buffer.writeBoolean(eduMode);
        buffer.writeFloat(rainLevel);
        buffer.writeFloat(lightingLevel);
        buffer.writeBoolean(multiplayer);
        buffer.writeBoolean(broadcastToLan);
        buffer.writeBoolean(broadcastToXbl);
        buffer.writeBoolean(enableCommands);
        buffer.writeBoolean(isTexturepacksRequired);
        Varints.encodeUnsigned(buffer, gameRules.length);
        for(Gamerule rule : gameRules){
            McpeUtil.writeVarintLengthString(buffer, rule.getName());
            Object value = rule.getValue();
            if(value instanceof Boolean){
                buffer.writeByte((byte) 1);
                buffer.writeBoolean((Boolean) value);
            }else if(value instanceof Integer){
                buffer.writeByte((byte) 2);
                Varints.encodeUnsigned(buffer, (int) value);
            }else if(value instanceof Float){
                buffer.writeByte((byte) 3);
                McpeUtil.writeFloatLE(buffer, (float) value);
            }
        }
        buffer.writeBoolean(bonusChest);
        buffer.writeBoolean(mapEnabled);
        buffer.writeBoolean(trustPlayers);
        Varints.encodeSigned(buffer, permissionLevel.ordinal());
        Varints.encodeSigned(buffer, gamePublishSettings);
        McpeUtil.writeVarintLengthString(buffer, levelId);
        McpeUtil.writeVarintLengthString(buffer, worldName);
        McpeUtil.writeVarintLengthString(buffer, premiumWorldTemplateId);
        buffer.writeBoolean(unknown0);
        buffer.writeLongLE(currentTick);
        Varints.encodeSigned(buffer, enchantmentSeed);
    }
}
