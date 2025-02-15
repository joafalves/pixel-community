package org.pixel.network.message;

public class DataMessage extends NetworkMessage {
    private final byte[] payload;

    public DataMessage(NetworkMessageType type, byte[] payload) {
        super(NetworkMessageType.DATA);
        this.payload = payload;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}