package ru.dargen.fancy.handler.context;

import ru.dargen.fancy.remote.FancyRemote;
import lombok.Data;

@Data
public class FancyRemoteConnectHandlerContext extends FancyRemoteHandlerContext {

    private final FancyRemote remote;

}
