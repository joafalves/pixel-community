package org.pixel.network.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.data.NetworkSession;
import org.pixel.network.handler.netty.*;
import org.pixel.network.handler.netty.server.ConnectionHandler;
import org.pixel.network.handler.netty.server.HandshakeRequestHandler;
import org.pixel.network.handler.netty.server.InboundSecurityHandler;
import org.pixel.network.io.GameServer;
import org.pixel.network.io.GameServerSettings;
import org.pixel.network.security.AuthType;

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
            log.warn("Server already initialized.");
            return false;
        }

        if (authenticator == null && !settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.warn("Authenticator is not set.");
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
                            p.addLast(new NetworkLoggerHandler());

                            // Handshake is *special* and HAS TO be added before the security handler:
                            p.addLast(new HandshakeRequestHandler(settings, authenticator));
                            p.addLast(new InboundSecurityHandler());

                            p.addLast(new ExceptionHandler());

                            // GameServer clean-up handler:
                            p.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    NetworkSession session = NettyUtils.getSession(ctx.channel());
                                    if (session != null) {
                                        log.debug("Session {0} inactive.", session.getId());
                                    } else {
                                        log.debug("Session inactive from {0}.", ctx.channel().remoteAddress());
                                    }
                                    // Called when the channel becomes inactive...
                                    ctx.channel().attr(NettyUtils.SESSION).set(null);
                                    super.channelInactive(ctx);
                                }
                            });
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
