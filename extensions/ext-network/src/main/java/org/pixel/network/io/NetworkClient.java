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
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null.");
        }

        if (settings.getChannels() == null || settings.getChannels().isEmpty()) {
            throw new IllegalArgumentException("Channels cannot be null or empty.");
        }

        if (settings.getClientListener() == null) {
            throw new IllegalArgumentException("Client listener cannot be null.");
        }

        this.settings = settings;
    }

    /**
     * Write a message to the channel
     *
     * @param channel - The channel to send the message to
     * @param msg     - The message to be sent
     * @return True if the message was queued for sending
     */
    public abstract boolean send(NetworkChannel channel, NetworkMessage msg);

    /**
     * Write a message to the channel
     *
     * @param channelName - The channel name to send the message to
     * @param msg         - The message to be sent
     * @return True if the message was queued for sending
     */
    public abstract boolean send(String channelName, NetworkMessage msg);

    /**
     * Get the current network state
     *
     * @return True if the client is connected
     */
    public abstract boolean isConnected();
}