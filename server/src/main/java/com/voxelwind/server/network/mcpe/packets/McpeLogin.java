package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.util.VersionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import lombok.Data;

@Data
public class McpeLogin implements NetworkPackage {

    private int protocolVersion;
    private AsciiString chainData;
    private AsciiString skinData;

    @Override
    public void decode(ByteBuf buffer) {
        protocolVersion = buffer.readInt();
        if (!VersionUtil.isCompatible(protocolVersion)) {
            return;
        }

        int bodyLength = (int) Varints.decodeUnsigned(buffer);
        ByteBuf body = buffer.readSlice(bodyLength);

        chainData = McpeUtil.readLELengthAsciiString(body);
        skinData = McpeUtil.readLELengthAsciiString(body);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
        McpeUtil.writeLELengthAsciiString(buffer, chainData);
        McpeUtil.writeLELengthAsciiString(buffer, skinData);
    }
}
