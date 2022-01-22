package ru.dargen.fancy.server;

import ru.dargen.fancy.FancyConnected;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.callback.Callback;
import ru.dargen.fancy.packet.callback.CallbackProvider;

import java.net.SocketAddress;

public interface FancyRemote extends FancyConnected {

    <P extends Packet> Callback<P> write(Packet packet);

    <P extends Packet> Callback<P> write(Packet packet, String id);

    SocketAddress getAddress();

    CallbackProvider getCallbackProvider();

}
