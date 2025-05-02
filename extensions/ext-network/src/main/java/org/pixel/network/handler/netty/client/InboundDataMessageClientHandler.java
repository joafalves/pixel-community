package org.pixel.network.handler.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.handler.netty.server.HandshakeRequestHandler;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.message.DataMessage;

@RequiredArgsConstructor
public class InboundDataMessageClientHandler extends SimpleChannelInboundHandler<DataMessage> {

    private static final Logger log = LoggerFactory.getLogger(HandshakeRequestHandler.class);

    private final NetworkClientSettings settings;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage message) throws Exception {
        if (settings.getClientListener() != null) {
            settings.getClientListener().onMessage(message);
        } else {
            log.warn("No event listener set, ignoring data message: {0}.", message);
        }
    }
}
