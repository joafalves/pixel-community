package org.pixel.network.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.network.message.NetworkMessage;

@RequiredArgsConstructor
@Getter
public class NetworkRequest {

    private final NetworkMessage message;
}
