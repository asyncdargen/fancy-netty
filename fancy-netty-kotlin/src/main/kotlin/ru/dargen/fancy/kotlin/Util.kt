package ru.dargen.fancy.kotlin

import ru.dargen.fancy.FancyConnected
import ru.dargen.fancy.handler.context.PacketHandlerContext
import ru.dargen.fancy.handler.context.RemoteConnectHandlerContext
import ru.dargen.fancy.handler.context.RemoteDisconnectHandlerContext
import ru.dargen.fancy.packet.DataPacket
import ru.dargen.fancy.packet.Packet
import ru.dargen.fancy.packet.registry.HandlerPacketRegistry
import ru.dargen.fancy.packet.registry.PacketRegistryImpl
import ru.dargen.fancy.server.FancyRemote
import ru.dargen.fancy.util.FancyException

inline fun <reified P : DataPacket> FancyConnected.registerHandler(
    noinline handler: P.(FancyRemote, String) -> Unit
) {
    if (packetRegistry !is HandlerPacketRegistry)
        throw FancyException("packet registry not " + HandlerPacketRegistry::class.simpleName)
    (packetRegistry as HandlerPacketRegistry).registerHandler(P::class.java, handler)
}

inline fun <reified P : DataPacket> HandlerPacketRegistry.registerHandler(
    noinline handler: P.(FancyRemote, String) -> Unit
) = this.registerHandler(P::class.java, handler)

inline fun <reified P : DataPacket> FancyConnected.registerResponseHandler(noinline handler: P.(FancyRemote, String) -> Packet?) {
    registerHandler<P> { remote, id -> handler(this, remote, id)?.let { remote.write<Packet>(it, id) } }
}

inline fun <reified P : DataPacket> HandlerPacketRegistry.registerResponseHandler(noinline handler: P.(FancyRemote, String) -> Packet?) {
    registerHandler<P> { remote, id -> handler(this, remote, id)?.let { remote.write<Packet>(it, id) } }
}


inline fun <reified P : Packet> PacketRegistryImpl.register(id: Int) = this.register(id, P::class.java)

fun FancyConnected.onConnect(handler: RemoteConnectHandlerContext.() -> Unit) = this.handlers.onConnect(handler)

fun FancyConnected.onDisconnect(handler: RemoteDisconnectHandlerContext.() -> Unit) = this.handlers.onDisconnect(handler)

fun FancyConnected.onOutPacket(handler: PacketHandlerContext.() -> Unit) = this.handlers.onOutPacket(handler)

fun FancyConnected.onInPacket(handler: PacketHandlerContext.() -> Unit) = this.handlers.onInPacket(handler)

fun <P : Packet> FancyRemote.writeAwait(packet: Packet, handler: P.() -> Unit) = this.write<P>(packet).await().thenAccept(handler)

fun <P : Packet> FancyRemote.writeAwait(packet: Packet, id: String, handler: P.() -> Unit) =
    this.write<P>(packet, id).await().thenAccept(handler)

fun FancyRemote.write(packet: Packet) { this.write<Packet>(packet) }

fun FancyRemote.write(packet: Packet, id: String) { this.write<Packet>(packet, id) }