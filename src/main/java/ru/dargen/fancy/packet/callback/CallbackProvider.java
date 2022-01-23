package ru.dargen.fancy.packet.callback;

import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.packet.Packet;

import java.util.UUID;

public interface CallbackProvider {

    <P extends Packet> Callback<P> get(String id);

    <P extends Packet> Callback<P> remove(String id);

    <P extends Packet> Callback<P> create(FancyRemote remote, String id);

    default <P extends Packet> Callback<P> create(FancyRemote remote) {
        return create(
                remote, UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 16)
        );
    }

    boolean completeCallback(String id, Packet packet);

}
