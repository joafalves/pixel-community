package org.pixel.network.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.data.NetworkSession;
import org.pixel.network.handler.netty.*;
import org.pixel.network.handler.netty.server.ConnectionHandler;
import org.pixel.network.handler.netty.server.HandshakeRequestHandler;
import org.pixel.network.handler.netty.server.InboundSecurityHandler;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.security.AuthType;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyNetworkServer extends NetworkServer {

    private static final Logger log = LoggerFactory.getLogger(NettyNetworkServer.class);

    private final AtomicInteger connectionCount = new AtomicInteger(0);

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    public NettyNetworkServer(NetworkServerSettings settings) {
        super(settings);
    }

    @Override
    public boolean init() {
        if (isActive()) {
            log.warn("Server already active, dispose before re-initializing.");
            return false;
        }

        if (authenticator == null && !settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.warn("Authenticator is not set!");
            return false;
        }

        bossGroup = new MultiThreadIoEventLoopGroup(settings.getNumThreads(), NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        connectionCount.set(0);

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
                            p.addLast(new IdleStateHandler(settings.getMaxIdleTimeSeconds(), 0, 0));

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

                                @Override
                                public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                    connectionCount.incrementAndGet();
                                    super.channelRegistered(ctx);
                                }

                                @Override
                                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                    connectionCount.decrementAndGet();
                                    super.channelUnregistered(ctx);
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent e) {
                                        if (e.state() == IdleState.READER_IDLE) {
                                            NetworkSession session = NettyUtils.getSession(ctx.channel());
                                            if (session != null) {
                                                log.info("Closing idle session {0}", session.getId());
                                            }
                                            ctx.close(); // or custom session purge logic
                                        }
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }
                            });
                        }
                    });

            // bind the server and accept incoming connections:
            var future = bootstrap.bind(settings.getBindAddress().getHost(), settings.getBindAddress().getPort()).sync();
            channel = future.channel();

            log.info("Server started on {0}:{1}.",
                    settings.getBindAddress().getHost(), settings.getBindAddress().getPort());

        } catch (Exception e) {
            log.error("Failed to initialize server: {0}.", e.getMessage(), e);
            return false;
        }

        return true;
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
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public int getConnectionCount() {
        if (!isActive()) {
            return 0;
        }

        return connectionCount.get();
    }
}
