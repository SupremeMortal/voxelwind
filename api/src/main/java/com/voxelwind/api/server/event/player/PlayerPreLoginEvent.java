package com.voxelwind.api.server.event.player;

import com.voxelwind.api.server.event.Cancellable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.InetSocketAddress;

/**
 * Called as soon as the player sends their first request packet to join the server.
 */
@ParametersAreNonnullByDefault
public class PlayerPreLoginEvent implements Cancellable {
    private boolean cancelled;
    private final InetSocketAddress socketAddress;

    public PlayerPreLoginEvent(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        cancelled = false;
    }

    /**
     * Set the cancellation state of the event. Set true if you want the player be disconnected.
     *
     * @param cancelled true to cancel the event.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the cancellation state of the event.
     *
     * @return whether the event has been canceled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * The address of the player connecting.
     *
     * @return player's address
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

}
