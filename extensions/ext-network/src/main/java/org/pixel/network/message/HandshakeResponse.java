package org.pixel.network.message;

public class HandshakeResponse extends KeyValueMessage {
    public HandshakeResponse() {
        super(NetworkMessageType.HANDSHAKE_RESPONSE);
    }
}