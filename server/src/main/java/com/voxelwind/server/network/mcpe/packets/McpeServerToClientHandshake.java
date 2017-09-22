package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.annotations.ForceClearText;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import lombok.Data;

@ForceClearText
@Data
public class McpeServerToClientHandshake implements NetworkPackage {
    private AsciiString payload;

    @Override
    public void decode(ByteBuf buffer) {
        payload = McpeUtil.readLELengthAsciiString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeLELengthAsciiString(buffer, payload);
    }
}
