package org.pixel.network.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.util.TextHelper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
@Getter
public class BasicAuth implements Auth {

    private final String username;
    private final String password;

    @Override
    public String getPlayerId() {
        return username;
    }

    @Override
    public String toString() {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "basic " + encoded;
    }

    public static BasicAuth fromString(String value) throws IllegalArgumentException {
        value = value.trim();
        if (!value.toLowerCase().startsWith("basic ")) {
            throw new IllegalArgumentException("Not a basic auth header");
        }
        String[] split = value.split(" ");
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid basic auth header format");
        }

        String[] parts = TextHelper.decodeBase64(split[1]).split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid basic credentials format");
        }
        return new BasicAuth(parts[0], parts[1]);
    }
}
