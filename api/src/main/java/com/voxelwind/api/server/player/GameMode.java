package com.voxelwind.api.server.player;

import lombok.Getter;

/**
 * Specifies the game mode that the player is in.
 */
@Getter
public enum GameMode {
    SURVIVAL(false, false, false),
    CREATIVE(true, false, false),
    ADVENTURE(false, true, false),
    SPECTATOR(true, true, true);

    private boolean allowedToFly;
    private boolean immutableWorld;
    private boolean noClip;

    GameMode(boolean allowedToFly, boolean immutableWorld, boolean noClip) {
        this.allowedToFly = allowedToFly;
        this.immutableWorld = immutableWorld;
        this.noClip = noClip;
    }
}
