package org.pixel.network.io.netty.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.network.io.NetworkConnection;

@RequiredArgsConstructor
@Getter
public class HandshakeSuccessEvent {
    private final NetworkConnection connection;
}
