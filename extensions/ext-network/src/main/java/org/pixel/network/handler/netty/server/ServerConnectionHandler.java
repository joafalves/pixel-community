package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectionHandler.class);

    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final int maxConnections;

    public ServerConnectionHandler(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int numConnections = connectionCount.incrementAndGet();
        if (maxConnections > 0 && numConnections > maxConnections) {
            // TODO: Implement a queue for handling connection requests when the maximum number of connections is reached
            log.warn("Connection rejected: maximum connections ({0}) reached.", maxConnections);
            ctx.close();
            return;
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionCount.decrementAndGet();
        super.channelInactive(ctx);
    }

    public int getCurrentConnections() {
        return connectionCount.get();
    }
}