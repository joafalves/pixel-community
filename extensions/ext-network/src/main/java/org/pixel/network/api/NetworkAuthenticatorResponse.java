package org.pixel.network.api;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.data.DataMap;

@Builder
@RequiredArgsConstructor
@Getter
public class NetworkAuthenticatorResponse {
    /**
     * The global identifier of the player.
     * This is used to identify the player across all connections.
     */
    private final String playerId;
    @Builder.Default
    private final boolean success = true;
    @Builder.Default
    private final String message = null;
    @Builder.Default
    private final DataMap data = DataMap.concurrent();
}
