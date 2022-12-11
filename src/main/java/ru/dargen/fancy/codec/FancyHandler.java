package ru.dargen.fancy.codec;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.server.FancyRemote;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Level;

@RequiredArgsConstructor
public class FancyHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    protected final FancyRemote remote;

    protected final Consumer<FancyRemote> onActive;
    protected final Consumer<FancyRemote> onClose;

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (onActive != null) onActive.accept(remote);

        if (!remote.getHandlers().handleConnect(remote))
            remote.close();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (onClose != null) onClose.accept(remote);

        remote.getHandlers().handleDisconnect(remote);
    }

    public boolean acceptInboundMessage(Object msg) {
        return msg instanceof TextWebSocketFrame;
    }

    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (!(msg instanceof TextWebSocketFrame))
            return;

        String text = ((TextWebSocketFrame) msg).text();
        try {
            val json = remote.getGson().fromJson(text, JsonObject.class);

            val typeId = json.getAsJsonPrimitive("type").getAsInt();
            val id = json.getAsJsonPrimitive("id").getAsString();
            val packetJson = json.get("packet");

            Class<? extends Packet> type = remote.getPacketRegistry().getPacketTypeFromId(typeId);

            if (type == null)
                throw new IllegalStateException("unknown packet type " + typeId);


            val packet = remote.getGson().fromJson(packetJson, type);

            if (packet != null && remote.getHandlers().handleInPacket(remote, packet))
                if (!remote.getCallbackProvider().completeCallback(id, packet))
                    packet.handle(remote, id);

            remote.getMetrics().incrementInPackets(text.getBytes(StandardCharsets.UTF_8).length);
        } catch (Throwable e) {
            remote.getLogger().log(Level.SEVERE, "Exception while read packet", e);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        remote.getLogger().log(Level.SEVERE, "Exception caught", cause);
    }

}
