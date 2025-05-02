package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.netty.NettyNetworkSession;
import org.pixel.network.io.netty.NettyHelper;

public class InboundSecurityHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(InboundSecurityHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        NettyNetworkSession session = NettyHelper.getNetworkSession(ctx.channel());
        if (session == null) {
            log.warn("Received message from an unknown session: {0}.", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        if (!session.isActive()) {
            log.warn("Received message from an invalid session: {0} (state: {1}).",
                    session.getId(), session.getState());
            ctx.close();
            return;
        }

        // TODO: other security checks can be added here (e.g. IP filtering, rate limiting, etc.)

        // Update the last activity time for the session:
        session.setLastRemoteActivity(System.currentTimeMillis());

        // Call the next handler in the pipeline
        ctx.fireChannelRead(message);
    }
}
