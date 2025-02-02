package org.pixel.demo.learning.network;

import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.network.NettyUdpClient;
import org.pixel.network.NettyUdpServer;
import org.pixel.network.command.HelloCommand;

public class SimpleNetworkDemo extends DemoGame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private static final NettyUdpServer server;
    private static final NettyUdpClient client;

    static {
        server = new NettyUdpServer(SERVER_HOST, SERVER_PORT);
        if (!server.start()) {
            System.out.println("Failed to start server");
        }

        client = new NettyUdpClient(); // ephemeral port
        if (!client.start()) {
            System.out.println("Failed to start client");
        }

        client.send(SERVER_HOST, SERVER_PORT,
                HelloCommand.builder()
                        .username("admin")
                        .password("admin")
                        .build());
    }

    /**
     * Constructor
     *
     * @param settings The game settings.
     */
    public SimpleNetworkDemo(WindowSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        final WindowSettings settings = new WindowSettings(800, 600);
        settings.setTitle("Chat Demo");
        settings.setTargetFps(60);

        var game = new SimpleNetworkDemo(settings);
        game.start();
    }
}
