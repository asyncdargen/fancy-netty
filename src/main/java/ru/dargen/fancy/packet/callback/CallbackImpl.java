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
    protected CompletableFuture<P> future;

    public boolean complete(P packet) {
        if (future != null && !future.isDone()) {
            future.complete(packet);
            return true;
        }
        return false;
    }

    public <T extends Packet> Callback<T> respond(Packet packet) {
        return remote.write(packet, id);
    }

    public CompletableFuture<P> await() {
        return future = new CompletableFuture<>();
    }

}
