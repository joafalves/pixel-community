package org.pixel.arkade;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.io.SocketAddress;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.NettyNetworkServer;

public class Arkade implements Initializable, Runnable, Disposable {

    private static final Logger log = LoggerFactory.getLogger(Arkade.class);

    private final ArkadeSettings settings;

    private State state = State.NEW;
    private NetworkServer networkServer = null;

    public Arkade(ArkadeSettings settings) {
        this.settings = settings;
    }

    @Override
    public synchronized boolean init() {
        if (state.hasInitialized()) {
            log.warn("Arkade is already initialized.");
            return false;
        }

        state = State.INITIALIZING;

        var bindingAddress = new SocketAddress(
                settings.getNetwork().getBindingAddress(),
                settings.getNetwork().getBindingPort()
        );

        // Bootstrap the game server
        networkServer = new NettyNetworkServer(NetworkServerSettings.builder()
                .bindAddress(bindingAddress)
                .maxConnections(settings.getNetwork().getMaxConnections())
                .numThreads(settings.getNetwork().getThreads())
                .build());




        if (!networkServer.init()) {
            throw new RuntimeException("Failed to initialize game server");
        }


        state = State.INITIALIZED;

        return true;
    }

    @Override
    public void run() {
        if (!state.hasInitialized()) {
            log.warn("Arkade is not initialized.");
            return;
        }


    }

    @Override
    public synchronized void dispose() {
        if (!state.isActive()) {
            log.warn("Arkade is not active. Cannot dispose.");
            return;
        }

        state = State.DISPOSING;

        // TODO: Dispose resources here

        state = State.DISPOSED;
    }
}
