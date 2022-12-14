package ru.dargen.fancy.spring.server.config;

import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dargen.fancy.Fancy;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.registry.HandlerPacketRegistry;
import ru.dargen.fancy.server.FancyServer;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@SuppressWarnings("UnstableApiUsage")
public class FancyServerConfig {

    @Autowired
    private ApplicationContext context;

    @Value("${fancy.server.port:8080}")
    private int serverPort;

    @Value("${fancy.packets.package:#{null}}")
    private String packetsPackage;

    @SneakyThrows
    public List<Class<? extends Packet>> getClassPathPackets() {
        return ClassPath.from(FancyServerConfig.class.getClassLoader())
                .getAllClasses()
                .stream()
                .filter(info -> info.getName().startsWith(packetsPackage))
                .map(info -> ((Class<? extends Packet>) info.load()))
                .filter(clazz -> clazz.isAnnotationPresent(Packet.Id.class))
                .collect(Collectors.toList());
    }

    @Bean
    public HandlerPacketRegistry packetRegistry() {
        return Fancy.createHandlerPacketRegistry();
    }

    @Bean
    public Handlers handlers() {
        return Fancy.createDefaultHandlers();
    }

    @Bean
    public FancyServer server() {
        val server = Fancy.createServer(
                packetRegistry(),
                handlers(),
                context.getBean(Gson.class)
        );
        if (packetsPackage != null) packetRegistry().register(getClassPathPackets().toArray(new Class[0]));
        server.bind(serverPort);
        return server;
    }

    @Bean
    public FancyPostBeanProcessor fancyBeanPostProcessor() {
        return new FancyPostBeanProcessor(packetRegistry(), handlers());
    }

}
