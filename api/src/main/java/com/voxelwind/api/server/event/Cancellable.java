package com.voxelwind.api.server.event;

/**
 * An event that can be cancelled.
 */
public interface Cancellable extends Event {

    /**
     * Decides whether to cancel the event.
     *
     * @param cancelled true to cancel the event.
     */
    void setCancelled(boolean cancelled);

    /**
     * Whether or not to cancel the event.
     *
     * @return whether the event will be cancelled.
     */
    boolean isCancelled();
}
