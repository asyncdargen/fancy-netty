package ru.dargen.fancy.handler.context;

import lombok.Data;
import ru.dargen.fancy.server.FancyRemote;

@Data
public class RemoteDisconnectHandlerContext extends HandlerContext {

    private final FancyRemote remote;

    @Override
    public void setCancelled(boolean cancelled) {
        throw new UnsupportedOperationException("Not supported on disconnect handler context");
    }

}
