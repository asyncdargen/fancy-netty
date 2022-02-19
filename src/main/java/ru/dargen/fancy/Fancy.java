package ru.dargen.fancy;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import ru.dargen.fancy.client.FancyClient;
import ru.dargen.fancy.client.FancyClientImpl;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.handler.HandlersImpl;
import ru.dargen.fancy.packet.registry.HandlerPacketRegistry;
import ru.dargen.fancy.packet.registry.PacketRegistry;
import ru.dargen.fancy.packet.registry.PacketRegistryImpl;
import ru.dargen.fancy.server.FancyServer;
import ru.dargen.fancy.server.FancyServerImpl;
import ru.dargen.fancy.util.NettyUtil;

import java.util.logging.Logger;

@UtilityClass
public class Fancy {

    //Client

    public FancyClient createClient(PacketRegistry packetRegistry) {
        return createClient(packetRegistry, createDefaultHandlers(), new Gson());
    }

    public FancyClient createClient(PacketRegistry packetRegistry, Gson gson) {
        return createClient(packetRegistry, createDefaultHandlers(), gson);
    }

    public FancyClient createClient(PacketRegistry packetRegistry, Handlers handlers, Gson gson) {
        return createClient(packetRegistry, handlers, gson, NettyUtil.LOGGER);
    }

    public FancyClient createClient(PacketRegistry packetRegistry, Handlers handlers, Gson gson, Logger logger) {
        return new FancyClientImpl(logger, gson, packetRegistry, handlers);
    }

    //Server

    public FancyServer createServer(PacketRegistry packetRegistry) {
        return createServer(packetRegistry, createDefaultHandlers(), new Gson());
    }

    public FancyServer createServer(PacketRegistry packetRegistry, Gson gson) {
        return createServer(packetRegistry, createDefaultHandlers(), gson);
    }

    public FancyServer createServer(PacketRegistry packetRegistry, Handlers handlers, Gson gson) {
        return createServer(packetRegistry, handlers, gson, NettyUtil.LOGGER);
    }

    public FancyServer createServer(PacketRegistry packetRegistry, Handlers handlers, Gson gson, Logger logger) {
        return new FancyServerImpl(logger, gson, packetRegistry, handlers);
    }

    //Utilities
    public Handlers createDefaultHandlers() {
        return new HandlersImpl();
    }

    public PacketRegistryImpl createDefaultPacketRegistry() {
        return new PacketRegistryImpl();
    }

    public HandlerPacketRegistry createHandlerPacketRegistry() {
        return new HandlerPacketRegistry();
    }

}
