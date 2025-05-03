package org.pixel.network.io;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.network.message.NetworkMessage;

import java.io.IOException;

public abstract class NetworkClient implements Initializable, Disposable {

    protected final NetworkClientSettings settings;

    /**
     * Constructor
     *
     * @param settings - The game client settings
     */
    protected NetworkClient(NetworkClientSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null.");
        }

        if (settings.getServerAddress() == null) {
            throw new IllegalArgumentException("Server address cannot be null.");
        }

        if (settings.getClientListener() == null) {
            throw new IllegalArgumentException("Client listener cannot be null.");
        }

        this.settings = settings;
    }

    /**
     * Write a message to the server
     *
     * @param msg - The message to be sent
     * @return True if the message was queued for sending
     */
    public abstract boolean send(NetworkMessage msg);

    /**
     * Get the current network state
     *
     * @return True if the client is connected
     */
    public abstract boolean isConnected();
}