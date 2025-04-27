package org.pixel.network.message;

public class HandshakeRequest extends KeyValueMessage {

    private static final String KEY_VER = "ver";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_AUTH = "auth";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_NONCE = "nonce";
    private static final String KEY_CNONCE = "cnonce";
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_QOP = "qop";
    private static final String KEY_NC = "nc";

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

    public String getAuthMethod() {
        return get(KEY_AUTH);
    }

    public void setAuthMethod(String authMethod) {
        add(KEY_AUTH, authMethod);
    }

    public String getUsername() {
        return get(KEY_USERNAME);
    }

    public void setUsername(String username) {
        add(KEY_USERNAME, username);
    }

    public String getPassword() {
        return get(KEY_PASSWORD);
    }

    public void setPassword(String password) {
        add(KEY_PASSWORD, password);
    }

    public String getNonce() {
        return get(KEY_NONCE);
    }

    public void setNonce(String nonce) {
        add(KEY_NONCE, nonce);
    }

    public String getCnonce() {
        return get(KEY_CNONCE);
    }

    public void setCnonce(String cnonce) {
        add(KEY_CNONCE, cnonce);
    }

    public String getResponse() {
        return get(KEY_RESPONSE);
    }

    public void setResponse(String response) {
        add(KEY_RESPONSE, response);
    }

    public String getQop() {
        return get(KEY_QOP);
    }

    public void setQop(String qop) {
        add(KEY_QOP, qop);
    }

    public String getNonceCount() {
        return get(KEY_NC);
    }

    public void setNonceCount(String nonceCount) {
        add(KEY_NC, nonceCount);
    }
}
