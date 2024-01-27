package ru.dargen.fancy.handler.context;

import ru.dargen.fancy.remote.FancyRemote;
import lombok.Data;

@Data
public class FancyRemoteDisconnectHandlerContext extends FancyRemoteHandlerContext {

    private final FancyRemote remote;

    @Override
    public void setCancelled(boolean cancelled) {
        throw new UnsupportedOperationException("Not supported on disconnect handler context");
    }

}
