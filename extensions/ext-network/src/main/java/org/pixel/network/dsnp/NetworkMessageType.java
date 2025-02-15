package org.pixel.network.dsnp;

import lombok.Getter;

@Getter
public enum NetworkMessageType {
    HANDSHAKE_REQUEST((byte) 0x01),
    HANDSHAKE_RESPONSE((byte) 0x02),
    DATA((byte) 0x03),
    HEARTBEAT((byte) 0x04),
    DISCONNECT((byte) 0x05);

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