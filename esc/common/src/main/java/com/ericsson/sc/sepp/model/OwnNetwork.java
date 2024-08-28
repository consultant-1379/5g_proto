
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
                     "trusted-certificate-list",
                     "trusted-cert-in-list-ref",
                     "trusted-cert-out-list-ref",
                     "ingress-connection-profile-ref",
                     "local-rate-limit-profile-ref",
                     "global-ingress-rate-limit-profile-ref" })
public class OwnNetwork implements IfNetwork
{

    /**
     * Name uniquely identifying the own network (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the own network")
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
     * received from an NF instance in the own PLMN
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    @JsonPropertyDescription("Reference to the request screening case that is applied when a request is received from an NF instance in the own PLMN")
    private String inRequestScreeningCaseRef;
    /**
     * Reference to a defined routing case, used for all traffic originating from
     * own PLMN (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    @JsonPropertyDescription("Reference to a defined routing case, used for all traffic originating from own PLMN")
    private String routingCaseRef;
    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in the own PLMN
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    @JsonPropertyDescription("Reference to the response screening case that is applied when the response is targeting an NF instance in the own PLMN")
    private String outResponseScreeningCaseRef;
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
     * Name uniquely identifying the own network (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the own network (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public OwnNetwork withName(String name)
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

    public OwnNetwork withServiceAddressRef(String serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received from an NF instance in the own PLMN
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public String getInRequestScreeningCaseRef()
    {
        return inRequestScreeningCaseRef;
    }

    /**
     * Reference to the request screening case that is applied when a request is
     * received from an NF instance in the own PLMN
     * 
     */
    @JsonProperty("in-request-screening-case-ref")
    public void setInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
    }

    public OwnNetwork withInRequestScreeningCaseRef(String inRequestScreeningCaseRef)
    {
        this.inRequestScreeningCaseRef = inRequestScreeningCaseRef;
        return this;
    }

    /**
     * Reference to a defined routing case, used for all traffic originating from
     * own PLMN (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    public String getRoutingCaseRef()
    {
        return routingCaseRef;
    }

    /**
     * Reference to a defined routing case, used for all traffic originating from
     * own PLMN (Required)
     * 
     */
    @JsonProperty("routing-case-ref")
    public void setRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
    }

    public OwnNetwork withRoutingCaseRef(String routingCaseRef)
    {
        this.routingCaseRef = routingCaseRef;
        return this;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in the own PLMN
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public String getOutResponseScreeningCaseRef()
    {
        return outResponseScreeningCaseRef;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * targeting an NF instance in the own PLMN
     * 
     */
    @JsonProperty("out-response-screening-case-ref")
    public void setOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
    }

    public OwnNetwork withOutResponseScreeningCaseRef(String outResponseScreeningCaseRef)
    {
        this.outResponseScreeningCaseRef = outResponseScreeningCaseRef;
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

    public OwnNetwork withTrustedCertificateList(String trustedCertificateList)
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

    public OwnNetwork withTrustedCertInListRef(String trustedCertInListRef)
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

    public OwnNetwork withTrustedCertOutListRef(String trustedCertOutListRef)
    {
        this.trustedCertOutListRef = trustedCertOutListRef;
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

    public OwnNetwork withIngressConnectionProfileRef(String ingressConnectionProfileRef)
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

    public OwnNetwork withLocalRateLimitProfileRef(String localRateLimitProfileRef)
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

    public OwnNetwork withGlobalIngressRateLimitProfileRef(List<String> globalIngressRateLimitProfileRef)
    {
        this.globalIngressRateLimitProfileRef = globalIngressRateLimitProfileRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OwnNetwork.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        result = ((result * 31) + ((this.ingressConnectionProfileRef == null) ? 0 : this.ingressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertificateList == null) ? 0 : this.trustedCertificateList.hashCode()));
        result = ((result * 31) + ((this.globalIngressRateLimitProfileRef == null) ? 0 : this.globalIngressRateLimitProfileRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertInListRef == null) ? 0 : this.trustedCertInListRef.hashCode()));
        result = ((result * 31) + ((this.trustedCertOutListRef == null) ? 0 : this.trustedCertOutListRef.hashCode()));
        result = ((result * 31) + ((this.outResponseScreeningCaseRef == null) ? 0 : this.outResponseScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.routingCaseRef == null) ? 0 : this.routingCaseRef.hashCode()));
        result = ((result * 31) + ((this.localRateLimitProfileRef == null) ? 0 : this.localRateLimitProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OwnNetwork) == false)
        {
            return false;
        }
        OwnNetwork rhs = ((OwnNetwork) other);
        return ((((((((((((this.inRequestScreeningCaseRef == rhs.inRequestScreeningCaseRef)
                          || ((this.inRequestScreeningCaseRef != null) && this.inRequestScreeningCaseRef.equals(rhs.inRequestScreeningCaseRef)))
                         && ((this.ingressConnectionProfileRef == rhs.ingressConnectionProfileRef)
                             || ((this.ingressConnectionProfileRef != null) && this.ingressConnectionProfileRef.equals(rhs.ingressConnectionProfileRef))))
                        && ((this.serviceAddressRef == rhs.serviceAddressRef)
                            || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))))
                       && ((this.trustedCertificateList == rhs.trustedCertificateList)
                           || ((this.trustedCertificateList != null) && this.trustedCertificateList.equals(rhs.trustedCertificateList))))
                      && ((this.globalIngressRateLimitProfileRef == rhs.globalIngressRateLimitProfileRef)
                          || ((this.globalIngressRateLimitProfileRef != null)
                              && this.globalIngressRateLimitProfileRef.equals(rhs.globalIngressRateLimitProfileRef))))
                     && ((this.trustedCertInListRef == rhs.trustedCertInListRef)
                         || ((this.trustedCertInListRef != null) && this.trustedCertInListRef.equals(rhs.trustedCertInListRef))))
                    && ((this.trustedCertOutListRef == rhs.trustedCertOutListRef)
                        || ((this.trustedCertOutListRef != null) && this.trustedCertOutListRef.equals(rhs.trustedCertOutListRef))))
                   && ((this.outResponseScreeningCaseRef == rhs.outResponseScreeningCaseRef)
                       || ((this.outResponseScreeningCaseRef != null) && this.outResponseScreeningCaseRef.equals(rhs.outResponseScreeningCaseRef))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.routingCaseRef == rhs.routingCaseRef) || ((this.routingCaseRef != null) && this.routingCaseRef.equals(rhs.routingCaseRef))))
                && ((this.localRateLimitProfileRef == rhs.localRateLimitProfileRef)
                    || ((this.localRateLimitProfileRef != null) && this.localRateLimitProfileRef.equals(rhs.localRateLimitProfileRef))));
    }

}
