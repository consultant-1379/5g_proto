package com.ericsson.esc.bsf.worker;

import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.bsf.model.ServiceAddress;

public class BsfSrvCfg
{
    public enum Stack
    {
        IPV4("IPv4 Mode"),
        IPV6("IPv6 Mode"),
        DUAL("Dualstack Mode"),
        ERR("Configuration is Missing");

        private String description;

        Stack(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }

    private final BsfSrvOptions bsfSrvOptions;
    private final boolean port;
    private final boolean tlsPort;
    private final boolean ipv4Address;
    private final boolean ipv6Address;
    private final boolean fqdn;
    private final Stack stack;

    public BsfSrvCfg(BsfSrvOptions bsfSrvOptions,
                     Optional<ServiceAddress> srvAddr)
    {
        this.bsfSrvOptions = bsfSrvOptions;
        this.port = srvAddr.isPresent() && (srvAddr.get().getPort()) != null;
        this.tlsPort = srvAddr.isPresent() && (srvAddr.get().getTlsPort()) != null;
        this.ipv4Address = srvAddr.isPresent() && (srvAddr.get().getIpv4Address()) != null;
        this.ipv6Address = srvAddr.isPresent() && (srvAddr.get().getIpv6Address()) != null;
        // fqdn is used as fallback when neither ipv4-address nor ipv6-address is set in
        // the config
        this.fqdn = srvAddr.isPresent() && (srvAddr.get().getFqdn()) != null;

        if (this.ipv4Address && this.ipv6Address)
        {
            this.stack = Stack.DUAL;
        }
        else if (this.ipv4Address)
        {
            this.stack = Stack.IPV4;
        }
        else if (this.ipv6Address)
        {
            this.stack = Stack.IPV6;
        }
        else if (this.fqdn)
        {
            this.stack = Stack.DUAL;
        }
        else
        {
            this.stack = Stack.ERR;
        }
    }

    public BsfSrvCfg()
    {
        this.bsfSrvOptions = new BsfSrvOptions();
        this.port = false;
        this.tlsPort = false;
        this.fqdn = false;
        this.stack = Stack.ERR;
        this.ipv4Address = false;
        this.ipv6Address = false;
    }

    public BsfSrvOptions getBsfSrvOptions()
    {
        return bsfSrvOptions;
    }

    public boolean isPort()
    {
        return port;
    }

    public boolean isTlsPort()
    {
        return tlsPort;
    }

    public boolean isIpv4Address()
    {
        return ipv4Address;
    }

    public boolean isIpv6Address()
    {
        return ipv6Address;
    }

    public boolean isFqdn()
    {
        return fqdn;
    }

    public Stack getStack()
    {
        return stack;
    }

    @Override
    public String toString()
    {
        return "BsfSrvCfg [bsfSrvOptions=" + bsfSrvOptions + ", port=" + port + ", tlsPort=" + tlsPort + ", ipv4Address=" + ipv4Address + ", ipv6Address="
               + ipv6Address + ", fqdn=" + fqdn + ", stack=" + stack + "]";
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
        BsfSrvCfg other = (BsfSrvCfg) obj;
        return Objects.equals(bsfSrvOptions, other.bsfSrvOptions) && ipv4Address == other.ipv4Address && ipv6Address == other.ipv6Address && port == other.port
               && stack == other.stack && tlsPort == other.tlsPort;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bsfSrvOptions, ipv4Address, ipv6Address, port, stack, tlsPort);
    }

}
