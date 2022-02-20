package ru.dargen.fancy.packet.callback;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.util.FancyException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CallbackProviderImpl implements CallbackProvider {

    protected Cache<String, Callback<?>> callbackCache = Caffeine.newBuilder()
            .scheduler(Scheduler.systemScheduler())
            .removalListener(this::handleExpire)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public <P extends Packet> Callback<P> get(String id) {
        return (Callback<P>) callbackCache.getIfPresent(id);
    }

    public <P extends Packet> Callback<P> remove(String id) {
        Callback<P> callback = get(id);
        callbackCache.invalidate(id);
        closeCallback(callback);
        return callback;
    }

    public <P extends Packet> Callback<P> create(FancyRemote remote, String id) {
        Callback<P> callback = new CallbackImpl<>(remote, id);
        callbackCache.put(id, callback);
        return callback;
    }

    public boolean completeCallback(String id, Packet packet) {
        Callback<Packet> callback = get(id);
        if (callback != null) {
            try {
                callbackCache.invalidate(id);
                return callback.complete(packet);
            } catch (Throwable e) {
                throw new FancyException("Exception while complete callback, id " + callback.getId(), e);
            }
        }
        return false;
    }

    protected void handleExpire(String id, Callback<?> callback, RemovalCause cause) {
        closeCallback(callback);
    }

    protected void closeCallback(Callback<?> callback) {
        if (callback == null) return;

        CompletableFuture<?> handler = callback.getFuture();
        if (handler != null && !handler.isDone())
            handler.completeExceptionally(new FancyException("time outed callback, id " + callback.getId()));
    }
}
