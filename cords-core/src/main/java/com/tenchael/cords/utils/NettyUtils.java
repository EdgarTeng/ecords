package com.tenchael.cords.utils;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NettyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyUtils.class);

    private static final int DEFAULT_INET_ADDRESS_COUNT = 1;


    /**
     * If {@link Epoll#isAvailable()} <code>== true</code>, returns a new
     * {@link EpollEventLoopGroup}, otherwise returns a new
     * {@link NioEventLoopGroup}. Creates the event loop group using the
     * specified number of threads instead of the default.
     *
     * @param nThreads see {@link NioEventLoopGroup#NioEventLoopGroup(int)}.
     * @return a new {@link EventLoopGroup}.
     */
    public static EventLoopGroup newNioOrEpollEventLoopGroup(int nThreads) {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(nThreads);
        } else {
            return new NioEventLoopGroup(nThreads);
        }
    }

    /**
     * Attempts to detect and return the number of local network addresses that could be
     * used by a client to reach this server. This means we exclude the following address types:
     * <ul>
     *     <li>Multicast addresses. Zookeeper server sockets use TCP, thus cannot bind to a multicast address.</li>
     *     <li>Link-local addresses. Routers don't forward traffic sent to a link-local address, so
     *     any realistic server deployment would not have clients using these.</li>
     *     <li>Loopback addresses. These are typically only used for testing.</li>
     * </ul>
     * Any remaining addresses are counted, and the total count is returned. This number is
     * used to configure the number of threads for the "boss" event loop group, to make sure we have
     * enough threads for each address in case the server is configured to listen on
     * all available addresses.
     * If listing the network interfaces fails, this method will return 1.
     *
     * @return the number of client-reachable local network addresses found, or
     * 1 if listing the network interfaces fails.
     */
    public static int getClientReachableLocalInetAddressCount() {
        try {
            Set<InetAddress> validInetAddresses = new HashSet<>();
            Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(allNetworkInterfaces)) {
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (inetAddress.isLinkLocalAddress()) {
                        LOGGER.debug("Ignoring link-local InetAddress {}", inetAddress);
                        continue;
                    }
                    if (inetAddress.isMulticastAddress()) {
                        LOGGER.debug("Ignoring multicast InetAddress {}", inetAddress);
                        continue;
                    }
                    if (inetAddress.isLoopbackAddress()) {
                        LOGGER.debug("Ignoring loopback InetAddress {}", inetAddress);
                        continue;
                    }
                    validInetAddresses.add(inetAddress);
                }
            }
            LOGGER.debug("Detected {} local network addresses: {}", validInetAddresses.size(), validInetAddresses);
            return validInetAddresses.size() > 0 ? validInetAddresses.size() : DEFAULT_INET_ADDRESS_COUNT;
        } catch (SocketException ex) {
            LOGGER.warn("Failed to list all network interfaces, assuming 1", ex);
            return DEFAULT_INET_ADDRESS_COUNT;
        }
    }

    /**
     * If {@link Epoll#isAvailable()} <code>== true</code>, returns
     * {@link EpollServerSocketChannel}, otherwise returns
     * {@link NioServerSocketChannel}.
     *
     * @return a server socket channel class.
     */
    public static Class<? extends ServerSocketChannel> nioOrEpollServerSocketChannel() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

}
