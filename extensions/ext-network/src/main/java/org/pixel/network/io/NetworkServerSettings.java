package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.NetworkTransport;
import org.pixel.network.api.NetworkAuthenticator;
import org.pixel.network.api.NetworkServerListener;
import org.pixel.network.security.AuthType;

import java.io.File;
import java.util.List;

@Builder
@Getter
public class NetworkServerSettings {
    // Network settings
    @Builder.Default
    private final SocketAddress bindAddress = new SocketAddress("0.0.0.0", 8080);
    @Builder.Default
    private final int maxConnections = 100;
    @Builder.Default
    private final int numThreads = 10;
    @Builder.Default
    private final int maxIdleTimeSeconds = 120;
    @Builder.Default
    private final NetworkTransport transport = NetworkTransport.TCP;

    // Channels
    @Builder.Default
    private final List<String> allowedChannels = List.of("game", "chat", "asset");

    // Listeners
    private final NetworkServerListener serverListener;

    // SSL/TLS
    private final File certChainFile;      // Nullable
    private final File privateKeyFile;     // Nullable
    private final String privateKeyPassword; // Nullable

    // Security
    private final NetworkAuthenticator authenticator;
    @Builder.Default
    private final List<AuthType> allowedAuthTypes = List.of(AuthType.NONE, AuthType.BASIC);
}
