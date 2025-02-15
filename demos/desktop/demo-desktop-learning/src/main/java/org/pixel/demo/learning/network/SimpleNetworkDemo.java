package org.pixel.demo.learning.network;

import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.network.*;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.dsnp.NetworkMessage;
import org.pixel.network.dsnp.NetworkMessageType;

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
    }

    @Override
    public void load() {
        super.load();

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

        var handshake = new NetworkMessage(NetworkMessageType.HANDSHAKE_REQUEST, null);
        gameClient.write(handshake);
    }

    @Override
    public void dispose() {
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
