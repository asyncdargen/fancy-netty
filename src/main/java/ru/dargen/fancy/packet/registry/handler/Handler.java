package ru.dargen.fancy.packet.registry.handler;

import ru.dargen.fancy.packet.DataPacket;
import ru.dargen.fancy.server.FancyRemote;

public interface Handler<P extends DataPacket> {

    void handle(P packet, FancyRemote remote, String id);

}
