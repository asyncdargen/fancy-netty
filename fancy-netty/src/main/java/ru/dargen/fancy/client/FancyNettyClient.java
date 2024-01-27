package ru.dargen.fancy.client;

import ru.dargen.fancy.FancyBase;
import ru.dargen.fancy.codec.FancyChannelInitializer;
import ru.dargen.fancy.packet.registry.FancyPacketRegistry;
import ru.dargen.fancy.util.NettyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.dargen.fancy.handler.FancyRemoteHandler;
import ru.dargen.fancy.metrics.FancyMetrics;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.builtin.FancyKeepAlivePacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.FancyCallbackProvider;
import ru.dargen.fancy.remote.FancyRemote;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Setter
@Getter
@RequiredArgsConstructor
public class FancyNettyClient implements FancyClient {

    protected final ChannelInitializer<SocketChannel> clientChannelInitializer = new FancyChannelInitializer(
            this,
            remote -> {
                this.remote = remote;
                if (!getPacketRegistry().hasPacketHandlers(FancyKeepAlivePacket.class))
                    getPacketRegistry().registerHandler(FancyKeepAlivePacket.class, FancyRemote::write);
                synchronized (getLock()) {
                    getLock().notifyAll();
                }
                getLogger().info("Connected to remote on " + remote.getAddress().getHostAddress());
            },
            remote -> {
                getLogger().info("Disconnected from remote on " + remote.getAddress().getHostAddress());
                this.remote = null;
                tryAutoReconnect();
            }
    );

    protected final Object lock = new Object();
    protected final EventLoopGroup eventLoop = NettyUtil.getWorkerLoopGroup();
    protected final FancyCallbackProvider callbackProvider = new FancyCallbackProvider();
    protected final FancyMetrics metrics = new FancyMetrics();

    protected final Logger logger;
    protected final FancyPacketRegistry packetRegistry;
    protected final FancyRemoteHandler remoteHandler;

    protected FancyRemote remote;
    protected boolean autoReconnect = true;
    protected boolean throwInactive = false;

    protected InetSocketAddress remoteAddress;

    {
        eventLoop.scheduleAtFixedRate(callbackProvider::tick, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public ChannelFuture connect(String host, int port) {
        if (isActive())
            throw new IllegalStateException("Client already connected");

        return new Bootstrap()
                .group(eventLoop)
                .channel(NettyUtil.CLIENT_CHANNEL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2500)
                .handler(clientChannelInitializer)
                .remoteAddress(remoteAddress = InetSocketAddress.createUnresolved(host, port))
                .connect().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        logger.info("Connection established to " + future.channel());
                    } else {
                        getLogger().log(Level.WARNING, "Connection error", future.cause());
                        tryAutoReconnect();
                    }
                });
    }

    public void tryAutoReconnect() {
        if (autoReconnect) {
            getLogger().info("Auto reconnect in 1.5 seconds");
            eventLoop.schedule(this::reconnect, 1500L, TimeUnit.MILLISECONDS);
        }
    }

    public void reconnect() {
        getLogger().info("Reconnecting to " + remoteAddress);
        connect(remoteAddress.getHostName(), remoteAddress.getPort());
    }

    public void await() {
        synchronized (lock) {
            while (!isActive())
                try {
                    lock.wait();
                } catch (InterruptedException e) {

                }
        }
    }

    public boolean isActive() {
        return remote != null && remote.isActive();
    }

    public void close() {
        if (!isActive())
            return;

        remote.close();
    }

    public InetSocketAddress getSocketAddress() {
        return isActive() ? remote.getSocketAddress() : null;
    }

    @Override
    public FancyBase getParent() {
        return null;
    }

    @Override
    public Channel getChannel() {
        return isActive() ? remote.getChannel(): null;
    }

    @Override
    public void write(FancyPacket packet, UUID id) {
        if (remote != null) remote.write(packet, id);
    }

    @Override
    public void write(FancyPacket packet) {
        if (remote != null) remote.write(packet);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, long timeout, Runnable timeoutHandler) {
        return remote == null ? null : remote.writeAwait(packet, id, timeout, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, long timeout, Runnable timeoutHandler) {
        return remote == null ? null : remote.writeAwait(packet, timeout, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, long timeout) {
        return remote == null ? null : remote.writeAwait(packet, timeout);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, long timeout) {
        return remote == null ? null : remote.writeAwait(packet, timeout);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, Runnable timeoutHandler) {
        return remote == null ? null : remote.writeAwait(packet, id, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, Runnable timeoutHandler) {
        return remote == null ? null : remote.writeAwait(packet, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id) {
        return remote == null ? null : remote.writeAwait(packet, id);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet) {
        return remote == null ? null : remote.writeAwait(packet);
    }

}
