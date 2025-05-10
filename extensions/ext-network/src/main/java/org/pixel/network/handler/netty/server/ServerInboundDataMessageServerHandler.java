package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.NetworkSessionManager;
import org.pixel.network.io.netty.NettyHelper;
import org.pixel.network.message.DataMessage;

@RequiredArgsConstructor
public class ServerInboundDataMessageServerHandler extends SimpleChannelInboundHandler<DataMessage> {

    private static final Logger log = LoggerFactory.getLogger(ServerInboundDataMessageServerHandler.class);

    private final NetworkServerSettings settings;
    private final NetworkSessionManager sessionManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage message) throws Exception {
        var connectionId = NettyHelper.getConnectionId(ctx.channel());
        var connection = sessionManager.getConnection(connectionId);
        if (connection == null) {
            log.warn("Received data message from an unknown connection: {0}.", connectionId);
            return;
        }

        var player = sessionManager.getPlayer(connection.getPlayerId());
        if (player == null) {
            log.warn("Received data message from an unknown player: {0}.", connection.getPlayerId());
            return;
        }

        if (settings.getServerListener() != null) {
            settings.getServerListener().onConnectionMessage(player, connection, message);

        } else {
            log.warn("No event listener set, ignoring data message: {0}.", message);
        }
    }
}
