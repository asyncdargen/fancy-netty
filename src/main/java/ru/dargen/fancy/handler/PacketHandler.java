package ru.dargen.fancy.handler;

import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.packet.Packet;

public interface PacketHandler {

    boolean on(FancyRemote remote, Packet packet);

}
