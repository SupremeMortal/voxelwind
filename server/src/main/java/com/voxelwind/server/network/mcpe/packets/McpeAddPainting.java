package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeAddPainting implements NetworkPackage {
    private long entityId;
    private long runtimeEntityId;
    private Vector3i position;
    private int direction;
    private String title;

    @Override
    public void decode(ByteBuf buffer) {
        entityId = Varints.decodeSignedLong(buffer);
        runtimeEntityId = Varints.decodeUnsigned(buffer);
        position = McpeUtil.readBlockCoords(buffer);
        direction = Varints.decodeSigned(buffer);
        title = McpeUtil.readVarintLengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSignedLong(buffer, entityId);
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, direction);
        McpeUtil.writeVarintLengthString(buffer, title);
    }
}
