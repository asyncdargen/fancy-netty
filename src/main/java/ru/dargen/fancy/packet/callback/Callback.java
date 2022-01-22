package ru.dargen.fancy.packet.callback;

import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.util.FancyException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Callback<P extends Packet> {

    String getId();

    FancyRemote getRemote();

    CompletableFuture<P> getFuture();

    void complete(P packet);

    <T extends Packet> Callback<T> respond(Packet packet);

    CompletableFuture<P> await();

    default P await(long time, TimeUnit unit) {
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
