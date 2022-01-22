package ru.dargen.fancy.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PacketContainer {

    protected final Packet packet;
    protected final String id;
    protected int type = -1;

    public void validate() {
        if (type == -1)
            throw new IllegalStateException("Unknown packet type");
        if (packet == null)
            throw new NullPointerException("packet == null");
    }

}
