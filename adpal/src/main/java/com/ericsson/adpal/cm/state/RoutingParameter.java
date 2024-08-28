package com.ericsson.adpal.cm.state;

/**
 * @author edimsyr An enum that stores the path parameter with which the routing
 *         of the state data shall occur
 *
 */
public enum RoutingParameter
{
    n32_c("n32-c"),
    external_network("external-network"),
    bsfLastUpdate("last-update");

    private final String t;

    RoutingParameter(final String routingParameter)
    {
        t = routingParameter;
    }

    @Override
    public String toString()
    {
        return t;
    }

    public String stringForRegex()
    {
        return ".*" + t + ".*";
    }
}