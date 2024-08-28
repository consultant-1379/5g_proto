
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfRoutingAction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "action-slf-lookup",
                     "action-nf-discovery",
                     "action-route-strict",
                     "action-route-preferred",
                     "action-route-round-robin",
                     "action-route-remote-round-robin",
                     "action-route-remote-preferred",
                     "action-reject-message",
                     "action-log",
                     "action-drop-message" })
public class RoutingAction implements IfRoutingAction
{

    /**
     * Name identifying the routing-action (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the routing-action")
    private String name;
    /**
     * Perform SLF interrogation
     * 
     */
    @JsonProperty("action-slf-lookup")
    @JsonPropertyDescription("Perform SLF interrogation")
    private ActionSlfLookup actionSlfLookup;
    /**
     * Perform delegated NF discovery by querying the NRF with the parameters from
     * the received request.Specific discovery parameters or all the discovery
     * parameters are extracted from the received request based on the configuration
     * of either use-discovery-parameter or add-discovery-parameter.
     * 
     */
    @JsonProperty("action-nf-discovery")
    @JsonPropertyDescription("Perform delegated NF discovery by querying the NRF with the parameters from the received request.Specific discovery parameters or all the discovery parameters are extracted from the received request based on the configuration of either use-discovery-parameter or add-discovery-parameter.")
    private ActionNfDiscovery actionNfDiscovery;
    /**
     * Route strictly to the targeted host
     * 
     */
    @JsonProperty("action-route-strict")
    @JsonPropertyDescription("Route strictly to the targeted host")
    private ActionRouteStrict actionRouteStrict;
    /**
     * Route to the targeted host which belongs to the specified nf-pool. In case of
     * failure, re-select among the rest of the hosts in this nf-pool
     * 
     */
    @JsonProperty("action-route-preferred")
    @JsonPropertyDescription("Route to the targeted host which belongs to the specified nf-pool. In case of failure, re-select among the rest of the hosts in this nf-pool")
    private ActionRoutePreferred actionRoutePreferred;
    /**
     * Route to any of the hosts in the specified target-nf-pool
     * 
     */
    @JsonProperty("action-route-round-robin")
    @JsonPropertyDescription("Route to any of the hosts in the specified target-nf-pool")
    private ActionRouteRoundRobin actionRouteRoundRobin;
    /**
     * Select the host name to include in the target-api-root header and route the
     * request to any of the hosts in the specified target-nf-pool. The
     * target-api-root header is replaced on reselects in a round-robin fashion.
     * 
     */
    @JsonProperty("action-route-remote-round-robin")
    @JsonPropertyDescription("Select the host name to include in the target-api-root header and route the request to any of the hosts in the specified target-nf-pool. The target-api-root header is replaced on reselects in a round-robin fashion.")
    private ActionRouteRemoteRoundRobin actionRouteRemoteRoundRobin;
    /**
     * Route indirectly to the targeted host as specified in the
     * target-api-root-header. In cases where re-selection is needed, select the
     * host name to include in the target-api-root header based on the result of the
     * preceded nf-discovery action. The target-api-root header is replaced in a
     * round-robin fashion for every subsequent re-selection
     * 
     */
    @JsonProperty("action-route-remote-preferred")
    @JsonPropertyDescription("Route indirectly to the targeted host as specified in the target-api-root-header. In cases where re-selection is needed, select the host name to include in the target-api-root header based on the result of the preceded nf-discovery action. The target-api-root header is replaced in a round-robin fashion for every subsequent re-selection")
    private ActionRouteRemotePreferred actionRouteRemotePreferred;
    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    @JsonPropertyDescription("Rejects an http request and sends back a response with an operator defined status code and title with detailed explanation")
    private ActionRejectMessage actionRejectMessage;
    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    @JsonPropertyDescription("Logs a user-defined message with the configured log-level")
    private ActionLog__2 actionLog;
    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    @JsonPropertyDescription("Drops an http request message and the HTTP/2 stream is reset gracefully")
    private ActionDropMessage actionDropMessage;

