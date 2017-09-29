package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeLevelSoundEvent implements NetworkPackage{
    private byte soundId;
    private Vector3i position;
    private int blockId;
    private int entityType;
    private boolean babyMob;
    private boolean global;

    @Override
    public void decode(ByteBuf buffer) {
        soundId = buffer.readByte();
        position = McpeUtil.readBlockCoords(buffer);
        blockId = Varints.decodeSigned(buffer);
        entityType = Varints.decodeSigned(buffer);
        babyMob = buffer.readBoolean();
        global = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(soundId);
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, blockId);
        Varints.encodeSigned(buffer, entityType);
        buffer.writeBoolean(babyMob);
        buffer.writeBoolean(global);
    }
}
