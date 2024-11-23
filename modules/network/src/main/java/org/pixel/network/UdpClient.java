package org.pixel.network;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.net.*;

public class UdpClient extends NetworkClient {
    private static final Logger log = LoggerFactory.getLogger(UdpClient.class);
    private DatagramSocket socket;

    public UdpClient(String serverAddress, int serverPort) {
        super(serverAddress, serverPort);
    }

    @Override
    public void connect() throws Exception {
        socket = new DatagramSocket();
    }

    @Override
    public void disconnect() {
        socket.close();
    }

    @Override
    public void send(byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    InetAddress.getByName(serverAddress), serverPort);
            socket.send(packet);
        } catch (Exception e) {
            log.error("Error sending data", e);
        }
    }
}
