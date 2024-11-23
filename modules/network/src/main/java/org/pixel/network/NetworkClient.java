package org.pixel.network;

public abstract class NetworkClient {
    protected String serverAddress;
    protected int serverPort;

    public NetworkClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public abstract void connect() throws Exception;

    public abstract void disconnect();

    public abstract void send(byte[] data);
}
