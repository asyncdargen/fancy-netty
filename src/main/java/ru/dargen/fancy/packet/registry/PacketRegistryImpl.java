package ru.dargen.fancy.packet.registry;

import com.google.common.reflect.ClassPath;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.util.FancyException;

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
            register(type.getAnnotation(Packet.Id.class).value(), type);
        }
    }

    public void registerFromCurrentJar() {
        registerFromCurrentJar(Packet.class);
    }

    public void registerFromCurrentJar(Class<? extends Packet> clazz) {
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
//            //TODO: Maybe caught exception in javac
//            File currentJarFile = new File(PacketRegistryImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//            System.out.println(currentJarFile);
//            JarFile jar = new JarFile(currentJarFile);
//            Enumeration<JarEntry> enumeration = jar.entries();
//            JarEntry entry;
//            while (enumeration.hasMoreElements() && (entry = enumeration.nextElement()) != null) {
//                if (!entry.getName().endsWith(".class"))
//                    continue;
//
//                String path = entry.getName()
//                        .replace("/", ".")
//                        .replace(".class", "");
//                try {
//                    Class<?> clazz = loader.loadClass(path);
//                    if (Packet.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Packet.Id.class)) {
//                        register(clazz.getAnnotation(Packet.Id.class).value(), (Class<? extends Packet>) clazz);
//                    }
//                } catch (Throwable e) {}
//            }
        } catch (Throwable e) {
            throw new FancyException("Error while search and register packets: " + e);
        }
    }

    public int getPacketIdFromType(Class<? extends Packet> type) {
        return REGISTRY_TYPE2ID.getOrDefault(type, -1);
    }

    public Class<? extends Packet> getPacketTypeFromId(int id) {
        return REGISTRY_ID2TYPE.get(id);
    }

}
