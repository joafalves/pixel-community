package org.pixel.network.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.handler.*;

public class NettyGameServer extends GameServer {

    private static final Logger log = LoggerFactory.getLogger(NettyGameServer.class);

    private final Timer statsTimer;

    private State state = State.NEW;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    public NettyGameServer(GameServerSettings settings) {
        super(settings);
        this.statsTimer = new Timer(settings.getStatsLogYieldSeconds() * 1000L);
    }

    @Override
    public boolean init() {
        if (state.isActive()) {
            log.error("Server already initialized.");
            return false;
        }

        state = State.INITIALIZING;

        bossGroup = new MultiThreadIoEventLoopGroup(settings.getNumThreads(), NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            // TODO: support SSL
            var bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            var p = socketChannel.pipeline();

                            p.addLast(new ConnectionHandler(settings.getMaxConnections()));
                            p.addLast(new NetworkMessageDecoder());
                            p.addLast(new NetworkMessageEncoder());
                            p.addLast(new NetworkMessageLogger());

                            p.addLast(new ExceptionHandler());
                        }
                    });

            // bind the server and accept incoming connections:
            bootstrap.bind(settings.getBindAddress().getHost(), settings.getBindAddress().getPort()).sync();

        } catch (Exception e) {
            log.error("Failed to initialize server: {0}.", e.getMessage(), e);
            state = State.NEW;
            return false;
        }

        state = State.INITIALIZED;

        return true;
    }

    @Override
    public void update(DeltaTime delta) {
        if (!state.isActive()) {
            return;
        }

        if (statsTimer.check(delta)) {
            logStats();
        }
    }

    @Override
    public void dispose() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }

        state = State.DISPOSED;
    }

    @Override
    public State getState() {
        return state;
    }

    private void logStats() {
        log.info("Active connections: {0}.", 0);
    }
}
