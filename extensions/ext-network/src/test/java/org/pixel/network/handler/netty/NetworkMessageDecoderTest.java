package org.pixel.network.handler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.pixel.network.message.DataMessage;
import org.pixel.network.message.NetworkMessage;
import org.pixel.network.message.NetworkMessageType;

import static org.junit.jupiter.api.Assertions.*;

class NetworkMessageDecoderTest {

    private static ByteBuf header(byte type, int payloadLength) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(NetworkMessage.MAGIC_HEADER);
        buf.writeByte(type);
        buf.writeInt(payloadLength);
        return buf;
    }

    @Test
    void decodesValidMessage() {
        EmbeddedChannel channel = new EmbeddedChannel(new NetworkMessageDecoder());
        ByteBuf buf = header(NetworkMessageType.DATA.getValue(), 3);
        buf.writeBytes(new byte[]{1, 2, 3});

        assertTrue(channel.writeInbound(buf));
        Object decoded = channel.readInbound();
        assertInstanceOf(DataMessage.class, decoded);
        assertTrue(channel.isOpen());
    }

    @Test
    void closesConnectionOnOversizedPayloadLength() {
        EmbeddedChannel channel = new EmbeddedChannel(new NetworkMessageDecoder(1024));
        channel.writeInbound(header(NetworkMessageType.DATA.getValue(), Integer.MAX_VALUE));

        assertNull(channel.readInbound());
        assertFalse(channel.isOpen());
    }

    @Test
    void closesConnectionOnNegativePayloadLength() {
        EmbeddedChannel channel = new EmbeddedChannel(new NetworkMessageDecoder());
        channel.writeInbound(header(NetworkMessageType.DATA.getValue(), -1));

        assertNull(channel.readInbound());
        assertFalse(channel.isOpen());
    }

    @Test
    void closesConnectionOnInvalidMagic() {
        EmbeddedChannel channel = new EmbeddedChannel(new NetworkMessageDecoder());
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(0x1234);
        buf.writeByte(0);
        buf.writeInt(0);
        channel.writeInbound(buf);

        assertFalse(channel.isOpen());
    }
}
