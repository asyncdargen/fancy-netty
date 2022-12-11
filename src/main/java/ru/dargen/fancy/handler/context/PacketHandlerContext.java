package ru.dargen.fancy.handler.context;

import lombok.Data;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.server.FancyRemote;

@Data
public class PacketHandlerContext extends HandlerContext {

    private final FancyRemote remote;
    private final Packet packet;

}
