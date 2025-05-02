package org.pixel.network.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.handler.netty.*;
import org.pixel.network.handler.netty.server.ConnectionHandler;
import org.pixel.network.handler.netty.server.HandshakeRequestHandler;
import org.pixel.network.handler.netty.server.InboundDataMessageServerHandler;
import org.pixel.network.handler.netty.server.InboundSecurityHandler;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.event.PlayerStateChangeEvent;
import org.pixel.network.message.NetworkMessage;
import org.pixel.network.security.AuthType;

import java.io.IOException;
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

        if (settings.getAuthenticator() == null && !settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
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
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            var p = socketChannel.pipeline();

                            if (settings.isSecure()) {
                                var sslContext = createSslContext();
                                p.addLast(sslContext.newHandler(socketChannel.alloc()));
                            }

                            p.addLast(new ConnectionHandler(settings.getMaxConnections()));
                            p.addLast(new NetworkMessageDecoder());
                            p.addLast(new NetworkMessageEncoder());
                            p.addLast(new NetworkLoggerHandler());

                            // Handshake is *special* and HAS TO be added before the security handler:
                            p.addLast(new HandshakeRequestHandler(settings));
                            p.addLast(new InboundSecurityHandler());
                            p.addLast(new InboundDataMessageServerHandler(settings));

                            p.addLast(new ExceptionHandler());
                            p.addLast(new IdleStateHandler(settings.getMaxIdleTimeSeconds(), 0, 0));

                            // State handler:
                            p.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    NettyNetworkSession session = NettyHelper.getNetworkSession(ctx.channel());
                                    if (session != null) {
                                        log.debug("Session {0} inactive.", session.getId());
                                    } else {
                                        log.debug("Session inactive from {0}.", ctx.channel().remoteAddress());
                                    }
                                    // Called when the channel becomes inactive...
                                    ctx.channel().attr(NettyHelper.NETWORK_SESSION).set(null);
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
                                    if (!handlePipelineEvent(ctx, evt)) {
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
    public boolean send(NetworkPlayer player, NetworkMessage message) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
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

    private boolean handlePipelineEvent(ChannelHandlerContext ctx, Object event) {
        if (event instanceof IdleStateEvent e) {
            if (e.state() == IdleState.READER_IDLE) {
                var session = NettyHelper.getNetworkSession(ctx.channel());
                if (session != null) {
                    log.info("Closing idle session {0}", session.getId());
                }
                ctx.close(); // or custom session purge logic
            }
            return true;

        } else if (event instanceof PlayerStateChangeEvent e) {
            var session = e.getSession();
            switch (session.getState()) {
                case ACTIVE -> {
                    log.info("Session {0} active.", session.getId());
                    if (settings.getServerListener() != null) {
                        settings.getServerListener().onPlayerActive(session.getPlayer());
                    }
                }
                case INACTIVE -> {
                    log.info("Session {0} inactive.", session.getId());
                    if (settings.getServerListener() != null) {
                        settings.getServerListener().onPlayerInactive(session.getPlayer());
                    }
                }
            }
        }

        return false;
    }

    private SslContext createSslContext() throws Exception {
        if (settings.getCertChainFile() != null) {
            // user-provided certs
            return SslContextBuilder.forServer(
                    settings.getCertChainFile(),
                    settings.getPrivateKeyFile(),
                    settings.getPrivateKeyPassword()
            ).build();
        } else {
            throw new UnsupportedOperationException("SSL is not supported without user-provided certs.");
        }
    }
}
