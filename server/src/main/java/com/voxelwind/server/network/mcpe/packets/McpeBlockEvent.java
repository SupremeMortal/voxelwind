package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeBlockEvent implements NetworkPackage{
    private Vector3i position;
    private int case1;
    private int case2;

    @Override
    public void decode(ByteBuf buffer) {
        position = McpeUtil.readBlockCoords(buffer);
        case1 = Varints.decodeSigned(buffer);
        case2 = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, case1);
        Varints.encodeSigned(buffer, case2);
    }
}
