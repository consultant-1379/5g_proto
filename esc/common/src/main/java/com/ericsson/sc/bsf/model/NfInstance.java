
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfGenericNfInstance;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nf-peer-info",
                     "nf-instance-id",
                     "dns-profile",
                     "nf-profile",
                     "service-address",
                     "oauth2-key-profile",
                     "ingress-connection-profile",
                     "ingress-connection-profile-ref",
                     "nrf-group",
                     "nrf-service",
                     "bsf-service",
                     "vtap" })
public class NfInstance implements IfGenericNfInstance
{

    /**
     * Name uniquely identifying the BSF instance (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the BSF instance")
    private String name;
    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    @JsonPropertyDescription("Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level")
    private NfPeerInfo nfPeerInfo;
    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the BSF registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("A Universally Unique IDentifier in the string representation defined in RFC 4122. The canonical representation uses lower case characters. The following is an example of a UUID in string representation: f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It has a meaning for how the BSF registers in its NRF(s): If not set, an automatically generated NF instance ID (a different one for each NRF group) is used for the registration. If set, this is used for the registration rather than the automatically generated NF instance IDs of the NRF groups.")
    private String nfInstanceId;
    /**
     * Settings for DNS resolution.
     * 
     */
    @JsonProperty("dns-profile")
    @JsonPropertyDescription("Settings for DNS resolution.")
    private List<DnsProfile> dnsProfile = new ArrayList<DnsProfile>();
    /**
     * Profile consisting of general parameters of the BSF instance and the services
     * provided by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("Profile consisting of general parameters of the BSF instance and the services provided by it, sent to the NRF at registration")
    private List<NfProfile> nfProfile = new ArrayList<NfProfile>();
    /**
     * Address of the BSF, referenced in the nf-profile sent to the NRF
     * 
     */
    @JsonProperty("service-address")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("Address of the BSF, referenced in the nf-profile sent to the NRF")
    private List<ServiceAddress> serviceAddress = new ArrayList<ServiceAddress>();
    /**
     * The oAuth2 key profile of the BSF
     * 
     */
    @JsonProperty("oauth2-key-profile")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("oauth2-key-profile of the BSF, referenced in the bsf-function")
    private List<Oauth2KeyProfile> oauth2KeyProfile = new ArrayList<Oauth2KeyProfile>();
    /**
     * Connection profile for ingress traffic cases
     * 
     */
    @JsonProperty("ingress-connection-profile")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("Connection profile for ingress traffic cases")
    private List<IngressConnectionProfile> ingressConnectionProfile = new ArrayList<IngressConnectionProfile>();
    /**
     * Reference to a defined ingress-connection-profile
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    @JsonPropertyDescription("Reference to a defined ingress-connection-profile")
    private String ingressConnectionProfileRef;
    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this BSF instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile.
     * 
     */
    @JsonProperty("nrf-group")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("The NRF group specifies all relevant information of NRFs that are available to this BSF instance. Regarding the Nnrf_NFManagement interface, it determines the NRF registration behavior and triggers the NRF registration for the NF instance specified in the nf-profile.")
    private List<NrfGroup> nrfGroup = new ArrayList<NrfGroup>();
    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service.
     * 
     */
    @JsonProperty("nrf-service")
    @JsonPropertyDescription("Defines which NRF services are used by this NF instance, referencing groups of NRFs that provide the respective service.")
    private NrfService nrfService;
    /**
     * BSF service related properties.
     * 
     */
    @JsonProperty("bsf-service")
    @JsonDeserialize(as = java.util.ArrayList.class)
    @JsonPropertyDescription("BSF service related properties.")
    private List<BsfService> bsfService = new ArrayList<BsfService>();
    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    @JsonPropertyDescription("Defines the required data for traffic tapping")
    private Vtap vtap;

    /**
     * Name uniquely identifying the BSF instance (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the BSF instance (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfInstance withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    public NfPeerInfo getNfPeerInfo()
    {
        return nfPeerInfo;
    }

    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    public void setNfPeerInfo(NfPeerInfo nfPeerInfo)
    {
        this.nfPeerInfo = nfPeerInfo;
    }

    public NfInstance withNfPeerInfo(NfPeerInfo nfPeerInfo)
    {
        this.nfPeerInfo = nfPeerInfo;
        return this;
    }

    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the BSF registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the BSF registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public NfInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * Settings for DNS resolution.
     * 
     */
    @JsonProperty("dns-profile")
    public List<DnsProfile> getDnsProfile()
    {
        return dnsProfile;
    }

    /**
     * Settings for DNS resolution.
     * 
     */
    @JsonProperty("dns-profile")
    public void setDnsProfile(List<DnsProfile> dnsProfile)
    {
        this.dnsProfile = dnsProfile;
    }

    public NfInstance withDnsProfile(List<DnsProfile> dnsProfile)
    {
        this.dnsProfile = dnsProfile;
        return this;
    }

