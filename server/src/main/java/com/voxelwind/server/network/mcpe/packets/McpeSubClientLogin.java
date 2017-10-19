package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import lombok.Data;

@Data
public class McpeSubClientLogin implements NetworkPackage{

    private AsciiString chainData;
    private AsciiString skinData;

    @Override
    public void decode(ByteBuf buffer) {
        int bodyLength = (int) Varints.decodeUnsigned(buffer);
        ByteBuf body = buffer.readSlice(bodyLength);

        chainData = McpeUtil.readLELengthAsciiString(body);
        skinData = McpeUtil.readLELengthAsciiString(body);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeLELengthAsciiString(buffer, chainData);
        McpeUtil.writeLELengthAsciiString(buffer, skinData);
    }
}
