package org.pixel.network;

import lombok.RequiredArgsConstructor;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.network.data.SocketAddress;
import org.pixel.pipeline.DataPipelineFactory;

@RequiredArgsConstructor
public abstract class NetworkChannel implements Disposable {

    protected final DataPipelineFactory<?> upstreamPipelineFactory;
    protected final DataPipelineFactory<?> downstreamPipelineFactory;

    /**
     * Bind the network channel to the specified address. You can bind multiple times to different addresses.
     *
     * @param address The address to bind to.
     * @return The address that was successfully bound to, or null if the binding failed.
     */
    public abstract SocketAddress bind(SocketAddress address);

    /**
     * Send the data to the specified address.
     *
     * @param data          the data to send
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @return true if the data was successfully sent, false otherwise
     */
    public abstract boolean send(byte[] data, SocketAddress localAddress, SocketAddress remoteAddress);
}