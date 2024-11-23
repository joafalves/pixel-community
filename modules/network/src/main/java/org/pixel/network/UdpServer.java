package org.pixel.network;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.net.*;

public class UdpServer extends NetworkServer {
    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);

    private DatagramSocket socket;

    public UdpServer(ConnectionListener listener) {
        super(listener);
    }

    @Override
    public void start(int port) throws Exception {
        socket = new DatagramSocket(port);
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!socket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String clientAddress = packet.getAddress().getHostAddress();
                    listener.onDataReceived(clientAddress, packet.getData());
                } catch (Exception e) {
                    log.error("Error receiving data", e);
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        socket.close();
    }

    @Override
    public void broadcast(byte[] data) {
        throw new UnsupportedOperationException("Broadcasting is not yet supported in UDP");
    }
}
