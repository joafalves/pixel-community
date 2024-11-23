package org.pixel.network;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TcpServer extends NetworkServer {
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<String, Socket> clients;

    public TcpServer(ConnectionListener listener) {
        super(listener);
        this.clients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        threadPool.execute(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    clients.put(clientAddress, clientSocket);

                    listener.onConnect(clientAddress);
                    handleClient(clientSocket, clientAddress);
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        log.error("Error accepting client connection", e);
                    }
                }
            }
        });
    }

    private void handleClient(Socket clientSocket, String clientAddress) {
        threadPool.execute(() -> {
            try (InputStream in = clientSocket.getInputStream()) {
                byte[] buffer = new byte[1024];
                while (in.read(buffer) != -1) {
                    listener.onDataReceived(clientAddress, buffer);
                }
            } catch (IOException e) {
                listener.onDisconnect(clientAddress);
                clients.remove(clientAddress);

                log.error("Error handling client connection", e);
            }
        });
    }

    @Override
    public void stop() {
        try {
            for (Socket socket : clients.values()) {
                socket.close();
            }
            clients.clear();
            serverSocket.close();
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }
    }

    @Override
    public void broadcast(byte[] data) {
        clients.values().forEach(socket -> {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(data);
            } catch (IOException e) {
                log.error("Error broadcasting data to client", e);
            }
        });
    }
}
