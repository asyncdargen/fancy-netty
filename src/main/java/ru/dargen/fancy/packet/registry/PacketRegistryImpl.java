package ru.dargen.fancy.packet.registry;

import com.google.common.reflect.ClassPath;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.util.FancyException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketRegistryImpl implements PacketRegistry {

    protected final Map<Integer, Class<? extends Packet>> id2Type = new ConcurrentHashMap<>();
    protected final Map<Class<? extends Packet>, Integer> type2id = new ConcurrentHashMap<>();

    public void register(int id, Class<? extends Packet> type) {
        id2Type.put(id, type);
        type2id.put(type, id);
    }

    //for annotated packets
    public void register(Class<? extends Packet>... types) {
        for (Class<? extends Packet> type : types) {
            if (!type.isAnnotationPresent(Packet.Id.class))
                throw new IllegalArgumentException("packet class not annotated");
            register(type.getAnnotation(Packet.Id.class).value(), type);
        }
    }

    public void registerFromCurrentClassLoader() {
        registerFromCurrentClassLoader(Packet.class);
    }

    public void registerFromCurrentClassLoader(Class<? extends Packet> clazz) {
        registerFromClassLoader(Thread.currentThread().getContextClassLoader(), clazz);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void registerFromClassLoader(ClassLoader loader, Class<? extends Packet> type) {
        try {
            for (ClassPath.ClassInfo info : ClassPath.from(loader).getAllClasses()) {
                try {
                    Class<?> clazz = info.load();
                    if (type.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Packet.Id.class)) {
                        register(clazz.getAnnotation(Packet.Id.class).value(), (Class<? extends Packet>) clazz);
                    }
                } catch (Throwable __) {}
            }
        } catch (Throwable e) {
            throw new FancyException("Error while search and register packets: ", e);
        }
    }

    public int getPacketIdFromType(Class<? extends Packet> type) {
        return type2id.getOrDefault(type, -1);
    }

    public Class<? extends Packet> getPacketTypeFromId(int id) {
        return id2Type.get(id);
    }

}
