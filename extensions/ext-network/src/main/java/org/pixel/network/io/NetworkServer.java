package org.pixel.network.io;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.message.NetworkMessage;

import java.io.IOException;

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
     * Write message to the player
     *
     * @param player  The player to send the message to
     * @param message The message to be sent
     * @return True if the message was queued for sending
     * @throws IOException - If the message could not be sent
     */
    public abstract boolean send(NetworkPlayer player, NetworkMessage message) throws IOException;

    /**
     * Get the current network state
     *
     * @return True if the server is bound and listening
     */
    public abstract boolean isActive();

    /**
     * Get the number of active connections
     *
     * @return The number of active connections
     */
    public abstract int getConnectionCount();
}
