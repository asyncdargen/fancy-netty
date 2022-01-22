package ru.dargen.fancy.handler;

import ru.dargen.fancy.server.FancyRemote;

public interface RemoteConnectHandler {

    boolean on(FancyRemote remote);

}
