package ru.dargen.fancy.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.RequiredArgsConstructor;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.server.FancyRemote;

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
//        remote.getEventLoop().execute(() -> {
            try {
                String text = ((TextWebSocketFrame) msg).text();
                JsonObject json = remote.getGson().fromJson(text, JsonObject.class);

                int typeId = json.getAsJsonPrimitive("type").getAsInt();
                String id = json.getAsJsonPrimitive("id").getAsString();
                JsonElement packetRaw = json.get("packet");

                Class<? extends Packet> type = remote.getPacketRegistry().getPacketTypeFromId(typeId);

                if (type == null)
                    throw new IllegalStateException("unknown packet type " + typeId);

                Packet packet = remote.getGson().fromJson(packetRaw, type);

                if (packet != null && remote.getHandlers().handleInPacket(remote, packet))
                    if (!remote.getCallbackProvider().completeCallback(id, packet))
                        packet.handle(remote, id);

            } catch (Throwable e) {
                remote.getLogger().log(Level.SEVERE, "Exception while read packet", e);
            }
//        });
    }

}
