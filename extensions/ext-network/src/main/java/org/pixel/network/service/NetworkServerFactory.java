package org.pixel.network.service;

import org.pixel.commons.service.ServiceFactoryWithSettings;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.NettyNetworkServer;

public class NetworkServerFactory implements ServiceFactoryWithSettings<NetworkServer, NetworkServerSettings> {

    @Override
    public NetworkServer get(NetworkServerSettings settings) {
        // Construct with exactly NetworkServerSettings
        return new NettyNetworkServer(settings);
    }
}