    /**
     * Name identifying the routing-action (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the routing-action (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RoutingAction withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Perform SLF interrogation
     * 
     */
    @JsonProperty("action-slf-lookup")
    public ActionSlfLookup getActionSlfLookup()
    {
        return actionSlfLookup;
    }

    /**
     * Perform SLF interrogation
     * 
     */
    @JsonProperty("action-slf-lookup")
    public void setActionSlfLookup(ActionSlfLookup actionSlfLookup)
    {
        this.actionSlfLookup = actionSlfLookup;
    }

    public RoutingAction withActionSlfLookup(ActionSlfLookup actionSlfLookup)
    {
        this.actionSlfLookup = actionSlfLookup;
        return this;
    }

    /**
     * Perform delegated NF discovery by querying the NRF with the parameters from
     * the received request.Specific discovery parameters or all the discovery
     * parameters are extracted from the received request based on the configuration
     * of either use-discovery-parameter or add-discovery-parameter.
     * 
     */
    @JsonProperty("action-nf-discovery")
    public ActionNfDiscovery getActionNfDiscovery()
    {
        return actionNfDiscovery;
    }

    /**
     * Perform delegated NF discovery by querying the NRF with the parameters from
     * the received request.Specific discovery parameters or all the discovery
     * parameters are extracted from the received request based on the configuration
     * of either use-discovery-parameter or add-discovery-parameter.
     * 
     */
    @JsonProperty("action-nf-discovery")
    public void setActionNfDiscovery(ActionNfDiscovery actionNfDiscovery)
    {
        this.actionNfDiscovery = actionNfDiscovery;
    }

    public RoutingAction withActionNfDiscovery(ActionNfDiscovery actionNfDiscovery)
    {
        this.actionNfDiscovery = actionNfDiscovery;
        return this;
    }

    /**
     * Route strictly to the targeted host
     * 
     */
    @JsonProperty("action-route-strict")
    public ActionRouteStrict getActionRouteStrict()
    {
        return actionRouteStrict;
    }

    /**
     * Route strictly to the targeted host
     * 
     */
    @JsonProperty("action-route-strict")
    public void setActionRouteStrict(ActionRouteStrict actionRouteStrict)
    {
        this.actionRouteStrict = actionRouteStrict;
    }

    public RoutingAction withActionRouteStrict(ActionRouteStrict actionRouteStrict)
    {
        this.actionRouteStrict = actionRouteStrict;
        return this;
    }

    /**
     * Route to the targeted host which belongs to the specified nf-pool. In case of
     * failure, re-select among the rest of the hosts in this nf-pool
     * 
     */
    @JsonProperty("action-route-preferred")
    public ActionRoutePreferred getActionRoutePreferred()
    {
        return actionRoutePreferred;
    }

    /**
     * Route to the targeted host which belongs to the specified nf-pool. In case of
     * failure, re-select among the rest of the hosts in this nf-pool
     * 
     */
    @JsonProperty("action-route-preferred")
    public void setActionRoutePreferred(ActionRoutePreferred actionRoutePreferred)
    {
        this.actionRoutePreferred = actionRoutePreferred;
    }

    public RoutingAction withActionRoutePreferred(ActionRoutePreferred actionRoutePreferred)
    {
        this.actionRoutePreferred = actionRoutePreferred;
        return this;
    }

    /**
     * Route to any of the hosts in the specified target-nf-pool
     * 
     */
    @JsonProperty("action-route-round-robin")
    public ActionRouteRoundRobin getActionRouteRoundRobin()
    {
        return actionRouteRoundRobin;
    }

    /**
     * Route to any of the hosts in the specified target-nf-pool
     * 
     */
    @JsonProperty("action-route-round-robin")
    public void setActionRouteRoundRobin(ActionRouteRoundRobin actionRouteRoundRobin)
    {
        this.actionRouteRoundRobin = actionRouteRoundRobin;
    }

    public RoutingAction withActionRouteRoundRobin(ActionRouteRoundRobin actionRouteRoundRobin)
    {
        this.actionRouteRoundRobin = actionRouteRoundRobin;
        return this;
    }

