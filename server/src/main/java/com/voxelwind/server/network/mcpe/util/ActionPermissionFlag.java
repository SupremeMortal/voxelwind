package com.voxelwind.server.network.mcpe.util;

public enum ActionPermissionFlag {
    BUILD_AND_MINE(0x1),
    DOORS_AND_SWITCHES(0x2),
    OPEN_CONTAINER(0x4),
    ATTACK_PLAYERS(0x8),
    ATTACK_MOBS(0x10),
    OPERATOR(0x20),
    TELEPORT(0x80),
    DEFAULT(0x1 | 0x2 | 0x4 | 0x8 | 0x10),
    ALL(0x1 | 0x2 | 0x4 | 0x8 | 0x10 | 0x20 | 0x80);

    private int flagVal;

    ActionPermissionFlag(int flagVal) {
        this.flagVal = flagVal;
    }

    public int getVal() {
        return flagVal;
    }
}
