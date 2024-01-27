package ru.dargen.fancy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import ru.dargen.fancy.codec.FancyChannelInitializer;
import ru.dargen.fancy.handler.FancyRemoteHandler;
import ru.dargen.fancy.metrics.FancyMetrics;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.builtin.FancyKeepAlivePacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.FancyCallbackProvider;
import ru.dargen.fancy.packet.registry.FancyPacketRegistry;
import ru.dargen.fancy.remote.FancyRemote;
import ru.dargen.fancy.util.NettyUtil;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class FancyNettyServer implements FancyServer {

    protected final ChannelInitializer<SocketChannel> remoteChannelInitializer = new FancyChannelInitializer(
            this,
            remote -> {
                this.getRemotes().add(remote);
                getLogger().info("Remote connected from " + remote.getAddress().getHostAddress());
            },
            remote -> {
                this.getRemotes().remove(remote);
                getLogger().info("Remote disconnected on " + remote.getAddress().getHostAddress());
            }
    );

    protected final Set<FancyRemote> remotes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final EventLoopGroup eventLoop = NettyUtil.getWorkerLoopGroup();
    protected final FancyMetrics metrics = new FancyMetrics();

    protected final Logger logger;
    protected final FancyPacketRegistry packetRegistry;
    protected final FancyRemoteHandler remoteHandler;

    protected ChannelFuture channelFuture;
    protected int port;

    public boolean isActive() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    @SneakyThrows
    public ChannelFuture bind(int port) {
        if (isActive())
            throw new IllegalStateException("Server already bind!");

        return channelFuture = new ServerBootstrap()
                .channel(NettyUtil.SERVER_CHANNEL)
                .group(eventLoop, NettyUtil.getBossLoopGroup())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2500)
                .childHandler(remoteChannelInitializer)
                .bind(this.port = port)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        eventLoop.scheduleAtFixedRate(
                                () -> remotes.forEach(remote -> remote.getCallbackProvider().tick()),
                                50L, 50L, TimeUnit.MILLISECONDS
                        );
                        eventLoop.scheduleAtFixedRate(this::keepAliveRemotes, 2L, 2L, TimeUnit.SECONDS);

                        logger.info("Server bound on *: " + port);
                    }
                    else logger.log(Level.SEVERE, "Error while bind server", future.cause());
                });
    }

    @SneakyThrows
    public void close() {
        if (isActive()) {
            remotes.forEach(FancyRemote::close);
            getChannel().close();
            channelFuture = null;
            logger.info("Server on *:" + port + " closed");
        }
    }

    protected void keepAliveRemotes() {
        val timestamp = System.currentTimeMillis();
        remotes.forEach(remote -> {
            val metrics = remote.getMetrics();
            if (timestamp - metrics.getLastReceiveTime() > 40_000L) {
                logger.warning("Remote not alive on " + remote.getAddress());
                remote.close();
            } else {
                remote.write(new FancyKeepAlivePacket());
            }
        });
    }

    public void broadcast(FancyPacket packet) {
        if (!isActive())
            throw new IllegalStateException("Server not started!");

        remotes.forEach(remote -> {
            try {
                remote.write(packet);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception while send packet", e);
            }
        });
    }

    public <P extends FancyPacket> List<Callback<P>> broadcastCallback(FancyPacket packet) {
        if (!isActive())
            throw new IllegalStateException("Server not started!");

        return remotes.stream().map(remote -> {
            Callback<P> callback = null;
            try {
                callback = remote.writeAwait(packet);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception while send packet", e);
            }

            return callback;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public InetSocketAddress getSocketAddress() {
        return InetSocketAddress.createUnresolved("0.0.0.0", port);
    }

    @Override
    public FancyCallbackProvider getCallbackProvider() {
        return new FancyCallbackProvider();
    }

    @Override
    @SneakyThrows
    public Channel getChannel() {
        return channelFuture.sync().channel();
    }

}
