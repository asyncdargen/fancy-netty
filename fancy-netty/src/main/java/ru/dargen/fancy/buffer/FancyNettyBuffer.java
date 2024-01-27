package ru.dargen.fancy.buffer;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class FancyNettyBuffer extends FancyBuffer {

    private ByteBuf handle;

    public FancyNettyBuffer(ByteBuf buffer) {
        this.handle = buffer;
    }

    @Override
    public byte readByte() {
        return this.handle.readByte();
    }

    @Override
    public void writeByte(byte val) {
        this.handle.writeByte(val);
    }

    @Override
    public short readShort() {
        return this.handle.readShort();
    }

    @Override
    public void writeShort(short val) {
        this.handle.writeShort(val);
    }

    @Override
    public int readInt() {
        return this.handle.readInt();
    }

    @Override
    public void writeInt(int val) {
        this.handle.writeInt(val);
    }

    @Override
    public long readLong() {
        return this.handle.readLong();
    }

    @Override
    public void writeLong(long val) {
        this.handle.writeLong(val);
    }

    @Override
    public float readFloat() {
        return this.handle.readFloat();
    }

    @Override
    public void writeFloat(float val) {
        this.handle.writeFloat(val);
    }

    @Override
    public double readDouble() {
        return this.handle.readDouble();
    }

    @Override
    public void writeDouble(double val) {
        this.handle.writeDouble(val);
    }

    @Override
    public byte[] readBytes() {
        byte[] bytes = new byte[readVarInt()];
        handle.readBytes(bytes);
        return bytes;
    }

    @Override
    public void writeBytes(byte[] bytes) {
        writeVarInt(bytes.length);
        handle.writeBytes(bytes);
    }

}
