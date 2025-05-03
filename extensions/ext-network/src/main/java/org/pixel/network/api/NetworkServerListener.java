package org.pixel.network.api;

import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.message.DataMessage;

public interface NetworkServerListener {

    /**
     * Called when a player becomes active (e.g., connected and authenticated).
     *
     * @param player The player that became active.
     */
    void onPlayerActive(NetworkPlayer player);

    /**
     * Called when a player becomes inactive (e.g., disconnected).
     *
     * @param player The player that became inactive.
     */
    void onPlayerInactive(NetworkPlayer player);

    /**
     * Called when the server receives a message from a player.
     *
     * @param player  The player that sent the message.
     * @param message The message that was sent.
     */
    void onPlayerMessage(NetworkPlayer player, DataMessage message);
}
