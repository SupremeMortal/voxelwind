package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.util.CommandOriginData;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeCommandRequest implements NetworkPackage {
    private String command;
    private CommandOriginData originData;
    private boolean internal;

    @Override
    public void decode(ByteBuf buffer) {
        command = McpeUtil.readVarintLengthString(buffer);
        originData = McpeUtil.readCommandOriginData(buffer);
        internal = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }
}
