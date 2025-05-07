package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.data.Pair;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.NetworkHelper;
import org.pixel.network.api.NetworkAuthenticatorContext;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.SocketAddress;
import org.pixel.network.io.netty.NettyNetworkConnection;
import org.pixel.network.io.netty.event.HandshakeSuccessEvent;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.message.HandshakeResponse;
import org.pixel.network.security.Auth;
import org.pixel.network.security.AuthType;
import org.pixel.network.security.BasicAuth;

@RequiredArgsConstructor
public class ServerHandshakeHandler extends SimpleChannelInboundHandler<HandshakeRequest> {

    private static final Logger log = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    private final NetworkServerSettings settings;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeRequest request) throws Exception {
        handleHandshake(ctx, request);
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeRequest request) {
        // Handle the handshake request here
        log.debug("Received handshake request from: {0}.", ctx.channel().remoteAddress());

        var error = preflightHandshake(request);
        if (error != null) {
            log.warn("Handshake request is invalid: {0}.", error);

            // Send an error response back to the client
            var response = new HandshakeResponse();
            response.setStatus(error.getA());
            response.setReason(error.getB());
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        var auth = getAuth(request);
        if (auth == null) {
            handleNoneAuth(ctx, request);
        } else if (auth instanceof BasicAuth basicAuth) {
            handleBasicAuth(ctx, request, basicAuth);

        } else {
            log.warn("Unsupported authentication type: {0}.", auth.getClass().getSimpleName());

            var response = new HandshakeResponse();
            response.setStatus("500");
            response.setReason("Unsupported authentication type");
            ctx.writeAndFlush(response);
            ctx.close();
        }
    }

    private void handleNoneAuth(ChannelHandlerContext ctx, HandshakeRequest request) {
        if (!settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.debug("Blocking handshake request to {0} because anonymous access is disabled.",
                    ctx.channel().remoteAddress());

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("Anonymous access is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Allowing anonymous access to {0}.", ctx.channel().remoteAddress());

        // Since there is no identification, when we create an anonymous session, we cannot have automatic association
        // per player id, so we need to create a new session for each channel:
        var playerId = ctx.channel().id().asLongText();
        triggerHandshakeSuccessEvent(ctx, request, playerId, DataMap.concurrent());

        // send OK response
        var response = new HandshakeResponse();
        response.setStatus("200");
        ctx.writeAndFlush(response);
    }

    private void handleBasicAuth(ChannelHandlerContext ctx, HandshakeRequest request, BasicAuth auth) {
        if (!settings.getAllowedAuthTypes().contains(AuthType.BASIC)) {
            log.debug("Blocking handshake request to {0} because BASIC authentication is disabled.",
                    ctx.channel().remoteAddress());

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("BASIC authentication is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Handling BASIC authentication to {0}.", ctx.channel().remoteAddress());

        if (settings.getAuthenticator() == null) {
            log.warn("Authenticator is not set, cannot handle BASIC authentication.");
            var response = new HandshakeResponse();
            response.setStatus("500");
            response.setReason("Authenticator is not set");
            ctx.writeAndFlush(response);
            ctx.close();
            return;
        }

        var channelName = NetworkHelper.normalizeChannelName(request.getChannel());
        var context = new NetworkAuthenticatorContext(channelName, SocketAddress.from(ctx.channel().remoteAddress()));

        var username = auth.getUsername();
        var password = auth.getPassword();

        // Attempt to authenticate the user:
        var authResponse = settings.getAuthenticator().authenticate(username, password, context);

        if (authResponse == null || !authResponse.isSuccess()) {
            // Authentication failed:
            log.debug("Authentication failed for session {0}: user not found.", ctx.channel().remoteAddress());

            var response = new HandshakeResponse();
            response.setStatus("401");
            if (authResponse != null && authResponse.getMessage() != null) {
                response.setReason(authResponse.getMessage());
            } else {
                response.setReason("Authentication failed: user not found");
            }
            ctx.writeAndFlush(response);
            ctx.close();
            return;
        }

        // Handle successful authentication:
        triggerHandshakeSuccessEvent(ctx, request, auth.getPlayerId(), authResponse.getData());

        // send OK response
        var response = new HandshakeResponse();
        response.setStatus("200");
        ctx.writeAndFlush(response);
    }

    private void triggerHandshakeSuccessEvent(ChannelHandlerContext ctx, HandshakeRequest request, String playerId, DataMap data) {
        var channelName = NetworkHelper.normalizeChannelName(request.getChannel());
        var connection = new NettyNetworkConnection(ctx.channel(), channelName, playerId, data);
        ctx.fireUserEventTriggered(new HandshakeSuccessEvent(connection));
    }

    private Pair<String, String> preflightHandshake(HandshakeRequest request) {
        // Check if the handshake has the channel name defined:
        if (request.getChannel() == null || request.getChannel().isEmpty()) {
            return new Pair<>("400", "Channel name is missing");
        }

        AuthType clientAuth = getAuthType(request);
        if (clientAuth != AuthType.NONE && settings.getAuthenticator() == null) {
            return new Pair<>("500", "Authenticator is not set");
        }

        return null; // No error
    }

    private AuthType getAuthType(HandshakeRequest request) {
        var auth = request.getAuth();
        if (auth == null || auth.isEmpty()) {
            return AuthType.NONE;
        }

        if (auth.startsWith("basic ")) {
            return AuthType.BASIC;
        } else if (auth.startsWith("digest ")) {
            return AuthType.DIGEST;
        } else if (auth.startsWith("bearer ")) {
            return AuthType.BEARER;
        }

        return AuthType.NONE;
    }

    private Auth getAuth(HandshakeRequest request) {
        if (request.getAuth() == null || request.getAuth().isEmpty()) {
            return null;
        }

        // TODO: support other auth types
        var authType = getAuthType(request);
        return switch (authType) {
            case BASIC -> BasicAuth.fromString(request.getAuth());
            case NONE -> null;
            default -> throw new IllegalStateException("Unsupported authentication type: " + authType);
        };
    }
}
