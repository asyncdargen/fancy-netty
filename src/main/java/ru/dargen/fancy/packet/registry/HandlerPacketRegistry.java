package ru.dargen.fancy.packet.registry;

import ru.dargen.fancy.packet.DataPacket;
import ru.dargen.fancy.packet.registry.handler.Handler;
import ru.dargen.fancy.util.FancyException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerPacketRegistry extends PacketRegistryImpl {

    protected final Map<Integer, Handler<?>> REGISTRY_ID2HANDLER = new ConcurrentHashMap<>();

    public <P extends DataPacket> void registerHandler(Class<P> type, Handler<P> handler) {
        registerHandler(getPacketIdFromType(type), handler);
    }

    public <P extends DataPacket> void registerHandler(int type, Handler<P> handler) {
        REGISTRY_ID2HANDLER.put(type, handler);
    }

    public <P extends DataPacket> Handler<P> getHandler(Class<P> type) {
        return getHandler(getPacketIdFromType(type));
    }

    @SuppressWarnings("ALL")
    public <P extends DataPacket> Handler<P> getHandler(int type) {
        Handler handler = REGISTRY_ID2HANDLER.get(type);
        if (handler == null)
            throw new FancyException("handler not finded");
        return handler;
    }

}
