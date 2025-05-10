package org.pixel.network.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.network.io.SocketAddress;

@RequiredArgsConstructor
@Getter
public class NetworkAuthenticatorContext {
    private final String channelName;
    private final SocketAddress remoteAddress;
}
