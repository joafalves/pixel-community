package org.pixel.network.io.netty;


import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.pixel.network.data.NetworkSession;

public class NettyUtils {

    public static final AttributeKey<NetworkSession> SESSION = AttributeKey.valueOf("SESSION");

    public static NetworkSession getSession(Channel channel) {
        return channel.attr(SESSION).get();
    }

    public static void attachSession(Channel channel, NetworkSession session) {
        channel.attr(SESSION).set(session);
    }
}
