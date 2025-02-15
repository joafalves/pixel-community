package org.pixel.network;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.data.SocketAddress;

@Builder
@Getter
public class GameServerSettings {
    private SocketAddress bindAddress;
    private int maxConnections;
    private int numThreads;
    @Builder.Default
    private int statsLogYieldSeconds = 60;
}
