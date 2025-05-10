package org.pixel.network.handler.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.NetworkChannel;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.message.HandshakeResponse;

@RequiredArgsConstructor
public class ClientHandshakeHandler extends SimpleChannelInboundHandler<HandshakeResponse> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandshakeHandler.class);

    private final NetworkClientSettings settings;
    private final NetworkChannel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeResponse response) throws Exception {
        if (response.getStatus().trim().equals("200")) {
            // Handshake successful
            log.debug("Handshake successful!");

            if (settings.getClientListener() != null) {
                settings.getClientListener().onChannelActive(channel);
            }

        } else {
            // TODO: handle auth challenge / specific errors
            // Handshake failed
            log.error("Handshake failed: {0} - {1}", response.getStatus(), response.getReason());
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Trigger the handshake request:
        if (settings.getAuth() != null) {
            sendHandshakeRequest(ctx);
        } else {
            log.debug("No authentication required, skipping handshake request.");
        }

        super.channelActive(ctx);
    }

    private void sendHandshakeRequest(ChannelHandlerContext ctx) {
        log.trace("Sending handshake request ({0}) to {1}.", channel.getName(), ctx.channel().remoteAddress());

        var handshakeRequest = new HandshakeRequest();
        handshakeRequest.setAuth(settings.getAuth().toString());
        handshakeRequest.setChannel(channel.getName());

        ctx.channel().writeAndFlush(handshakeRequest);
    }
}
