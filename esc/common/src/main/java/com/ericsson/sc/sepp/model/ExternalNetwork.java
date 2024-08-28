
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfNetwork;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "service-address-ref",
                     "in-request-screening-case-ref",
                     "routing-case-ref",
                     "out-response-screening-case-ref",
                     "topology-hiding-ref",
                     "topology-hiding-with-admin-state",
                     "roaming-partner",
                     "ingress-connection-profile-ref",
                     "local-rate-limit-profile-ref",
                     "global-ingress-rate-limit-profile-ref",
                     "firewall-profile-ref" })
public class ExternalNetwork implements IfNetwork
{

    /**
     * Name uniquely identifying the external network (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the external network")
    private String name;
    /**
     * Reference to a defined service-address (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Reference to a defined service-address")
    private String serviceAddressRef;
    /**
     * Reference to the request screening case that is applied when a request is
     * received from an NF instance in this external network and its contained
     * roaming partners
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    @JsonPropertyDescription("Reference to the request screening case that is applied when a request is received from an NF instance in this external network and its contained roaming partners")
    private String inRequestScreeningCaseRef;
    /**
     * Reference to a defined routing case, used for all traffic originating from
     * this external network and its contained roaming partners (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    @JsonPropertyDescription("Reference to a defined routing case, used for all traffic originating from this external network and its contained roaming partners")
    private String routingCaseRef;
    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in this external network and its contained roaming
     * partners
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    @JsonPropertyDescription("Reference to the response screening case that is applied when the response is targeting an NF instance in this external network and its contained roaming partners")
    private String outResponseScreeningCaseRef;
    /**
     * Reference to a defined topology hiding profile for either Pseudo Search
     * Result or IP Address Hiding, applied to all roaming-partners of this network
     * 
     */
    @JsonProperty("topology-hiding-ref")
    @JsonPropertyDescription("Reference to a defined topology hiding profile for either Pseudo Search Result or IP Address Hiding, applied to all roaming-partners of this network")
    private List<String> topologyHidingRef = new ArrayList<String>();
    @JsonProperty("topology-hiding-with-admin-state")
    private List<TopologyHidingWithAdminState> topologyHidingWithAdminState = new ArrayList<TopologyHidingWithAdminState>();
    /**
     * Definition of a roaming partner of this SEPP instance
     * 
     */
    @JsonProperty("roaming-partner")
    @JsonPropertyDescription("Definition of a roaming partner of this SEPP instance")
    private List<RoamingPartner> roamingPartner = new ArrayList<RoamingPartner>();
    /**
     * Reference to a defined ingress-connection-profile
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    @JsonPropertyDescription("Reference to a defined ingress-connection-profile")
    private String ingressConnectionProfileRef;
    /**
     * Reference to a defined local-rate-limit-profile
     * 
     */
    @JsonProperty("local-rate-limit-profile-ref")
    @JsonPropertyDescription("Reference to a defined local-rate-limit-profile")
    private String localRateLimitProfileRef;
    /**
     * Reference to a defined global-rate-limit-profile
     * 
     */
    @JsonProperty("global-ingress-rate-limit-profile-ref")
    @JsonPropertyDescription("Reference to a defined global-rate-limit-profile")
    private List<String> globalIngressRateLimitProfileRef = new ArrayList<String>();
    /**
     * Reference to a firewall profile that is applied to messages coming from NF
     * instances in this external network and its roaming partners.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    @JsonPropertyDescription("Reference to a firewall profile that is applied to messages coming from NF instances in this external network and its roaming partners.")
    private String firewallProfileRef;

    /**
     * Name uniquely identifying the external network (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the external network (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ExternalNetwork withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to a defined service-address (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    public String getServiceAddressRef()
    {
        return serviceAddressRef;
    }

    /**
     * Reference to a defined service-address (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    public void setServiceAddressRef(String serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
    }

    public ExternalNetwork withServiceAddressRef(String serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received from an NF instance in this external network and its contained
     * roaming partners
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public String getInRequestScreeningCaseRef()
    {
        return inRequestScreeningCaseRef;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received from an NF instance in this external network and its contained
     * roaming partners
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public void setInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
    }

    public ExternalNetwork withInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
        return this;
    }

    /**
     * Reference to a defined routing case, used for all traffic originating from
     * this external network and its contained roaming partners (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    public String getRoutingCaseRef()
    {
        return routingCaseRef;
    }

    /**
     * Reference to a defined routing case, used for all traffic originating from
     * this external network and its contained roaming partners (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    public void setRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
    }

    public ExternalNetwork withRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
        return this;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in this external network and its contained roaming
     * partners
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public String getOutResponseScreeningCaseRef()
    {
        return outResponseScreeningCaseRef;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in this external network and its contained roaming
     * partners
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public void setOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
    }

    public ExternalNetwork withOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
        return this;
    }

    /**
     * Reference to a defined topology hiding profile for either Pseudo Search
     * Result or IP Address Hiding, applied to all roaming-partners of this network
     * 
     */
    @JsonProperty("topology-hiding-ref")
    public List<String> getTopologyHidingRef()
    {
        return topologyHidingRef;
    }

