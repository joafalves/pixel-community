package org.pixel.demo.learning.network;

import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;

public class SimpleNetworkDemo extends DemoGame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    /*private static final UdpServer networkServer;
    private static final UdpClient networkClient;
    private static final Thread networkThread;

    static {
        networkServer = new UdpServer(SERVER_HOST, SERVER_PORT);
        networkServer.setAuthenticationResolver(new PermissiveAuthenticationResolver());
        if (!networkServer.start()) {
            System.out.println("Failed to start server");
        }

        networkClient = new UdpClient(); // ephemeral port
        if (!networkClient.start()) {
            System.out.println("Failed to start client");
        }

        networkThread = new Thread(() -> {
            networkClient.send(SERVER_HOST, SERVER_PORT,
                    BasicAuthenticationCommand.builder()
                            .username("demo")
                            .password("demo")
                            .build());
        });
        networkThread.start();
    }*/

    /**
     * Constructor
     *
     * @param settings The game settings.
     */
    public SimpleNetworkDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void dispose() {
        /*if (networkThread.isAlive()) {
            networkThread.interrupt();
        }
        networkServer.stop();
        networkClient.stop();*/
        super.dispose();
    }

    public static void main(String[] args) {
        final WindowSettings settings = new WindowSettings(800, 600);
        settings.setTitle("Chat Demo");
        settings.setTargetFps(60);

        var game = new SimpleNetworkDemo(settings);
        game.start();
    }
}
