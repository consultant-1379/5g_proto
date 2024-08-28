package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.Objects;

public class ProxyVirtualCluster
{
    String name;
    String regex;
    String method;

    public ProxyVirtualCluster(String name,
                               String regex,
                               String method)
    {
        this.name = name;
        this.regex = regex;
        this.method = method;
    }

    public ProxyVirtualCluster(ProxyVirtualCluster anotherPxVirtualCluster)
    {
        this.name = anotherPxVirtualCluster.name;
        this.regex = anotherPxVirtualCluster.regex;
        this.method = anotherPxVirtualCluster.method;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyVirtualCluster [name=" + name + ", regex=" + regex + ", method=" + method + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(method, name, regex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyVirtualCluster other = (ProxyVirtualCluster) obj;
        return Objects.equals(method, other.method) && Objects.equals(name, other.name) && Objects.equals(regex, other.regex);
    }

}
