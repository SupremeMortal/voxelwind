package com.voxelwind.api.server.event.network;

import com.voxelwind.api.server.event.Event;
import com.voxelwind.api.server.player.GameMode;
import lombok.Getter;

import java.net.InetSocketAddress;

@Getter
public class PingEvent implements Event {
    long playerCount;
    private String motd;
    private String motd2;
    private final InetSocketAddress socketAddress;
    private GameMode gameMode;

    public PingEvent(long playerCount, String motd, String motd2, InetSocketAddress socketAddress, GameMode gameMode) {
        this.playerCount = playerCount;
        this.motd = motd;
        this.motd2 = motd2;
        this.socketAddress = socketAddress;
        this.gameMode = gameMode;
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
    public void setGameMode(GameMode gamemode) {
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
    public void setMotd(String motd) {
        this.motd = motd;
    }

    /**
     * Gets the second line of the MOTD sent to clients on the server list. This is only visible on LAN or featured servers.
     *
     * @return second line of motd.
     */
    public String getMotd2() {
        return motd;
    }

    /**
     * Sets the second line of the MOTD sent to clients on the server list. This is only visible on LAN or featured servers.
     *
     * @param motd2 second line of motd.
     */
    public void setMotd2(String motd2) {
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
}