    /**
     * Reference to a defined topology hiding profile for either Pseudo Search
     * Result or IP Address Hiding, applied to all roaming-partners of this network
     * 
     */
    @JsonProperty("topology-hiding-ref")
    public void setTopologyHidingRef(List<String> topologyHidingRef)
    {
        this.topologyHidingRef = topologyHidingRef;
    }

    public ExternalNetwork withTopologyHidingRef(List<String> topologyHidingRef)
    {
        this.topologyHidingRef = topologyHidingRef;
        return this;
    }

    @JsonProperty("topology-hiding-with-admin-state")
    public List<TopologyHidingWithAdminState> getTopologyHidingWithAdminState()
    {
        return topologyHidingWithAdminState;
    }

    @JsonProperty("topology-hiding-with-admin-state")
    public void setTopologyHidingWithAdminState(List<TopologyHidingWithAdminState> topologyHidingWithAdminState)
    {
        this.topologyHidingWithAdminState = topologyHidingWithAdminState;
    }

    public ExternalNetwork withTopologyHidingWithAdminState(List<TopologyHidingWithAdminState> topologyHidingWithAdminState)
    {
        this.topologyHidingWithAdminState = topologyHidingWithAdminState;
        return this;
    }

    /**
     * Definition of a roaming partner of this SEPP instance
     * 
     */
    @JsonProperty("roaming-partner")
    public List<RoamingPartner> getRoamingPartner()
    {
        return roamingPartner;
    }

    /**
     * Definition of a roaming partner of this SEPP instance
     * 
     */
    @JsonProperty("roaming-partner")
    public void setRoamingPartner(List<RoamingPartner> roamingPartner)
    {
        this.roamingPartner = roamingPartner;
    }

    public ExternalNetwork withRoamingPartner(List<RoamingPartner> roamingPartner)
    {
        this.roamingPartner = roamingPartner;
        return this;
    }

    /**
     * Reference to a defined ingress-connection-profile
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    public String getIngressConnectionProfileRef()
    {
        return ingressConnectionProfileRef;
    }

    /**
     * Reference to a defined ingress-connection-profile
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    public void setIngressConnectionProfileRef(String ingressConnectionProfileRef)
    {
        this.ingressConnectionProfileRef = ingressConnectionProfileRef;
    }

    public ExternalNetwork withIngressConnectionProfileRef(String ingressConnectionProfileRef)
    {
        this.ingressConnectionProfileRef = ingressConnectionProfileRef;
        return this;
    }

    /**
     * Reference to a defined local-rate-limit-profile
     * 
     */
    @JsonProperty("local-rate-limit-profile-ref")
    public String getLocalRateLimitProfileRef()
    {
        return localRateLimitProfileRef;
    }

