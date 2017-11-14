package com.voxelwind.server.network.query;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class QueryUtil {
    public static final byte[] LONG_RESPONSE_PADDING_TOP = DatatypeConverter.parseHexBinary("73706c69746e756d008000");
    public static final byte[] LONG_RESPONSE_PADDING_BOTTOM = DatatypeConverter.parseHexBinary("01706c617965725f0000");

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
