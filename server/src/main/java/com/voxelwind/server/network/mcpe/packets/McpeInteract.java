package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInteract implements NetworkPackage {
    private Action type;
    private long entityId;
    private Vector3f position;

    @Override
    public void decode(ByteBuf buffer) {
        type = Action.values()[buffer.readByte()];
        entityId = Varints.decodeUnsigned(buffer);
        if (type == Action.MOUSE_OVER) {
            position = McpeUtil.readVector3f(buffer);
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeByte(type.ordinal());
        Varints.encodeUnsigned(buffer, entityId);
        if (type == Action.MOUSE_OVER) {
            McpeUtil.writeVector3f(buffer, position);
        }
    }

    public enum Action {
        NONE,
        RIGHT_CLICK,
        LEFT_CLICK,
        LEAVE_VEHICLE,
        MOUSE_OVER,
        UNKNOWN0,
        OPEN_INVENTORY
    }
}
