package org.pixel.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.dsnp.NetworkMessage;
import org.pixel.network.dsnp.NetworkMessageType;

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
            out.add(new NetworkMessage(type, payload));

        } catch (IllegalArgumentException e) {
            log.error("Invalid message type: 0x" + Integer.toHexString(typeValue));
            ctx.close(); // TODO: Close the connection?
        }
    }
}