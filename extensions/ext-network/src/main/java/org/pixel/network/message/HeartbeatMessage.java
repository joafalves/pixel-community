package org.pixel.network.message;

public class HeartbeatMessage extends KeyValueMessage {
    public HeartbeatMessage() {
        super(NetworkMessageType.HEARTBEAT);
    }
}