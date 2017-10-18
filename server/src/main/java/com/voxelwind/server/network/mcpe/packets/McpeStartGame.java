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
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
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
    private boolean texturepacksRequired; // = null;
    @Getter
    private final List<Gamerule> gameRules = new ArrayList<>();
    private boolean bonusChest;
    private boolean mapEnabled;
    private boolean trustPlayers;
    private PermissionLevel permissionLevel;
    private int gamePublishSettings;
    private String levelId; // = null;
    private String worldName; // = null;
    private String premiumWorldTemplateId = "";
    private boolean trial;
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
        McpeUtil.writeFloatLE(buffer, pitch);
        McpeUtil.writeFloatLE(buffer, yaw);
        Varints.encodeSigned(buffer, seed);
        Varints.encodeSigned(buffer, dimension);
        Varints.encodeSigned(buffer, generator);
        Varints.encodeSigned(buffer, worldGamemode);
        Varints.encodeSigned(buffer, difficulty);
        McpeUtil.writeBlockCoords(buffer, worldSpawn);
        buffer.writeBoolean(hasAchievementsDisabled);
        Varints.encodeSigned(buffer, dayCycleStopTime);
        buffer.writeBoolean(eduMode);
        McpeUtil.writeFloatLE(buffer, rainLevel);
        McpeUtil.writeFloatLE(buffer, lightingLevel);
        buffer.writeBoolean(multiplayer);
        buffer.writeBoolean(broadcastToLan);
        buffer.writeBoolean(broadcastToXbl);
        buffer.writeBoolean(enableCommands);
        buffer.writeBoolean(texturepacksRequired);
        Varints.encodeUnsigned(buffer, gameRules.size());
        for(Gamerule rule : gameRules){
            Object value = rule.getValue();
            McpeUtil.writeVarintLengthString(buffer, rule.getName());
            if(value instanceof Boolean){
                buffer.writeByte((byte) 1);
                buffer.writeBoolean((boolean) value);
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
        buffer.writeBoolean(trial);
        buffer.writeLongLE(currentTick);
        Varints.encodeSigned(buffer, enchantmentSeed);
    }
}
