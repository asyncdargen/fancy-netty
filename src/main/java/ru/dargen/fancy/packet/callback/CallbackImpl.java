package ru.dargen.fancy.packet.callback;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.server.FancyRemote;

import java.util.concurrent.CompletableFuture;

@Data
@RequiredArgsConstructor
public class CallbackImpl<P extends Packet> implements Callback<P> {

    protected final FancyRemote remote;
    protected final String id;
    protected final CompletableFuture<P> future = new CompletableFuture<>();

    public void complete(P packet) {
        if (!future.isDone())
            future.complete(packet);
    }

    public <T extends Packet> Callback<T> respond(Packet packet) {
        return remote.write(packet, id);
    }

    public CompletableFuture<P> await() {
        return future;
    }

}