    /**
     * Reference to a defined local-rate-limit-profile
     * 
     */
    @JsonProperty("local-rate-limit-profile-ref")
    public void setLocalRateLimitProfileRef(String localRateLimitProfileRef)
    {
        this.localRateLimitProfileRef = localRateLimitProfileRef;
    }

    public ExternalNetwork withLocalRateLimitProfileRef(String localRateLimitProfileRef)
    {
        this.localRateLimitProfileRef = localRateLimitProfileRef;
        return this;
    }

    /**
     * Reference to a defined global-rate-limit-profile
     * 
     */
    @JsonProperty("global-ingress-rate-limit-profile-ref")
    public List<String> getGlobalIngressRateLimitProfileRef()
    {
        return globalIngressRateLimitProfileRef;
    }

    /**
     * Reference to a defined global-rate-limit-profile
     * 
     */
    @JsonProperty("global-ingress-rate-limit-profile-ref")
    public void setGlobalIngressRateLimitProfileRef(List<String> globalIngressRateLimitProfileRef)
    {
        this.globalIngressRateLimitProfileRef = globalIngressRateLimitProfileRef;
    }

    public ExternalNetwork withGlobalIngressRateLimitProfileRef(List<String> globalIngressRateLimitProfileRef)
    {
        this.globalIngressRateLimitProfileRef = globalIngressRateLimitProfileRef;
        return this;
    }

