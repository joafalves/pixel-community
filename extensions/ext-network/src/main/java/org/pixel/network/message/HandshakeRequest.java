package org.pixel.network.message;

public class HandshakeRequest extends KeyValueMessage {

    private static final String KEY_VER = "ver";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_AUTH = "auth";

    public HandshakeRequest() {
        super(NetworkMessageType.HANDSHAKE_REQUEST);
    }

    public String getVersion() {
        return get(KEY_VER);
    }

    public void setVersion(String version) {
        add(KEY_VER, version);
    }

    public String getScope() {
        return get(KEY_SCOPE);
    }

    public void setScope(String scope) {
        add(KEY_SCOPE, scope);
    }

    public String getAuth() {
        return get(KEY_AUTH);
    }

    public void setAuth(String auth) {
        add(KEY_AUTH, auth);
    }
}
