package org.pixel.network;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.data.SocketAddress;

@Builder
@Getter
public class GameClientSettings {
    private final SocketAddress remoteAddress;
}