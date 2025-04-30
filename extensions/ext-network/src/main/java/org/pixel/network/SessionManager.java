package org.pixel.network;

import org.pixel.network.data.NetworkSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // Session map where the key is the session token and the value is the session object
    private final Map<String, NetworkSession> sessions = new ConcurrentHashMap<>();

    /**
     * Add session to the session manager.
     *
     * @param session - session object
     * @return session object if there was a session with the same token, null otherwise
     */
    public NetworkSession addSession(NetworkSession session) {
        return sessions.put(session.getId(), session);
    }

    /**
     * Remove session from the session manager.
     *
     * @param session - session object
     * @return session object if there was a session with the same token, null otherwise
     */
    public NetworkSession removeSession(NetworkSession session) {
        return sessions.remove(session.getId());
    }

    /**
     * Get session by token.
     *
     * @param token - session token
     * @return session object if there was a session with the same token, null otherwise
     */
    public NetworkSession getSession(String token) {
        return sessions.get(token);
    }
}
