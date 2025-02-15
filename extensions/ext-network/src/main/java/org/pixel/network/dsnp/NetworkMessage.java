package org.pixel.network.dsnp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
@AllArgsConstructor
public class NetworkMessage {
    public static final short MAGIC_HEADER = (short) 0xDEAD;
    public static final int HEADER_SIZE = 7; // 2 (magic) + 1 (type) + 4 (length)

    private NetworkMessageType type;
    private byte[] payload;

    public int getPayloadLength() {
        return payload != null ? payload.length : 0;
    }

    public byte[] toBytes() {
        int payloadLength = getPayloadLength();
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payloadLength);
        buffer.putShort(MAGIC_HEADER);
        buffer.put(type.getValue());
        buffer.putInt(payloadLength);
        if (payload != null) {
            buffer.put(payload);
        }
        return buffer.array();
    }
}