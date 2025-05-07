package org.pixel.network.api;

import org.pixel.network.io.NetworkChannel;
import org.pixel.network.message.DataMessage;

public interface NetworkClientListener {
    /**
     * Called when a client channel becomes active (e.g., connected to the server).
     */
    void onChannelActive(NetworkChannel channel);

    /**
     * Called when a client channel becomes inactive (e.g., disconnected from the server).
     */
    void onChannelInactive(NetworkChannel channel);

    /**
     * Called when the client receives a message from a network channel.
     *
     * @param channel - The channel through which the message was received
     * @param message - The message received from the server
     */
    void onChannelMessage(NetworkChannel channel, DataMessage message);
}