    /**
     * Reference to a firewall profile that is applied to messages coming from NF
     * instances in this external network and its roaming partners.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    public String getFirewallProfileRef()
    {
        return firewallProfileRef;
    }

    /**
     * Reference to a firewall profile that is applied to messages coming from NF
     * instances in this external network and its roaming partners.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    public void setFirewallProfileRef(String firewallProfileRef)
    {
        this.firewallProfileRef = firewallProfileRef;
    }

    public ExternalNetwork withFirewallProfileRef(String firewallProfileRef)
    {
        this.firewallProfileRef = firewallProfileRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ExternalNetwork.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("serviceAddressRef");
        sb.append('=');
        sb.append(((this.serviceAddressRef == null) ? "<null>" : this.serviceAddressRef));
        sb.append(',');
        sb.append("inRequestScreeningCaseRef");
        sb.append('=');
        sb.append(((this.inRequestScreeningCaseRef == null) ? "<null>" : this.inRequestScreeningCaseRef));
        sb.append(',');
        sb.append("routingCaseRef");
        sb.append('=');
        sb.append(((this.routingCaseRef == null) ? "<null>" : this.routingCaseRef));
        sb.append(',');
        sb.append("outResponseScreeningCaseRef");
        sb.append('=');
        sb.append(((this.outResponseScreeningCaseRef == null) ? "<null>" : this.outResponseScreeningCaseRef));
        sb.append(',');
        sb.append("topologyHidingRef");
        sb.append('=');
        sb.append(((this.topologyHidingRef == null) ? "<null>" : this.topologyHidingRef));
        sb.append(',');
        sb.append("topologyHidingWithAdminState");
        sb.append('=');
        sb.append(((this.topologyHidingWithAdminState == null) ? "<null>" : this.topologyHidingWithAdminState));
        sb.append(',');
        sb.append("roamingPartner");
        sb.append('=');
        sb.append(((this.roamingPartner == null) ? "<null>" : this.roamingPartner));
        sb.append(',');
        sb.append("ingressConnectionProfileRef");
        sb.append('=');
        sb.append(((this.ingressConnectionProfileRef == null) ? "<null>" : this.ingressConnectionProfileRef));
        sb.append(',');
        sb.append("localRateLimitProfileRef");
        sb.append('=');
        sb.append(((this.localRateLimitProfileRef == null) ? "<null>" : this.localRateLimitProfileRef));
        sb.append(',');
        sb.append("globalIngressRateLimitProfileRef");
        sb.append('=');
        sb.append(((this.globalIngressRateLimitProfileRef == null) ? "<null>" : this.globalIngressRateLimitProfileRef));
        sb.append(',');
        sb.append("firewallProfileRef");
        sb.append('=');
        sb.append(((this.firewallProfileRef == null) ? "<null>" : this.firewallProfileRef));
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
        result = ((result * 31) + ((this.inRequestScreeningCaseRef == null) ? 0 : this.inRequestScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.globalIngressRateLimitProfileRef == null) ? 0 : this.globalIngressRateLimitProfileRef.hashCode()));
        result = ((result * 31) + ((this.outResponseScreeningCaseRef == null) ? 0 : this.outResponseScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.topologyHidingWithAdminState == null) ? 0 : this.topologyHidingWithAdminState.hashCode()));
        result = ((result * 31) + ((this.routingCaseRef == null) ? 0 : this.routingCaseRef.hashCode()));
        result = ((result * 31) + ((this.localRateLimitProfileRef == null) ? 0 : this.localRateLimitProfileRef.hashCode()));
        result = ((result * 31) + ((this.ingressConnectionProfileRef == null) ? 0 : this.ingressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.roamingPartner == null) ? 0 : this.roamingPartner.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.topologyHidingRef == null) ? 0 : this.topologyHidingRef.hashCode()));
        result = ((result * 31) + ((this.firewallProfileRef == null) ? 0 : this.firewallProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ExternalNetwork) == false)
        {
            return false;
        }
        ExternalNetwork rhs = ((ExternalNetwork) other);
        return (((((((((((((this.inRequestScreeningCaseRef == rhs.inRequestScreeningCaseRef)
                           || ((this.inRequestScreeningCaseRef != null) && this.inRequestScreeningCaseRef.equals(rhs.inRequestScreeningCaseRef)))
                          && ((this.serviceAddressRef == rhs.serviceAddressRef)
                              || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))))
                         && ((this.globalIngressRateLimitProfileRef == rhs.globalIngressRateLimitProfileRef)
                             || ((this.globalIngressRateLimitProfileRef != null)
                                 && this.globalIngressRateLimitProfileRef.equals(rhs.globalIngressRateLimitProfileRef))))
                        && ((this.outResponseScreeningCaseRef == rhs.outResponseScreeningCaseRef)
                            || ((this.outResponseScreeningCaseRef != null) && this.outResponseScreeningCaseRef.equals(rhs.outResponseScreeningCaseRef))))
                       && ((this.topologyHidingWithAdminState == rhs.topologyHidingWithAdminState)
                           || ((this.topologyHidingWithAdminState != null) && this.topologyHidingWithAdminState.equals(rhs.topologyHidingWithAdminState))))
                      && ((this.routingCaseRef == rhs.routingCaseRef) || ((this.routingCaseRef != null) && this.routingCaseRef.equals(rhs.routingCaseRef))))
                     && ((this.localRateLimitProfileRef == rhs.localRateLimitProfileRef)
                         || ((this.localRateLimitProfileRef != null) && this.localRateLimitProfileRef.equals(rhs.localRateLimitProfileRef))))
                    && ((this.ingressConnectionProfileRef == rhs.ingressConnectionProfileRef)
                        || ((this.ingressConnectionProfileRef != null) && this.ingressConnectionProfileRef.equals(rhs.ingressConnectionProfileRef))))
                   && ((this.roamingPartner == rhs.roamingPartner) || ((this.roamingPartner != null) && this.roamingPartner.equals(rhs.roamingPartner))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.topologyHidingRef == rhs.topologyHidingRef)
                     || ((this.topologyHidingRef != null) && this.topologyHidingRef.equals(rhs.topologyHidingRef))))
                && ((this.firewallProfileRef == rhs.firewallProfileRef)
                    || ((this.firewallProfileRef != null) && this.firewallProfileRef.equals(rhs.firewallProfileRef))));
    }

}
