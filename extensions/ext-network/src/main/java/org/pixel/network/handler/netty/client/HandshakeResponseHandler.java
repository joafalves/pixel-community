package org.pixel.network.handler.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.message.HandshakeResponse;

@RequiredArgsConstructor
public class HandshakeResponseHandler extends SimpleChannelInboundHandler<HandshakeResponse> {

    private static final Logger log = LoggerFactory.getLogger(HandshakeResponseHandler.class);

    private final NetworkClientSettings settings;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeResponse response) throws Exception {
        if (response.getStatus().equals("200")) {
            // Handshake successful
            log.debug("Handshake successful!");

            if (settings.getClientListener() != null) {
                settings.getClientListener().onReady();
            }

        } else {
            // TODO: handle auth challenge
            // Handshake failed
            log.error("Handshake failed: {0} - {1}", response.getStatus(), response.getReason());

            ctx.close();
        }
    }
}
