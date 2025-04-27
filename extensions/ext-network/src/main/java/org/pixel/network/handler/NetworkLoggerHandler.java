package org.pixel.network.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPromise;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.message.NetworkMessage;

@Sharable
public class NetworkLoggerHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(NetworkLoggerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof NetworkMessage message) {
            log.debug("Received message: type={0}.", message.getType());
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof NetworkMessage message) {
            log.debug("Sending message: type={0}.", message.getType());
        }
        ctx.write(msg, promise);
    }
}