package ru.dargen.fancy.client;

import io.netty.channel.ChannelFuture;
import ru.dargen.fancy.server.FancyRemote;

public interface FancyClient extends FancyRemote {

    ChannelFuture connect(String host, int port);

    default ChannelFuture connectLocal(int port) {
        return connect("127.0.0.1", port);
    }

    int getPort();

    String getHost();

    void reconnect();

    boolean isAutoReconnect();

    void setAutoReconnect(boolean set);

    void await();

}
