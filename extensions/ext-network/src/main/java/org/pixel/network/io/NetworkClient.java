package org.pixel.network.io;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.network.message.NetworkMessage;

public abstract class NetworkClient implements Initializable, Disposable {

    protected final NetworkClientSettings settings;

    /**
     * Constructor
     *
     * @param settings - The game client settings
     */
    protected NetworkClient(NetworkClientSettings settings) {
        this.settings = settings;
    }

    /**
     * Write message to the server
     *
     * @param msg - The message to be sent
     */
    public abstract void write(NetworkMessage msg);

    /**
     * Get the current network state
     *
     * @return True if the client is connected
     */
    public abstract boolean isConnected();
}