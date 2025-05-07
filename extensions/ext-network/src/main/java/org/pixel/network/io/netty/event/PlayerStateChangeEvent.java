package org.pixel.network.io.netty.event;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.io.NetworkPlayer;
import org.pixel.network.io.netty.NettyNetworkConnection;
import org.pixel.network.io.netty.NettySessionState;

@Builder
@Getter
public class PlayerStateChangeEvent {
    private final NetworkPlayer player;
    private final NettyNetworkConnection connection;
    private final NettySessionState oldState;
}
