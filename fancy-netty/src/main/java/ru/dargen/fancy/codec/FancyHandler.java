package ru.dargen.fancy.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.remote.FancyNettyRemote;
import ru.dargen.fancy.remote.FancyRemote;

import java.util.function.Consumer;
import java.util.logging.Level;

@RequiredArgsConstructor
public class FancyHandler extends SimpleChannelInboundHandler<FancyPacket> {

    protected FancyRemote remote;

    protected final Consumer<FancyRemote> onActive;
    protected final Consumer<FancyRemote> onClose;

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        val channel = ctx.channel();
        val parent = channel.attr(FancyRemote.ATTRIBUTE_PARENT_KEY).get();
        remote = new FancyNettyRemote(parent, parent.getMetrics().fork(), ctx.channel(), parent.getCallbackProvider());
        channel.attr(FancyRemote.ATTRIBUTE_REMOTE_KEY).set(remote);

        channel.pipeline().addBefore("handler", "codec", new FancyCodec(remote));

        if (onActive != null) onActive.accept(remote);

        if (!remote.getRemoteHandler().handleConnect(remote)) remote.close();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (onClose != null) onClose.accept(remote);

        remote.getRemoteHandler().handleDisconnect(remote);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FancyPacket packet) throws Exception {
        val callback = remote.getCallbackProvider().get(packet.getUniqueId());

        if (remote.getRemoteHandler().handleInPacket(remote, callback, packet)) {
            if (!remote.getCallbackProvider().completeCallback(packet))
                remote.getPacketRegistry().firePacketHandlers(packet, remote);
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        remote.getLogger().log(Level.SEVERE, "Exception caught", cause);
    }

}
