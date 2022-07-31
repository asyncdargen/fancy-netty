package ru.dargen.fancy.client;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.dargen.fancy.Fancy;
import ru.dargen.fancy.codec.FancyHandler;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.metrics.Metrics;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.PacketContainer;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.CallbackProvider;
import ru.dargen.fancy.packet.callback.CallbackProviderImpl;
import ru.dargen.fancy.packet.registry.PacketRegistry;
import ru.dargen.fancy.util.NettyUtil;

import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
@RequiredArgsConstructor
public class FancyClientImpl implements FancyClient {

    protected final Object lock = new Object();
    protected final EventLoopGroup eventLoop = NettyUtil.EVENT_LOOP.get();
    protected final CallbackProvider callbackProvider = new CallbackProviderImpl();
    protected final Metrics metrics = Fancy.createMetrics();

    protected final Logger logger;
    protected final Gson gson;
    protected final PacketRegistry packetRegistry;
    protected final Handlers handlers;

    protected Channel channel;
    protected boolean autoReconnect = true;
    protected boolean throwInactive = false;

    protected String host;
    protected int port;

    public ChannelFuture connect(String host, int port) {
        if (isActive())
            throw new IllegalStateException("Client already connected");

        return new Bootstrap()
                .group(eventLoop)
                .channel(NettyUtil.CLIENT_CHANNEL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2500)
                .option(ChannelOption.TCP_NODELAY, true)
//                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.IP_TOS, 24)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel channel) {
                        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                        channel.pipeline()
                                .addLast("codec", new HttpClientCodec())
                                .addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE))
                                .addLast("protocol_handler", new WebSocketClientProtocolHandler(
                                        WebSocketClientHandshakerFactory.newHandshaker(
                                                URI.create("ws://" + host + ":" + port + "/"),
                                                WebSocketVersion.V13,
                                                null,
                                                false,
                                                new DefaultHttpHeaders(),
                                                Integer.MAX_VALUE
                                        ),
                                        true
                                ))
                                .addLast("handler", new FancyHandler(FancyClientImpl.this, null, null));
                    }
                }).remoteAddress(this.host = host, this.port = port)
                .connect().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        channel = future.channel();
                        logger.info("Connection successful to " + channel);
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    } else {
                        getLogger().log(Level.WARNING, "Connection error", future.cause());
                        if (autoReconnect) {
                            getLogger().info("Auto reconnect in 1.5 seconds");
                            eventLoop.schedule(this::reconnect, 1500, TimeUnit.MILLISECONDS);
                        }
                    }
                });
    }

    public void reconnect() {
        getLogger().info("Reconnecting");
        connect(host, port);
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
        return channel != null && channel.isActive();
    }

    public void close() {
        if (!isActive()) return;
        logger.info("Disconnecting from " + channel);
        channel.close();
        channel = null;
    }

    public <P extends Packet> Callback<P> write(Packet packet) {
        if (!isActive())
            if (throwInactive)
                throw new IllegalStateException("Remote inactive");
            else return null;

        Callback<P> callback = callbackProvider.create(this);

        if (getHandlers().handleOutPacket(this, packet)) {
            PacketContainer container = new PacketContainer(packet, callback.getId(), getPacketRegistry().getPacketIdFromType(packet.getClass()));
            container.validate();
//            eventLoop.execute(() -> {
                String json = getGson().toJson(container);
                channel.writeAndFlush(new TextWebSocketFrame(json));
                getMetrics().incrementOutPackets(json.getBytes(StandardCharsets.UTF_8).length);
//            });
        }

        return callback;
    }

    public <P extends Packet> Callback<P> write(Packet packet, String id) {
        if (!isActive())
            if (throwInactive)
                throw new IllegalStateException("Remote inactive");
            else return null;

        Callback<P> callback = callbackProvider.create(this, id);

        if (getHandlers().handleOutPacket(this, packet)) {
            PacketContainer container = new PacketContainer(packet, callback.getId(), getPacketRegistry().getPacketIdFromType(packet.getClass()));
            container.validate();
//            eventLoop.execute(() -> {
                String json = getGson().toJson(container);
                channel.writeAndFlush(new TextWebSocketFrame(json));
                getMetrics().incrementOutPackets(json.getBytes(StandardCharsets.UTF_8).length);
//            });
        }

        return callback;
    }

    public SocketAddress getAddress() {
        return isActive() ? channel.remoteAddress() : null;
    }

}
