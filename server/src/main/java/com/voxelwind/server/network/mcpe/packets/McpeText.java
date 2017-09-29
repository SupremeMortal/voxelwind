package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.server.player.TranslatedMessage;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeText implements NetworkPackage {
    private TextType type;
    private boolean needsTranslation= false;
    private String source = "";
    private String message = "";
    private TranslatedMessage translatedMessage;
    private String xuid = "";

    @Override
    public void decode(ByteBuf buffer) {
        type = TextType.values()[buffer.readByte()];
        needsTranslation = buffer.readBoolean();
        switch (type) {
            case RAW:
            case CHAT:
            case TRANSLATION:
            case POPUP:
            case JUKEBOX_POPUP:
                message = McpeUtil.readVarintLengthString(buffer);
                //TODO: add parameters.
            case TIP:
            case SYSTEM:
                message = McpeUtil.readVarintLengthString(buffer);
            case WHISPER:
            case ANNOUNCEMENT:
                source = McpeUtil.readVarintLengthString(buffer);
                message = McpeUtil.readVarintLengthString(buffer);
        }
        xuid = McpeUtil.readVarintLengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(type.ordinal());
        buffer.writeBoolean(needsTranslation);
        switch (type) {
            case RAW:
            case CHAT:
            case TRANSLATION:
            case POPUP:
            case JUKEBOX_POPUP:
                McpeUtil.writeVarintLengthString(buffer, message);
                //TODO: add parameters.
            case TIP:
            case SYSTEM:
                McpeUtil.writeVarintLengthString(buffer, message);
            case WHISPER:
            case ANNOUNCEMENT:
                McpeUtil.writeVarintLengthString(buffer, source);
                McpeUtil.writeVarintLengthString(buffer, message);
        }
        McpeUtil.writeVarintLengthString(buffer, xuid);
    }

    @Override
    public String toString() {
        return "McpeText{" +
                "type=" + type +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", translatedMessage=" + translatedMessage +
                '}';
    }

    public enum TextType {
        RAW,
        CHAT,
        TRANSLATION,
        POPUP,
        JUKEBOX_POPUP,
        TIP,
        SYSTEM,
        WHISPER,
        ANNOUNCEMENT
    }
}
