package org.pixel.network;

import lombok.Getter;

@Getter
public enum NetworkTransport {
    TCP("tcp"),
    TLS("tls"),
    UDP("udp");

    private final String name;

    NetworkTransport(String name) {
        this.name = name;
    }

    public static NetworkTransport fromName(String name) {
        for (NetworkTransport transport : values()) {
            if (transport.name.equalsIgnoreCase(name)) {
                return transport;
            }
        }
        throw new IllegalArgumentException("Unknown transport: " + name);
    }
}

// TODO: - ADD NETWORK CHANNELS CONFIGS ON CLIENT
// TODO: SUPPORT BOTH TCP AND TLS SAME TIME ON SERVER
// TODO: - Change the settings to consider NetworkTrasport