package ru.dargen.fancy.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.dargen.fancy.buffer.FancyNettyBuffer;
import ru.dargen.fancy.packet.FancyPacket;
import ru.dargen.fancy.remote.FancyRemote;
import ru.dargen.fancy.util.NettyUtil;

import java.util.List;

@RequiredArgsConstructor
public class FancyCodec extends ByteToMessageCodec<FancyPacket> {

    private final FancyRemote remote;

    @Override
    protected void encode(ChannelHandlerContext ctx, FancyPacket packet, ByteBuf out) throws Exception {
        val packetBuf = ctx.alloc().buffer();
        val buffer = new FancyNettyBuffer(packetBuf);

        buffer.writeSignedVarInt(remote.getPacketRegistry().getPacketId(packet));
        buffer.writeUUID(packet.getUniqueId());
        packet.write(buffer);

        val start = out.writerIndex();

        buffer.setHandle(out);
        buffer.writeVarInt(packetBuf.readableBytes());
        out.writeBytes(packetBuf);

        packetBuf.release();

        remote.getMetrics().incrementOutPackets(out.writerIndex() - start);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int readerIndex = buf.readerIndex();

        int length = 0;
        int lengthOfLength = 0;

        validator: {
            byte in;
            do {

                if (buf.readableBytes() == 0) {
                    buf.readerIndex(readerIndex);
                    return;
                }

                in = buf.readByte();

                length |= (in & 0x7F) << (lengthOfLength++ * 7);

                if (lengthOfLength > 5)
                    throw new DecoderException("Wrong packet length");

            } while ((in & 0x80) == 0x80);

            if (length < 0)
                throw new DecoderException("Packet length must be >= 0, received " + length);

            if (length > NettyUtil.MAX_PACKET_LENGTH)
                throw new DecoderException("Maximum allowed packet length is " + NettyUtil.MAX_PACKET_LENGTH + ", received " + length);

            if (buf.readableBytes() < length) {
                buf.readerIndex(readerIndex);
                return;
            }
        }

        val buffer = new FancyNettyBuffer(buf);
        readerIndex = buf.readerIndex();

        val packetId = buffer.readSignedVarInt();
        val uniqueId = buffer.readUUID();

        val packet = remote.getPacketRegistry().constructPacket(packetId);
        if (packet == null) {
            buf.skipBytes(length);
            throw new DecoderException("Unknown packet ID " + packetId + ", size " + length);
        } else packet.setUniqueId(uniqueId);

        try {
            packet.read(buffer);

            if (buf.readerIndex() - readerIndex != length) {
                int diff = length - (buf.readerIndex() - readerIndex);
                remote.getLogger().warning(
                        "After reading packet " + packet.getClass().getSimpleName() + ", there are " +
                                (diff > 0 ? diff + " bytes left" : -diff + " extra bytes read") +
                                " (length " + length + "). Packet ignored."
                );
                buf.readerIndex(readerIndex + length);
                return;
            }
        } catch (Exception ex) {
            throw new DecoderException("Decoding packet " + packet.getClass().getSimpleName() + ", size " + length, ex);
        }

        out.add(packet);

        remote.getMetrics().incrementInPackets(length + lengthOfLength);
    }

}
