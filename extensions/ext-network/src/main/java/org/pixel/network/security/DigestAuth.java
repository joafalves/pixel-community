package org.pixel.network.security;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigestAuth implements Auth {
    // Matches key="value" or key=value
    private static final Pattern PARAM_PATTERN = Pattern.compile("(\\w+)=\\s*(?:\"([^\"]*)\"|([^,\\s]+))");

    private final String username;
    private final String realm;
    private final String nonce;
    private final String uri;
    private final String response;
    private final String nc;
    private final String cnonce;
    private final String qop;
    private final String algorithm;
    private final String opaque;

    public DigestAuth(String username,
                      String realm,
                      String nonce,
                      String uri,
                      String response,
                      String nc,
                      String cnonce,
                      String qop,
                      String algorithm,
                      String opaque) {
        this.username = username;
        this.realm = realm;
        this.nonce = nonce;
        this.uri = uri;
        this.response = response;
        this.nc = nc;
        this.cnonce = cnonce;
        this.qop = qop;
        this.algorithm = algorithm;
        this.opaque = opaque;
    }

    @Override
    public String getPlayerId() {
        return username;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("digest");
        appendParam(sb, "username", username);
        appendParam(sb, "realm", realm);
        appendParam(sb, "nonce", nonce);
        appendParam(sb, "uri", uri);
        appendParam(sb, "response", response);
        if (nc != null) appendRaw(sb, "nc", nc);
        appendParam(sb, "cnonce", cnonce);
        appendParam(sb, "qop", qop);
        if (algorithm != null) appendRaw(sb, "algorithm", algorithm);
        appendParam(sb, "opaque", opaque);
        return sb.toString();
    }

    private void appendParam(StringBuilder sb, String key, String value) {
        if (value != null) {
            sb.append(" ").append(key).append("=\"").append(value).append("\",");
        }
    }

    private void appendRaw(StringBuilder sb, String key, String value) {
        sb.append(" ").append(key).append("=").append(value).append(",");
    }

    public static DigestAuth fromString(String value) {
        if (value == null || !value.trim().toLowerCase().startsWith("digest")) {
            throw new IllegalArgumentException("Not a digest auth header");
        }
        String paramsPart = value.trim().substring(6).trim();
        Matcher m = PARAM_PATTERN.matcher(paramsPart);
        Map<String, String> params = new HashMap<>();
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(2) != null ? m.group(2) : m.group(3);
            params.put(key, val);
        }
        return new DigestAuth(
                params.get("username"),
                params.get("realm"),
                params.get("nonce"),
                params.get("uri"),
                params.get("response"),
                params.get("nc"),
                params.get("cnonce"),
                params.get("qop"),
                params.get("algorithm"),
                params.get("opaque")
        );
    }
}