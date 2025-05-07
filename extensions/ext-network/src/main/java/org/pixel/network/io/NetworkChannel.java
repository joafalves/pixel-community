package org.pixel.network.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.network.NetworkTransport;

@RequiredArgsConstructor
@Getter
public class NetworkChannel {
    private final String name;
    private final NetworkTransport transport;
    private final SocketAddress remoteAddress;
}
