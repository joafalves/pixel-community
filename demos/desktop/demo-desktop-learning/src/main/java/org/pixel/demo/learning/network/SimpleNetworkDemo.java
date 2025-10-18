package org.pixel.demo.learning.network;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.commons.util.TextHelper;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.tween.Tween;
import org.pixel.ext.tween.TweenEasingMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;
import org.pixel.network.NetworkHelper;
import org.pixel.network.NetworkTransport;
import org.pixel.network.api.*;
import org.pixel.network.io.*;
import org.pixel.network.message.DataMessage;
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
    private static final String CHANNEL_GAME = NetworkHelper.normalizeChannelName("game");
    private static final String CHANNEL_DUMMY = NetworkHelper.normalizeChannelName("dummy");

    private final PlayerData localPlayer = new PlayerData(TextHelper.randomString(8), new Vector2(), null);
    private final ConcurrentHashMap<String, PlayerData> others = new ConcurrentHashMap<>();
    private final Timer ticker = new Timer(1000 / 10); // 10 FPS for network updates

    private NetworkServer networkServer;
    private NetworkClient networkClient;

    private ContentManager content;
    private SpriteBatch spriteBatch;
    private Font debugFont;
    private boolean dirty = false; // Flag to indicate if we need to send a message

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
        var transport = NetworkTransport.TCP;
        var channels = List.of(
                NetworkChannelSettings.builder()
                        .name(CHANNEL_GAME)
                        .transport(transport)
                        .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                        .build(),
                NetworkChannelSettings.builder()
                        .name(CHANNEL_DUMMY)
                        .transport(transport)
                        .serverAddress(new SocketAddress(SERVER_HOST, SERVER_PORT))
                        .build()
        );
        networkClient = ServiceProvider.get(NetworkClient.class, NetworkClientSettings.builder()
                .channels(channels)
                .clientListener(this)
                .auth(new BasicAuth(localPlayer.username(), "doesnotmatter"))
                .build());

        // Initialize both SERVER and CLIENTS
        if (!networkServer.init()) {
            // Do not throw an exception here, since the server is already running in a separate thread.
            log.warn("Failed to initialize server, it may already be running.");
        }
        if (!networkClient.init()) {
            throw new RuntimeException("Failed to initialize clients");
        }

        // Complementary assets:
        content = ContentManager.create();
        spriteBatch = SpriteBatch.create();
        gameCamera.setOrigin(Vector2.ZERO);

        debugFont = content.load("fonts/gidole-regular.ttf", Font.class, FontImporterSettings.builder()
                .fontSize(28)
                .build());
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) {
            localPlayer.position.add(100 * delta.getElapsed(), 0);
            dirty = true;
        } else if (Keyboard.isKeyDown(KeyboardKey.LEFT)) {
            localPlayer.position.add(-100 * delta.getElapsed(), 0);
            dirty = true;
        }

        for (var other : others.values()) {
            other.tween.update(delta);
        }

        if (ticker.elapsed() && dirty && networkServer.isActive()) {
            // send my position to other players:
            var playerData = new DataMap();
            playerData.put("username", localPlayer.username);
            playerData.put("posX", localPlayer.position.getX());
            playerData.put("posY", localPlayer.position.getY());

            var playerMessage = new DataMessage(new DemoGameMessage(DemoGameMessage.TYPE_PLAYER_DATA, playerData).toBytes());
            broadcast(playerMessage);

            dirty = false; // Reset the dirty flag
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        // draw connection count in the top left corner:
        if (networkServer.isActive()) {
            spriteBatch.drawText(debugFont, "Players: " + networkServer.getPlayerCount(), Vector2.ZERO, Color.WHITE);
            spriteBatch.drawText(debugFont, "Connections: " + networkServer.getConnectionCount(), new Vector2(0, 30), Color.WHITE);
        }

        // draw our player:
        spriteBatch.drawText(debugFont, "Player: " + localPlayer.username, localPlayer.position, Color.WHITE);

        // draw other players (if any):
        for (var other : others.values()) {
            spriteBatch.drawText(debugFont, "Player: " + other.username, other.position, Color.YELLOW);
        }

        spriteBatch.end();
    }

    //region server listener and authenticator

    @Override
    public NetworkAuthenticatorResponse authenticate(String username, String password, NetworkAuthenticatorContext ctx) {
        // In a real application, you would check the username and password against a proper authentication system.
        // For this demo, we will just check if the username is not empty...
        if (username.isEmpty() || password.isEmpty()) {
            return NetworkAuthenticatorResponse.builder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        }

        // (Best Practice) Note that the DataMap is being created with a concurrent implementation since in this
        // context, it can be accessed and modified by multiple threads:
        // Note that this is just an example on how to attach data to the CONNECTION. You can also attach data to
        // the PLAYER (see #onConnectionAccepted(..)):
        var channelData = DataMap.concurrent();
        channelData.put("scope", ctx.getChannelName());
        channelData.put("host", ctx.getRemoteAddress().getHost());

        // Note that the player ID we define here is what will be used to globally identify the player in the server
        // across all channels:
        return NetworkAuthenticatorResponse.builder()
                .success(true)
                .playerId(username)
                .data(channelData)
                .build();
    }

    @Override
    public void onConnectionAccepted(NetworkPlayer player, NetworkConnection connection) {
        log.info("Player [{0} : {1}] connection accepted!", player.getId(), connection.getChannelName());

        if (connection.getChannelName().equalsIgnoreCase(CHANNEL_GAME)) {
            // THE DATA DEFINED ABOVE IS ONLY AVAILABLE ON THE SERVER. YOU MUST HANDLE WHAT INFO YOU SHARE WITH YOUR
            // PLAYERS MANUALLY (via DataMessage or other means).
            var playerData = player.getData();
            playerData.put("posX", ThreadLocalRandom.current().nextFloat(50, getViewportWidth() - 50));
            playerData.put("posY", ThreadLocalRandom.current().nextFloat(50, getViewportHeight() - 50));

            // Broadcast all-players info to all players (a bit redundant, but this is a demo):
            for (var activePlayer : networkServer.getPlayers()) {
                var filterData = new DataMap();
                filterData.put("username", activePlayer.getId());
                filterData.putAll(activePlayer.getData());

                var playerInfoMsg = new DemoGameMessage(DemoGameMessage.TYPE_PLAYER_DATA, filterData);
                var playerMessage = new DataMessage(playerInfoMsg.toBytes());

                networkServer.broadcast(playerMessage);
            }
        }
    }

    @Override
    public void onConnectionRemoved(NetworkPlayer player, NetworkConnection connection) {
        log.info("Player [{0} : {1}] connection removed.", player.getId(), connection.getChannelName());
        // TODO: announce disconnection to other players:
    }

    @Override
    public void onConnectionMessage(NetworkPlayer player, NetworkConnection connection, NetworkMessage message) {
        log.info("Player [{0} : {1}] sent message: {2}", player.getId(), connection.getChannelName(), message);
    }

    //endregion server listener

    //region client listener

    @Override
    public void onChannelActive(NetworkChannel channel) {
        log.info("Channel active: {0}", channel.getName());
    }

    @Override
    public void onChannelInactive(NetworkChannel channel) {
        log.info("Channel inactive: {0}", channel.getName());
    }

    @Override
    public void onChannelMessage(NetworkChannel channel, DataMessage message) {
        var gameMessage = DemoGameMessage.fromBytes(message.getPayload());
        if (gameMessage.type().equals(DemoGameMessage.TYPE_PLAYER_DATA)) {
            // This is a player data message, we can use it to update the client state.
            log.info("Received player data: {0}", gameMessage.data());
            handlePlayerDataUpdate(gameMessage);

        } else if (gameMessage.type().equals(DemoGameMessage.TYPE_PLAYER_MESSAGE)) {
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

    private void handlePlayerDataUpdate(DemoGameMessage playerDataMsg) {
        // check if the username is ours (our data) or not (other players):
        var username = playerDataMsg.data().getString("username");

        if (username.equals(localPlayer.username)) {
            // This is our data, we can update our state.
            localPlayer.position.set(
                    Float.parseFloat(playerDataMsg.data().getString("posX")),
                    Float.parseFloat(playerDataMsg.data().getString("posY"))
            );
        } else {
            // This is another player's data, we can update the other players list.
            var newPos = new Vector2(
                    Float.parseFloat(playerDataMsg.data().getString("posX")),
                    Float.parseFloat(playerDataMsg.data().getString("posY"))
            );

            PlayerData player = others.get(username);
            if (player == null) {
                player = new PlayerData(username, newPos, new Tween());
                others.put(username, player);
            }
            // Basic simulation of smooth movement:
            player.tween
                    .easing(TweenEasingMode.LINEAR)
                    .duration(0.25f)
                    .from(player.position)
                    .to(newPos)
                    .target(player.position)
                    .restart();
        }
    }

    private void broadcast(NetworkMessage message) {
        try {
            // Note that you can also broadcast to a specific channel (see NetworkServer#broadcast(...) implementations)
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
    record PlayerData(String username, Vector2 position, Tween tween) {

    }

    /**
     * This is a very simple game message wrapper that can be sent over the network.
     * It contains a type and a data map.
     * This is for DEMO PURPOSES ONLY. Ideally you would use a more robust serialization mechanism.
     *
     * @param type
     * @param data
     */
    record DemoGameMessage(String type, DataMap data) {

        static String TYPE_PLAYER_DATA = "player_data";
        static String TYPE_PLAYER_MESSAGE = "player_message";

        static DemoGameMessage fromBytes(byte[] payload) {
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

            return new DemoGameMessage(type, data);
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
        settings.setIdleThrottle(false);

        var game = new SimpleNetworkDemo(settings);
        game.start();
    }

}
