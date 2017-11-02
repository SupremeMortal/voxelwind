package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.session.auth.PlayerRecord;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class McpePlayerList implements NetworkPackage {
    private byte type;
    private final List<PlayerRecord> records = new ArrayList<>();

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(type);
        Varints.encodeUnsigned(buffer, records.size());
        for (PlayerRecord record : records) {
            McpeUtil.writeUuid(buffer, record.getUuid());
            // 0 is ADD, 1 is REMOVE
            if (type == 0) {
                Varints.encodeUnsigned(buffer, record.getEntityId());
                McpeUtil.writeVarintLengthString(buffer, record.getName());
                McpeUtil.writeSkin(buffer, record.getSkin());
                McpeUtil.writeVarintLengthString(buffer, Long.toString(record.getXuid()));
            }
        }
    }
}
