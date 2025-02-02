package org.pixel.network;

import org.pixel.network.command.Command;

public abstract class Client {

    protected String localHost;
    protected int localPort;

    /**
     * Initialize the client
     */
    abstract public boolean start();

    /**
     * Stop the client
     */
    abstract public void stop();

    /**
     * Send a command to the server
     *
     * @param remoteHost - remote host to send the command to
     * @param remotePort - remote port to send the command to
     * @param command    - command to send
     */
    abstract void send(String remoteHost, int remotePort, Command command);
}
