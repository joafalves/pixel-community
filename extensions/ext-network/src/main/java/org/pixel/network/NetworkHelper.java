package org.pixel.network;

public class NetworkHelper {

    private NetworkHelper() {
        // Prevent instantiation
    }

    public static String normalizeChannelName(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            throw new IllegalArgumentException("Channel name cannot be null or empty");
        }
        return channelName.trim().replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
