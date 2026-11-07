package org.pixel.network;

import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.NettyNetworkClient;
import org.pixel.network.io.netty.NettyNetworkServer;

/**
 * Entry point of the network extension. The caller owns the returned objects (init, then dispose).
 */
public final class Network {

    private Network() {
    }

    /**
     * Create a network server (Netty).
     *
     * @param settings The server settings.
     * @return A new, not yet initialized server.
     */
    public static NetworkServer createServer(NetworkServerSettings settings) {
        return new NettyNetworkServer(settings);
    }

    /**
     * Create a network client (Netty).
     *
     * @param settings The client settings.
     * @return A new, not yet initialized client.
     */
    public static NetworkClient createClient(NetworkClientSettings settings) {
        return new NettyNetworkClient(settings);
    }
}
