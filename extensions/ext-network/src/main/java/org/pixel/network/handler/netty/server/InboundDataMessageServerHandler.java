package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.NettyHelper;
import org.pixel.network.message.DataMessage;

@RequiredArgsConstructor
public class InboundDataMessageServerHandler extends SimpleChannelInboundHandler<DataMessage> {

    private static final Logger log = LoggerFactory.getLogger(org.pixel.network.handler.netty.server.HandshakeRequestHandler.class);

    private final NetworkServerSettings settings;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage message) throws Exception {
        var session = NettyHelper.getNetworkSession(ctx.channel());
        if (session == null || !session.isActive()) {
            log.warn("Received data message from an invalid session: {0}.", ctx.channel().id());
            return;
        }

        if (settings.getServerListener() != null) {
            settings.getServerListener().onPlayerMessage(session.getPlayer(), message);

        } else {
            log.warn("No event listener set, ignoring data message: {0}.", message);
        }
    }
}
