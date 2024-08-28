package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;

public class ProxyVirtualHost
{
    private String vHostname;
    private List<ProxyEndpoint> endpoints = new ArrayList<>();
    private List<ProxySubnet> subnets = new ArrayList<>();
    /**
     * Sort virtualhosts routes based on routePriority attribute: <br>
     * Higher prio goes to the top of the list
     * 
     */
    private Set<ProxyRoute> routes = new TreeSet<>((r1,
                                                    r2) ->
    {
        if (r1.equals(r2))
        {
            return 0;
        }

        if (r1.getRoutePriority() == r2.getRoutePriority())
        {
            return 1;
        }

        return r1.getRoutePriority().compareTo(r2.getRoutePriority());
    });

    private List<ProxyVirtualCluster> vClusters = new ArrayList<>();

    /**
     * Create a new Virtual Host with a given name. A VirtualHost without Endpoints
     * will have domains = "*", thus matching any domain. (See RdsHelper)<br>
     * Note that there can be only one VHost with domain "*".
     * 
     * @param name
     */
    public ProxyVirtualHost(String name)
    {
        this.vHostname = name;
    }

    public String getvHostName()
    {
        return vHostname;
    }

    public void setvHostName(String vHost)
    {
        this.vHostname = vHost;
    }

    public void addEndpoint(ProxyEndpoint ep)
    {
        this.endpoints.add(ep);
    }

    public List<ProxyEndpoint> getEndpoints()
    {
        return this.endpoints;
    }

    public void addRoute(ProxyRoute route)
    {
        this.routes.add(route);
    }

    public void addRoutes(List<ProxyRoute> routes)
    {
        this.routes.addAll(routes);
    }

    public Set<ProxyRoute> getRoutes()
    {
        return this.routes;
    }

    public void addSubnet(ProxySubnet subn)
    {
        this.subnets.add(subn);
    }

    public List<ProxySubnet> getSubnets()
    {
        return this.subnets;
    }

    /**
     * add the given instance of ProxyVirtualCluster to the virtualClusters list
     * after checking that it's not already there
     * 
     * @param vc : The ProxyVirtualCluster to add to the list
     */
    public void addVirtualCluster(ProxyVirtualCluster vc)
    {
        if (!this.vClusters.contains(vc))
            this.vClusters.add(vc);
    }

    public List<ProxyVirtualCluster> getVirtualClusters()
    {
        return this.vClusters;
    }

    @Override
    public String toString()
    {

        return "\n    VirtualHost:" //
               + "\n  vHostname=" + this.vHostname //
               + "\n  endpoints=" + endpoints //
               + "\n  routes=" + routes //
               + "\n  vClusters=" + vClusters //
               + "]\n";
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((vHostname == null) ? 0 : vHostname.hashCode());
        result = prime * result + ((endpoints == null) ? 0 : endpoints.hashCode());
        result = prime * result + ((routes == null) ? 0 : routes.hashCode());
        result = prime * result + ((vClusters == null) ? 0 : vClusters.hashCode());

        return result;
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
        ProxyVirtualHost other = (ProxyVirtualHost) obj;
        if (vHostname == null)
        {
            if (other.vHostname != null)
                return false;
        }
        else if (!vHostname.equals(other.vHostname))
            return false;
        if (endpoints == null)
        {
            if (other.endpoints != null)
                return false;
        }
        else if (!endpoints.equals(other.endpoints))
            return false;
        if (routes == null)
        {
            if (other.routes != null)
                return false;
        }
        else if (!routes.equals(other.routes))
            return false;
        if (vClusters == null)
        {
            if (other.vClusters != null)
                return false;
        }
        else if (!vClusters.equals(other.vClusters))
            return false;

        return true;
    }

}
