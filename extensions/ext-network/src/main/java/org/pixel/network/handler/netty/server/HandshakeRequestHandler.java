package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.api.NetworkAuthenticator;
import org.pixel.network.data.NetworkSession;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.GameServerSettings;
import org.pixel.network.io.netty.NettyUtils;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.message.HandshakeResponse;
import org.pixel.network.security.AuthType;
import org.pixel.network.security.BasicAuth;

@RequiredArgsConstructor
public class HandshakeRequestHandler extends SimpleChannelInboundHandler<HandshakeRequest> {

    private static final Logger log = LoggerFactory.getLogger(HandshakeRequestHandler.class);

    private final GameServerSettings serverSettings;
    private final NetworkAuthenticator authenticator;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeRequest request) {
        NetworkSession session = getOrCreateSession(ctx, request);
        if (session.isActive()) {
            log.debug("Received handshake request from an already active session: {0}.", session.getId());

            // Gracefully reject the handshake:
            var response = new HandshakeResponse();
            response.setStatus("400");
            response.setReason("Session has already been established");
            ctx.writeAndFlush(response);

            return;
        }

        handleHandshake(ctx, request, session);
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeRequest request, NetworkSession session) {
        // Handshake the session...
        AuthType clientAuth = getAuthType(request);
        if (clientAuth != AuthType.NONE && authenticator == null) {
            log.warn("Blocking handshake request to {0} because no authenticator is set.",
                    session.getId());

            var response = new HandshakeResponse();
            response.setStatus("500");
            response.setReason("Internal server error: no authenticator is set");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        if (clientAuth == AuthType.NONE) {
            handleNoneAuth(ctx, request, session);

        } else if (clientAuth == AuthType.BASIC) {
            handleBasicAuth(ctx, request, session);

        } else {
            // TODO: implement more authentication types...
            throw new IllegalStateException("Unsupported authentication type: " + clientAuth);
        }

        if (session.getUserData() != null && log.isTraceEnabled()) {
            log.trace("User data: {0}", session.getUserData());
        }
    }

    private void handleNoneAuth(ChannelHandlerContext ctx, HandshakeRequest request, NetworkSession session) {
        if (!serverSettings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.debug("Blocking handshake request to {0} because anonymous access is disabled.",
                    session.getId());

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("Anonymous access is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Allowing anonymous access to {0}.", session.getId());
        session.setState(NetworkSession.State.ACTIVE);

        var response = new HandshakeResponse();
        response.setStatus("200");

        ctx.writeAndFlush(response);
    }

    private void handleBasicAuth(ChannelHandlerContext ctx, HandshakeRequest request, NetworkSession session) {
        if (!serverSettings.getAllowedAuthTypes().contains(AuthType.BASIC)) {
            log.debug("Blocking handshake request to {0} because basic authentication is disabled.",
                    session.getId());

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("Basic authentication is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Handling basic authentication to {0}.", session.getId());

        if (authenticator != null) {
            try {
                var credentials = BasicAuth.fromString(request.getAuth());
                var userData = authenticator.authenticate(credentials.getUsername(), credentials.getPassword(),
                        session.getUserAddress());

                if (userData == null) {
                    log.debug("Authentication failed for session {0}: user not found.", session.getId());

                    var response = new HandshakeResponse();
                    response.setStatus("401");
                    response.setReason("Authentication failed: user not found");
                    ctx.writeAndFlush(response);
                    ctx.close();
                    return;
                }

                session.setUserData(userData);
                session.setState(NetworkSession.State.ACTIVE);

                log.debug("User {0} authenticated successfully for session {1}.",
                        credentials.getUsername(), session.getId());

            } catch (Exception e) {
                log.error("Authentication failed for session {0}: {1}.", session.getId(), e.getMessage(), e);

                var response = new HandshakeResponse();
                response.setStatus("500");
                response.setReason("Server error");
                ctx.writeAndFlush(response);
                ctx.close();
            }
        }
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

    private NetworkSession getOrCreateSession(ChannelHandlerContext ctx, HandshakeRequest request) {
        // Check if the session already exists
        NetworkSession session = NettyUtils.getSession(ctx.channel());
        if (session == null) {
            // Create a new session:
            session = createSession(ctx);
            NettyUtils.attachSession(ctx.channel(), session);
        }
        return session;
    }

    private NetworkSession createSession(ChannelHandlerContext ctx) {
        return NetworkSession.builder()
                .id(ctx.channel().id().asLongText())
                .lastRemoteActivity(System.currentTimeMillis())
                .userAddress(SocketAddress.from(ctx.channel().remoteAddress()))
                .build();
    }
}
