package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.data.SocketAddress;

@Builder
@Getter
public class NetworkClientSettings {
    private SocketAddress serverAddress;
    @Builder.Default
    private int heartbeatIntervalSeconds = 30;
}