package org.pixel.network.data;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class Session implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionToken;
    private String username;
    private long lastActivity;
}