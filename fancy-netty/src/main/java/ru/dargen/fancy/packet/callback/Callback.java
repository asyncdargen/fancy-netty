package ru.dargen.fancy.packet.callback;

import ru.dargen.fancy.remote.FancyRemote;
import ru.dargen.fancy.util.FancyException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.dargen.fancy.packet.FancyPacket;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
@RequiredArgsConstructor
public class Callback<P extends FancyPacket> {

    protected final UUID id;
    protected final FancyRemote remote;
    protected final long timeout;
    protected final Runnable timeoutHandler;
    protected final long timestamp = System.currentTimeMillis();
    protected CompletableFuture<P> future;

    public boolean complete(P packet) {
        if (future != null && !future.isDone()) {
            future.complete(packet);
            return true;
        }
        return false;
    }

    public CompletableFuture<P> await() {
        return future = new CompletableFuture<>();
    }

    public P await(long time, TimeUnit unit) {
        CompletableFuture<P> handler = await();
        try {
            return handler.get(time, unit);
        } catch (InterruptedException e) {
            throw new FancyException("Exception while wait callback, id " + getId(), e);
        } catch (ExecutionException e) {
            throw new FancyException("Exception while get callback response, id " + getId(), e);
        } catch (TimeoutException e) {
            throw new FancyException("Callback is timeout, id " + getId(), e);
        }
    }

}
