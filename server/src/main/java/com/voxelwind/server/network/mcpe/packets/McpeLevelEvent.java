package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeLevelEvent implements NetworkPackage{
    private int eventId;
    private Vector3i position;
    private int data;

    @Override
    public void decode(ByteBuf buffer) {
        eventId = Varints.decodeSigned(buffer);
        position = McpeUtil.readBlockCoords(buffer);
        data = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSigned(buffer, eventId);
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, data);
    }
}
