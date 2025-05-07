package org.pixel.network.io;

import lombok.Builder;
import lombok.Getter;
import org.pixel.network.api.NetworkClientListener;
import org.pixel.network.security.Auth;

import java.io.File;
import java.util.List;

@Builder
@Getter
public class NetworkClientSettings {
    // General
    private final List<NetworkChannelSettings> channels;
    private final Auth auth;

    // Listeners
    private final NetworkClientListener clientListener;

    // Security
    private final File trustCertChainFile = null;   // nullable, PEM for CA chain
}