package ru.dargen.fancy.packet.registry;

import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.remote.FancyRemote;

@FunctionalInterface
public interface FancyPacketHandler<P extends FancyPacket> {

    void handle(FancyRemote remote, P packet);

}
