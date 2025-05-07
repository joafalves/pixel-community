package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.NetworkTransport;

@Builder
@Getter
public class NetworkChannelSettings {
    // General settings
    @Builder.Default
    private final String name = "game";

    // Network settings
    private final SocketAddress serverAddress;
    @Builder.Default
    private final int heartbeatIntervalSeconds = 30;
    @Builder.Default
    private final boolean autoReconnect = true;
    @Builder.Default
    private final NetworkTransport transport = NetworkTransport.TCP;
}
