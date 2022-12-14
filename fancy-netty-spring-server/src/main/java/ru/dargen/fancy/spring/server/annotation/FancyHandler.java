package ru.dargen.fancy.spring.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FancyHandler {

    HandlerType value() default HandlerType.PACKET;

    static enum HandlerType {

        PACKET, CONNECT, DISCONNECT, PRE_OUT_PACKET, PRE_IN_PACKET

    }

}
