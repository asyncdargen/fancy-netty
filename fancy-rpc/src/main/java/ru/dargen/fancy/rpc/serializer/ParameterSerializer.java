package ru.dargen.fancy.rpc.serializer;

import ru.dargen.fancy.buffer.FancyBuffer;

public interface ParameterSerializer<T> {

    void serialize(T object, FancyBuffer buffer, SerializationContext ctx);

    T deserialize(FancyBuffer buffer, SerializationContext ctx);

}
