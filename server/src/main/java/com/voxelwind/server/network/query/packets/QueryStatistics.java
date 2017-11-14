package com.voxelwind.server.network.query.packets;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryStatistics implements QueryPackage {
    // Both
    private int sessionId;
    // Request
    private int token;
    private boolean full;
    // Response
    private ByteBuf payload;

    @Override
    public void decode(ByteBuf buffer) {
        sessionId = buffer.readInt();
        token = buffer.readInt();
        full = (buffer.readableBytes() > 0);
        buffer.skipBytes(buffer.readableBytes());
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(sessionId);
        buffer.writeBytes(payload);
    }
}
