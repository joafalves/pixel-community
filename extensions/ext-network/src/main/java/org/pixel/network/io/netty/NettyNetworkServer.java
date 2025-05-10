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
import org.pixel.network.NetworkTransport;
import org.pixel.network.handler.netty.ExceptionHandler;
import org.pixel.network.handler.netty.NetworkLoggerHandler;
import org.pixel.network.handler.netty.NetworkMessageDecoder;
import org.pixel.network.handler.netty.NetworkMessageEncoder;
import org.pixel.network.handler.netty.server.ServerConnectionHandler;
import org.pixel.network.handler.netty.server.ServerHandshakeHandler;
import org.pixel.network.handler.netty.server.ServerInboundDataMessageServerHandler;
import org.pixel.network.handler.netty.server.ServerInboundSecurityHandler;
import org.pixel.network.io.NetworkPlayer;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.NetworkSessionManager;
import org.pixel.network.io.netty.event.HandshakeSuccessEvent;
import org.pixel.network.message.NetworkMessage;
import org.pixel.network.security.AuthType;

import java.util.Collection;

public class NettyNetworkServer extends NetworkServer {

    private static final Logger log = LoggerFactory.getLogger(NettyNetworkServer.class);

    private static final NetworkSessionManager sessionManager = new NetworkSessionManager();

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

        if (settings.getTransport() == NetworkTransport.UDP) {
            throw new UnsupportedOperationException("UDP transport is not yet supported.");
        }

        if (settings.getAuthenticator() == null && !settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.warn("Authenticator is not set!");
            return false;
        }

        bossGroup = new MultiThreadIoEventLoopGroup(settings.getNumThreads(), NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            var bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            var p = socketChannel.pipeline();
                            if (settings.getTransport() == NetworkTransport.TLS) {
                                var sslContext = createSslContext();
                                p.addLast(sslContext.newHandler(socketChannel.alloc()));
                            }

                            p.addLast(new ServerConnectionHandler(settings.getMaxConnections()));
                            p.addLast(new NetworkMessageDecoder());
                            p.addLast(new NetworkMessageEncoder());
                            p.addLast(new NetworkLoggerHandler());

                            // Handshake is *special* and HAS TO be added before the security handler:
                            p.addLast(new ServerHandshakeHandler(settings));
                            p.addLast(new ServerInboundSecurityHandler(sessionManager));
                            p.addLast(new ServerInboundDataMessageServerHandler(settings, sessionManager));

                            p.addLast(new ExceptionHandler());
                            p.addLast(new IdleStateHandler(settings.getMaxIdleTimeSeconds(), 0, 0));

                            p.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    final var connectionId = NettyHelper.getConnectionId(ctx.channel());
                                    final var connection = sessionManager.getConnection(connectionId);
                                    final NetworkPlayer player;
                                    if (connection != null) {
                                        player = sessionManager.getPlayer(connection.getPlayerId());
                                        log.debug("Player {0} disconnected.", connection.getPlayerId());
                                    } else {
                                        player = null;
                                        log.debug("Player disconnected from {0}.", ctx.channel().remoteAddress());
                                    }

                                    sessionManager.removeConnection(connectionId);

                                    if (settings.getServerListener() != null && player != null) {
                                        settings.getServerListener().onConnectionRemoved(player, connection);
                                    }

                                    super.channelInactive(ctx);
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
    public Collection<NetworkPlayer> getPlayers() {
        return sessionManager.getPlayers();
    }

    @Override
    public Collection<NetworkPlayer> getPlayers(String channelName) {
        return sessionManager.getPlayers().stream()
                .filter(player -> player.getConnections().containsKey(channelName))
                .toList();
    }

    @Override
    public void broadcast(NetworkMessage message) {
        for (NetworkPlayer player : sessionManager.getPlayers()) {
            player.send(message);
        }
    }

    @Override
    public void broadcast(String channelName, NetworkMessage message) {
        for (NetworkPlayer player : sessionManager.getPlayers()) {
            player.send(channelName, message);
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

        return sessionManager.getConnectionCount();
    }

    @Override
    public int getPlayerCount() {
        if (!isActive()) {
            return 0;
        }

        return sessionManager.getPlayerCount();
    }

    private boolean handlePipelineEvent(ChannelHandlerContext ctx, Object event) {
        if (event instanceof IdleStateEvent e) {
            if (e.state() == IdleState.READER_IDLE) {
                final var connectionId = NettyHelper.getConnectionId(ctx.channel());
                final var connection = sessionManager.getConnection(connectionId);

                if (connection != null) {
                    log.info("Closing idle connection {0} for player {1}.", connectionId, connection.getPlayerId());
                } else {
                    log.info("Closing idle connection {0}.", connectionId);
                }

                ctx.close(); // or custom session purge logic
            }
            return true;

        } else if (event instanceof HandshakeSuccessEvent e) {
            final var connection = e.getConnection();
            final var player = sessionManager.addConnection(connection);

            log.info("Handshake successful for player {0} (channel: {1}).",
                    player.getId(), connection.getChannelName());

            if (settings.getServerListener() != null) {
                settings.getServerListener().onConnectionAccepted(player, connection);
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
