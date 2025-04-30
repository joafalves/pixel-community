package org.pixel.network.api;

import org.pixel.commons.data.DataMap;
import org.pixel.network.data.SocketAddress;

public interface NetworkAuthenticator {

    /**
     * Authenticate the user with the given username and password.
     *
     * @param username The username.
     * @param password The password.
     * @param userAddress The network address of the user.
     * @return A DataHashMap containing the user data if authentication is successful, null otherwise.
     */
    DataMap authenticate(String username, String password, SocketAddress userAddress);
}
