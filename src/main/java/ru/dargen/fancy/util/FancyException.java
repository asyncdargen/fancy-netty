package ru.dargen.fancy.util;

public class FancyException extends RuntimeException {

    public FancyException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FancyException(String message) {
        super(message);
    }

    public static void call(String message) {
        throw new FancyException(message);
    }

    public static void call(String message, Throwable throwable) {
        throw new FancyException(message, throwable);
    }



}
