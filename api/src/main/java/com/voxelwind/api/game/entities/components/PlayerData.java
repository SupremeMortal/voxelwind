package com.voxelwind.api.game.entities.components;

import com.voxelwind.api.server.Skin;
import com.voxelwind.api.server.player.GameMode;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents data associated with a player.
 */
public interface PlayerData extends Component {
    Skin getSkin();

    @Nonnull
    GameMode getGameMode();

    void setGameMode(@Nonnull GameMode mode);

    float getBaseSpeed();

    void setBaseSpeed(float baseSpeed);

    @Nonnegative
    int getHunger();

    void setHunger(@Nonnegative int hunger);

    float getSaturation();

    void setSaturation(@Nonnegative float saturation);

    float getExhaustion();

    void setExhaustion(@Nonnegative float exhaustion);

    boolean isFlying();

    void setFlying(boolean flying);

    boolean isAllowedToFly();

    void setAllowedToFly(boolean allowedToFly);

    boolean isImmutableWorld();

    void setImmutableWorld(boolean immutableWorld);

    boolean canPvP();

    void setPvP(boolean pvp);

    boolean canAutoJump();

    void setAutoJump(boolean autoJump);

    boolean canNoClip();

    void setNoClip(boolean noClip);

    boolean isWorldBuilder();

    void setWorldBuilder(boolean worldBuilder);

    boolean isMuted();

    void setMuted(boolean muted);
}
