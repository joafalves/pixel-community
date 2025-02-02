package org.pixel.network;

import org.pixel.network.command.Command;

import java.util.ArrayList;
import java.util.List;

public abstract class Server {
    protected String localHost;
    protected int localPort;

    protected final List<NetworkCommandListener> listeners = new ArrayList<>();

    /**
     * Start the server
     */
    abstract public boolean start();

    /**
     * Stop the server
     */
    abstract public void stop();

    /**
     * Broadcast a command to all connected clients
     *
     * @param command - command to broadcast
     */
    abstract public void broadcast(Command command);

    /**
     * Add a listener to the server
     *
     * @param listener - listener to add
     */
    public void addListener(NetworkCommandListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the server
     *
     * @param listener - listener to remove
     */
    public void removeListener(NetworkCommandListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of a command
     *
     * @param command - command to notify
     */
    protected void notifyListeners(Command command) {
        listeners.forEach(listener -> listener.onCommand(command, null));
    }
}
