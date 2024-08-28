package com.ericsson.utilities.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelperHttp
{
    private static final Logger log = LoggerFactory.getLogger(HelperHttp.class);

    public static Integer getAvailablePort(String host)
    {
        return getAvailablePort(host, 1).get(0);
    }

    public static List<Integer> getAvailablePort(String host,
                                                 int count)
    {
        try
        {
            final var ports = new ArrayList<Integer>();
            final var addr = InetAddress.getByName(host);
            for (var i = 0; i < count; i++)
            {
                try (var tmpSocket = new ServerSocket(0, 50, addr))
                {
                    log.trace("Available port: {}", tmpSocket.getLocalPort());
                    ports.add(tmpSocket.getLocalPort());

                }
            }

            return List.copyOf(ports);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPortAvailable(int port,
                                          String host)
    {
        InetAddress addr;
        try
        {
            addr = InetAddress.getByName(host);
        }
        catch (UnknownHostException e1)
        {
            throw new IllegalArgumentException("Invalid address " + host, e1);
        }
        try (final var ss = new ServerSocket(port, 50, addr))
        {
            // ss.setReuseAddress(true);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