    /**
     * Select the host name to include in the target-api-root header and route the
     * request to any of the hosts in the specified target-nf-pool. The
     * target-api-root header is replaced on reselects in a round-robin fashion.
     * 
     */
    @JsonProperty("action-route-remote-round-robin")
    public ActionRouteRemoteRoundRobin getActionRouteRemoteRoundRobin()
    {
        return actionRouteRemoteRoundRobin;
    }

    /**
     * Select the host name to include in the target-api-root header and route the
     * request to any of the hosts in the specified target-nf-pool. The
     * target-api-root header is replaced on reselects in a round-robin fashion.
     * 
     */
    @JsonProperty("action-route-remote-round-robin")
    public void setActionRouteRemoteRoundRobin(ActionRouteRemoteRoundRobin actionRouteRemoteRoundRobin)
    {
        this.actionRouteRemoteRoundRobin = actionRouteRemoteRoundRobin;
    }

    public RoutingAction withActionRouteRemoteRoundRobin(ActionRouteRemoteRoundRobin actionRouteRemoteRoundRobin)
    {
        this.actionRouteRemoteRoundRobin = actionRouteRemoteRoundRobin;
        return this;
    }

    /**
     * Route indirectly to the targeted host as specified in the
     * target-api-root-header. In cases where re-selection is needed, select the
     * host name to include in the target-api-root header based on the result of the
     * preceded nf-discovery action. The target-api-root header is replaced in a
     * round-robin fashion for every subsequent re-selection
     * 
     */
    @JsonProperty("action-route-remote-preferred")
    public ActionRouteRemotePreferred getActionRouteRemotePreferred()
    {
        return actionRouteRemotePreferred;
    }

    /**
     * Route indirectly to the targeted host as specified in the
     * target-api-root-header. In cases where re-selection is needed, select the
     * host name to include in the target-api-root header based on the result of the
     * preceded nf-discovery action. The target-api-root header is replaced in a
     * round-robin fashion for every subsequent re-selection
     * 
     */
    @JsonProperty("action-route-remote-preferred")
    public void setActionRouteRemotePreferred(ActionRouteRemotePreferred actionRouteRemotePreferred)
    {
        this.actionRouteRemotePreferred = actionRouteRemotePreferred;
    }

