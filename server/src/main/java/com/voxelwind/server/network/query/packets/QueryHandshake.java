package com.voxelwind.server.network.query.packets;

import com.voxelwind.server.network.query.QueryUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryHandshake implements QueryPackage {
    // Both
    private int sessionId;
    // Response
    private String token;

    @Override
    public void decode(ByteBuf buffer) {
        sessionId = buffer.readInt();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(sessionId);
        QueryUtil.writeNullTerminatedString(buffer, token);
    }
}
