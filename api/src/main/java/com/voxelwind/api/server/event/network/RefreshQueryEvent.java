package com.voxelwind.api.server.event.network;

import com.voxelwind.api.plugin.PluginContainer;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.Server;
import com.voxelwind.api.server.event.Event;

import java.util.ArrayList;
import java.util.Collection;

public class RefreshQueryEvent implements Event {
    private final Collection<Player> players;
    private final Collection<PluginContainer> plugins;
    private String motd;
    private String gametype;
    private String version;
    private String map;
    private long playerCount;
    private long maxPlayers;
    private short hostPort;
    private String hostIp;
    private boolean whitelisted;

    public RefreshQueryEvent(Server server, String motd, String gametype, String map, long playerCount, long maxPlayers, short hostPort, String hostIp, boolean whitelisted) {
        this.players = new ArrayList<>(server.getAllOnlinePlayers());
        this.plugins = new ArrayList<>(server.getPluginManager().getAllPlugins());
        this.motd = motd;
        this.gametype = gametype;
        this.version = server.getVersion();
        this.map = map;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
        this.hostPort = hostPort;
        this.hostIp = hostIp;
        this.whitelisted = whitelisted;
    }

    /**
     * Player names to be sent with the query. Remove all to hide players on your server.
     *
     * @return players that will be sent in the query.
     */
    public Collection<Player> getPlayers() {
        return players;
    }

    /**
     * List of plugins sent with the query.
     *
     * @return currently enabled plugins to be sent in the query.
     */
    public Collection<PluginContainer> getPlugins() {
        return plugins;
    }

    /**
     * MOTD to be sent in the query.
     *
     * @return MOTD.
     */
    public String getMotd() {
        return motd;
    }

    /**
     * Sets the MOTD to be sent in the query.
     *
     * @param motd MOTD.
     */
    public void setMotd(String motd) {
        this.motd = motd;
    }

    /**
     * Gametype to be sent in the query.
     *
     * @return gametype.
     */
    public String getGametype() {
        return gametype;
    }

    /**
     * Sets the gametype to be sent in the query.
     *
     * @param gametype gametype.
     */
    public void setGametype(String gametype) {
        this.gametype = gametype;
    }

    /**
     * Minecraft version to be sent in the query
     *
     * @return Minecraft version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the Minecraft version to be sent in the query.
     *
     * @param version Minecraft version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The map or world name to be sent in the query.
     *
     * @return map name.
     */
    public String getMap() {
        return map;
    }

    /**
     * Sets the map or world name to be sent in the query.
     *
     * @param map map name.
     */
    public void setMap(String map) {
        this.map = map;
    }

    /**
     * Player count to be set in the query.
     *
     * @return player count.
     */
    public long getPlayerCount() {
        return playerCount;
    }

    /**
     * Sets the player count to be sent in the query.
     *
     * @param playerCount player count.
     */
    public void setPlayerCount(long playerCount) {
        this.playerCount = playerCount;
    }

    /**
     * Maximum amount of players to be sent in the query.
     *
     * @return max players.
     */
    public long getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum amount of players to be sent in the query.
     *
     * @param maxPlayers max players.
     */
    public void setMaxPlayers(long maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * The port at which the server is hosted on sent in the query.
     *
     * @return server port.
     */
    public short getHostPort() {
        return hostPort;
    }

    /**
     * Sets the port of the server which is sent in the query.
     *
     * @param hostPort server port.
     */
    public void setHostPort(short hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * Address of the server to be sent in the query.
     *
     * @return server address.
     */
    public String getHostIp() {
        return hostIp;
    }

    /**
     * Sets the address of the server to be sent in the query.
     *
     * @param hostIp server address.
     */
    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    /**
     * Whether or not the server is whitelisted in the query.
     *
     * @return server whitelisted.
     */
    public boolean isWhitelisted() {
        return whitelisted;
    }

    /**
     * Sets whether or not the server is whitelisted in the query.
     *
     * @param whitelisted server whitelisted.
     */
    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }
}
