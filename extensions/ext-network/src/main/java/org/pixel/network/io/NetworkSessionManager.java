package org.pixel.network.io;

import lombok.RequiredArgsConstructor;
import org.pixel.network.message.NetworkMessage;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class NetworkSessionManager {
    // network sessionId → Connection
    private final ConcurrentMap<String, NetworkConnection> connectionsById = new ConcurrentHashMap<>();
    // playerId → NetworkPlayer
    private final ConcurrentMap<String, NetworkPlayer> playersById = new ConcurrentHashMap<>();

    /**
     * Get the number of connections
     *
     * @return the number of connections
     */
    public int getConnectionCount() {
        return connectionsById.size();
    }

    /**
     * Get the number of players
     *
     * @return the number of players
     */
    public int getPlayerCount() {
        return playersById.size();
    }

    /**
     * Get a connection by its id
     *
     * @param connectionId the connection id
     * @return the connection or null if not found
     */
    public NetworkConnection getConnection(String connectionId) {
        return connectionsById.get(connectionId);
    }

    /**
     * Called after successful handshake/auth
     *
     * @return the player
     */
    public NetworkPlayer addConnection(NetworkConnection connection) {
        connectionsById.put(connection.getConnectionId(), connection);

        // get-or-create the player, then attach
        var player = playersById.computeIfAbsent(connection.getPlayerId(), id -> {
            var newPlayer = NetworkPlayer.builder()
                    .id(id)
                    .build();
            return newPlayer;
        });
        player.addConnection(connection);

        return player;
    }

    /**
     * Called when a channel closes or idle‐times out
     */
    public void removeConnection(String sessionId) {
        var connection = connectionsById.remove(sessionId);
        if (connection == null) return;

        final NetworkPlayer player = playersById.get(connection.getPlayerId());
        if (player == null) return;

        synchronized (player) {
            player.removeConnection(connection.getChannelName());
            if (player.getConnections().isEmpty()) {
                playersById.remove(player.getId());
            }
        }
    }

    /**
     * Get all players (each with their live connections)
     */
    public Collection<NetworkPlayer> getPlayers() {
        return playersById.values();
    }

    /**
     * Get a player by their ID
     *
     * @param playerId the player id
     * @return the player or null if not found
     */
    public NetworkPlayer getPlayer(String playerId) {
        return playersById.get(playerId);
    }

    /**
     * Get a player by their connection ID
     *
     * @param connectionId the connection id
     * @return the player or null if not found
     */
    public NetworkPlayer getPlayerByConnectionId(String connectionId) {
        var connection = connectionsById.get(connectionId);
        if (connection == null) return null;
        return playersById.get(connection.getPlayerId());
    }

    /**
     * Send a message to a single player (all their connections)
     */
    public boolean send(NetworkPlayer player, NetworkMessage msg) {
        boolean sent = false;
        for (var con : player.getConnections().values()) {
            if (con.isActive()) {
                con.send(msg);
                sent = true;
            }
        }
        return sent;
    }

    /**
     * Broadcast to every connection on every player
     */
    public void broadcast(NetworkMessage msg) {
        connectionsById.values().forEach(c -> {
            if (c.isActive()) c.send(msg);
        });
    }
}
