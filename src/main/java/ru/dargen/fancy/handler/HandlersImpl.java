package ru.dargen.fancy.handler;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import ru.dargen.fancy.handler.context.PacketHandlerContext;
import ru.dargen.fancy.handler.context.RemoteConnectHandlerContext;
import ru.dargen.fancy.handler.context.RemoteDisconnectHandlerContext;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.server.FancyRemote;

import java.util.function.Consumer;

@Setter
@Accessors(chain = true, fluent = true)
public class HandlersImpl implements Handlers {

    protected Consumer<RemoteConnectHandlerContext> onConnect;
    protected Consumer<RemoteDisconnectHandlerContext> onDisconnect;
    protected Consumer<PacketHandlerContext> onOutPacket, onInPacket;

    public boolean handleConnect(FancyRemote remote) {
        val context = new RemoteConnectHandlerContext(remote);
        if (onConnect != null) onConnect.accept(context);
        return onConnect == null || !context.isCancelled();
    }

    public void handleDisconnect(FancyRemote remote) {
        if (onDisconnect != null)
            onDisconnect.accept(new RemoteDisconnectHandlerContext(remote));
    }

    public boolean handleOutPacket(FancyRemote remote, Callback<?> callback, Packet packet) {
        val context = new PacketHandlerContext(remote, callback, packet);
        if (onOutPacket != null) onOutPacket.accept(context);
        return onOutPacket == null || !context.isCancelled();
    }

    public boolean handleInPacket(FancyRemote remote, Callback<?> callback, Packet packet) {
        val context = new PacketHandlerContext(remote, callback, packet);
        if (onInPacket != null) onInPacket.accept(context);
        return onInPacket == null || !context.isCancelled();
    }

}
