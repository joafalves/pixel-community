package org.pixel.network.io.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.handler.netty.ExceptionHandler;
import org.pixel.network.handler.netty.NetworkLoggerHandler;
import org.pixel.network.handler.netty.NetworkMessageDecoder;
import org.pixel.network.handler.netty.NetworkMessageEncoder;
import org.pixel.network.handler.netty.client.HandshakeResponseHandler;
import org.pixel.network.handler.netty.client.InboundDataMessageClientHandler;
import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.message.HeartbeatMessage;
import org.pixel.network.message.NetworkMessage;

import javax.net.ssl.SSLException;

public class NettyNetworkClient extends NetworkClient implements ChannelFutureListener {

    private static final Logger log = LoggerFactory.getLogger(NettyNetworkClient.class);

    private EventLoopGroup group;
    private Channel channel;

    public NettyNetworkClient(NetworkClientSettings settings) {
        super(settings);
    }

    @Override
    public boolean init() {
        if (isConnected()) {
            log.error("Client already connected.");
            return false;
        }

        // TODO: RECONNECT
        group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            var bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            var p = ch.pipeline();

                            if (settings.isSecure()) {
                                // hostname and port help SNI and host verification:
                                var sslContext = createClientSslContext();
                                p.addLast(sslContext.newHandler(ch.alloc(), settings.getServerAddress().getHost(),
                                        settings.getServerAddress().getPort()));
                            }

                            p.addLast(new NetworkMessageDecoder());
                            p.addLast(new NetworkMessageEncoder());
                            p.addLast(new NetworkLoggerHandler());

                            p.addLast(new HandshakeResponseHandler(settings));
                            p.addLast(new InboundDataMessageClientHandler(settings));

                            p.addLast(new ExceptionHandler());
                            p.addLast(new IdleStateHandler(0, settings.getHeartbeatIntervalSeconds(), 0));

                            p.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("Connection to server closed: {0}.", ctx.channel().remoteAddress());

                                    settings.getClientListener().onDisconnect();

                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("Connected to server: {0}.", ctx.channel().remoteAddress());

                                    super.channelActive(ctx);
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent e) {
                                        if (e.state() == IdleState.WRITER_IDLE) {
                                            // Write idle time has passed, send a heartbeat message:
                                            send(new HeartbeatMessage());
                                        }
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }
                            });
                        }
                    });

            // Connect to the server
            channel = bootstrap.connect(settings.getServerAddress().getHost(), settings.getServerAddress().getPort())
                    .sync().channel();


        } catch (Exception e) {
            log.error("Failed to initialize client: {0}.", e.getMessage(), e);
            return false;
        }

        return true;
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
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public boolean send(NetworkMessage msg) {
        // TODO: implement buffering of messages (priority queue)
        if (!isConnected()) {
            log.warn("Client is not connected, cannot send message: {0}.", msg);
            return false;
        }

        channel.writeAndFlush(msg).addListener(this);

        return true;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        if (!channelFuture.isSuccess()) {
            log.error("Failed to write message: {0}.", channelFuture.cause().getMessage(), channelFuture.cause());
        }
    }

    private SslContext createClientSslContext() throws SSLException {
        SslContextBuilder builder = SslContextBuilder.forClient();
        if (settings.getTrustCertChainFile() != null) {
            // production: trust a specific CA chain
            builder.trustManager(settings.getTrustCertChainFile());
        } // by default, if no trust manager is set, the default system trust manager is used

        // Note: dev/testing: trust all (self-signed) — NOT for production!
        //builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        //log.warn("Client is in insecure trust‐all mode; only for testing!");

        return builder.build();
    }
}