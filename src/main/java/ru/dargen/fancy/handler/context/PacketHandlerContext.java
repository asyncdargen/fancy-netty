package ru.dargen.fancy.handler.context;

import lombok.Data;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.server.FancyRemote;

@Data
public class PacketHandlerContext extends HandlerContext {

    private final FancyRemote remote;
    private final Callback<?> callback;
    private final Packet packet;

}
