package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.message.NetworkMessage;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Builder
@Getter
public class NetworkPlayer {
    private static final Logger log = LoggerFactory.getLogger(NetworkPlayer.class);

    private final String id;
    private final DataMap data = DataMap.concurrent(); // global player data
    private final ConcurrentMap<String, NetworkConnection> connections = new ConcurrentHashMap<>();

    public Collection<NetworkConnection> getNetworkConnections() {
        return connections.values();
    }

    /**
     * Get a connection by the channel name
     *
     * @param channelName the channel name
     * @return the connection or null if not found
     */
    public NetworkConnection getConnection(String channelName) {
        return connections.get(channelName);
    }

    /**
     * Add a connection to the player
     *
     * @param c the connection to add
     */
    protected void addConnection(NetworkConnection c) {
        connections.put(c.getChannelName(), c);
    }

    /**
     * Remove a connection from the player
     *
     * @param channelName the channel name
     */
    protected void removeConnection(String channelName) {
        connections.remove(channelName);
    }

    /**
     * Send a message to all connections
     *
     * @param message The message to be sent
     */
    public void send(NetworkMessage message) {
        for (NetworkConnection networkConnection : getNetworkConnections()) {
            try {
                if (networkConnection.isActive()) {
                    networkConnection.send(message);

                } else {
                    log.warn("Connection {0} : {1} is not active, skipping broadcast.",
                            id, networkConnection.getChannelName());
                }
            } catch (Exception ex) {
                log.error("Failed to send message to player {0}: {1}", id, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Send a message to a specific channel
     *
     * @param channelName The channel name to send the message to
     * @param message     The message to be sent
     * @return true if the message was sent, false otherwise
     */
    public boolean send(String channelName, NetworkMessage message) {
        var networkConnection = connections.get(channelName);
        if (networkConnection != null && networkConnection.isActive()) {
            try {
                networkConnection.send(message);
                return true;

            } catch (Exception ex) {
                log.error("Failed to send message to player {0}: {1}", id, ex.getMessage(), ex);
            }
        } else {
            log.warn("Player {0} not connected on channel {1}.", id, channelName);
        }

        return false;
    }
}
