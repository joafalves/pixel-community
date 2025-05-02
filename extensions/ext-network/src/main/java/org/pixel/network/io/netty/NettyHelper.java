package org.pixel.network.io.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.pixel.network.io.netty.event.PlayerStateChangeEvent;

public class NettyHelper {

    public static final AttributeKey<NettyNetworkSession> NETWORK_SESSION = AttributeKey.valueOf("NET.SESSION");

    public static NettyNetworkSession getNetworkSession(Channel channel) {
        return channel.attr(NETWORK_SESSION).get();
    }

    public static void attachNetworkSession(Channel channel, NettyNetworkSession session) {
        channel.attr(NETWORK_SESSION).set(session);
    }

    public static void changeSessionState(ChannelHandlerContext ctx, NettyNetworkSession session, NettySessionState newState) {
        if (session == null) {
            return; // No session to change state for
        }

        final NettySessionState oldState;
        synchronized (session) {
            oldState = session.getState();
            if (session.getState() == newState || newState == NettySessionState.INACTIVE) {
                return; // Already in the desired state or inactive (final state)
            }
            session.setState(newState);
        }

        if (ctx != null) {
            ctx.fireUserEventTriggered(PlayerStateChangeEvent.builder()
                    .session(session)
                    .oldState(oldState)
                    .build());
        }
    }
}
