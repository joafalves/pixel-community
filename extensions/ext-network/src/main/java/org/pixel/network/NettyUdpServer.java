package org.pixel.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.command.Command;
import org.pixel.network.handler.CommandDecoderHandler;

public class NettyUdpServer extends Server {

    private static final Logger log = LoggerFactory.getLogger(NettyUdpServer.class);

    private MultiThreadIoEventLoopGroup group;
    private Channel channel;

    public NettyUdpServer(String localHost, int localPort) {
        this.localHost = localHost;
        this.localPort = localPort;
    }

    @Override
    public boolean start() {
        log.debug("Starting UDP server on: {0}:{1}", localHost, localPort);

        // already started?
        if (channel != null && channel.isActive()) {
            log.warn("Server already started, stop first before starting again.");
            return false;
        }

        group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new CommandDecoderHandler());
                        pipeline.addLast(new SimpleChannelInboundHandler<Command>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
                                log.trace("Inbound Command: {0}.", msg);
                                notifyListeners(msg);
                            }
                        });
                    }
                });

        try {
            channel = bootstrap.bind(localHost, localPort).sync().channel();
            log.info("UDP server started on: {0}:{1}", localHost, localPort);
            return true;

        } catch (InterruptedException e) {
            log.error("Error starting UDP server.", e);
        }

        return false;
    }

    @Override
    public void stop() {
        log.debug("Stopping UDP server on: {0}:{1}", localHost, localPort);
        try {
            channel.close().sync();
            group.shutdownGracefully().sync();
            log.info("UDP server stopped on: {0}:{1}", localHost, localPort);
        } catch (InterruptedException e) {
            log.error("Error stopping UDP server.", e);
        }
    }

    @Override
    public void broadcast(Command command) {

    }
}
