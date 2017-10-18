package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpePlayerAction implements NetworkPackage {
    private long runtimeEntityId;
    private Action action;
    private Vector3i position;
    private int face;

    @Override
    public void decode(ByteBuf buffer) {
        runtimeEntityId = Varints.decodeUnsigned(buffer);
        action = Action.values()[Varints.decodeSigned(buffer)];
        position = McpeUtil.readBlockCoords(buffer);
        face = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        Varints.encodeSigned(buffer, action.ordinal());
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, face);
    }

    public enum Action {
        START_BREAK,
        ABORT_BREAK,
        STOP_BREAK,
        GET_UPDATE_BLOCK,
        DROP_ITEM,
        START_SLEEPING,
        STOP_SLEEPING,
        RESPAWN,
        JUMP,
        START_SPRINT,
        STOP_SPRINT,
        START_SNEAK,
        STOP_SNEAK,
        DIMENSION_CHANGE,
        DIMENSION_CHANGE_ACK,
        START_GLIDE,
        STOP_GLIDE,
        WORLD_IMMUTABLE,
        BREAKING,
        CHANGE_SKIN
    }

}
