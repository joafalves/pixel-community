package org.pixel.network.io;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.network.message.NetworkMessage;

import java.util.Collection;

@Setter
@Getter
public abstract class NetworkServer implements Initializable, Disposable {

    protected final NetworkServerSettings settings;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    protected NetworkServer(NetworkServerSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null.");
        }

        if (settings.getBindAddress() == null) {
            throw new IllegalArgumentException("Bind address cannot be null.");
        }

        if (settings.getBindAddress().getPort() <= 0) {
            throw new IllegalArgumentException("Bind port must be greater than 0.");
        }

        if (settings.getServerListener() == null) {
            throw new IllegalArgumentException("Event listener cannot be null.");
        }

        this.settings = settings;
    }

    /**
     * Get all players connected to the server
     *
     * @return The list of players connected to the server
     */
    public abstract Collection<NetworkPlayer> getPlayers();

    /**
     * Get all players connected to a specific channel
     *
     * @param channelName The channel name to get the players from
     * @return The list of players connected to the channel
     */
    public abstract Collection<NetworkPlayer> getPlayers(String channelName);

    /**
     * Write a message to all players to all channels
     *
     * @param message The message to be sent
     */
    public abstract void broadcast(NetworkMessage message);

    /**
     * Write a message to all players on a specific channel
     *
     * @param channelName The channel name to send the message to
     * @param message     The message to be sent
     */
    public abstract void broadcast(String channelName, NetworkMessage message);

    /**
     * Get the current network state
     *
     * @return True if the server is bound and listening
     */
    public abstract boolean isActive();

    /**
     * Get the number of active connections - a single player might have multiple connections
     *
     * @return The number of active connections
     */
    public abstract int getConnectionCount();

    /**
     * Get the number of players connected to the server
     *
     * @return The number of players connected to the server
     */
    public abstract int getPlayerCount();

}
