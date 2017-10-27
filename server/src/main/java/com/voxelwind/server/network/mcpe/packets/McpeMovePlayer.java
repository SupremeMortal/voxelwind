package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.api.util.Rotation;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeMovePlayer implements NetworkPackage {
    private long runtimeEntityId;
    private Vector3f position;
    private Rotation rotation;
    private Mode mode;
    private boolean onGround;
    private long ridingEntityId;
    private TeleportationCause teleportationCause;
    private int unknown0;

    @Override
    public void decode(ByteBuf buffer) {
        runtimeEntityId = Varints.decodeUnsigned(buffer);
        position = McpeUtil.readVector3f(buffer);
        rotation = McpeUtil.readRotation(buffer);
        mode = Mode.values()[(int) buffer.readByte()];
        onGround = buffer.readBoolean();
        ridingEntityId = Varints.decodeUnsigned(buffer);
        if (mode == Mode.TELEPORT) {
            teleportationCause = TeleportationCause.values()[buffer.readInt()];
            unknown0 = buffer.readInt();
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        McpeUtil.writeVector3f(buffer, position);
        McpeUtil.writeRotation(buffer, rotation);
        buffer.writeByte((byte) mode.ordinal());
        buffer.writeBoolean(onGround);
        Varints.encodeUnsigned(buffer, ridingEntityId);
        if (mode == Mode.TELEPORT) {
            buffer.writeInt(teleportationCause.ordinal());
            buffer.writeInt(1);
        }
    }

    public enum Mode {
        NORMAL,
        RESET,
        TELEPORT,
        PITCH
    }

    public enum TeleportationCause {
        UNKNOWN,
        PROJECTILE,
        CHORUS_FRUIT,
        COMMAND,
        BEHAVIOR,
        COUNT
    }
}
