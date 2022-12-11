package ru.dargen.fancy.handler.context;

import lombok.Data;
import ru.dargen.fancy.server.FancyRemote;

@Data
public class RemoteConnectHandlerContext extends HandlerContext {

    private final FancyRemote remote;

}
