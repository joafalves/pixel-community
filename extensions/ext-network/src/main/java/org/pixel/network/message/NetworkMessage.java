package org.pixel.network.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
@AllArgsConstructor
public abstract class NetworkMessage {
    public static final short MAGIC_HEADER = (short) 0xDEAD;
    public static final int HEADER_SIZE = 7; // 2 (magic) + 1 (type) + 4 (length)

    private NetworkMessageType type;

    public abstract byte[] getPayload();

    public byte[] toBytes() {
        byte[] payload = getPayload();
        int payloadLength = payload != null ? payload.length : 0;
        byte[] message = new byte[HEADER_SIZE + payloadLength];

        // Magic header (2 bytes)
        message[0] = (byte) (MAGIC_HEADER >> 8);
        message[1] = (byte) MAGIC_HEADER;

        // Message type (1 byte)
        message[2] = type.getValue();

        // Payload length (4 bytes)
        message[3] = (byte) (payloadLength >> 24);
        message[4] = (byte) (payloadLength >> 16);
        message[5] = (byte) (payloadLength >> 8);
        message[6] = (byte) payloadLength;

        // Payload
        if (payloadLength > 0) {
            System.arraycopy(payload, 0, message, HEADER_SIZE, payloadLength);
        }

        return message;
    }
}