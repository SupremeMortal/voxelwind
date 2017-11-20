package com.voxelwind.server.network.rcon;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

public class RconEncodeDecodeTest {
    private static final byte[] MESSAGE = new byte[]{
            0, 0, 0, 0, 3, 0, 0, 0, 109, 121, 32, 118, 111, 105, 99, 101, 32, 105, 115, 32, 109, 121, 32, 112, 97, 115, 115, 112, 111, 114, 116, 0, 0
    };
    @Test
    public void decodeTest() throws Exception {
        ByteBuf buf = Unpooled.wrappedBuffer(MESSAGE);
        EmbeddedChannel channel = new EmbeddedChannel(new RconDecoder());
        try {
            channel.writeInbound(buf);
            RconMessage message = (RconMessage) channel.readInbound();
            RconMessage intended = new RconMessage(0, 3, "my voice is my passport");
            Assert.assertEquals("Read message is invalid.", intended, message);
        } finally {
            channel.close();
        }
    }

    @Test
    public void encodeTest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new RconEncoder());
        ByteBuf expected = Unpooled.wrappedBuffer(MESSAGE);
        try {
            channel.writeOutbound(new RconMessage(0, 3, "my voice is my passport"));
            ByteBuf buf = (ByteBuf) channel.readOutbound();
            Assert.assertEquals("Read message is invalid.", expected, buf);
        } finally {
            expected.release();
            channel.close();
        }
    }
}
