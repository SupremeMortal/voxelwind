package com.voxelwind.api.server.player;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * Specifies the game mode that the player is in.
 */
@Getter
public enum GameMode {
    SURVIVAL(false, false, false, "0", "s", "survival"),
    CREATIVE(true, false, false, "1", "c", "creative"),
    ADVENTURE(false, true, false, "2", "a", "adventure"),
    SPECTATOR(true, true, true, "3", "sp", "spectator");

    private boolean allowedToFly;
    private boolean immutableWorld;
    private boolean noClip;
    private String[] aliases;

    GameMode(boolean allowedToFly, boolean immutableWorld, boolean noClip, String... aliases) {
        this.allowedToFly = allowedToFly;
        this.immutableWorld = immutableWorld;
        this.noClip = noClip;
        this.aliases = aliases;
    }

    @Nonnull
    public static GameMode parse(String gamemodeString) {
        gamemodeString = gamemodeString.toLowerCase();
        for (GameMode gameMode : GameMode.values()) {
            for (String alias : gameMode.aliases) {
                if (gamemodeString.equalsIgnoreCase(alias)) return gameMode;
            }
        }
        return GameMode.SURVIVAL;
    }

    @NonNull
    public static GameMode parse(int gamemodeNum) {
        GameMode gameMode = GameMode.values()[gamemodeNum];
        return (gameMode == null ? GameMode.SURVIVAL : gameMode);
    }
}
