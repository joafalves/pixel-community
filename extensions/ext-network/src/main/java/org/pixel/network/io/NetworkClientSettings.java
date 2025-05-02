package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.api.NetworkClientListener;
import org.pixel.network.data.SocketAddress;

import java.io.File;

@Builder
@Getter
public class NetworkClientSettings {
    // Network settings
    private final SocketAddress serverAddress;
    @Builder.Default
    private final int heartbeatIntervalSeconds = 30;
    @Builder.Default
    private final boolean autoReconnect = true;

    // Listeners
    private final NetworkClientListener clientListener;

    // Security
    @Builder.Default
    private final boolean isSecure = false;
    private final File trustCertChainFile = null;   // nullable, PEM for CA chain
}