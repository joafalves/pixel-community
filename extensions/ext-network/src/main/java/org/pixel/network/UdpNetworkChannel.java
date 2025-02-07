package org.pixel.network;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.data.SocketAddress;
import org.pixel.pipeline.DataPipelineFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class UdpNetworkChannel extends NetworkChannel {
    private static final int INPUT_BUFFER_SIZE = 65536;

    private static final Logger log = LoggerFactory.getLogger(UdpNetworkChannel.class);

    private final ConcurrentHashMap<SocketAddress, DatagramSocket> localSockets = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param upstreamPipelineFactory   The upstream pipeline factory.
     * @param downstreamPipelineFactory The downstream pipeline factory.
     */
    public UdpNetworkChannel(DataPipelineFactory<?> upstreamPipelineFactory, DataPipelineFactory<?> downstreamPipelineFactory) {
        super(upstreamPipelineFactory, downstreamPipelineFactory);
    }

    @Override
    public SocketAddress bind(SocketAddress address) {
        // is already bound?
        DatagramSocket socket = localSockets.get(address);
        if (socket != null && !socket.isClosed()) {
            log.warn("Attempted to bind an already bound address: {0}.", address);
            return address;
        }

        try {
            // Create a new DatagramSocket bound to the specified host and port.
            InetSocketAddress inetAddress = new InetSocketAddress(address.getHost(), address.getPort());
            DatagramSocket newSocket = new DatagramSocket(inetAddress);
            localSockets.put(address, newSocket);
            log.info("Bound UDP socket to {0}.", address);

            // Start a receiver thread for this socket.
            Thread receiverThread = new Thread(() -> receiveLoop(newSocket, address));
            receiverThread.setDaemon(true); // Mark as daemon so it doesn't block JVM exit.
            receiverThread.start();

            return address;
        } catch (Exception e) {
            log.error("Failed to bind UDP socket to {0}.", address, e);
            return null;
        }
    }

    @Override
    public boolean send(byte[] data, SocketAddress localAddress, SocketAddress remoteAddress) {
        DatagramSocket socket = localSockets.get(localAddress);
        if (socket == null || socket.isClosed()) {
            log.warn("Attempted to send data from an unbound or closed local address: {0}.", localAddress);
            return false;
        }

        try {
            InetSocketAddress targetAddress = new InetSocketAddress(remoteAddress.getHost(), remoteAddress.getPort());
            DatagramPacket packet = new DatagramPacket(data, data.length, targetAddress);
            socket.send(packet);
            log.debug("Sent packet from " + localAddress + " to " + remoteAddress);
            return true;
        } catch (IOException e) {
            log.error("Failed to send UDP packet from " + localAddress + " to " + remoteAddress, e);
            return false;
        }
    }

    /**
     * Terminates the network channel.
     * <p>
     * This method closes all bound UDP sockets and stops all receiver threads.
     */
    @Override
    public void dispose() {
        for (SocketAddress address : localSockets.keySet()) {
            DatagramSocket socket = localSockets.get(address);
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Closed UDP socket on " + address);
            }
        }
        localSockets.clear();
    }

    private void receiveLoop(DatagramSocket socket, SocketAddress address) {
        byte[] buffer = new byte[INPUT_BUFFER_SIZE];
        while (!socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);

                // Extract data from the packet.
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

                // Create a SocketAddress representing the sender.
                SocketAddress remoteAddress = new SocketAddress(packet.getAddress().getHostAddress(), packet.getPort());

                log.trace("Processing received data from \"{0}\" on \"{1}\".", remoteAddress, address);

                // Pass the received data to the upstream pipeline.
                processReceivedData(data, address, remoteAddress);

            } catch (IOException e) {
                log.error("Error receiving UDP packet on {0}.", address, e);
                // Break out if the socket is closed or error occurs.
                break;
            }
        }
    }

    private void processReceivedData(byte[] data, SocketAddress localAddress, SocketAddress remoteAddress) {
        // Pass the received data to the upstream pipeline.
        if (upstreamPipelineFactory != null) {
            // TODO: Implement the pipeline context.
            // TODO: check if we can cache the pipeline per bound address.
            // upstreamPipelineFactory.create().begin(null);

        } else {
            log.warn("Received data on {0} but no upstream pipeline is available.", localAddress);
        }
    }
}
