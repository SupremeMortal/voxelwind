package com.voxelwind.server.network.raknet.packets;

import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import static com.voxelwind.server.network.raknet.RakNetConstants.RAKNET_PROTOCOL_VERSION;
import static com.voxelwind.server.network.raknet.RakNetConstants.RAKNET_UNCONNECTED_MAGIC;

@Data
public class IncompatibleProtocolVersion implements NetworkPackage {
    private long serverGuid;

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(RAKNET_PROTOCOL_VERSION);
        buffer.writeBytes(RAKNET_UNCONNECTED_MAGIC);
        buffer.writeLong(serverGuid);
    }
}
