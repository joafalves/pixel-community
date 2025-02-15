package org.pixel.network.message;

public class HandshakeRequest extends KeyValueMessage {

    public HandshakeRequest() {
        super(NetworkMessageType.HANDSHAKE_REQUEST);
    }
}
