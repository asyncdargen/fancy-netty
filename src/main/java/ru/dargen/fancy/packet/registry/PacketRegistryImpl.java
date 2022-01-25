package ru.dargen.fancy.packet.registry;

import ru.dargen.fancy.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketRegistryImpl implements PacketRegistry {

    protected final Map<Integer, Class<? extends Packet>> REGISTRY_ID2TYPE = new ConcurrentHashMap<>();
    protected final Map<Class<? extends Packet>, Integer> REGISTRY_TYPE2ID = new ConcurrentHashMap<>();

    public void register(int id, Class<? extends Packet> type) {
        REGISTRY_ID2TYPE.put(id, type);
        REGISTRY_TYPE2ID.put(type, id);
    }

    //for annotated packets
    public void register(Class<? extends Packet>... types) {
        for (Class<? extends Packet> type : types) {
            if (!type.isAnnotationPresent(Packet.Id.class))
                throw new IllegalArgumentException("packet class not annotated");
            register(type.getAnnotation(Packet.Id.class).id, type);
        }
    }

    public int getPacketIdFromType(Class<? extends Packet> type) {
        return REGISTRY_TYPE2ID.getOrDefault(type, -1);
    }

    public Class<? extends Packet> getPacketTypeFromId(int id) {
        return REGISTRY_ID2TYPE.get(id);
    }

}
