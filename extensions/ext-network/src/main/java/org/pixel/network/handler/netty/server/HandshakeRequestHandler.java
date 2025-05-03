package org.pixel.network.handler.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.data.SocketAddress;
import org.pixel.network.io.NetworkServerSettings;
import org.pixel.network.io.netty.NettyHelper;
import org.pixel.network.io.netty.NettyNetworkSession;
import org.pixel.network.io.netty.NettySessionState;
import org.pixel.network.message.HandshakeRequest;
import org.pixel.network.message.HandshakeResponse;
import org.pixel.network.security.AuthType;
import org.pixel.network.security.BasicAuth;

@RequiredArgsConstructor
public class HandshakeRequestHandler extends SimpleChannelInboundHandler<HandshakeRequest> {

    private static final Logger log = LoggerFactory.getLogger(HandshakeRequestHandler.class);

    private final NetworkServerSettings settings;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HandshakeRequest request) {
        NettyNetworkSession session = getOrCreateSession(ctx, request);
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

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeRequest request, NettyNetworkSession session) {
        // Handshake the session...
        AuthType clientAuth = getAuthType(request);
        if (clientAuth != AuthType.NONE && settings.getAuthenticator() == null) {
            log.warn("Blocking handshake request to {0} because no authenticator is set.",
                    session.getId());

            NettyHelper.changeSessionState(ctx, session, NettySessionState.INACTIVE);

            var response = new HandshakeResponse();
            response.setStatus("500");
            response.setReason("Internal server error: no authenticator is set");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        if (clientAuth == AuthType.NONE) {
            handleNoneAuth(ctx, session);

        } else if (clientAuth == AuthType.BASIC) {
            handleBasicAuth(ctx, request, session);

        } else {
            // TODO: implement more authentication types...
            throw new IllegalStateException("Unsupported authentication type: " + clientAuth);
        }

        if (log.isTraceEnabled() && session.getPlayer().getData() != null) {
            log.trace("User data: {0}", session.getPlayer().getData());
        }
    }

    private void handleNoneAuth(ChannelHandlerContext ctx, NettyNetworkSession session) {
        if (!settings.getAllowedAuthTypes().contains(AuthType.NONE)) {
            log.debug("Blocking handshake request to {0} because anonymous access is disabled.",
                    session.getId());

            NettyHelper.changeSessionState(ctx, session, NettySessionState.INACTIVE);

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("Anonymous access is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Allowing anonymous access to {0}.", session.getId());
        session.setState(NettySessionState.ACTIVE);

        var response = new HandshakeResponse();
        response.setStatus("200");

        ctx.writeAndFlush(response);
    }

    private void handleBasicAuth(ChannelHandlerContext ctx, HandshakeRequest request, NettyNetworkSession session) {
        if (!settings.getAllowedAuthTypes().contains(AuthType.BASIC)) {
            log.debug("Blocking handshake request to {0} because BASIC authentication is disabled.",
                    session.getId());

            NettyHelper.changeSessionState(ctx, session, NettySessionState.INACTIVE);

            var response = new HandshakeResponse();
            response.setStatus("403");
            response.setReason("BASIC authentication is not allowed");
            ctx.writeAndFlush(response);
            ctx.close();

            return;
        }

        log.debug("Handling BASIC authentication to {0}.", session.getId());

        if (settings.getAuthenticator() != null) {
            try {
                var credentials = BasicAuth.fromString(request.getAuth());
                var userData = settings.getAuthenticator().authenticate(
                        credentials.getUsername(), credentials.getPassword(), session.getUserAddress());

                if (userData == null) {
                    log.debug("Authentication failed for session {0}: user not found.", session.getId());

                    NettyHelper.changeSessionState(ctx, session, NettySessionState.INACTIVE);

                    var response = new HandshakeResponse();
                    response.setStatus("401");
                    response.setReason("Authentication failed: user not found");
                    ctx.writeAndFlush(response);
                    ctx.close();
                    return;
                }

                session.getPlayer().setData(userData);

                NettyHelper.changeSessionState(ctx, session, NettySessionState.ACTIVE);

                var response = new HandshakeResponse();
                response.setStatus("200");
                ctx.writeAndFlush(response);

                log.debug("User {0} authenticated successfully for session {1}.",
                        credentials.getUsername(), session.getId());

            } catch (Exception e) {
                log.error("Authentication failed for session {0}: {1}.", session.getId(), e.getMessage(), e);

                NettyHelper.changeSessionState(ctx, session, NettySessionState.INACTIVE);

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

    private NettyNetworkSession getOrCreateSession(ChannelHandlerContext ctx, HandshakeRequest request) {
        // Check if the session already exists
        NettyNetworkSession session = NettyHelper.getNetworkSession(ctx.channel());
        if (session == null) {
            var channel = ctx.channel();

            // Create a new network session:
            session = createSession(ctx);
            NettyHelper.attachNetworkSession(channel, session);
        }
        return session;
    }

    private NettyNetworkSession createSession(ChannelHandlerContext ctx) {
        var playerId = ctx.channel().id().asLongText();
        var player = NetworkPlayer.builder()
                .id(playerId)
                .build();

        return NettyNetworkSession.builder()
                .id(playerId)
                .lastRemoteActivity(System.currentTimeMillis())
                .userAddress(SocketAddress.from(ctx.channel().remoteAddress()))
                .player(player)
                .build();
    }
}
