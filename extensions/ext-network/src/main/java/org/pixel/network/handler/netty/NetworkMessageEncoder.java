package org.pixel.network.handler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.pixel.network.message.NetworkMessage;

public class NetworkMessageEncoder extends MessageToByteEncoder<NetworkMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NetworkMessage msg, ByteBuf out) {
        // Write the message:
        out.writeBytes(msg.toBytes());
    }
}