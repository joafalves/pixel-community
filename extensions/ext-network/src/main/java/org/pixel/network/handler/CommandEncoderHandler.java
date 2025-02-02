package org.pixel.network.handler;

import io.netty.channel.*;
import org.pixel.network.DataSerializer;

@ChannelHandler.Sharable
public class CommandEncoderHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Serialize the object and continue the flow (useDataSerializer class)
        super.write(ctx, DataSerializer.serialize(msg), promise);
    }
}
