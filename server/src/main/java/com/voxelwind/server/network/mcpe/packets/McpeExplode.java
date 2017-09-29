package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeExplode implements NetworkPackage{
    private Vector3f position;
    private int radius;
    //private Records records; TODO: Implement records.


    @Override
    public void decode(ByteBuf buffer) {
        position = McpeUtil.readVector3f(buffer);
        radius = Varints.decodeSigned(buffer);
        //records = McpeUtil.readRecords(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeVector3f(buffer, position);
        Varints.encodeSigned(buffer, radius);
        //McpeUtil.writeRecords(buffer, records);
    }
}
