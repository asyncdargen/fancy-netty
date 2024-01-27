package ru.dargen.fancy.packet;

import ru.dargen.fancy.buffer.FancyBuffer;

public abstract class EmptyPacket extends FancyPacket {

    @Override
    public void write(FancyBuffer buffer) {

    }

    @Override
    public void read(FancyBuffer buffer) {

    }

}
