package org.pixel.demo.learning.network;

import org.pixel.commons.Color;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.*;
import org.pixel.network.message.HandshakeRequest;

public class SimpleNetworkDemo extends DemoGame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private GameServer gameServer;
    private GameClient gameClient;

    /**
     * Constructor
     *
     * @param settings The game settings.
     */
    public SimpleNetworkDemo(WindowSettings settings) {
        super(settings);
        // set log level:
        ConsoleLogger.setLogLevel(LogLevel.TRACE);
    }

    @Override
    public void load() {
        super.load();

        // Note: This demo contains both the server and clients in the same application for demonstration purposes.

        gameServer = new NettyGameServer(GameServerSettings.builder()
                .bindAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .maxConnections(10)
                .numThreads(5)
                .build());

        gameClient = new NettyGameClient(GameClientSettings.builder()
                .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .build());

        if (!gameServer.init()) {
            throw new RuntimeException("Failed to initialize server");
        }

        if (!gameClient.init()) {
            throw new RuntimeException("Failed to initialize client");
        }

        var handshake = new HandshakeRequest();
        handshake.add("username", "player1");

        gameClient.write(handshake);
        gameClient.write(handshake);
        gameClient.write(handshake);
    }

    @Override
    public void dispose() {
        gameClient.dispose();
        gameServer.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        final WindowSettings settings = new WindowSettings(800, 600);
        settings.setBackgroundColor(Color.INDIGO);
        settings.setTitle("Chat Demo");
        settings.setTargetFps(60);

        var game = new SimpleNetworkDemo(settings);
        game.start();
    }
}
