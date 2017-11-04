package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeLevelEvent implements NetworkPackage{
    private int eventId;
    private Vector3f position;
    private int data;

    @Override
    public void decode(ByteBuf buffer) {
        eventId = Varints.decodeSigned(buffer);
        position = McpeUtil.readVector3f(buffer);
        data = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSigned(buffer, eventId);
        McpeUtil.writeVector3f(buffer, position);
        Varints.encodeSigned(buffer, data);
    }
}
