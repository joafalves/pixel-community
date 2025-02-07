package org.pixel.network.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SocketAddressTest {

    @Test
    public void equalsTest() {
        SocketAddress socketAddressA = new SocketAddress("localhost", 8080);
        SocketAddress socketAddressB = new SocketAddress("localhost", 8080);

        Assertions.assertEquals(socketAddressA, socketAddressB);
        Assertions.assertEquals(socketAddressA.hashCode(), socketAddressB.hashCode());
        Assertions.assertEquals(socketAddressA.toString(), socketAddressB.toString());
    }
}
