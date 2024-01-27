package ru.dargen.fancy.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import sun.misc.Unsafe;

import java.util.function.Supplier;

@UtilityClass
public class UnsafeUtil {

    public final Unsafe UNSAFE = findUnsafe();

    public boolean isSupported() {
        return UNSAFE != null;
    }

    @SneakyThrows
    public <T> T allocateInstance(Class<T> declaredClass) {
        return (T) UNSAFE.allocateInstance(declaredClass);
    }

    public <T> Supplier<T> getAllocator(Class<T> declaredClass) {
        return isSupported() ? () -> {
            try {
                return allocateInstance(declaredClass);
            } catch (Throwable e) {
                return null;
            }
        } : null;
    }

    @SneakyThrows
    private static Unsafe findUnsafe() {
        try {
            val unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Throwable throwable) {
            System.err.println("Unsafe not supported");
            return null;
        }
    }

}
