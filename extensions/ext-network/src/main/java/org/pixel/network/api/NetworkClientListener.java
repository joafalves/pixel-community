package org.pixel.network.api;

import org.pixel.network.message.DataMessage;

public interface NetworkClientListener {
    /**
     * Called when the client is ready to send and receive packets.
     */
    void onConnect();

    /**
     * Called when the client is disconnected from the server.
     */
    void onDisconnect();

    /**
     * Called when the client receives a message from the server.
     *
     * @param message - The message received from the server
     */
    void onMessage(DataMessage message);
}
