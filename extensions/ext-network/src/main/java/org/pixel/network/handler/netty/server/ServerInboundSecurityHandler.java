package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.NetworkSessionManager;
import org.pixel.network.io.netty.NettyHelper;

@RequiredArgsConstructor
public class ServerInboundSecurityHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ServerInboundSecurityHandler.class);

    private final NetworkSessionManager sessionManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        var connectionId = NettyHelper.getConnectionId(ctx.channel());
        var connection = sessionManager.getConnection(connectionId);

        if (connection == null) {
            log.warn("Received message from an unknown connection: {0}.", connectionId);
            ctx.close();
            return;
        }

        // TODO: other security checks can be added here (e.g. IP filtering, rate limiting, max conns per ip, etc.)

        // Call the next handler in the pipeline
        ctx.fireChannelRead(message);
    }
}
