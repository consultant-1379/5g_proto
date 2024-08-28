package com.ericsson.sc.tapcol.pcap;

import java.io.StringWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.pcap4j.packet.namednumber.TcpPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;

/**
 * 
 * Represents a TCP socket, that is, an IP/port pair
 *
 */
public class Socket
{
    private static final Logger log = LoggerFactory.getLogger(Socket.class);
    private static final byte[] nilIPv4Array = new byte[] { 0, 0, 0, 0 };
    private static final byte[] nilIPv6Array = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public static final TcpPort NIL_PORT = new TcpPort((short) 0, "NULL");
    public static final Socket NIL = new Socket(getIpByIp(null), NIL_PORT);
    public static final Socket NIL6 = new Socket(getIpByIp6(null), NIL_PORT);

    Socket(InetAddress ip,
           short port)
    {
        this.ip = getIpByIp(ip);
        this.port = new TcpPort(port, "???");
    }

    Socket(InetAddress ip,
           TcpPort port)
    {
        this.ip = getIpByIp(ip);
        this.port = port;
    }

    final InetAddress ip;
    final TcpPort port;

    @Override
    public String toString()
    {
        var socketAsString = new StringWriter(64);
        if (ip instanceof Inet6Address)
            socketAsString.append('[');
        socketAsString.append(ip.getHostAddress());
        if (ip instanceof Inet6Address)
            socketAsString.append(']');
        socketAsString.append(':');
        socketAsString.append(String.valueOf(port));

        return socketAsString.toString();
    }

    public static Socket getSocketByAddress(Address address)
    {
        var gotValidTcpSocket = false;
        InetAddress ip = null;
        short port = 0;

        if (address.hasSocketAddress())
        {
            var socketAddress = address.getSocketAddress();

            gotValidTcpSocket = socketAddress.getProtocol() == SocketAddress.Protocol.TCP && socketAddress.hasPortValue();

            if (gotValidTcpSocket)
            {
                try
                {
                    // FIXME do not do any DNS lookups, the address should be IP
                    ip = InetAddress.getByName(socketAddress.getAddress());
                    port = (short) socketAddress.getPortValue();
                }
                catch (Exception e)
                {
                    log.error("ERROR: Could not resolve \"" + socketAddress.getAddress() + "\" to valid IPv4 or IPv6 address!", e);

                    gotValidTcpSocket = false;
                }
            }
        }

        if (!gotValidTcpSocket)
        {
            log.error("ERROR: Only TCP sockets with standard port handled. Got {}", address);
        }

        return gotValidTcpSocket ? new Socket(ip, port) : Socket.NIL;
    }

    // handle NIL case
    private static InetAddress getIpByIp(InetAddress ip)
    {
        InetAddress nilIpv4 = null;

        try
        {
            nilIpv4 = InetAddress.getByAddress(nilIPv4Array);
        }
        catch (UnknownHostException e)
        {
            // can be considered fatal, should never happen
            log.error("FATAL: Fatal initialization error!", e);
            System.exit(1);
        }

        return ip != null ? ip : nilIpv4;
    }

    private static InetAddress getIpByIp6(InetAddress ip)
    {
        InetAddress nilIpv6 = null;

        try
        {
            nilIpv6 = InetAddress.getByAddress(nilIPv6Array);
        }
        catch (UnknownHostException e)
        {
            // can be considered fatal, should never happen
            log.error("FATAL: Fatal initialization error!", e);
            System.exit(1);
        }

        return ip != null ? ip : nilIpv6;
    }
}
