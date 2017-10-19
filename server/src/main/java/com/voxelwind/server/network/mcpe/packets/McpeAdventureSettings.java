package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.permissions.PermissionLevel;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.util.ActionPermissionFlag;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeAdventureSettings implements NetworkPackage {
    private int flags = 0;
    private int commandPermissions;
    private int actionPermissions = -1;
    private PermissionLevel permissionLevel;
    private int customStoredPermissions;
    private long userId;

    @Override
    public void decode(ByteBuf buffer) {
        flags = (int) Varints.decodeUnsigned(buffer);
        commandPermissions = (int) Varints.decodeUnsigned(buffer);
        actionPermissions = (int) Varints.decodeUnsigned(buffer);
        permissionLevel = PermissionLevel.values()[(int) Varints.decodeUnsigned(buffer)];
        customStoredPermissions = (int) Varints.decodeUnsigned(buffer);
        userId = buffer.readLongLE();
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, flags);
        Varints.encodeUnsigned(buffer, commandPermissions);
        Varints.encodeUnsigned(buffer, actionPermissions);
        Varints.encodeUnsigned(buffer, permissionLevel.ordinal());
        Varints.encodeUnsigned(buffer, customStoredPermissions);
        buffer.writeLongLE(userId);
    }

    public void setFlags(Flag flag, boolean value) {
        if (value) {
            flags |= flag.getVal();
        } else {
            flags &= ~flag.getVal();
        }
    }

    public void setActionPermissions(ActionPermissionFlag flag, boolean value) {
        if (value) {
            actionPermissions |= flag.getVal();
        } else {
            actionPermissions &= ~flag.getVal();
        }
    }

    public enum Flag {
        IMMUTABLE_WORLD(0x01),
        NO_PVP(0x02),
        NO_PVM(0x04),
        NO_MVP(0x08),
        AUTO_JUMP(0x20),
        ALLOW_FLIGHT(0x40),
        NO_CLIP(0x80),
        WORLD_BUILDER(0x100),
        FLYING(0x200),
        MUTED(0x400);

        private int flagVal;

        Flag(int flagVal) {
            this.flagVal = flagVal;
        }

        public int getVal() {
            return flagVal;
        }
    }
}
