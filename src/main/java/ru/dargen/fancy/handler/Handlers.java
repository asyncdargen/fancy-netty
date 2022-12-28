package ru.dargen.fancy.handler;

import ru.dargen.fancy.handler.context.PacketHandlerContext;
import ru.dargen.fancy.handler.context.RemoteConnectHandlerContext;
import ru.dargen.fancy.handler.context.RemoteDisconnectHandlerContext;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.server.FancyRemote;

import java.util.function.Consumer;

public interface Handlers {

    Handlers onConnect(Consumer<RemoteConnectHandlerContext> handler);

    Handlers onDisconnect(Consumer<RemoteDisconnectHandlerContext> handler);

    Handlers onOutPacket(Consumer<PacketHandlerContext> handler);

    Handlers onInPacket(Consumer<PacketHandlerContext> handler);

    boolean handleConnect(FancyRemote remote);
    
    void handleDisconnect(FancyRemote remote);
    
    boolean handleOutPacket(FancyRemote remote, Callback<?> callback, Packet packet);
    
    boolean handleInPacket(FancyRemote remote, Callback<?> callback, Packet packet);

}
