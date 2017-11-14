package com.voxelwind.server.network.raknet.handler;

import com.voxelwind.api.server.event.network.PingEvent;
import com.voxelwind.api.server.event.player.PlayerPreLoginEvent;
import com.voxelwind.api.server.player.GameMode;
import com.voxelwind.server.VoxelwindServer;
import com.voxelwind.server.network.mcpe.util.VersionUtil;
import com.voxelwind.server.network.query.packets.QueryPackage;
import com.voxelwind.server.network.raknet.RakNetSession;
import com.voxelwind.server.network.raknet.enveloped.DirectAddressedRakNetPacket;
import com.voxelwind.server.network.raknet.packets.*;
import com.voxelwind.server.network.session.InitialNetworkPacketHandler;
import com.voxelwind.server.network.session.McpeSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.StringJoiner;

import static com.voxelwind.server.network.raknet.RakNetConstants.MAXIMUM_MTU_SIZE;
import static com.voxelwind.server.network.raknet.RakNetConstants.RAKNET_PROTOCOL_VERSION;

public class RakNetDirectPacketHandler extends SimpleChannelInboundHandler<DirectAddressedRakNetPacket> {
    private static final long SERVER_ID = 68382;
    private final VoxelwindServer server;

    public RakNetDirectPacketHandler(VoxelwindServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DirectAddressedRakNetPacket packet) throws Exception {
        try {
            if (server.getQueryHandler().isPresent() && packet.content() instanceof QueryPackage) {
                server.getQueryHandler().get().handlePackage(ctx, packet);
                return;
            }
            McpeSession session = server.getSessionManager().get(packet.sender());

            // ** Everything we can handle without a session **
            if (session == null) {
                if (packet.content() instanceof UnconnectedPingPacket) {
                    UnconnectedPingPacket request = (UnconnectedPingPacket) packet.content();
                    UnconnectedPongPacket response = new UnconnectedPongPacket();
                    response.setPingId(request.getPingId());
                    response.setServerId(SERVER_ID);
                    PingEvent event = new PingEvent(server.getSessionManager().countConnected(),
                            server.getConfiguration().getMaximumPlayerLimit(), "Voxelwind server",
                            "High performance Bedrock Edition Server", packet.sender(), GameMode.SURVIVAL,
                            VersionUtil.getBroadcastProtocolVersion(),
                            VersionUtil.getHumanVersionName(VersionUtil.getBroadcastProtocolVersion())
                    );
                    server.getEventManager().fire(event);
                    StringJoiner joiner = new StringJoiner(";");
                    joiner.add("MCPE")
                            .add(event.getMotd())
                            .add(Integer.toString(event.getProtocolVersion()))
                            .add(event.getMinecraftVersion())
                            .add(Long.toString(event.getPlayerCount()))
                            .add(Long.toString(event.getMaxPlayers()))
                            .add(event.getMotd().hashCode() + packet.sender().getHostString() + packet.sender().getPort())
                            .add(event.getMotd2())
                            .add(event.getGameMode().name());
                    response.setAdvertise(joiner.toString());
                    ctx.writeAndFlush(new DirectAddressedRakNetPacket(response, packet.sender(), packet.recipient()), ctx.voidPromise());
                    return;
                }
                if (packet.content() instanceof OpenConnectionRequest1Packet) {
                    PlayerPreLoginEvent event = new PlayerPreLoginEvent(packet.sender());
                    server.getEventManager().fire(event);
                    if (event.isCancelled()) {
                        ConnectionBannedPacket bannedPacket = new ConnectionBannedPacket();
                        bannedPacket.setServerGuid(SERVER_ID);
                        ctx.writeAndFlush(new DirectAddressedRakNetPacket(bannedPacket, packet.sender(), packet.recipient()), ctx.voidPromise());
                        return;
                    }
                    OpenConnectionRequest1Packet request = (OpenConnectionRequest1Packet) packet.content();
                    if (request.getProtocolVersion() != RAKNET_PROTOCOL_VERSION) {
                        // Incorrect protocol version.
                        IncompatibleProtocolVersion badVersion = new IncompatibleProtocolVersion();
                        badVersion.setServerGuid(SERVER_ID);
                        ctx.writeAndFlush(new DirectAddressedRakNetPacket(badVersion, packet.sender(), packet.recipient()), ctx.voidPromise());
                        return;
                    }
                    int maximum = server.getConfiguration().getMaximumPlayerLimit();
                    if (maximum > 0 && server.getSessionManager().countConnected() >= maximum) {
                        // Server is full
                        NoFreeIncomingConnectionsPacket badResponse = new NoFreeIncomingConnectionsPacket();
                        badResponse.setServerGuid(SERVER_ID);
                        ctx.writeAndFlush(new DirectAddressedRakNetPacket(badResponse, packet.sender(), packet.recipient()), ctx.voidPromise());
                        return;
                    }
                    OpenConnectionResponse1Packet response = new OpenConnectionResponse1Packet();
                    response.setMtuSize((request.getMtu() > MAXIMUM_MTU_SIZE ? MAXIMUM_MTU_SIZE : request.getMtu()));
                    response.setServerSecurity((byte) 0);
                    response.setServerGuid(SERVER_ID);
                    ctx.writeAndFlush(new DirectAddressedRakNetPacket(response, packet.sender(), packet.recipient()), ctx.voidPromise());
                    return;
                }
                if (packet.content() instanceof OpenConnectionRequest2Packet) {
                    OpenConnectionRequest2Packet request = (OpenConnectionRequest2Packet) packet.content();
                    OpenConnectionResponse2Packet response = new OpenConnectionResponse2Packet();
                    response.setMtuSize(request.getMtuSize());
                    response.setServerSecurity((byte) 0);
                    response.setClientAddress(packet.sender());
                    response.setServerId(SERVER_ID);
                    session = new McpeSession(null, server, new RakNetSession(packet.sender(), request.getMtuSize(), ctx.channel(), server));
                    session.setHandler(new InitialNetworkPacketHandler(session));
                    server.getSessionManager().add(packet.sender(), session);
                    ctx.writeAndFlush(new DirectAddressedRakNetPacket(response, packet.sender(), packet.recipient()), ctx.voidPromise());
                }
            } else {
                if (packet.content() instanceof AckPacket) {
                    if (session.getConnection() instanceof RakNetSession) {
                        ((RakNetSession) session.getConnection()).onAck(((AckPacket) packet.content()).getIds());
                    }
                }
                if (packet.content() instanceof NakPacket) {
                    if (session.getConnection() instanceof RakNetSession) {
                        ((RakNetSession) session.getConnection()).onNak(((NakPacket) packet.content()).getIds());
                    }
                }
            }
        } finally {
            packet.release();
        }
    }
}
