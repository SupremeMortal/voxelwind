package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.permissions.PermissionLevel;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeAdventureSettings implements NetworkPackage {
    private int flags;
    private int commandPermissions;
    private int actionPermissions;
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
        userId = buffer.readLong();
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, flags);
        Varints.encodeUnsigned(buffer, commandPermissions);
        Varints.encodeUnsigned(buffer, actionPermissions);
        Varints.encodeUnsigned(buffer, permissionLevel.ordinal());
        Varints.encodeUnsigned(buffer, customStoredPermissions);
        buffer.writeLong(userId);
    }
}
