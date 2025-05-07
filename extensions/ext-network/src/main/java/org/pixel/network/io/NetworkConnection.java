package org.pixel.network.io;

import org.pixel.commons.data.DataMap;
import org.pixel.network.message.NetworkMessage;

public interface NetworkConnection {
    /**
     * Unique per‐connection ID (e.g. Netty’s channel.id().asLongText())
     */
    String getConnectionId();

    /**
     * Unique player ID
     */
    String getPlayerId();

    /**
     * Logical “channel name” from your handshake
     */
    String getChannelName();

    /**
     * Get the data map for this connection
     *
     * @return the data map
     */
    DataMap getData();

    /**
     * Write out a message on this connection
     */
    void send(NetworkMessage msg);

    /**
     * Is the underlying socket still active?
     */
    boolean isActive();
}
