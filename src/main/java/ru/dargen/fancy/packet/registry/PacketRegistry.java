package ru.dargen.fancy.packet.registry;

import ru.dargen.fancy.packet.Packet;

public interface PacketRegistry {

    int getPacketIdFromType(Class<? extends Packet> type);

    Class<? extends Packet> getPacketTypeFromId(int id);

}