    /**
     * Profile consisting of general parameters of the BSF instance and the services
     * provided by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    public List<NfProfile> getNfProfile()
    {
        return nfProfile;
    }

    /**
     * Profile consisting of general parameters of the BSF instance and the services
     * provided by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    public void setNfProfile(List<NfProfile> nfProfile)
    {
        this.nfProfile = nfProfile;
    }

    public NfInstance withNfProfile(List<NfProfile> nfProfile)
    {
        this.nfProfile = nfProfile;
        return this;
    }

    /**
     * Address of the BSF, referenced in the nf-profile sent to the NRF
     * 
     */
    @JsonProperty("service-address")
    public List<ServiceAddress> getServiceAddress()
    {
        return serviceAddress;
    }

    /**
     * Address of the BSF, referenced in the nf-profile sent to the NRF
     * 
     */
    @JsonProperty("service-address")
    public void setServiceAddress(List<ServiceAddress> serviceAddress)
    {
        this.serviceAddress = serviceAddress;
    }

    public NfInstance withServiceAddress(List<ServiceAddress> serviceAddress)
    {
        this.serviceAddress = serviceAddress;
        return this;
    }

    /**
     * The oAuth2 key profile of the BSF
     * 
     */
    @JsonProperty("oauth2-key-profile")
    public List<Oauth2KeyProfile> getOauth2KeyProfile()
    {
        return oauth2KeyProfile;
    }

    /**
     * oauth2-key-profile of the BSF, referenced in the bsf-function
     * 
     */
    @JsonProperty("oauth2-key-profile")
    public void setOauth2KeyProfile(List<Oauth2KeyProfile> oauth2KeyProfile)
    {
        this.oauth2KeyProfile = oauth2KeyProfile;
    }

    public NfInstance withOauth2KeyProfile(List<Oauth2KeyProfile> oauth2KeyProfile)
    {
        this.oauth2KeyProfile = oauth2KeyProfile;
        return this;
    }

    /**
     * Connection profile for ingress traffic cases
     * 
     */
    @JsonProperty("ingress-connection-profile")
    public List<IngressConnectionProfile> getIngressConnectionProfile()
    {
        return ingressConnectionProfile;
    }

    /**
     * Connection profile for ingress traffic cases
     * 
     */
    @JsonProperty("ingress-connection-profile")
    public void setIngressConnectionProfile(List<IngressConnectionProfile> ingressConnectionProfile)
    {
        this.ingressConnectionProfile = ingressConnectionProfile;
    }

