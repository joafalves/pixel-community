package org.pixel.network.io.netty;

import io.netty.channel.Channel;
import lombok.Getter;
import org.pixel.commons.data.DataMap;
import org.pixel.network.io.NetworkConnection;
import org.pixel.network.message.NetworkMessage;

@Getter
public class NettyNetworkConnection implements NetworkConnection {
    private final String connectionId;
    private final String playerId;
    private final String channelName;
    private final Channel nettyChannel;
    private final DataMap data;    // per‐channel data

    public NettyNetworkConnection(Channel ch, String channelName, String playerId, DataMap data) {
        this.connectionId = ch.id().asLongText();
        this.playerId = playerId;
        this.nettyChannel = ch;
        this.channelName = channelName;
        this.data = data;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public void send(NetworkMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (!nettyChannel.isActive()) {
            throw new IllegalStateException("Channel is not active");
        }

        nettyChannel.writeAndFlush(msg);
    }

    @Override
    public boolean isActive() {
        return nettyChannel.isActive();
    }
}
