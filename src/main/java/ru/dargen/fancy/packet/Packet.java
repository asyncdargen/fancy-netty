package ru.dargen.fancy.packet;

import ru.dargen.fancy.server.FancyRemote;

public interface Packet {

    void handle(FancyRemote remote, String id);

}
