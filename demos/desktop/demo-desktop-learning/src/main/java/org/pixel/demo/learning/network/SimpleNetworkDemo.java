package org.pixel.demo.learning.network;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;
import org.pixel.network.api.NetworkAuthenticator;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.*;
import org.pixel.network.io.netty.NettyNetworkClient;
import org.pixel.network.io.netty.NettyNetworkServer;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.security.AuthType;
import org.pixel.network.security.BasicAuth;

import java.util.List;

/**
 * This is a simple network demo that demonstrates how to use the *core* network library.
 * Use this demo as an example to create your own networked game with full control over the network layer.
 * Please check Arkade for a more complete and higher-level network solution.
 */
public class SimpleNetworkDemo extends DemoGame implements NetworkAuthenticator {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private NetworkServer networkServer;
    private NetworkClient networkClientA;
    private NetworkClient networkClientB;

    private ContentManager content;
    private SpriteBatch spriteBatch;
    private Font debugFont;

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
        // In a real application, you would check the username and password against a proper authentication system...
        var userData = new DataMap();
        userData.put("username", username);
        userData.put("email", username + "@example.com");
        userData.put("credits", 1000);
        userData.put("host", userAddress.getHost());
        // ...

        return userData; // You MUST return null if authentication fails.
    }

    @Override
    public void load() {
        super.load();

        // Note: This demo contains both the server and clients in the same application for demonstration purposes.

        // Create a new SERVER instance:
        networkServer = new NettyNetworkServer(NetworkServerSettings.builder()
                .bindAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .maxConnections(10)
                .numThreads(5)
                .allowedAuthTypes(List.of(AuthType.BASIC))
                .maxIdleTimeSeconds(60)
                .build());
        networkServer.setAuthenticator(this);

        // Create a new CLIENT instance:
        networkClientA = new NettyNetworkClient(NetworkClientSettings.builder()
                .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .heartbeatIntervalSeconds(30)
                .build());

        // Create a new CLIENT instance:
        networkClientB = new NettyNetworkClient(NetworkClientSettings.builder()
                .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .heartbeatIntervalSeconds(30)
                .build());

        // TODO: add network state listener (clients)

        // Initialize both SERVER and CLIENTS
        if (!networkServer.init()) {
            throw new RuntimeException("Failed to initialize server");
        }
        if (!networkClientA.init() || !networkClientB.init()) {
            throw new RuntimeException("Failed to initialize clients");
        }

        // Authenticate player A:
        var handshakeA = new HandshakeRequest();
        handshakeA.add("auth", new BasicAuth("joe", "pwd").toString());
        networkClientA.write(handshakeA);
        // Authenticate player B:
        var handshakeB = new HandshakeRequest();
        handshakeB.add("auth", new BasicAuth("jane", "pwd").toString());
        networkClientB.write(handshakeB);

        // Complementary assets:
        content = ServiceProvider.get(ContentManager.class);
        spriteBatch = ServiceProvider.get(SpriteBatch.class);
        gameCamera.setOrigin(Vector2.ZERO);

        debugFont = content.load("fonts/gidole-regular.ttf", Font.class);
        debugFont.setFontSize(24); // the base font-size (as it will be applied on the generated texture)
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        // draw connection count in the top left corner:
        spriteBatch.drawText(debugFont, "Players: " + networkServer.getConnectionCount(), Vector2.ZERO, Color.WHITE);

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        networkClientA.dispose();
        networkClientB.dispose();
        networkServer.dispose();
        content.dispose();
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
