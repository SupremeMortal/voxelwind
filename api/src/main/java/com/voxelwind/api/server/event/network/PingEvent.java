package com.voxelwind.api.server.event.network;

import com.voxelwind.api.server.event.Event;
import com.voxelwind.api.server.player.GameMode;
import lombok.NonNull;

import java.net.InetSocketAddress;

public class PingEvent implements Event {
    private long playerCount;
    private long maxPlayers;
    private String motd;
    private String motd2;
    private final InetSocketAddress socketAddress;
    private GameMode gameMode;
    private int protocolVersion;
    private String minecraftVersion;

    public PingEvent(long playerCount, long maxPlayers, String motd, String motd2, InetSocketAddress socketAddress, GameMode gameMode, int protocolVersion, String minecraftVersion) {
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
        this.motd = motd;
        this.motd2 = motd2;
        this.socketAddress = socketAddress;
        this.gameMode = gameMode;
        this.protocolVersion = protocolVersion;
        this.minecraftVersion = minecraftVersion;
    }

    /**
     * The default gamemode broadcast to clients on the server list.
     *
     * @return The default gamemode.
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Sets the default gamemode to broadcast to clients on the server list.
     *
     * @param gamemode default gamemode.
     */
    public void setGameMode(@NonNull GameMode gamemode) {
        this.gameMode = gamemode;
    }

    /**
     * Gets the address of the client pinging the server.
     *
     * @return client's address
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Gets the first line of the MOTD sent to clients on the server list.
     *
     * @return first line of motd.
     */
    public String getMotd() {
        return motd;
    }

    /**
     * Sets the first line of the MOTD sent to clients on the server list.
     *
     * @param motd first line of motd.
     */
    public void setMotd(@NonNull String motd) {
        this.motd = motd;
    }

    /**
     * Gets the second line of the MOTD sent to clients on the server list. This is only visible on LAN or featured servers.
     *
     * @return second line of motd.
     */
    public String getMotd2() {
        return motd2;
    }

    /**
     * Sets the second line of the MOTD sent to clients on the server list. This is only visible on LAN or featured servers.
     *
     * @param motd2 second line of motd.
     */
    public void setMotd2(@NonNull String motd2) {
        this.motd2 = motd2;
    }

    /**
     * Gets the player count which will be displayed on client's server list.
     *
     * @return player count.
     */
    public long getPlayerCount() {
        return playerCount;
    }

    /**
     * Sets the player count which will be displayed on client's server list.
     *
     * @param playerCount player count.
     */
    public void setPlayerCount(long playerCount) {
        this.playerCount = playerCount;
    }

    /**
     * Gets the maximum player count which will be displayed on client's server list.
     *
     * @return max player count.
     */
    public long getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum player count which will be displayed on client's server list.
     *
     * @param maxPlayers max player count.
     */
    public void setMaxPlayers(long maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Gets the protocol version which will be shown on the server list.
     *
     * @return current protocol version.
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Set the protocol version to be broadcast to clients on the server list.
     *
     * @param protocolVersion broadcasted protocol version.
     */
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Gets the Minecraft version visible on the server list to clients.
     *
     * @return current version used.
     */
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    /**
     * Set the Minecraft version visible on the server list to clients.
     *
     * @param minecraftVersion broadcasted Minecraft version.
     */
    public void setMinecraftVersion(@NonNull String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }
}
