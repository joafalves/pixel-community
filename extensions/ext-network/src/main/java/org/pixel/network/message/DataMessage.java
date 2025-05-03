package org.pixel.network.message;

public class DataMessage extends NetworkMessage {
    private final byte[] payload;

    public DataMessage(byte[] payload) {
        super(NetworkMessageType.DATA);
        this.payload = payload;
    }

    public DataMessage(String payload) {
        super(NetworkMessageType.DATA);
        this.payload = payload.getBytes();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}