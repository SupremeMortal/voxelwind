package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;

public class McpeResourcePackStack implements NetworkPackage{

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        // TODO: Write pack stack.
    }
}
