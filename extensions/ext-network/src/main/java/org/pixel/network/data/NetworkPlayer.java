package org.pixel.network.data;

import lombok.Builder;
import lombok.Data;
import org.pixel.commons.data.DataMap;

@Builder
@Data
public class NetworkPlayer {
    // The unique global identifier for the player
    private final String id;
    // The user data associated with the player (provided by the authenticator)
    private DataMap data;
}
