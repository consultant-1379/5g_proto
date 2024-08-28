
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "comment",
                     "user-label",
                     "in-request-screening-case-ref",
                     "routing-case-ref",
                     "topology-hiding-ref",
                     "topology-hiding-with-admin-state",
                     "out-response-screening-case-ref",
                     "global-ingress-rate-limit-profile-ref",
                     "domain-name",
                     "trusted-certificate-list",
                     "trusted-cert-in-list-ref",
                     "trusted-cert-out-list-ref",
                     "supports-target-apiroot",
                     "n32-c",
                     "firewall-profile-ref" })
public class RoamingPartner implements IfNamedListItem
{

    /**
     * Name identifying the roaming partner (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the roaming partner")
    private String name;
    /**
     * Deprecated, use user-label instead. Some space for comments
     * 
     */
    @JsonProperty("comment")
    @JsonPropertyDescription("Deprecated, use user-label instead. Some space for comments")
    private String comment;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Reference to the request screening case that is applied when a request is
     * received originating from this roaming partner. If not defined, the
     * corresponding screening-case referenced at external network level is used per
     * default
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    @JsonPropertyDescription("Reference to the request screening case that is applied when a request is received originating from this roaming partner. If not defined, the corresponding screening-case referenced at external network level is used per default")
    private String inRequestScreeningCaseRef;
    /**
     * Reference to a defined routing case, used for requests originating from this
     * roaming partner. If not defined, the routing-case referenced at external
     * network level is used per default.
     * 
     */
    @JsonProperty("routing-case-ref")
    @JsonPropertyDescription("Reference to a defined routing case, used for requests originating from this roaming partner. If not defined, the routing-case referenced at external network level is used per default.")
    private String routingCaseRef;
    /**
     * Reference to a defined topology hiding profile for Pseudo Search Result or IP
     * Address Hiding, applied to this roaming partner
     * 
     */
    @JsonProperty("topology-hiding-ref")
    @JsonPropertyDescription("Reference to a defined topology hiding profile for Pseudo Search Result or IP Address Hiding, applied to this roaming partner")
    private List<String> topologyHidingRef = new ArrayList<String>();
    @JsonProperty("topology-hiding-with-admin-state")
    private List<TopologyHidingWithAdminState> topologyHidingWithAdminState = new ArrayList<TopologyHidingWithAdminState>();
    /**
     * Reference to the response screening case that is applied when the response is
     * targeting this roaming partner. If not defined, the corresponding
     * screening-case referenced at external network level is used per default
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    @JsonPropertyDescription("Reference to the response screening case that is applied when the response is targeting this roaming partner. If not defined, the corresponding screening-case referenced at external network level is used per default")
    private String outResponseScreeningCaseRef;
    /**
     * Reference to a defined global-rate-limit-profile
     * 
     */
    @JsonProperty("global-ingress-rate-limit-profile-ref")
    @JsonPropertyDescription("Reference to a defined global-rate-limit-profile")
    private List<String> globalIngressRateLimitProfileRef = new ArrayList<String>();
    /**
     * The domains that this roaming-partner is associated with. The values are
     * matched with the Subject Alternative Names received within the certificates.
     * (Required)
     * 
     */
    @JsonProperty("domain-name")
    @JsonPropertyDescription("The domains that this roaming-partner is associated with. The values are matched with the Subject Alternative Names received within the certificates.")
    private List<String> domainName = new ArrayList<String>();
    /**
     * Deprecated, name of the installed trusted certificate group defined in the
     * ietf-truststore
     * 
     */
    @JsonProperty("trusted-certificate-list")
    @JsonPropertyDescription("Deprecated, name of the installed trusted certificate group defined in the ietf-truststore")
    private String trustedCertificateList;
    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding incoming requests
     * 
     */
    @JsonProperty("trusted-cert-in-list-ref")
    @JsonPropertyDescription("Reference to the trusted-cert-list that reference to the installed trusted certificate group defined in the ietf-truststore regarding incoming requests")
    private String trustedCertInListRef;
    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding outgoing requests
     * 
     */
    @JsonProperty("trusted-cert-out-list-ref")
    @JsonPropertyDescription("Reference to the trusted-cert-list that reference to the installed trusted certificate group defined in the ietf-truststore regarding outgoing requests")
    private String trustedCertOutListRef;
    /**
     * Indicates if this roaming-partner?s SEPP supports the 3gpp-Sbi-Target-apiRoot
     * header or not. Ignored if N32-C applies. (Required)
     * 
     */
    @JsonProperty("supports-target-apiroot")
    @JsonPropertyDescription("Indicates if this roaming-partner?s SEPP supports the 3gpp-Sbi-Target-apiRoot header or not. Ignored if N32-C applies.")
    private RoamingPartner.SupportsTargetApiroot supportsTargetApiroot;
    /**
     * Defines data to be used for the N32 handshake procedure between the SEPPs in
     * two PLMNs.
     * 
     */
    @JsonProperty("n32-c")
    @JsonPropertyDescription("Defines data to be used for the N32 handshake procedure between the SEPPs in two PLMNs.")
    private N32C__1 n32C;
    /**
     * Reference to a firewall profile that is applied to messages coming from this
     * roaming partner, replacing the firewall profile from external network.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    @JsonPropertyDescription("Reference to a firewall profile that is applied to messages coming from this roaming partner, replacing the firewall profile from external network.")
    private String firewallProfileRef;

    /**
     * Name identifying the roaming partner (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the roaming partner (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RoamingPartner withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Deprecated, use user-label instead. Some space for comments
     * 
     */
    @JsonProperty("comment")
    public String getComment()
    {
        return comment;
    }

