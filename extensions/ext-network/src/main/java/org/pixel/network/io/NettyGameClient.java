package org.pixel.network.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.handler.ExceptionHandler;
import org.pixel.network.handler.NetworkMessageDecoder;
import org.pixel.network.handler.NetworkMessageEncoder;
import org.pixel.network.handler.NetworkLoggerHandler;

public class NettyGameClient extends GameClient implements ChannelFutureListener {

    private static final Logger log = LoggerFactory.getLogger(NettyGameClient.class);

    private State state = State.NEW;
    private EventLoopGroup group;
    private Channel channel;

    public NettyGameClient(GameClientSettings settings) {
        super(settings);
    }

    @Override
    public boolean init() {
        if (state.isActive()) {
            log.error("Client already initialized.");
            return false;
        }

        state = State.INITIALIZING;
        group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            var bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            var p = ch.pipeline();

                            p.addLast(new NetworkMessageDecoder());
                            p.addLast(new NetworkMessageEncoder());
                            p.addLast(new NetworkLoggerHandler());

                            p.addLast(new ExceptionHandler());
                        }
                    });

            // Connect to the server
            channel = bootstrap.connect(settings.getServerAddress().getHost(), settings.getServerAddress().getPort())
                    .sync().channel();

        } catch (Exception e) {
            log.error("Failed to initialize client: {0}.", e.getMessage(), e);
            state = State.NEW;
            return false;
        }

        state = State.INITIALIZED;
        return true;
    }

    @Override
    public void update(DeltaTime delta) {
        // Nothing for now
    }

    @Override
    public void dispose() {
        if (channel != null) {
            channel.close();
            channel = null;
        }

        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }

        state = State.DISPOSED;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void write(Object msg) {
        if (channel != null) {
            channel.writeAndFlush(msg).addListener(this);
        }
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            log.error("Failed to write message: {0}.", channelFuture.cause().getMessage(), channelFuture.cause());
        }
    }
}