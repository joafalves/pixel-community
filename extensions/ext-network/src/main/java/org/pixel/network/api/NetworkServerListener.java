package org.pixel.network.api;

import org.pixel.network.io.NetworkConnection;
import org.pixel.network.io.NetworkPlayer;
import org.pixel.network.message.NetworkMessage;

public interface NetworkServerListener {

    /**
     * Called when a player channel becomes active (e.g., connected successfully).
     *
     * @param player     the player that is connected
     * @param connection the connection object representing the player's connection
     */
    void onConnectionAccepted(NetworkPlayer player, NetworkConnection connection);

    /**
     * Called when a player channel becomes inactive (e.g., disconnected).
     *
     * @param player     the player that is disconnected
     * @param connection the connection object representing the player's connection
     */
    void onConnectionRemoved(NetworkPlayer player, NetworkConnection connection);

    /**
     * Called when receiving a message from a player.
     *
     * @param player     the player that sent the message
     * @param connection the connection object representing the player's connection
     * @param message    the message sent by the player
     */
    void onConnectionMessage(NetworkPlayer player, NetworkConnection connection, NetworkMessage message);
}
