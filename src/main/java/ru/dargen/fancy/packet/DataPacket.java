package ru.dargen.fancy.packet;

import ru.dargen.fancy.packet.registry.HandlerPacketRegistry;
import ru.dargen.fancy.packet.registry.PacketRegistry;
import ru.dargen.fancy.packet.registry.handler.Handler;
import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.util.FancyException;

@SuppressWarnings("ALL")
public interface DataPacket extends Packet {

    default void handle(FancyRemote remote, String id) {
        PacketRegistry registry = remote.getPacketRegistry();
        if (!(registry instanceof HandlerPacketRegistry))
            throw new FancyException("client mast use " + HandlerPacketRegistry.class.getName());

        HandlerPacketRegistry handlerPacketRegistry = (HandlerPacketRegistry) registry;
        Handler handler = handlerPacketRegistry.getHandler(getClass());
        handler.handle(this, remote, id);
    }

}
