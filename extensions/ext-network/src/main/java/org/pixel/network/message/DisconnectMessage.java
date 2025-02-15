package org.pixel.network.message;

public class DisconnectMessage extends KeyValueMessage {
    public DisconnectMessage() {
        super(NetworkMessageType.DISCONNECT);
    }
}