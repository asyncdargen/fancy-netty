package ru.dargen.fancy.rpc.serializer;

import ru.dargen.fancy.buffer.FancyBuffer;

public interface SerializationContext {

    void serialize(Object object, FancyBuffer buffer);

    Object deserialize(FancyBuffer buffer);

}
