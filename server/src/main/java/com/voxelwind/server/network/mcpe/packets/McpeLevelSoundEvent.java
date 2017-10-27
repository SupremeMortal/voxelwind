package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.api.game.Sound;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeLevelSoundEvent implements NetworkPackage{
    private Sound soundId;
    private Vector3f position;
    private int extraData;
    private int pitch;
    private boolean babyMob;
    private boolean global;

    @Override
    public void decode(ByteBuf buffer) {
        soundId = Sound.values()[buffer.readByte()];
        position = McpeUtil.readVector3f(buffer);
        extraData = Varints.decodeSigned(buffer);
        pitch = Varints.decodeSigned(buffer);
        babyMob = buffer.readBoolean();
        global = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(soundId.ordinal());
        McpeUtil.writeVector3f(buffer, position);
        Varints.encodeSigned(buffer, extraData);
        Varints.encodeSigned(buffer, pitch);
        buffer.writeBoolean(babyMob);
        buffer.writeBoolean(global);
    }
}
