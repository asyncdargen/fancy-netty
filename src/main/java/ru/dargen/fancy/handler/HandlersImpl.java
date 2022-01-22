package ru.dargen.fancy.handler;

import lombok.Setter;
import lombok.experimental.Accessors;
import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.packet.Packet;

@Setter @Accessors(chain = true, fluent = true)
public class HandlersImpl implements Handlers {

    protected RemoteConnectHandler onConnect;
    protected RemoteDisconnectHandler onDisconnect;
    protected PacketHandler onOutPacket;
    protected PacketHandler onInPacket;

    public boolean handleConnect(FancyRemote remote) {
        return onConnect == null || onConnect.on(remote);
    }

    public void handleDisconnect(FancyRemote remote) {
        if (onDisconnect != null)
            onDisconnect.on(remote);
    }

    public boolean handleOutPacket(FancyRemote remote, Packet packet) {
        return onOutPacket == null || onOutPacket.on(remote, packet);
    }

    public boolean handleInPacket(FancyRemote remote, Packet packet) {
        return onInPacket == null || onInPacket.on(remote, packet);
    }

}
