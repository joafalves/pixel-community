package org.pixel.network.io;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
public class SocketAddress implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String host;
    private final int port;

    /**
     * Constructor
     *
     * @param host - The host address
     * @param port - The port number
     */
    public SocketAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static SocketAddress from(java.net.SocketAddress socketAddress) {
        if (socketAddress instanceof java.net.InetSocketAddress inetSocketAddress) {
            return new SocketAddress(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        }
        throw new IllegalArgumentException("Unsupported socket address type: " + socketAddress.getClass());
    }
}