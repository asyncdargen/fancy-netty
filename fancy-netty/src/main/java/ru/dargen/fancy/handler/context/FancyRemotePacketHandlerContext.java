package ru.dargen.fancy.handler.context;

import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.remote.FancyRemote;
import lombok.Data;

@Data
public class FancyRemotePacketHandlerContext extends FancyRemoteHandlerContext {

    private final FancyRemote remote;
    private final Callback<?> callback;
    private final FancyPacket packet;

}
