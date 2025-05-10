package org.pixel.network.service;

import org.pixel.commons.service.ServiceFactoryWithSettings;
import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.io.netty.NettyNetworkClient;

public class NetworkClientFactory implements ServiceFactoryWithSettings<NetworkClient, NetworkClientSettings> {

    @Override
    public NetworkClient get(NetworkClientSettings settings) {
        // Construct with exactly NetworkServerSettings
        return new NettyNetworkClient(settings);
    }
}
