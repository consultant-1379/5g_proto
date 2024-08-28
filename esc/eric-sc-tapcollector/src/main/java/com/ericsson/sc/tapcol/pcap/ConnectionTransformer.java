/**
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 03, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol.pcap;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.data.tap.v3.Connection;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Transforms Connections. This class can be used for example to replace own
 * socket address with a different IP/port pair
 *
 */
public final class ConnectionTransformer
{
    private static final Logger log = LoggerFactory.getLogger(ConnectionTransformer.class);
    private final Map<Integer, Tuple2<InetAddress, Integer>> targetPorts;
    private final Map<Integer, Tuple2<InetAddress, Integer>> targetIPv6Ports;

    ConnectionTransformer(Map<Integer, Tuple2<InetAddress, Integer>> targetPorts,
                          Map<Integer, Tuple2<InetAddress, Integer>> targetIPv6Ports)
    {
        this.targetPorts = targetPorts;
        this.targetIPv6Ports = targetIPv6Ports;
    }

    @Override
    public String toString()
    {
        return String.format("ConnectionTransformer [targetPorts=%s]", targetPorts);
    }

    /**
     * Perform a connection transformation
     * 
     * @param connection The original connection
     * @return The transformed connection
     */
    public Optional<Connection> transform(Connection connection)
    {
        final var transformed = replace(connection.getLocalAddress()) //
                                                                     .map(repl ->
                                                                     {

                                                                         final var newSocketAddress = SocketAddress.newBuilder(connection.getLocalAddress()
                                                                                                                                         .getSocketAddress())
                                                                                                                   .setAddress(repl.getT1().getHostAddress())
                                                                                                                   .setPortValue(repl.getT2());
                                                                         return Connection.newBuilder(connection)
                                                                                          .setLocalAddress(Address.newBuilder(connection.getLocalAddress())
                                                                                                                  .setSocketAddress(newSocketAddress))
                                                                                          .build();
                                                                     });

        log.debug("Transforming connection {} to {}", connection, transformed);

        return transformed;
    }

    Optional<Tuple2<InetAddress, Integer>> replace(Address address)
    {
        final var socketAddress = Optional.ofNullable(address) //
                                          .flatMap(addr -> Optional.ofNullable(addr.getSocketAddress()));

        return socketAddress.flatMap(sa ->
        {
            final var saAddr = sa.getAddress();
            final var saPort = sa.getPortValue();
            log.debug("Trying to replace: {} with port {}", saAddr, saPort);
            final var isIPv6 = saAddr != null && InetAddresses.isInetAddress(saAddr) && InetAddresses.forString(saAddr) instanceof Inet6Address;

            return isIPv6 ? Optional.ofNullable(targetIPv6Ports.get(saPort)) : Optional.ofNullable(targetPorts.get(saPort));
        });
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Builds ConnectionTransformers
     *
     */
    public static class Builder
    {
        private HashMap<Integer, Tuple2<InetAddress, Integer>> targetPorts = new HashMap<>();
        private HashMap<Integer, Tuple2<InetAddress, Integer>> targetIPv6Ports = new HashMap<>();

        /**
         * 
         * @param localPort       The original local port for a local socket that needs
         *                        to be replaced
         * @param newLocalAddress The new local address
         * @param newLocalPort    The new local port
         * @return
         */
        public Builder withMapping(int localPort,
                                   InetAddress newLocalAddress,
                                   int newLocalPort)
        {
            final var tPorts = newLocalAddress instanceof Inet6Address ? targetIPv6Ports : targetPorts;
            log.debug("Address: {} is ipv6: {}", newLocalAddress.getHostAddress(), newLocalAddress instanceof Inet6Address);

            if (tPorts.put(localPort, Tuples.of(newLocalAddress, newLocalPort)) != null)
                throw new IllegalArgumentException("local port " + localPort + " already mapped");
            return this;
        }

        /**
         * 
         * @return A new ConnectionTransformer
         */
        public ConnectionTransformer build()
        {
            return new ConnectionTransformer(new HashMap<>(this.targetPorts), new HashMap<>(this.targetIPv6Ports));
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(targetPorts);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectionTransformer other = (ConnectionTransformer) obj;
        return Objects.equals(targetPorts, other.targetPorts);
    }

}
