package org.pixel.arkade;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.annotation.Property;

@Getter
public class ArkadeSettings {
    // --- General ---
    @Getter
    @Setter
    public static class General {
        @Property(defaultValue = "Arkade")
        private String serverName;
        @Property(defaultValue = "1.0.0")
        private String serverVersion;
    }
    private final General general = new General();

    // --- Network ---
    @Getter
    @Setter
    public static class Network {
        @Property(mandatory = true)
        private String bindingAddress;
        @Property(mandatory = true)
        private int bindingPort;
        @Property(defaultValue = "100")
        private int maxConnections;
        @Property(defaultValue = "false")
        private boolean forceSSL;
        @Property(defaultValue = "10")
        private int threads;
    }
    private final Network network = new Network();
}
