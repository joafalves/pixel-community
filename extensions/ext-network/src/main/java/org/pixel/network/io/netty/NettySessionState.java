package org.pixel.network.io.netty;

/**
 * Enum representing the state of the session:
 * NEW - The session is newly created and not yet active.
 * ACTIVE - The session is active and can be used for communication (e.g. was handshaked / authenticated).
 * INACTIVE - The session is inactive and cannot be used for communication (e.g. was closed or blocked).
 */
public enum NettySessionState {
    NEW,
    ACTIVE,
    INACTIVE,
}
