package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeCommandRequest implements NetworkPackage {
    private String command;
    private int commandType;
    private String requestId;
    private boolean unknown0;

    @Override
    public void decode(ByteBuf buffer) {
        command = McpeUtil.readVarintLengthString(buffer);
        commandType = Varints.decodeSigned(buffer);
        requestId = McpeUtil.readVarintLengthString(buffer);
        unknown0 = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {

    }
}
