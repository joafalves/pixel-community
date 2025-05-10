package org.pixel.network.handler.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class ExceptionHandler extends ChannelDuplexHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Pipeline exception: {0}.", cause.getMessage(), cause);
        ctx.close();
    }
}