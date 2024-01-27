package ru.dargen.fancy.remote;

import ru.dargen.fancy.FancyBase;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.dargen.fancy.handler.FancyRemoteHandler;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.FancyCallbackProvider;
import ru.dargen.fancy.packet.registry.FancyPacketRegistry;
import ru.dargen.fancy.metrics.FancyMetrics;
import ru.dargen.fancy.util.FancyException;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Logger;

@Getter
@RequiredArgsConstructor
public class FancyNettyRemote implements FancyRemote {

    protected final FancyBase parent;
    protected final FancyMetrics metrics;
    protected final Channel channel;

    protected final FancyCallbackProvider callbackProvider;
    protected boolean throwInactive = false;

    public Logger getLogger() {
        return parent.getLogger();
    }

    public FancyPacketRegistry getPacketRegistry() {
        return parent.getPacketRegistry();
    }

    public FancyRemoteHandler getRemoteHandler() {
        return parent.getRemoteHandler();
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public void close() {
        if (isActive()) channel.close();
    }

    @Override
    public EventLoopGroup getEventLoop() {
        return parent.getEventLoop();
    }

    public InetSocketAddress getSocketAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public void write(FancyPacket packet, UUID id) {
        writeAwait(packet, id);
    }

    @Override
    public void write(FancyPacket packet) {
        write(packet, null);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, long timeout, Runnable timeoutHandler) {
        if (!isActive()) {
            if (throwInactive) throw new FancyException("Remote inactive " + channel);
            return null;
        }

        if (id != null)
            packet.setUniqueId(id);

        val callback = getCallbackProvider().<P>create(this, packet.getUniqueId(), timeout, timeoutHandler);

        if (getRemoteHandler().handleOutPacket(this, callback, packet))
            channel.writeAndFlush(packet);

        return callback;
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, long timeout) {
        return writeAwait(packet, id, timeout, null);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id, Runnable timeoutHandler) {
        return writeAwait(packet, id, 3000L, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, long timeout, Runnable timeoutHandler) {
        return writeAwait(packet, null, timeout, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, long timeout) {
        return writeAwait(packet, null, timeout);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, Runnable timeoutHandler) {
        return writeAwait(packet, null, timeoutHandler);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet, UUID id) {
        return writeAwait(packet, id, null);
    }

    @Override
    public <P extends FancyPacket> Callback<P> writeAwait(FancyPacket packet) {
        return writeAwait(packet, (Runnable) null);
    }

}
