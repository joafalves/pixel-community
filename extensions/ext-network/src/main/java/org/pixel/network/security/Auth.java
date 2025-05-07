package org.pixel.network.security;

public interface Auth {
    /**
     * Get the unique identifier for the user associated with this authentication.
     * This identifier is used to track the user across different sessions, so it MUST be the same for the same user
     * regardless of the authentication method used.
     *
     * @return The unique global identifier for the user.
     */
    String getPlayerId();
}
