package org.pixel.network;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.*;
import java.net.*;

public class TcpClient extends NetworkClient {
    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    private Socket socket;
    private OutputStream out;

    public TcpClient(String serverAddress, int serverPort) {
        super(serverAddress, serverPort);
    }

    @Override
    public void connect() throws Exception {
        socket = new Socket(serverAddress, serverPort);
        out = socket.getOutputStream();
    }

    @Override
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("Error closing client socket", e);
        }
    }

    @Override
    public void send(byte[] data) {
        try {
            out.write(data);
        } catch (IOException e) {
            log.error("Error closing client socket", e);
        }
    }
}
