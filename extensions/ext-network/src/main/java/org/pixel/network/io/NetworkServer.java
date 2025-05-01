package org.pixel.network.io;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.network.api.NetworkAuthenticator;

@Setter
@Getter
public abstract class NetworkServer implements Initializable, Disposable {

    protected final NetworkServerSettings settings;

    protected NetworkAuthenticator authenticator;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    protected NetworkServer(NetworkServerSettings settings) {
        this.settings = settings;
    }

    /**
     * Get the current network state
     *
     * @return True if the server is bound and listening
     */
    public abstract boolean isActive();

    /**
     * Get the number of active connections
     *
     * @return The number of active connections
     */
    public abstract int getConnectionCount();
}
