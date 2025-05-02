package org.pixel.network.io.netty;

import lombok.Builder;
import lombok.Data;
import org.pixel.network.data.NetworkPlayer;
import org.pixel.network.data.SocketAddress;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class NettyNetworkSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private SocketAddress userAddress;
    @Builder.Default
    private NettySessionState state = NettySessionState.NEW;
    private String id;
    private String scope;
    private long lastRemoteActivity;
    private NetworkPlayer player; // The player associated with this session (application layer)

    public boolean isActive() {
        return state == NettySessionState.ACTIVE;
    }
}