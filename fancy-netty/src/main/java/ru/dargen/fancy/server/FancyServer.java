package ru.dargen.fancy.server;

import ru.dargen.fancy.FancyBase;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.remote.FancyRemote;
import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.Set;

public interface FancyServer extends FancyBase {

    Set<FancyRemote> getRemotes();

    ChannelFuture bind(int port);

    void broadcast(FancyPacket packet);

    <P extends FancyPacket> List<Callback<P>> broadcastCallback(FancyPacket packet);

}
