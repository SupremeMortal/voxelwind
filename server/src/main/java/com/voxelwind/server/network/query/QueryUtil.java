package com.voxelwind.server.network.query;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class QueryUtil {
    public static final byte[] LONG_RESPONSE_PADDING_TOP = new byte[]{115, 112, 108, 105, 116, 110, 117, 109, 0, -128, 0};
    public static final byte[] LONG_RESPONSE_PADDING_BOTTOM = new byte[]{1, 112, 108, 97, 121, 101, 114, 95, 0, 0};

    public static void writeNullTerminatedByteArray(ByteBuf buf, byte[] array) {
        if (array != null) {
            buf.writeBytes(array);
        }
        buf.writeByte((byte) 0x00);
    }

    public static byte[] readNullTerminatedByteArray(ByteBuf buf) {
        int maxLength = buf.readableBytes();

        if (maxLength == 0) {
            return new byte[0];
        }

        int offset = buf.readerIndex();
        int zeroPos = 0;

        while ((zeroPos < maxLength) && (buf.getByte(zeroPos + offset) != 0x00)) {
            zeroPos++;
        }

        if (zeroPos >= maxLength) {
            return new byte[0];
        }

        byte[] result = null;
        if (zeroPos > 0) {
            // read a new byte array
            byte[] bytes = new byte[zeroPos];
            buf.readBytes(bytes);
        } else {
            result = new byte[0];
        }

        buf.skipBytes(1); // Skip null terminator.

        return result;
    }

    public static void writeNullTerminatedString(ByteBuf buf, String string) {
        writeNullTerminatedByteArray(buf, string.getBytes(StandardCharsets.UTF_8));
    }

    public static String readNullTerminatedString(ByteBuf buf) {
        return new String(readNullTerminatedByteArray(buf), StandardCharsets.UTF_8);
    }
}
