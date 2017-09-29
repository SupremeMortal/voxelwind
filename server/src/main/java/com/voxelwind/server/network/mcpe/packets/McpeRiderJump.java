package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeRiderJump implements NetworkPackage{
    private int unknown0;

    @Override
    public void decode(ByteBuf buffer) {
        unknown0 = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSigned(buffer, unknown0);
    }
}
