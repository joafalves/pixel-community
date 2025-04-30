package org.pixel.demo.learning.network;

import org.pixel.commons.Color;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.util.TextHelper;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.network.api.NetworkAuthenticator;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.*;
import org.pixel.network.io.netty.NettyGameClient;
import org.pixel.network.io.netty.NettyGameServer;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.security.AuthType;

import java.util.List;

/**
 * This is a simple network demo that demonstrates how to use the *core* network library.
 * Use this demo as an example to create your own networked game with full control over the network layer.
 * Please check Arkade for a more complete and higher-level network solution.
 */
public class SimpleNetworkDemo extends DemoGame implements NetworkAuthenticator {

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
    public DataMap authenticate(String username, String password, SocketAddress userAddress) {
        // In a real application, you would check the username and password against a database or other authentication:
        var userData = new DataMap();
        userData.put("username", username);
        userData.put("email", username + "@example.com");

        return userData;
    }

    @Override
    public void load() {
        super.load();

        // Note: This demo contains both the server and clients in the same application for demonstration purposes.
        // Please note that the game-server, in a real scenario, MIGHT actually be executed on the game-side (for e.g.
        // the player is hosting the game session).

        // Create a new SERVER instance:
        gameServer = new NettyGameServer(GameServerSettings.builder()
                .bindAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .maxConnections(10)
                .numThreads(5)
                .allowedAuthTypes(List.of(AuthType.BASIC))
                .build());
        gameServer.setAuthenticator(this);

        // Create a new CLIENT instance:
        gameClient = new NettyGameClient(GameClientSettings.builder()
                .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .build());

        // Initialize both SERVER and CLIENT
        if (!gameServer.init()) {
            throw new RuntimeException("Failed to initialize server");
        }
        if (!gameClient.init()) {
            throw new RuntimeException("Failed to initialize client");
        }

        var auth = "myUsername:myPassword";
        var handshake = new HandshakeRequest();
        handshake.add("auth", "basic " + TextHelper.encodeBase64(auth)) ;

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
