package ru.dargen.fancy.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;
import java.util.logging.Logger;

@UtilityClass
public class NettyUtil {

    public boolean EPOLL = Epoll.isAvailable();

    public Class<? extends SocketChannel> CLIENT_CHANNEL = EPOLL
            ? EpollSocketChannel.class
            : NioSocketChannel.class;

    public Class<? extends ServerSocketChannel> SERVER_CHANNEL = EPOLL
            ? EpollServerSocketChannel.class
            : NioServerSocketChannel.class;

    public Supplier<EventLoopGroup> EVENT_LOOP = () -> EPOLL
            ? new EpollEventLoopGroup(4)
            : new NioEventLoopGroup(4);

    public Logger LOGGER = Logger.getLogger("Fancy");

    static {
        System.out.println("Using " + (EPOLL ? "Epoll" : "Nio"));
    }

}
