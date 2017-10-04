package com.voxelwind.server.game.permissions;

public class ActionPermissions {
    public static final int BUILD_AND_MINE = 0x1;
    public static final int DOORS_AND_SWITCHES = 0x2;
    public static final int OPEN_CONTAINER = 0x4;
    public static final int ATTACK_PLAYERS = 0x8;
    public static final int ATTACK_MOBS = 0x10;
    public static final int OPERATOR = 0x20;
    public static final int TELEPORT = 0x80;
    public static final int DEFAULT = (BUILD_AND_MINE | DOORS_AND_SWITCHES | OPEN_CONTAINER | ATTACK_PLAYERS | ATTACK_MOBS);
    public static final int ALL = (BUILD_AND_MINE | DOORS_AND_SWITCHES | OPEN_CONTAINER | ATTACK_PLAYERS | ATTACK_MOBS | OPERATOR | TELEPORT);
}
