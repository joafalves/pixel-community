package org.pixel.network;

import org.pixel.network.data.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // Session map where the key is the session token and the value is the session object
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Add session to the session manager.
     *
     * @param session - session object
     * @return session object if there was a session with the same token, null otherwise
     */
    public Session addSession(Session session) {
        return sessions.put(session.getSessionToken(), session);
    }

    /**
     * Remove session from the session manager.
     *
     * @param session - session object
     * @return session object if there was a session with the same token, null otherwise
     */
    public Session removeSession(Session session) {
        return sessions.remove(session.getSessionToken());
    }

    /**
     * Get session by token.
     *
     * @param token - session token
     * @return session object if there was a session with the same token, null otherwise
     */
    public Session getSession(String token) {
        return sessions.get(token);
    }
}
