package org.pixel.network.io.netty.event;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.io.netty.NettyNetworkSession;
import org.pixel.network.io.netty.NettySessionState;

@Builder
@Getter
public class PlayerStateChangeEvent {
    private final NettyNetworkSession session;
    private final NettySessionState oldState;
}