    /**
     * Deprecated, use user-label instead. Some space for comments
     * 
     */
    @JsonProperty("comment")
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public RoamingPartner withComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public RoamingPartner withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received originating from this roaming partner. If not defined, the
     * corresponding screening-case referenced at external network level is used per
     * default
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public String getInRequestScreeningCaseRef()
    {
        return inRequestScreeningCaseRef;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received originating from this roaming partner. If not defined, the
     * corresponding screening-case referenced at external network level is used per
     * default
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public void setInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
    }

    public RoamingPartner withInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
        return this;
    }

    /**
     * Reference to a defined routing case, used for requests originating from this
     * roaming partner. If not defined, the routing-case referenced at external
     * network level is used per default.
     * 
     */
    @JsonProperty("routing-case-ref")
    public String getRoutingCaseRef()
    {
        return routingCaseRef;
    }

    /**
     * Reference to a defined routing case, used for requests originating from this
     * roaming partner. If not defined, the routing-case referenced at external
     * network level is used per default.
     * 
     */
    @JsonProperty("routing-case-ref")
    public void setRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
    }

    public RoamingPartner withRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
        return this;
    }

    /**
     * Reference to a defined topology hiding profile for Pseudo Search Result or IP
     * Address Hiding, applied to this roaming partner
     * 
     */
    @JsonProperty("topology-hiding-ref")
    public List<String> getTopologyHidingRef()
    {
        return topologyHidingRef;
    }

    /**
     * Reference to a defined topology hiding profile for Pseudo Search Result or IP
     * Address Hiding, applied to this roaming partner
     * 
     */
    @JsonProperty("topology-hiding-ref")
    public void setTopologyHidingRef(List<String> topologyHidingRef)
    {
        this.topologyHidingRef = topologyHidingRef;
    }

    public RoamingPartner withTopologyHidingRef(List<String> topologyHidingRef)
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

    public RoamingPartner withTopologyHidingWithAdminState(List<TopologyHidingWithAdminState> topologyHidingWithAdminState)
    {
        this.topologyHidingWithAdminState = topologyHidingWithAdminState;
        return this;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting this roaming partner. If not defined, the corresponding
     * screening-case referenced at external network level is used per default
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public String getOutResponseScreeningCaseRef()
    {
        return outResponseScreeningCaseRef;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting this roaming partner. If not defined, the corresponding
     * screening-case referenced at external network level is used per default
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public void setOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
    }

    public RoamingPartner withOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
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

    public RoamingPartner withGlobalIngressRateLimitProfileRef(List<String> globalIngressRateLimitProfileRef)
    {
        this.globalIngressRateLimitProfileRef = globalIngressRateLimitProfileRef;
        return this;
    }

    /**
     * The domains that this roaming-partner is associated with. The values are
     * matched with the Subject Alternative Names received within the certificates.
     * (Required)
     * 
     */
    @JsonProperty("domain-name")
    public List<String> getDomainName()
    {
        return domainName;
    }

    /**
     * The domains that this roaming-partner is associated with. The values are
     * matched with the Subject Alternative Names received within the certificates.
     * (Required)
     * 
     */
    @JsonProperty("domain-name")
    public void setDomainName(List<String> domainName)
    {
        this.domainName = domainName;
    }

    public RoamingPartner withDomainName(List<String> domainName)
    {
        this.domainName = domainName;
        return this;
    }

    /**
     * Deprecated, name of the installed trusted certificate group defined in the
     * ietf-truststore
     * 
     */
    @JsonProperty("trusted-certificate-list")
    public String getTrustedCertificateList()
    {
        return trustedCertificateList;
    }

    /**
     * Deprecated, name of the installed trusted certificate group defined in the
     * ietf-truststore
     * 
     */
    @JsonProperty("trusted-certificate-list")
    public void setTrustedCertificateList(String trustedCertificateList)
    {
        this.trustedCertificateList = trustedCertificateList;
    }

    public RoamingPartner withTrustedCertificateList(String trustedCertificateList)
    {
        this.trustedCertificateList = trustedCertificateList;
        return this;
    }

    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding incoming requests
     * 
     */
    @JsonProperty("trusted-cert-in-list-ref")
    public String getTrustedCertInListRef()
    {
        return trustedCertInListRef;
    }

    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding incoming requests
     * 
     */
    @JsonProperty("trusted-cert-in-list-ref")
    public void setTrustedCertInListRef(String trustedCertInListRef)
    {
        this.trustedCertInListRef = trustedCertInListRef;
    }

    public RoamingPartner withTrustedCertInListRef(String trustedCertInListRef)
    {
        this.trustedCertInListRef = trustedCertInListRef;
        return this;
    }

    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding outgoing requests
     * 
     */
    @JsonProperty("trusted-cert-out-list-ref")
    public String getTrustedCertOutListRef()
    {
        return trustedCertOutListRef;
    }

    /**
     * Reference to the trusted-cert-list that reference to the installed trusted
     * certificate group defined in the ietf-truststore regarding outgoing requests
     * 
     */
    @JsonProperty("trusted-cert-out-list-ref")
    public void setTrustedCertOutListRef(String trustedCertOutListRef)
    {
        this.trustedCertOutListRef = trustedCertOutListRef;
    }

    public RoamingPartner withTrustedCertOutListRef(String trustedCertOutListRef)
    {
        this.trustedCertOutListRef = trustedCertOutListRef;
        return this;
    }

    /**
     * Indicates if this roaming-partner?s SEPP supports the 3gpp-Sbi-Target-apiRoot
     * header or not. Ignored if N32-C applies. (Required)
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public RoamingPartner.SupportsTargetApiroot getSupportsTargetApiroot()
    {
        return supportsTargetApiroot;
    }

    /**
     * Indicates if this roaming-partner?s SEPP supports the 3gpp-Sbi-Target-apiRoot
     * header or not. Ignored if N32-C applies. (Required)
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public void setSupportsTargetApiroot(RoamingPartner.SupportsTargetApiroot supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
    }

    public RoamingPartner withSupportsTargetApiroot(RoamingPartner.SupportsTargetApiroot supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
        return this;
    }

    /**
     * Defines data to be used for the N32 handshake procedure between the SEPPs in
     * two PLMNs.
     * 
     */
    @JsonProperty("n32-c")
    public N32C__1 getN32C()
    {
        return n32C;
    }

    /**
     * Defines data to be used for the N32 handshake procedure between the SEPPs in
     * two PLMNs.
     * 
     */
    @JsonProperty("n32-c")
    public void setN32C(N32C__1 n32C)
    {
        this.n32C = n32C;
    }

    public RoamingPartner withN32C(N32C__1 n32C)
    {
        this.n32C = n32C;
        return this;
    }

    /**
     * Reference to a firewall profile that is applied to messages coming from this
     * roaming partner, replacing the firewall profile from external network.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    public String getFirewallProfileRef()
    {
        return firewallProfileRef;
    }

    /**
     * Reference to a firewall profile that is applied to messages coming from this
     * roaming partner, replacing the firewall profile from external network.
     * 
     */
    @JsonProperty("firewall-profile-ref")
    public void setFirewallProfileRef(String firewallProfileRef)
    {
        this.firewallProfileRef = firewallProfileRef;
    }

    public RoamingPartner withFirewallProfileRef(String firewallProfileRef)
    {
        this.firewallProfileRef = firewallProfileRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoamingPartner.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("comment");
        sb.append('=');
        sb.append(((this.comment == null) ? "<null>" : this.comment));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("inRequestScreeningCaseRef");
        sb.append('=');
        sb.append(((this.inRequestScreeningCaseRef == null) ? "<null>" : this.inRequestScreeningCaseRef));
        sb.append(',');
        sb.append("routingCaseRef");
        sb.append('=');
        sb.append(((this.routingCaseRef == null) ? "<null>" : this.routingCaseRef));
        sb.append(',');
        sb.append("topologyHidingRef");
        sb.append('=');
        sb.append(((this.topologyHidingRef == null) ? "<null>" : this.topologyHidingRef));
        sb.append(',');
        sb.append("topologyHidingWithAdminState");
        sb.append('=');
        sb.append(((this.topologyHidingWithAdminState == null) ? "<null>" : this.topologyHidingWithAdminState));
        sb.append(',');
        sb.append("outResponseScreeningCaseRef");
        sb.append('=');
        sb.append(((this.outResponseScreeningCaseRef == null) ? "<null>" : this.outResponseScreeningCaseRef));
        sb.append(',');
        sb.append("globalIngressRateLimitProfileRef");
        sb.append('=');
        sb.append(((this.globalIngressRateLimitProfileRef == null) ? "<null>" : this.globalIngressRateLimitProfileRef));
        sb.append(',');
        sb.append("domainName");
        sb.append('=');
        sb.append(((this.domainName == null) ? "<null>" : this.domainName));
        sb.append(',');
        sb.append("trustedCertificateList");
        sb.append('=');
        sb.append(((this.trustedCertificateList == null) ? "<null>" : this.trustedCertificateList));
        sb.append(',');
        sb.append("trustedCertInListRef");
        sb.append('=');
        sb.append(((this.trustedCertInListRef == null) ? "<null>" : this.trustedCertInListRef));
        sb.append(',');
        sb.append("trustedCertOutListRef");
        sb.append('=');
        sb.append(((this.trustedCertOutListRef == null) ? "<null>" : this.trustedCertOutListRef));
        sb.append(',');
        sb.append("supportsTargetApiroot");
        sb.append('=');
        sb.append(((this.supportsTargetApiroot == null) ? "<null>" : this.supportsTargetApiroot));
        sb.append(',');
        sb.append("n32C");
        sb.append('=');
        sb.append(((this.n32C == null) ? "<null>" : this.n32C));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.inRequestScreeningCaseRef == null) ? 0 : this.inRequestScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.globalIngressRateLimitProfileRef == null) ? 0 : this.globalIngressRateLimitProfileRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertInListRef == null) ? 0 : this.trustedCertInListRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertOutListRef == null) ? 0 : this.trustedCertOutListRef.hashCode()));
        result = ((result * 31) + ((this.n32C == null) ? 0 : this.n32C.hashCode()));
        result = ((result * 31) + ((this.outResponseScreeningCaseRef == null) ? 0 : this.outResponseScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.topologyHidingWithAdminState == null) ? 0 : this.topologyHidingWithAdminState.hashCode()));
        result = ((result * 31) + ((this.routingCaseRef == null) ? 0 : this.routingCaseRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertificateList == null) ? 0 : this.trustedCertificateList.hashCode()));
        result = ((result * 31) + ((this.domainName == null) ? 0 : this.domainName.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.topologyHidingRef == null) ? 0 : this.topologyHidingRef.hashCode()));
        result = ((result * 31) + ((this.supportsTargetApiroot == null) ? 0 : this.supportsTargetApiroot.hashCode()));
        result = ((result * 31) + ((this.comment == null) ? 0 : this.comment.hashCode()));
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
        if ((other instanceof RoamingPartner) == false)
        {
            return false;
        }
        RoamingPartner rhs = ((RoamingPartner) other);
        return (((((((((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                              && ((this.inRequestScreeningCaseRef == rhs.inRequestScreeningCaseRef)
                                  || ((this.inRequestScreeningCaseRef != null) && this.inRequestScreeningCaseRef.equals(rhs.inRequestScreeningCaseRef))))
                             && ((this.globalIngressRateLimitProfileRef == rhs.globalIngressRateLimitProfileRef)
                                 || ((this.globalIngressRateLimitProfileRef != null)
                                     && this.globalIngressRateLimitProfileRef.equals(rhs.globalIngressRateLimitProfileRef))))
                            && ((this.trustedCertInListRef == rhs.trustedCertInListRef)
                                || ((this.trustedCertInListRef != null) && this.trustedCertInListRef.equals(rhs.trustedCertInListRef))))
                           && ((this.trustedCertOutListRef == rhs.trustedCertOutListRef)
                               || ((this.trustedCertOutListRef != null) && this.trustedCertOutListRef.equals(rhs.trustedCertOutListRef))))
                          && ((this.n32C == rhs.n32C) || ((this.n32C != null) && this.n32C.equals(rhs.n32C))))
                         && ((this.outResponseScreeningCaseRef == rhs.outResponseScreeningCaseRef)
                             || ((this.outResponseScreeningCaseRef != null) && this.outResponseScreeningCaseRef.equals(rhs.outResponseScreeningCaseRef))))
                        && ((this.topologyHidingWithAdminState == rhs.topologyHidingWithAdminState)
                            || ((this.topologyHidingWithAdminState != null) && this.topologyHidingWithAdminState.equals(rhs.topologyHidingWithAdminState))))
                       && ((this.routingCaseRef == rhs.routingCaseRef) || ((this.routingCaseRef != null) && this.routingCaseRef.equals(rhs.routingCaseRef))))
                      && ((this.trustedCertificateList == rhs.trustedCertificateList)
                          || ((this.trustedCertificateList != null) && this.trustedCertificateList.equals(rhs.trustedCertificateList))))
                     && ((this.domainName == rhs.domainName) || ((this.domainName != null) && this.domainName.equals(rhs.domainName))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.topologyHidingRef == rhs.topologyHidingRef)
                       || ((this.topologyHidingRef != null) && this.topologyHidingRef.equals(rhs.topologyHidingRef))))
                  && ((this.supportsTargetApiroot == rhs.supportsTargetApiroot)
                      || ((this.supportsTargetApiroot != null) && this.supportsTargetApiroot.equals(rhs.supportsTargetApiroot))))
                 && ((this.comment == rhs.comment) || ((this.comment != null) && this.comment.equals(rhs.comment))))
                && ((this.firewallProfileRef == rhs.firewallProfileRef)
                    || ((this.firewallProfileRef != null) && this.firewallProfileRef.equals(rhs.firewallProfileRef))));
    }

    public enum SupportsTargetApiroot
    {

        TRUE("true"),
        FALSE("false"),
        NEGOTIATED("negotiated");

        private final String value;
        private final static Map<String, RoamingPartner.SupportsTargetApiroot> CONSTANTS = new HashMap<String, RoamingPartner.SupportsTargetApiroot>();

        static
        {
            for (RoamingPartner.SupportsTargetApiroot c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private SupportsTargetApiroot(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static RoamingPartner.SupportsTargetApiroot fromValue(String value)
        {
            RoamingPartner.SupportsTargetApiroot constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