    public NfInstance withIngressConnectionProfile(List<IngressConnectionProfile> ingressConnectionProfile)
    {
        this.ingressConnectionProfile = ingressConnectionProfile;
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

    public NfInstance withIngressConnectionProfileRef(String ingressConnectionProfileRef)
    {
        this.ingressConnectionProfileRef = ingressConnectionProfileRef;
        return this;
    }

    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this BSF instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile.
     * 
     */
    @JsonProperty("nrf-group")
    public List<NrfGroup> getNrfGroup()
    {
        return nrfGroup;
    }

    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this BSF instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile.
     * 
     */
    @JsonProperty("nrf-group")
    public void setNrfGroup(List<NrfGroup> nrfGroup)
    {
        this.nrfGroup = nrfGroup;
    }

    public NfInstance withNrfGroup(List<NrfGroup> nrfGroup)
    {
        this.nrfGroup = nrfGroup;
        return this;
    }

    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service.
     * 
     */
    @JsonProperty("nrf-service")
    public NrfService getNrfService()
    {
        return nrfService;
    }

    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service.
     * 
     */
    @JsonProperty("nrf-service")
    public void setNrfService(NrfService nrfService)
    {
        this.nrfService = nrfService;
    }

    public NfInstance withNrfService(NrfService nrfService)
    {
        this.nrfService = nrfService;
        return this;
    }

    /**
     * BSF service related properties.
     * 
     */
    @JsonProperty("bsf-service")
    public List<BsfService> getBsfService()
    {
        return bsfService;
    }

    /**
     * BSF service related properties.
     * 
     */
    @JsonProperty("bsf-service")
    public void setBsfService(List<BsfService> bsfService)
    {
        this.bsfService = bsfService;
    }

    public NfInstance withBsfService(List<BsfService> bsfService)
    {
        this.bsfService = bsfService;
        return this;
    }

    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    public Vtap getVtap()
    {
        return vtap;
    }

    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    public void setVtap(Vtap vtap)
    {
        this.vtap = vtap;
    }

    public NfInstance withVtap(Vtap vtap)
    {
        this.vtap = vtap;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfPeerInfo");
        sb.append('=');
        sb.append(((this.nfPeerInfo == null) ? "<null>" : this.nfPeerInfo));
        sb.append(',');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("dnsProfile");
        sb.append('=');
        sb.append(((this.dnsProfile == null) ? "<null>" : this.dnsProfile));
        sb.append(',');
        sb.append("nfProfile");
        sb.append('=');
        sb.append(((this.nfProfile == null) ? "<null>" : this.nfProfile));
        sb.append(',');
        sb.append("serviceAddress");
        sb.append('=');
        sb.append(((this.serviceAddress == null) ? "<null>" : this.serviceAddress));
        sb.append(',');
        sb.append("oauth2KeyProfile");
        sb.append('=');
        sb.append(((this.oauth2KeyProfile == null) ? "<null>" : this.oauth2KeyProfile));
        sb.append(',');
        sb.append("ingressConnectionProfile");
        sb.append('=');
        sb.append(((this.ingressConnectionProfile == null) ? "<null>" : this.ingressConnectionProfile));
        sb.append(',');
        sb.append("ingressConnectionProfileRef");
        sb.append('=');
        sb.append(((this.ingressConnectionProfileRef == null) ? "<null>" : this.ingressConnectionProfileRef));
        sb.append(',');
        sb.append("nrfGroup");
        sb.append('=');
        sb.append(((this.nrfGroup == null) ? "<null>" : this.nrfGroup));
        sb.append(',');
        sb.append("nrfService");
        sb.append('=');
        sb.append(((this.nrfService == null) ? "<null>" : this.nrfService));
        sb.append(',');
        sb.append("bsfService");
        sb.append('=');
        sb.append(((this.bsfService == null) ? "<null>" : this.bsfService));
        sb.append(',');
        sb.append("vtap");
        sb.append('=');
        sb.append(((this.vtap == null) ? "<null>" : this.vtap));
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
        result = ((result * 31) + ((this.nfPeerInfo == null) ? 0 : this.nfPeerInfo.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.dnsProfile == null) ? 0 : this.dnsProfile.hashCode()));
        result = ((result * 31) + ((this.oauth2KeyProfile == null) ? 0 : this.oauth2KeyProfile.hashCode()));
        result = ((result * 31) + ((this.serviceAddress == null) ? 0 : this.serviceAddress.hashCode()));
        result = ((result * 31) + ((this.nrfGroup == null) ? 0 : this.nrfGroup.hashCode()));
        result = ((result * 31) + ((this.nrfService == null) ? 0 : this.nrfService.hashCode()));
        result = ((result * 31) + ((this.bsfService == null) ? 0 : this.bsfService.hashCode()));
        result = ((result * 31) + ((this.ingressConnectionProfileRef == null) ? 0 : this.ingressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.nfProfile == null) ? 0 : this.nfProfile.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.ingressConnectionProfile == null) ? 0 : this.ingressConnectionProfile.hashCode()));
        result = ((result * 31) + ((this.vtap == null) ? 0 : this.vtap.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfInstance) == false)
        {
            return false;
        }
        NfInstance rhs = ((NfInstance) other);
        return ((((((((((((((this.nfPeerInfo == rhs.nfPeerInfo) || ((this.nfPeerInfo != null) && this.nfPeerInfo.equals(rhs.nfPeerInfo)))
                           && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                          && ((this.dnsProfile == rhs.dnsProfile) || ((this.dnsProfile != null) && this.dnsProfile.equals(rhs.dnsProfile))))
                         && ((this.oauth2KeyProfile == rhs.oauth2KeyProfile)
                             || ((this.oauth2KeyProfile != null) && this.oauth2KeyProfile.equals(rhs.oauth2KeyProfile))))
                        && ((this.serviceAddress == rhs.serviceAddress) || ((this.serviceAddress != null) && this.serviceAddress.equals(rhs.serviceAddress))))
                       && ((this.nrfGroup == rhs.nrfGroup) || ((this.nrfGroup != null) && this.nrfGroup.equals(rhs.nrfGroup))))
                      && ((this.nrfService == rhs.nrfService) || ((this.nrfService != null) && this.nrfService.equals(rhs.nrfService))))
                     && ((this.bsfService == rhs.bsfService) || ((this.bsfService != null) && this.bsfService.equals(rhs.bsfService))))
                    && ((this.ingressConnectionProfileRef == rhs.ingressConnectionProfileRef)
                        || ((this.ingressConnectionProfileRef != null) && this.ingressConnectionProfileRef.equals(rhs.ingressConnectionProfileRef))))
                   && ((this.nfProfile == rhs.nfProfile) || ((this.nfProfile != null) && this.nfProfile.equals(rhs.nfProfile))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.ingressConnectionProfile == rhs.ingressConnectionProfile)
                     || ((this.ingressConnectionProfile != null) && this.ingressConnectionProfile.equals(rhs.ingressConnectionProfile))))
                && ((this.vtap == rhs.vtap) || ((this.vtap != null) && this.vtap.equals(rhs.vtap))));
    }

}
