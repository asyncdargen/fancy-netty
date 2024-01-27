package ru.dargen.fancy.codec;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.dargen.fancy.FancyBase;
import ru.dargen.fancy.remote.FancyRemote;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class FancyChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static int IP_TOS = 0b0011111;
    public static boolean TCP_NO_DELAY = true;

    public static ByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;


    protected final FancyBase base;
    protected final Consumer<FancyRemote> activeHandler;
    protected final Consumer<FancyRemote> inactiveHandler;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        val config = channel.config();
        config.setAllocator(ALLOCATOR);
        config.setOption(ChannelOption.TCP_NODELAY, TCP_NO_DELAY);
        config.setOption(ChannelOption.SO_KEEPALIVE, true);
        config.setOption(ChannelOption.IP_TOS, IP_TOS);

        channel.attr(FancyRemote.ATTRIBUTE_PARENT_KEY).set(base);

        val pipeline = channel.pipeline();
        pipeline.addLast("handler", new FancyHandler(activeHandler, inactiveHandler));
    }

}
