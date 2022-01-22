package ru.dargen.fancy.handler;

import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.packet.Packet;

public interface Handlers {

    Handlers onConnect(RemoteConnectHandler handler);

    Handlers onDisconnect(RemoteDisconnectHandler handler);

    Handlers onOutPacket(PacketHandler handler);

    Handlers onInPacket(PacketHandler handler);

    boolean handleConnect(FancyRemote remote);
    
    void handleDisconnect(FancyRemote remote);
    
    boolean handleOutPacket(FancyRemote remote, Packet packet);
    
    boolean handleInPacket(FancyRemote remote, Packet packet);

}
