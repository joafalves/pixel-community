package org.pixel.network.io.netty;


import io.netty.channel.Channel;

public class NettyHelper {

    /*public static final AttributeKey<NettyNetworkSession> NETWORK_SESSION = AttributeKey.valueOf("NET.SESSION");

    public static NettyNetworkSession getNetworkSession(Channel channel) {
        return channel.attr(NETWORK_SESSION).get();
    }*/

    public static String getConnectionId(Channel channel) {
        return channel.id().asLongText();
    }
}
