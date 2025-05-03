package org.pixel.demo.learning.network;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.commons.util.TextHelper;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;
import org.pixel.network.api.NetworkAuthenticator;
import org.pixel.network.api.NetworkClientListener;
import org.pixel.network.api.NetworkServerListener;
import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.NetworkClient;
import org.pixel.network.io.NetworkClientSettings;
import org.pixel.network.io.NetworkServer;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.message.DataMessage;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.message.NetworkMessage;
import org.pixel.network.security.AuthType;
import org.pixel.network.security.BasicAuth;
import org.pixel.network.service.NetworkServiceRegistrar;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a simple network demo that demonstrates how to use the *core* network library.
 * Use this demo as an example to create your own networked game with full control over the network layer.
 * Please check Arkade for a more complete and higher-level network solution.
 */
public class SimpleNetworkDemo extends DemoGame
        implements NetworkAuthenticator, NetworkServerListener, NetworkClientListener {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private final PlayerData player = new PlayerData(TextHelper.randomString(8), new Vector2());
    private final ConcurrentHashMap<String, PlayerData> others = new ConcurrentHashMap<>();

    private NetworkServer networkServer;
    private NetworkClient networkClient;

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
        // Set log level to TRACE to see ALL logs:
        ConsoleLogger.setLogLevel(LogLevel.TRACE);
        // Add the network services to the global service provider:
        // Since this is an extension, we need to register the network services manually:
        ServiceProvider.register(new NetworkServiceRegistrar());
    }

    @Override
    public void load() {
        super.load();

        // Note: This demo contains both the server and clients in the same application for demonstration purposes.

        // YOU MAY RUN THIS APPLICATION MULTIPLE TIMES TO TEST THE SERVER WITH MULTIPLE CLIENTS.
        // THE APPLICATION WON'T CRASH IF YOU TRY TO REBIND THE SAME SERVER PORT.

        // Create a new SERVER instance:
        networkServer = ServiceProvider.get(NetworkServer.class, NetworkServerSettings.builder()
                .serverListener(this)
                .authenticator(this)
                .bindAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .maxConnections(10)
                .numThreads(5)
                .allowedAuthTypes(List.of(AuthType.BASIC))
                .maxIdleTimeSeconds(60)
                .build());

        // Create a new CLIENT instance:
        networkClient = ServiceProvider.get(NetworkClient.class, NetworkClientSettings.builder()
                .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                .heartbeatIntervalSeconds(30)
                .clientListener(this)
                .build());

        // Initialize both SERVER and CLIENTS
        if (!networkServer.init()) {
            // Do not throw an exception here, since the server is already running in a separate thread.
            log.warn("Failed to initialize server, it may already be running.");
        }
        if (!networkClient.init()) {
            throw new RuntimeException("Failed to initialize clients");
        }

        // Authenticate player A:
        var handshakeA = new HandshakeRequest();
        handshakeA.add("auth", new BasicAuth(player.username(), "doesnotmatter").toString());
        sendToServer(networkClient, handshakeA);

        // Complementary assets:
        content = ServiceProvider.get(ContentManager.class);
        spriteBatch = ServiceProvider.get(SpriteBatch.class);
        gameCamera.setOrigin(Vector2.ZERO);

        debugFont = content.load("fonts/gidole-regular.ttf", Font.class);
        debugFont.setFontSize(28); // the base font-size (as it will be applied on the generated texture)
    }

    @Override
    public void update(DeltaTime delta) {

    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        // draw connection count in the top left corner:
        if (networkServer.isActive()) {
            spriteBatch.drawText(debugFont, "Players: " + networkServer.getConnectionCount(), Vector2.ZERO, Color.WHITE);
        }

        // draw our player:
        spriteBatch.drawText(debugFont, "Player: " + player.username, player.position, Color.WHITE);

        // draw other players (if any):
        for (var other : others.values()) {
            spriteBatch.drawText(debugFont, "Player: " + other.username, other.position, Color.YELLOW);
        }

        spriteBatch.end();
    }

    //region server listener & authenticator

    @Override
    public DataMap authenticate(String username, String password, SocketAddress userAddress) {
        // In a real application, you would check the username and password against a proper authentication system...

        // (Best Practice) Note that the DataMap is being created with a concurrent implementation since in this
        // context, it can be accessed & modified by multiple threads:
        var userData = DataMap.createConcurrent();
        userData.put("username", username);
        userData.put("email", username + "@example.com");
        userData.put("money", 1000);
        userData.put("host", userAddress.getHost());
        userData.put("posX", ThreadLocalRandom.current().nextFloat(50, getVirtualWidth() - 50));
        userData.put("posY", ThreadLocalRandom.current().nextFloat(50, getVirtualHeight() - 50));
        // THE DATA DEFINED ABOVE IS ONLY AVAILABLE ON THE SERVER. YOU MUST HANDLE WHAT INFO YOU SHARE WITH YOUR
        // PLAYERS MANUALLY (via DataMessage or other means).

        // You MUST return null if authentication fails (you can return an empty map if you want to allow the connection
        // but with no data for the user at this point):
        return userData;
    }

    @Override
    public void onPlayerActive(NetworkPlayer player) {
        log.info("Player [{0}] connected.", player.getData().getString("username"));

        for (var activePlayer : networkServer.getPlayers()) {
            var simplifiedPlayerData = new DataMap();
            simplifiedPlayerData.put("username", activePlayer.getData().get("username"));
            simplifiedPlayerData.put("posX", activePlayer.getData().get("posX"));
            simplifiedPlayerData.put("posY", activePlayer.getData().get("posY"));

            // Send the new player the data of all other players:
            var playerInfoMsg = new GameMessage(GameMessage.TYPE_PLAYER_DATA, simplifiedPlayerData);
            broadcast(new DataMessage(playerInfoMsg.toBytes()));
        }
    }

    @Override
    public void onPlayerInactive(NetworkPlayer player) {
        log.info("Player [{0}] disconnected.", player.getData().getString("username"));
    }

    @Override
    public void onPlayerMessage(NetworkPlayer player, DataMessage message) {
        log.info("Player [{0}] sent message: {1}", player.getData().getString("username"), message);
    }

    //endregion server listener

    //region client listener

    @Override
    public void onReady() {
        log.info("I'm connected to the server!");
    }

    @Override
    public void onDisconnect() {
        log.info("I'm disconnected from the server!");
    }

    @Override
    public void onMessage(DataMessage message) {
        var gameMessage = GameMessage.fromBytes(message.getPayload());
        if (gameMessage.type().equals(GameMessage.TYPE_PLAYER_DATA)) {
            // This is a player data message, we can use it to update the client state.
            log.info("Received player data: {0}", gameMessage.data());
            handlePlayerDataUpdate(gameMessage);

        } else if (gameMessage.type().equals(GameMessage.TYPE_PLAYER_MESSAGE)) {
            // This is a player message, we can use it to update the client state.
            log.info("Received player message: {0}", gameMessage.data());
        } else {
            log.warn("Unknown message type: {0}", gameMessage.type());
        }
    }

    //endregion client listener

    @Override
    public void dispose() {
        networkClient.dispose();
        networkServer.dispose();
        content.dispose();
        super.dispose();
    }

    private void handlePlayerDataUpdate(GameMessage playerDataMsg) {
        // check if the username is ours (our data) or not (other players):
        var username = playerDataMsg.data().getString("username");

        if (username.equals(player.username)) {
            // This is our data, we can update our state.
            player.position.set(
                    Float.parseFloat(playerDataMsg.data().getString("posX")),
                    Float.parseFloat(playerDataMsg.data().getString("posY"))
            );
        } else {
            // This is another player's data, we can update the other players list.
            var otherPlayer = new PlayerData(username,
                    new Vector2(
                            Float.parseFloat(playerDataMsg.data().getString("posX")),
                            Float.parseFloat(playerDataMsg.data().getString("posY"))
                    ));
            others.put(username, otherPlayer);
        }
    }

    private void sendToServer(NetworkClient client, NetworkMessage message) {
        try {
            if (client.isConnected() && client.send(message)) {
                log.trace("Message queued for sending: {0}", message);
            }
        } catch (Exception e) {
            log.error("Failed to send message: {0}", e.getMessage(), e);
        }
    }

    private void sendToPlayer(NetworkPlayer player, NetworkMessage message) {
        try {
            if (networkServer.send(player, message)) {
                log.trace("Message queued for sending: {0}", message);
            }
        } catch (Exception e) {
            log.error("Failed to send message: {0}", e.getMessage(), e);
        }
    }

    private void broadcast(NetworkMessage message) {
        try {
            networkServer.broadcast(message);
        } catch (Exception e) {
            log.error("Failed to send message: {0}", e.getMessage(), e);
        }
    }

    /**
     * This is a simple data class that contains the player data.
     * It is used to send the player data over the network.
     * You can add more fields to this class if you want to send more data.
     *
     * @param username The username of the player.
     * @param position The position of the player.
     */
    record PlayerData(String username, Vector2 position) {

    }

    /**
     * This is a very simple game message wrapper that can be sent over the network.
     * It contains a type and a data map.
     * This is for DEMO PURPOSES ONLY. Ideally you would use a more robust serialization mechanism.
     *
     * @param type
     * @param data
     */
    record GameMessage(String type, DataMap data) {

        static String TYPE_PLAYER_DATA = "player_data";
        static String TYPE_PLAYER_MESSAGE = "player_message";

        static GameMessage fromBytes(byte[] payload) {
            String payloadString = new String(payload, StandardCharsets.UTF_8);
            String[] parts = payloadString.split("\\|", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid payload format");
            }

            String type = parts[0];
            String dataString = parts[1];
            DataMap data = new DataMap();
            for (String entry : dataString.split(";")) {
                String[] keyValue = entry.split("=");
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException("Invalid data format");
                }
                String key = keyValue[0];
                String value = keyValue[1];
                data.put(key, value);
            }

            return new GameMessage(type, data);
        }

        byte[] toBytes() {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append("|");
            for (String key : data.keySet()) {
                sb.append(key).append("=").append(data.get(key)).append(";");
            }
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
    
    public static void main(String[] args) {
        final WindowSettings settings = new WindowSettings(800, 600);
        settings.setBackgroundColor(Color.INDIGO);
        settings.setTitle("Network setup Demo");
        settings.setTargetFps(60);

        var game = new SimpleNetworkDemo(settings);
        game.start();
    }

}
