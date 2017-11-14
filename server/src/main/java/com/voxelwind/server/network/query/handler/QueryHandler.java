package com.voxelwind.server.network.query.handler;

import com.google.common.collect.ImmutableMap;
import com.voxelwind.api.server.event.network.RefreshQueryEvent;
import com.voxelwind.server.VoxelwindServer;
import com.voxelwind.server.jni.hash.VoxelwindHash;
import com.voxelwind.server.network.query.QueryUtil;
import com.voxelwind.server.network.query.packets.QueryHandshake;
import com.voxelwind.server.network.query.packets.QueryStatistics;
import com.voxelwind.server.network.raknet.enveloped.DirectAddressedRakNetPacket;
import com.voxelwind.server.network.util.EncryptionUtil;
import com.voxelwind.server.network.util.NativeCodeFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;

@Log4j2
public class QueryHandler {
    private final VoxelwindServer server;
    private byte[] lastToken;
    private byte[] token;
    private ByteBuf shortStats;
    private ByteBuf longStats;

    private static final ThreadLocal<VoxelwindHash> hashLocal = ThreadLocal.withInitial(NativeCodeFactory.hash::newInstance);

    public QueryHandler(VoxelwindServer server) {
        this.server = server;
        refreshInfo();
        refreshToken();
    }

    public void handlePackage(ChannelHandlerContext ctx, DirectAddressedRakNetPacket packet) throws Exception {
        if (packet.content() instanceof QueryHandshake) {
            QueryHandshake handshake = (QueryHandshake) packet.content();
            handshake.setToken(getTokenString(packet.sender()));
            ctx.writeAndFlush(new DirectAddressedRakNetPacket(handshake, packet.sender(), packet.recipient()), ctx.voidPromise());
        }
        if (packet.content() instanceof QueryStatistics) {
            QueryStatistics statistics = (QueryStatistics) packet.content();
            log.debug("\nReceived Token: {}\nActual Token:   {}", statistics.getToken(), getTokenInt(packet.sender()));
            if (!(statistics.getToken() == getTokenInt(packet.sender()))) {
                return;
            }

            if (statistics.isFull()) {
                statistics.setPayload(longStats);
            } else {
                statistics.setPayload(shortStats);
            }
            ctx.writeAndFlush(new DirectAddressedRakNetPacket(statistics, packet.sender(), packet.recipient()), ctx.voidPromise());
        }
    }

    public void refreshToken() {
        lastToken = token;
        token = EncryptionUtil.generateRandomToken();
    }

    public void refreshInfo() {
        if (longStats != null) {
            longStats.release();
            shortStats.release();
        }

        RefreshQueryEvent event = new RefreshQueryEvent(server, "Voxelwind Server", "SMP",
                server.getDefaultLevel().getName(), server.getSessionManager().countConnected(),
                server.getConfiguration().getMaximumPlayerLimit(), (short) server.getConfiguration().getMcpeListener().getPort(),
                server.getConfiguration().getMcpeListener().getHost(), false
        );
        server.getEventManager().fire(event);

        StringJoiner pluginJoiner = new StringJoiner(";");
        event.getPlugins().forEach(pluginContainer -> pluginJoiner.add(pluginContainer.getId() + " " + pluginContainer.getVersion()));

        ByteBuf longStat = PooledByteBufAllocator.DEFAULT.buffer();
        ByteBuf shortStat = PooledByteBufAllocator.DEFAULT.buffer();
        longStat.writeBytes(QueryUtil.LONG_RESPONSE_PADDING_TOP);
        ImmutableMap<String, String> kvs = ImmutableMap.<String, String>builder()
                .put("hostname", event.getMotd())
                .put("gametype", event.getGametype())
                .put("map", event.getMap())
                .put("numplayers", Long.toString(event.getPlayerCount()))
                .put("maxplayers", Long.toString(event.getMaxPlayers()))
                .put("hostport", Short.toString(event.getHostPort()))
                .put("hostip", event.getHostIp())
                .put("game_id", "MINECRAFT")
                .put("version", event.getVersion())
                .put("plugins", "Voxelwind " + server.getVoxelwindVersion() + ":" + pluginJoiner.toString())
                .put("whitelisted", Boolean.toString(event.isWhitelisted()))
                .build();

        kvs.forEach((key, value) -> {
            QueryUtil.writeNullTerminatedString(longStat, key);
            QueryUtil.writeNullTerminatedString(longStat, value);
        });
        longStat.writeByte(0x00);

        kvs.values().asList().subList(0, 4).forEach(value -> QueryUtil.writeNullTerminatedString(shortStat, value));
        shortStat.writeShortLE(event.getHostPort());
        QueryUtil.writeNullTerminatedString(shortStat, event.getHostIp());

        longStat.writeBytes(QueryUtil.LONG_RESPONSE_PADDING_BOTTOM);
        event.getPlayers().forEach(player -> QueryUtil.writeNullTerminatedString(longStat, player.getName()));
        this.shortStats = shortStat;
        this.longStats = longStat;
    }

    private String getTokenString(InetSocketAddress socketAddress) {
        return Integer.toString(getTokenInt(socketAddress));

    }

    private int getTokenInt(InetSocketAddress socketAddress) {
        return ByteBuffer.wrap(getToken(socketAddress)).getInt();
    }

    private byte[] getToken(InetSocketAddress socketAddress) {
        VoxelwindHash hash = hashLocal.get();
        ByteBuf counterBuf = PooledByteBufAllocator.DEFAULT.directBuffer(20);
        ByteBuf keyBuf = PooledByteBufAllocator.DEFAULT.directBuffer(16);
        try {
            counterBuf.writeBytes(socketAddress.toString().getBytes(StandardCharsets.UTF_8));
            keyBuf.writeBytes(token);

            hash.update(counterBuf);
            hash.update(keyBuf);
            byte[] digested = hash.digest();
            return Arrays.copyOf(digested, 4);
        } finally {
            counterBuf.release();
            keyBuf.release();
        }
    }
}
