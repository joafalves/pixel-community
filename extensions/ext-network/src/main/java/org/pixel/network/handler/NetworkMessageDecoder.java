package org.pixel.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.message.*;

import java.util.List;

public class NetworkMessageDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(NetworkMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < NetworkMessage.HEADER_SIZE) {
            return; // Need more data
        }

        in.markReaderIndex();

        // Read magic header
        short magic = in.readShort();
        if (magic != NetworkMessage.MAGIC_HEADER) {
            in.resetReaderIndex();
            throw new IllegalStateException("Invalid magic header: 0x" + Integer.toHexString(magic));
        }

        // Read message type and payload length
        byte typeValue = in.readByte();
        int payloadLength = in.readInt();

        // Check if the complete payload is available
        if (in.readableBytes() < payloadLength) {
            // TODO: support partial / streaming of message payloads (e.g. large files)
            in.resetReaderIndex();
            return; // Need more data
        }

        // Read payload
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);

        try {
            NetworkMessageType type = NetworkMessageType.fromValue(typeValue);
            NetworkMessage message = createMessage(type, payload);
            out.add(message);

        } catch (IllegalArgumentException e) {
            log.error("Invalid message type: 0x" + Integer.toHexString(typeValue));
            ctx.close(); // TODO: Close the connection?
        }
    }

    private NetworkMessage createMessage(NetworkMessageType type, byte[] payload) {
        log.trace("Creating message: type={0}.", type);

        return switch (type) {
            case DATA -> new DataMessage(type, payload);
            case HANDSHAKE_REQUEST, HANDSHAKE_RESPONSE, HEARTBEAT, DISCONNECT -> createKeyValueMessage(type, payload);
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };
    }

    private KeyValueMessage createKeyValueMessage(NetworkMessageType type, byte[] payload) {
        KeyValueMessage message = switch (type) {
            case HANDSHAKE_REQUEST -> new HandshakeRequest();
            case HANDSHAKE_RESPONSE -> new HandshakeResponse();
            case HEARTBEAT -> new HeartbeatMessage();
            case DISCONNECT -> new DisconnectMessage();
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };

        if (payload.length > 0) {
            String content = new String(payload);
            String[] pairs = content.split(";");
            for (String pair : pairs) {
                if (!pair.isEmpty()) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        log.trace("Parsed key-value pair: {0}={1}.", keyValue[0], keyValue[1]);
                        message.add(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return message;
    }
}