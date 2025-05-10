package org.pixel.network.message;

public class HandshakeResponse extends KeyValueMessage {

    private static final String KEY_STATUS = "status";
    private static final String KEY_REASON = "reason";
    private static final String KEY_HEARTBEAT = "heartbeat";
    private static final String KEY_AUTH = "auth";

    public HandshakeResponse() {
        super(NetworkMessageType.HANDSHAKE_RESPONSE);
    }

    public String getStatus() {
        return get(KEY_STATUS);
    }

    public void setStatus(String status) {
        add(KEY_STATUS, status);
    }

    public String getReason() {
        return get(KEY_REASON);
    }

    public void setReason(String reason) {
        add(KEY_REASON, reason);
    }

    public int getHeartbeat() {
        return Integer.parseInt(get(KEY_HEARTBEAT));
    }

    public void setHeartbeat(int heartbeat) {
        add(KEY_HEARTBEAT, String.valueOf(heartbeat));
    }

    public String getAuth() {
        return get(KEY_AUTH);
    }

    public void setAuth(String auth) {
        add(KEY_AUTH, auth);
    }
}