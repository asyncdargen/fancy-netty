package ru.dargen.fancy.packet.registry;


import ru.dargen.fancy.util.UnsafeUtil;
import ru.dargen.fancy.util.registry.ReversedHashRegistry;
import ru.dargen.fancy.util.registry.ReversedRegistry;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.builtin.FancyKeepAlivePacket;
import ru.dargen.fancy.remote.FancyRemote;

import java.util.*;
import java.util.function.Supplier;

public class FancyPacketRegistry {

    protected final ReversedRegistry<Integer, Class<? extends FancyPacket>> packets = new ReversedHashRegistry<>();
    protected final Map<Integer, Supplier<FancyPacket>> constructors = new HashMap<>();
    protected final Map<Integer, List<FancyPacketHandler<?>>> handlers = new HashMap<>();

    public FancyPacketRegistry() {
        registerPacket(FancyKeepAlivePacket.class);
    }

    @SuppressWarnings("unchecked")
    public <P extends FancyPacket> void registerPacket(int packetId, Class<P> packetClass, Supplier<P> constructor, FancyPacketHandler<P> handler) {
//        if (packetId < 0) throw new IllegalArgumentException("Packet id must be positive");

        if (getPacketClass(packetId) != null && packetClass.getName().contains("ru.bulk.fancy.packet.builtin"))
            return;

        packets.put(packetId, packetClass);
        constructors.put(packetId, (Supplier<FancyPacket>) (constructor == null ? UnsafeUtil.getAllocator(packetClass) : constructor));
        if (handler != null) registerHandler(packetId, handler);
    }

    public <P extends FancyPacket> void registerPacket(int id, Class<P> packetClass, FancyPacketHandler<P> handler) {
        registerPacket(id, packetClass, null, handler);
    }

    public <P extends FancyPacket> void registerPacket(int id, Class<P> packetClass, Supplier<P> constructor) {
        registerPacket(id, packetClass, constructor, null);
    }

    public <P extends FancyPacket> void registerPacket(int id, Class<P> packetClass) {
        registerPacket(id, packetClass, null, null);
    }

    public <P extends FancyPacket> void registerPacket(Class<P> packetClass, Supplier<P> constructor, FancyPacketHandler<P> handler) {
        registerPacket(packetClass.getAnnotation(FancyId.class).value(), packetClass, constructor, handler);
    }

    public <P extends FancyPacket> void registerPacket(Class<P> packetClass, Supplier<P> constructor) {
        registerPacket(packetClass, constructor, null);
    }

    public <P extends FancyPacket> void registerPacket(Class<P> packetClass, FancyPacketHandler<P> handler) {
        registerPacket(packetClass, null, handler);
    }

    public <P extends FancyPacket> void registerPacket(Class<P> packetClass) {
        registerPacket(packetClass, null, null);
    }

    public <P extends FancyPacket> void registerHandler(Class<P> packetClass, FancyPacketHandler<P> handler) {
        registerHandler(getPacketId(packetClass), handler);
    }

    public <P extends FancyPacket> void registerHandler(int packetId, FancyPacketHandler<P> handler) {
        handlers.computeIfAbsent(packetId, __ -> new ArrayList<>()).add(handler);
    }

    public void registerPackets(Class<? extends FancyPacket>... packetClasses) {
        for (Class<? extends FancyPacket> packetClass : packetClasses)
            registerPacket(packetClass);
    }

    public Class<? extends FancyPacket> getPacketClass(int id) {
        return packets.getValue(id);
    }

    public int getPacketId(Class<? extends FancyPacket> packetClass) {
        return packets.getKey(packetClass);
    }

    public int getPacketId(FancyPacket packet) {
        return getPacketId(packet.getClass());
    }

    @SuppressWarnings("unchecked")
    public <P extends FancyPacket> List<FancyPacketHandler<P>> getPacketHandlers(int packetId) {
        return ((List<FancyPacketHandler<P>>) ((Object) handlers.getOrDefault(packetId, Collections.emptyList())));
    }

    public <P extends FancyPacket> List<FancyPacketHandler<P>> getPacketHandlers(Class<P> packetClass) {
        return getPacketHandlers(getPacketId(packetClass));
    }

    public <P extends FancyPacket> List<FancyPacketHandler<P>> getPacketHandlers(P packet) {
        return getPacketHandlers(getPacketId(packet));
    }

    public void firePacketHandlers(FancyPacket packet, FancyRemote remote) {
        getPacketHandlers(packet).forEach(handler -> handler.handle(remote, packet));
    }

    public boolean hasPacketHandlers(int packetId) {
        return !getPacketHandlers(packetId).isEmpty();
    }

    public boolean hasPacketHandlers(Class<? extends FancyPacket> packetClass) {
        return hasPacketHandlers(getPacketId(packetClass));
    }

    public FancyPacket constructPacket(int id) {
        Supplier<FancyPacket> constructor = this.constructors.get(id);
        if (constructor == null)
            return null;
        return constructor.get();
    }

}
