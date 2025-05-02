package org.pixel.network.handler.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.pixel.network.message.HandshakeResponse;

public class HandshakeResponseHandler extends SimpleChannelInboundHandler<HandshakeResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeResponse response) throws Exception {

    }
}
