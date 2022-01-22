package ru.dargen.fancy.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@UtilityClass
public class AccessUtil {

    public Unsafe UNSAFE = getUnsafe();

    @SneakyThrows
    private Unsafe getUnsafe() {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    @SneakyThrows
    public <T> T allocateInstance(Class<T> declaredClass) {
        return (T) UNSAFE.allocateInstance(declaredClass);
    }

}
