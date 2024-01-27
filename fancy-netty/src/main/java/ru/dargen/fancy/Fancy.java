package ru.dargen.fancy;

import lombok.experimental.UtilityClass;
import ru.dargen.fancy.client.FancyClient;
import ru.dargen.fancy.client.FancyNettyClient;
import ru.dargen.fancy.handler.FancyRemoteHandler;
import ru.dargen.fancy.packet.registry.FancyPacketRegistry;
import ru.dargen.fancy.server.FancyNettyServer;
import ru.dargen.fancy.server.FancyServer;
import ru.dargen.fancy.util.NettyUtil;

import java.util.logging.Logger;

@UtilityClass
public class Fancy {

    public FancyClient createClient(FancyPacketRegistry packetRegistry) {
        return createClient(packetRegistry, new FancyRemoteHandler());
    }

    public FancyClient createClient(FancyPacketRegistry packetRegistry, FancyRemoteHandler handlers) {
        return createClient(packetRegistry, handlers, NettyUtil.LOGGER);
    }

    public FancyClient createClient(FancyPacketRegistry packetRegistry, FancyRemoteHandler handlers, Logger logger) {
        return new FancyNettyClient(logger, packetRegistry, handlers);
    }

    //Server

    public FancyServer createServer(FancyPacketRegistry packetRegistry) {
        return createServer(packetRegistry, new FancyRemoteHandler());
    }

    public FancyServer createServer(FancyPacketRegistry packetRegistry, FancyRemoteHandler handlers) {
        return createServer(packetRegistry, handlers, NettyUtil.LOGGER);
    }

    public FancyServer createServer(FancyPacketRegistry packetRegistry, FancyRemoteHandler handlers, Logger logger) {
        return new FancyNettyServer(logger, packetRegistry, handlers);
    }

}
