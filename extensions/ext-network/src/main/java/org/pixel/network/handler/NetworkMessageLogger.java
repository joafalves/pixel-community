package org.pixel.network.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelDuplexHandler;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.dsnp.NetworkMessage;

@Sharable
public class NetworkMessageLogger extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageLogger.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof NetworkMessage message) {
            log.debug("Received message: type={}, payload_length={}",
                    message.getType(),
                    message.getPayload().length);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) {
        if (msg instanceof NetworkMessage message) {
            log.debug("Sending message: type={}, payload_length={}",
                    message.getType(),
                    message.getPayload().length);
        }
        ctx.write(msg, promise);
    }
}