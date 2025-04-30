package org.pixel.network.data;

import lombok.Builder;
import lombok.Data;
import org.pixel.commons.data.DataMap;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class NetworkSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private SocketAddress userAddress;
    private DataMap userData;
    @Builder.Default
    private State state = State.NEW;
    private String id;
    private String scope;
    private long lastRemoteActivity;

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    /**
     * Enum representing the state of the session:
     * NEW - The session is newly created and not yet active.
     * ACTIVE - The session is active and can be used for communication (e.g. was handshaked / authenticated).
     * INACTIVE - The session is inactive and cannot be used for communication (e.g. was closed or blocked).
     */
    public enum State {
        NEW,
        ACTIVE,
        INACTIVE,
    }
}