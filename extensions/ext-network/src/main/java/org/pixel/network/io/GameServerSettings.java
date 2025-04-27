package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.security.AuthenticationType;

@Builder
@Getter
public class GameServerSettings {
    @Builder.Default
    private SocketAddress bindAddress = new SocketAddress("0.0.0.0", 8080);
    @Builder.Default
    private int maxConnections = 100;
    @Builder.Default
    private int numThreads = 10;
    @Builder.Default
    private int statsLogYieldSeconds = 60;
    /**
     * If enabled, SSL connections are MANDATORY!
     */
    @Builder.Default
    private boolean forceSSL = false;
    @Builder.Default
    private AuthenticationType[] authenticationTypes = new AuthenticationType[]{AuthenticationType.BASIC, AuthenticationType.DIGEST};
}
