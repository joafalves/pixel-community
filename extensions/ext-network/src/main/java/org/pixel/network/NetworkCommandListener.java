package org.pixel.network;

import org.pixel.network.command.Command;
import java.net.InetSocketAddress;

/**
 * Listener interface for incoming network commands.
 */
public interface NetworkCommandListener {
    /**
     * Called when a command is received.
     *
     * @param command the decoded NetworkCommand
     * @param sender  the address of the sender
     */
    void onCommand(Command command, InetSocketAddress sender);
}