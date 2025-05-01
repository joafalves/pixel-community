package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.security.AuthType;

import java.util.List;

@Builder
@Getter
public class NetworkServerSettings {
    @Builder.Default
    private SocketAddress bindAddress = new SocketAddress("0.0.0.0", 8080);
    @Builder.Default
    private int maxConnections = 100;
    @Builder.Default
    private int numThreads = 10;
    @Builder.Default
    private int maxIdleTimeSeconds = 120;
    /**
     * If enabled, SSL connections are MANDATORY!
     */
    @Builder.Default
    private boolean forceSSL = false;
    @Builder.Default
    private List<AuthType> allowedAuthTypes = List.of(AuthType.NONE, AuthType.BASIC);
}
