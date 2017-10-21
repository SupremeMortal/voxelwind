package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.server.player.TranslatedMessage;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
public class McpeText implements NetworkPackage {
    private TextType type;
    private boolean needsTranslation = false;
    private String source;
    private String message;
    private TranslatedMessage translatedMessage;
    private String xuid = "";
    @Getter
    private List<String> parameters = new ArrayList<>(); //TODO: implement parameters.

    @Override
    public void decode(ByteBuf buffer) {
        type = TextType.values()[buffer.readByte()];
        needsTranslation = buffer.readBoolean();
        switch (type) {
            case CHAT:
            case WHISPER:
            case ANNOUNCEMENT:
                source = McpeUtil.readVarintLengthString(buffer);
            case RAW:
            case TIP:
            case SYSTEM:
                message = McpeUtil.readVarintLengthString(buffer);
                break;
            case TRANSLATION:
            case POPUP:
            case JUKEBOX_POPUP:
                message = McpeUtil.readVarintLengthString(buffer);
                int count = (int) Varints.decodeUnsigned(buffer);
                for (int i = 0; i < count; i++) {
                    parameters.add(McpeUtil.readVarintLengthString(buffer));
                }
                break;
        }
        xuid = McpeUtil.readVarintLengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(type.ordinal());
        buffer.writeBoolean(needsTranslation);
        switch (type) {
            case CHAT:
            case WHISPER:
            case ANNOUNCEMENT:
                McpeUtil.writeVarintLengthString(buffer, source);
            case RAW:
            case TIP:
            case SYSTEM:
                McpeUtil.writeVarintLengthString(buffer, message);
                break;
            case TRANSLATION:
            case POPUP:
            case JUKEBOX_POPUP:
                McpeUtil.writeVarintLengthString(buffer, message);
                Varints.encodeUnsigned(buffer, parameters.size());
                for (String param : parameters) {
                    McpeUtil.writeVarintLengthString(buffer, param);
                }
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
