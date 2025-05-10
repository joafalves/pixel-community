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
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.handler.netty.ExceptionHandler;
import org.pixel.network.handler.netty.NetworkLoggerHandler;
import org.pixel.network.handler.netty.NetworkMessageDecoder;
import org.pixel.network.handler.netty.NetworkMessageEncoder;
import org.pixel.network.handler.netty.client.ClientHandshakeHandler;
import org.pixel.network.handler.netty.client.ClientInboundDataMessageHandler;
import org.pixel.network.io.NetworkChannel;
import org.pixel.network.io.NetworkChannelSettings;
import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.message.HeartbeatMessage;
import org.pixel.network.message.NetworkMessage;

import javax.net.ssl.SSLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyNetworkClient extends NetworkClient {

    private static final Logger log = LoggerFactory.getLogger(NettyNetworkClient.class);

    private final Map<String, ChannelHolder> channels = new ConcurrentHashMap<>();

    public NettyNetworkClient(NetworkClientSettings settings) {
        super(settings);
    }

    @Override
    public boolean init() {
        // Ensure that there are no duplicate channel names:
        var channelNames = settings.getChannels().stream()
                .map(NetworkChannelSettings::getName)
                .distinct()
                .toList();
        if (channelNames.size() != settings.getChannels().size()) {
            throw new IllegalArgumentException("Duplicate channel names found in settings.");
        }

        for (NetworkChannelSettings chSettings : settings.getChannels()) {
            connectChannel(chSettings);
        }
        return true;
    }

    private void connectChannel(NetworkChannelSettings chSettings) {
        final var networkChannel = new NetworkChannel(
                chSettings.getName(), chSettings.getTransport(), chSettings.getServerAddress());
        final var group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final var p = ch.pipeline();

                        if (settings.getTrustCertChainFile() != null) {
                            var sslCtx = createClientSslContext();
                            p.addLast( sslCtx.newHandler(
                                    ch.alloc(), chSettings.getServerAddress().getHost(), chSettings.getServerAddress().getPort()));
                        }

                        p.addLast(new NetworkMessageDecoder());
                        p.addLast(new NetworkMessageEncoder());
                        p.addLast(new NetworkLoggerHandler());

                        p.addLast(new ClientHandshakeHandler(settings, networkChannel));
                        p.addLast(new ClientInboundDataMessageHandler(settings, networkChannel));

                        p.addLast(new IdleStateHandler(0, chSettings.getHeartbeatIntervalSeconds(), 0));
                        p.addLast(new LifecycleHandler(networkChannel));
                        p.addLast(new ExceptionHandler());
                    }
                });

        bootstrap.connect(chSettings.getServerAddress().getHost(), chSettings.getServerAddress().getPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        var nettyChannel = future.channel();
                        channels.put(chSettings.getName(), new ChannelHolder(nettyChannel, group, chSettings));
                        settings.getClientListener().onChannelActive(networkChannel);
                        log.info("Connected to channel: {0}", chSettings.getName());
                    } else {
                        log.error("Failed to connect channel: {0}, scheduling reconnect...", chSettings.getName());
                        scheduleReconnect(chSettings, group);
                    }
                });
    }

    private void scheduleReconnect(NetworkChannelSettings chSettings, EventLoopGroup group) {
        if (!chSettings.isAutoReconnect()) return;
        group.schedule(() -> connectChannel(chSettings), 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        for (var entry : channels.entrySet()) {
            entry.getValue().channel.close();
            entry.getValue().group.shutdownGracefully();
        }
        channels.clear();
    }

    @Override
    public boolean isConnected() {
        return channels.values().stream().anyMatch(c -> c.channel.isActive());
    }

    @Override
    public boolean send(NetworkChannel channel, NetworkMessage msg) {
        return send(channel.getName(), msg);
    }

    @Override
    public boolean send(String channelName, NetworkMessage msg) {
        final var ioChannel = channels.get(channelName);
        if (ioChannel == null || !ioChannel.channel.isActive()) {
            log.warn("Channel not active or not found: {0}", channelName);
            return false;
        }

        ioChannel.channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("Failed to send message to {0}: {1}", channelName, future.cause().getMessage());
            }
        });

        return true;
    }

    private SslContext createClientSslContext() throws SSLException {
        var builder = SslContextBuilder.forClient();
        if (settings.getTrustCertChainFile() != null) {
            builder.trustManager(settings.getTrustCertChainFile());
        }
        return builder.build();
    }

    @RequiredArgsConstructor
    private static class ChannelHolder {
        final Channel channel;
        final EventLoopGroup group;
        final NetworkChannelSettings settings;
    }

    @RequiredArgsConstructor
    private class LifecycleHandler extends ChannelInboundHandlerAdapter {
        private final NetworkChannel channel;

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("Channel {0} disconnected.", channel.getName());
            settings.getClientListener().onChannelInactive(channel);

            var holder = channels.remove(channel.getName());
            if (holder != null && holder.settings.isAutoReconnect()) {
                scheduleReconnect(holder.settings, holder.group);
            }

            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent e && e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(new HeartbeatMessage());
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
