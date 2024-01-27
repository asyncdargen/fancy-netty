package ru.dargen.fancy.handler;

import ru.dargen.fancy.handler.context.FancyRemoteConnectHandlerContext;
import ru.dargen.fancy.handler.context.FancyRemoteDisconnectHandlerContext;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.remote.FancyRemote;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import ru.dargen.fancy.handler.context.FancyRemotePacketHandlerContext;

import java.util.function.Consumer;

@Setter
@Accessors(chain = true, fluent = true)
public class FancyRemoteHandler {

    protected Consumer<FancyRemoteConnectHandlerContext> onConnect;
    protected Consumer<FancyRemoteDisconnectHandlerContext> onDisconnect;
    protected Consumer<FancyRemotePacketHandlerContext> onOutPacket, onInPacket;

    public boolean handleConnect(FancyRemote remote) {
        val context = new FancyRemoteConnectHandlerContext(remote);
        if (onConnect != null) onConnect.accept(context);
        return onConnect == null || !context.isCancelled();
    }

    public void handleDisconnect(FancyRemote remote) {
        if (onDisconnect != null)
            onDisconnect.accept(new FancyRemoteDisconnectHandlerContext(remote));
    }

    public boolean handleOutPacket(FancyRemote remote, Callback<?> callback, FancyPacket packet) {
        val context = new FancyRemotePacketHandlerContext(remote, callback, packet);
        if (onOutPacket != null) onOutPacket.accept(context);
        return onOutPacket == null || !context.isCancelled();
    }

    public boolean handleInPacket(FancyRemote remote, Callback<?> callback, FancyPacket packet) {
        val context = new FancyRemotePacketHandlerContext(remote, callback, packet);
        if (onInPacket != null) onInPacket.accept(context);
        return onInPacket == null || !context.isCancelled();
    }

}
