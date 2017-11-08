package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.server.Skin;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
public class McpePlayerList implements NetworkPackage {
    private byte type;
    private final List<Entry> entries = new ArrayList<>();

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(type);
        Varints.encodeUnsigned(buffer, entries.size());
        for (Entry entry : entries) {
            McpeUtil.writeUuid(buffer, entry.getUuid());
            // 0 is ADD, 1 is REMOVE
            if (type == 0) {
                Varints.encodeSignedLong(buffer, entry.getEntityId());
                McpeUtil.writeVarintLengthString(buffer, entry.getName());
                McpeUtil.writeSkin(buffer, entry.getSkin());
                McpeUtil.writeVarintLengthString(buffer, entry.getXuid());
            }
        }
    }

    @Data
    public static class Entry {
        private final UUID uuid;
        private String xuid;
        private long entityId;
        private String name;
        private Skin skin;
    }

    @Override
    public String toString() {
        String[] array = new String[entries.size()];
        if (type == 0) {
            for (int i = 0; i < entries.size(); i++) {
                array[i] = entries.get(i).getName();
            }
            return "McpePlayerList(entriesToAdd=" + Arrays.toString(array) + ")";
        } else {
            for (int i = 0; i < entries.size(); i++) {
                array[i] = entries.get(i).getUuid().toString();
            }
            return "McpePlayerList(entriesToRemove=" + Arrays.toString(array) + ")";
        }
    }
}
