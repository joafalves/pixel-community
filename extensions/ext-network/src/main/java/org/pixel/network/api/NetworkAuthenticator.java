package org.pixel.network.api;

public interface NetworkAuthenticator {

    /**
     * Authenticate the user with the given username and password.
     *
     * @param username The username.
     * @param password The password.
     * @param context  The context of the authentication request, which may contain additional information.
     * @return A NetworkPlayer containing the user info and data if authentication is successful, null otherwise.
     */
    NetworkAuthenticatorResponse authenticate(String username, String password, NetworkAuthenticatorContext context);
}
