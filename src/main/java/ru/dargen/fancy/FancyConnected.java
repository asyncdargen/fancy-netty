package ru.dargen.fancy;

import com.google.gson.Gson;
import io.netty.channel.EventLoopGroup;
import ru.dargen.fancy.handler.Handlers;
import ru.dargen.fancy.metrics.Metrics;
import ru.dargen.fancy.packet.registry.PacketRegistry;

import java.util.logging.Logger;

public interface FancyConnected {

    Metrics getMetrics();

    Logger getLogger();

    Gson getGson();

    EventLoopGroup getEventLoop();

    PacketRegistry getPacketRegistry();

    Handlers getHandlers();

    boolean isActive();

    void close();

}
