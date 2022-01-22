package ru.dargen.fancy.server;

import io.netty.channel.ChannelFuture;
import ru.dargen.fancy.FancyConnected;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;

import java.util.List;
import java.util.Set;

public interface FancyServer extends FancyConnected {

    Set<FancyRemote> getClients();

    ChannelFuture bind(int port);

    void broadcast(Packet packet);

    <P extends Packet> List<Callback<P>> broadcastCallback(Packet packet);

}
