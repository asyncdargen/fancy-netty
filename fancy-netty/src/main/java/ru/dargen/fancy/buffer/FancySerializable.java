package ru.dargen.fancy.buffer;

public interface FancySerializable {

    void serialize(FancyBuffer buffer);

    void deserialize(FancyBuffer buffer);

}
