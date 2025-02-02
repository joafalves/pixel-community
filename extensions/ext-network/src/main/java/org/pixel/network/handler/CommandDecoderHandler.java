package org.pixel.network.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.DataSerializer;

@ChannelHandler.Sharable
public class CommandDecoderHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandDecoderHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // ByteBufUtil.getBytes(((DatagramPacket) msg).content())
        if (msg instanceof byte[] bytes) {
            super.channelRead(ctx, DataSerializer.deserialize(bytes));
        } else {
            log.debug("Received unknown object type: \"{0}\", discarding...", msg.getClass().getName());
        }
    }
}
