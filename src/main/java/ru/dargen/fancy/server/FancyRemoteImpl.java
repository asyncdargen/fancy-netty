package ru.dargen.fancy.server;

import com.google.gson.Gson;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.PacketContainer;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.CallbackProvider;
import ru.dargen.fancy.packet.registry.PacketRegistry;

import java.net.SocketAddress;
import java.util.logging.Logger;

@Getter
@RequiredArgsConstructor
public class FancyRemoteImpl implements FancyRemote {

    protected final FancyServer server;
    protected final CallbackProvider callbackProvider;
    protected final SocketChannel channel;

    protected boolean throwInactive = false;

    public Logger getLogger() {
        return server.getLogger();
    }

    public Gson getGson() {
        return server.getGson();
    }

    public EventLoopGroup getEventLoop() {
        return server.getEventLoop();
    }

    public PacketRegistry getPacketRegistry() {
        return server.getPacketRegistry();
    }

    public Handlers getHandlers() {
        return server.getHandlers();
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public void close() {
        if (isActive())
            channel.close();
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
            getEventLoop().execute(() -> {
                String json = getGson().toJson(container);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            });
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
            getEventLoop().execute(() -> {
                String json = getGson().toJson(container);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            });
        }

        return callback;
    }

    public SocketAddress getAddress() {
        return isActive() ? channel.remoteAddress() : null;
    }
}
