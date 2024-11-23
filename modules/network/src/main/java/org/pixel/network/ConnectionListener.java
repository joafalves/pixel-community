package org.pixel.network;

public interface ConnectionListener {
    void onConnect(String clientAddress);
    void onDisconnect(String clientAddress);
    void onDataReceived(String clientAddress, byte[] data);
}
