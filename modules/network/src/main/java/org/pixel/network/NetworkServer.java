package org.pixel.network;

public abstract class NetworkServer {
    protected ConnectionListener listener;

    public NetworkServer(ConnectionListener listener) {
        this.listener = listener;
    }

    public abstract void start(int port) throws Exception;

    public abstract void stop();

    public abstract void broadcast(byte[] data);
}
