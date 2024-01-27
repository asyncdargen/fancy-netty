package ru.dargen.fancy.rpc.serializer.builtin;

import ru.dargen.fancy.buffer.FancyBuffer;
import ru.dargen.fancy.rpc.serializer.ParameterSerializer;
import ru.dargen.fancy.rpc.serializer.SerializationContext;

public class ArraySerializer implements ParameterSerializer<Object[]> {

    @Override
    public void serialize(Object[] array, FancyBuffer buffer, SerializationContext ctx) {
        buffer.writeArray(array, object -> ctx.serialize(object, buffer));
    }

    @Override
    public Object[] deserialize(FancyBuffer buffer, SerializationContext ctx) {
        return 
    }
}
