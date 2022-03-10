package ru.dargen.fancy.packet;

import ru.dargen.fancy.server.FancyRemote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Packet {

    default void handle(FancyRemote remote, String id) {}

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Id {

        int value();

    }
}
