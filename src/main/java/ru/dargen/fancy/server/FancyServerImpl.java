package ru.dargen.fancy.server;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.dargen.fancy.Fancy;
import ru.dargen.fancy.codec.FancyHandler;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.metrics.Metrics;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.CallbackProviderImpl;
import ru.dargen.fancy.packet.registry.PacketRegistry;
import ru.dargen.fancy.util.NettyUtil;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class FancyServerImpl implements FancyServer {

    protected final Set<FancyRemote> clients = Sets.newConcurrentHashSet();
    protected final EventLoopGroup eventLoop = NettyUtil.EVENT_LOOP.get();
    protected final Metrics metrics = Fancy.createMetrics();

    protected final Logger logger;
    protected final Gson gson;
    protected final PacketRegistry packetRegistry;
    protected final Handlers handlers;

    protected ChannelFuture channelFuture;
    protected int port;

    public boolean isActive() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    public void close() {
        if (isActive()) {
            channelFuture.channel().close();
            channelFuture = null;
            logger.info("Server on *:" + port + " closed");
        }
    }

    public ChannelFuture bind(int port) {
        if (isActive())
            throw new IllegalStateException("Server already bind!");

        return channelFuture = new ServerBootstrap()
                .channel(NettyUtil.SERVER_CHANNEL)
                .group(eventLoop, NettyUtil.EVENT_LOOP.get())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2500)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .option(ChannelOption.IP_TOS, 24)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
//                        channel.config().setOption(ChannelOption.SO_KEEPALIVE, true);
                        channel.config().setOption(ChannelOption.IP_TOS, 24);
                        FancyRemote remote = new FancyRemoteImpl(FancyServerImpl.this, new CallbackProviderImpl(), metrics.fork(), channel);
                        SocketAddress address = remote.getAddress();
                        channel.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(Integer.MAX_VALUE),
//                                new HttpResponseEncoder(),
                                new WebSocketServerProtocolHandler("/", null, false, Integer.MAX_VALUE),
                                new FancyHandler(remote, r -> {
                                    logger.info("Client connected from " + address);
                                    clients.add(remote);
                                }, r -> {
                                    logger.info("Client disconnected from " + address);
                                    clients.remove(remote);
                                }));
                    }
                }).bind(this.port = port)
                .addListener(future -> {
                    if (future.isSuccess())
                        logger.info("Server bind on *: " + port);
                    else logger.log(Level.SEVERE, "Error while bind server", future.cause());
                });
    }

    public void broadcast(Packet packet) {
        if (!isActive())
            throw new IllegalStateException("Server not started!");

        clients.forEach(remote -> {
            try {
                remote.write(packet);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception while send packet", e);
            }
        });
    }

    public <P extends Packet> List<Callback<P>> broadcastCallback(Packet packet) {
        if (!isActive())
            throw new IllegalStateException("Server not started!");

        return clients.stream().map(remote -> {
            Callback<P> callback = null;
            try {
                callback = remote.write(packet);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception while send packet", e);
            }

            return callback;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
