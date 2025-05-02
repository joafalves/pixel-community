package org.pixel.network.service;

import org.pixel.commons.service.ServiceRegistrar;
import org.pixel.commons.service.ServiceRegistry;
import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;

import java.util.List;

public class NetworkServiceRegistrar implements ServiceRegistrar {

    private static final List<ServiceRegistry<?>> SERVICES = List.of(
            new ServiceRegistry<>(NetworkClient.class, NetworkClientSettings.class, new NetworkClientFactory()),
            new ServiceRegistry<>(NetworkServer.class, NetworkServerSettings.class, new NetworkServerFactory())
    );

    @Override
    public List<ServiceRegistry<?>> getServices() {
        return SERVICES;
    }
}
