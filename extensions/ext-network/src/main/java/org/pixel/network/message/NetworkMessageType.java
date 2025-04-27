package org.pixel.network.message;

import lombok.Getter;

@Getter
public enum NetworkMessageType {
    HANDSHAKE_REQUEST((byte) 0x01),
    HANDSHAKE_RESPONSE((byte) 0x02),
    DATA((byte) 0xA0),
    HEARTBEAT((byte) 0xF0),
    DISCONNECT((byte) 0xFA);

    private final byte value;

    NetworkMessageType(byte value) {
        this.value = value;
    }

    public static NetworkMessageType fromValue(byte value) {
        for (NetworkMessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}