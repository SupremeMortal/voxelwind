package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.annotations.ForceClearText;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import lombok.Data;

@ForceClearText
@Data
public class McpeServerToClientHandshake implements NetworkPackage {
    private String payload;

    @Override
    public void decode(ByteBuf buffer) {
        payload = McpeUtil.readVarintLengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeVarintLengthString(buffer, payload);
    }
}