    public RoutingAction withActionRouteRemotePreferred(ActionRouteRemotePreferred actionRouteRemotePreferred)
    {
        this.actionRouteRemotePreferred = actionRouteRemotePreferred;
        return this;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public ActionRejectMessage getActionRejectMessage()
    {
        return actionRejectMessage;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public void setActionRejectMessage(ActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
    }

    public RoutingAction withActionRejectMessage(ActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
        return this;
    }

    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    public ActionLog__2 getActionLog()
    {
        return actionLog;
    }

    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    public void setActionLog(ActionLog__2 actionLog)
    {
        this.actionLog = actionLog;
    }

    public RoutingAction withActionLog(ActionLog__2 actionLog)
    {
        this.actionLog = actionLog;
        return this;
    }

    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    public ActionDropMessage getActionDropMessage()
    {
        return actionDropMessage;
    }

    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    public void setActionDropMessage(ActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
    }

    public RoutingAction withActionDropMessage(ActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoutingAction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("actionSlfLookup");
        sb.append('=');
        sb.append(((this.actionSlfLookup == null) ? "<null>" : this.actionSlfLookup));
        sb.append(',');
        sb.append("actionNfDiscovery");
        sb.append('=');
        sb.append(((this.actionNfDiscovery == null) ? "<null>" : this.actionNfDiscovery));
        sb.append(',');
        sb.append("actionRouteStrict");
        sb.append('=');
        sb.append(((this.actionRouteStrict == null) ? "<null>" : this.actionRouteStrict));
        sb.append(',');
        sb.append("actionRoutePreferred");
        sb.append('=');
        sb.append(((this.actionRoutePreferred == null) ? "<null>" : this.actionRoutePreferred));
        sb.append(',');
        sb.append("actionRouteRoundRobin");
        sb.append('=');
        sb.append(((this.actionRouteRoundRobin == null) ? "<null>" : this.actionRouteRoundRobin));
        sb.append(',');
        sb.append("actionRouteRemoteRoundRobin");
        sb.append('=');
        sb.append(((this.actionRouteRemoteRoundRobin == null) ? "<null>" : this.actionRouteRemoteRoundRobin));
        sb.append(',');
        sb.append("actionRouteRemotePreferred");
        sb.append('=');
        sb.append(((this.actionRouteRemotePreferred == null) ? "<null>" : this.actionRouteRemotePreferred));
        sb.append(',');
        sb.append("actionRejectMessage");
        sb.append('=');
        sb.append(((this.actionRejectMessage == null) ? "<null>" : this.actionRejectMessage));
        sb.append(',');
        sb.append("actionLog");
        sb.append('=');
        sb.append(((this.actionLog == null) ? "<null>" : this.actionLog));
        sb.append(',');
        sb.append("actionDropMessage");
        sb.append('=');
        sb.append(((this.actionDropMessage == null) ? "<null>" : this.actionDropMessage));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.actionRejectMessage == null) ? 0 : this.actionRejectMessage.hashCode()));
        result = ((result * 31) + ((this.actionRouteRoundRobin == null) ? 0 : this.actionRouteRoundRobin.hashCode()));
        result = ((result * 31) + ((this.actionRoutePreferred == null) ? 0 : this.actionRoutePreferred.hashCode()));
        result = ((result * 31) + ((this.actionRouteRemoteRoundRobin == null) ? 0 : this.actionRouteRemoteRoundRobin.hashCode()));
        result = ((result * 31) + ((this.actionSlfLookup == null) ? 0 : this.actionSlfLookup.hashCode()));
        result = ((result * 31) + ((this.actionDropMessage == null) ? 0 : this.actionDropMessage.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.actionNfDiscovery == null) ? 0 : this.actionNfDiscovery.hashCode()));
        result = ((result * 31) + ((this.actionRouteStrict == null) ? 0 : this.actionRouteStrict.hashCode()));
        result = ((result * 31) + ((this.actionRouteRemotePreferred == null) ? 0 : this.actionRouteRemotePreferred.hashCode()));
        result = ((result * 31) + ((this.actionLog == null) ? 0 : this.actionLog.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RoutingAction) == false)
        {
            return false;
        }
        RoutingAction rhs = ((RoutingAction) other);
        return ((((((((((((this.actionRejectMessage == rhs.actionRejectMessage)
                          || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage)))
                         && ((this.actionRouteRoundRobin == rhs.actionRouteRoundRobin)
                             || ((this.actionRouteRoundRobin != null) && this.actionRouteRoundRobin.equals(rhs.actionRouteRoundRobin))))
                        && ((this.actionRoutePreferred == rhs.actionRoutePreferred)
                            || ((this.actionRoutePreferred != null) && this.actionRoutePreferred.equals(rhs.actionRoutePreferred))))
                       && ((this.actionRouteRemoteRoundRobin == rhs.actionRouteRemoteRoundRobin)
                           || ((this.actionRouteRemoteRoundRobin != null) && this.actionRouteRemoteRoundRobin.equals(rhs.actionRouteRemoteRoundRobin))))
                      && ((this.actionSlfLookup == rhs.actionSlfLookup)
                          || ((this.actionSlfLookup != null) && this.actionSlfLookup.equals(rhs.actionSlfLookup))))
                     && ((this.actionDropMessage == rhs.actionDropMessage)
                         || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.actionNfDiscovery == rhs.actionNfDiscovery)
                       || ((this.actionNfDiscovery != null) && this.actionNfDiscovery.equals(rhs.actionNfDiscovery))))
                  && ((this.actionRouteStrict == rhs.actionRouteStrict)
                      || ((this.actionRouteStrict != null) && this.actionRouteStrict.equals(rhs.actionRouteStrict))))
                 && ((this.actionRouteRemotePreferred == rhs.actionRouteRemotePreferred)
                     || ((this.actionRouteRemotePreferred != null) && this.actionRouteRemotePreferred.equals(rhs.actionRouteRemotePreferred))))
                && ((this.actionLog == rhs.actionLog) || ((this.actionLog != null) && this.actionLog.equals(rhs.actionLog))));
    }

}
