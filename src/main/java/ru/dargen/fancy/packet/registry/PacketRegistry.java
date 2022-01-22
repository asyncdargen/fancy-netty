package ru.dargen.fancy.packet.registry;

import io.netty.buffer.ByteBuf;
import ru.dargen.fancy.packet.Packet;

public interface PacketRegistry {

    int getPacketIdFromType(Class<? extends Packet> type);

    Class<? extends Packet> getPacketTypeFromId(int id);

}
