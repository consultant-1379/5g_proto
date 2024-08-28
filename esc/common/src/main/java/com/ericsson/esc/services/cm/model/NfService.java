
package com.ericsson.esc.services.cm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "service-instance-id",
                     "service-name",
                     "scheme",
                     "service-address-ref",
                     "admin-state",
                     "api-prefix",
                     "allowed-plmn",
                     "allowed-nf-type",
                     "allowed-nf-domain",
                     "allowed-nssai",
                     "service-priority",
                     "capacity",
                     "recovery-time" })
public class NfService
{
    public static class Version
    {
        private String apiVersionInUri;
        private String apiFullVersion;

        public String getApiVersionInUri()
        {
            return this.apiVersionInUri;
        }

        public String getApiFullVersion()
        {
            return this.apiFullVersion;
        }

        public void setApiVersionInUri(final String apiVersionInUri)
        {
            this.apiVersionInUri = apiVersionInUri;
        }

        public void setApiFullVersion(final String apiFullVersion)
        {
            this.apiFullVersion = apiFullVersion;
        }

        public Version withApiVersionInUri(final String apiVersionInUri)
        {
            this.apiVersionInUri = apiVersionInUri;
            return this;
        }

        public Version withApiFullVersion(final String apiFullVersion)
        {
            this.apiFullVersion = apiFullVersion;
            return this;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Version.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
            sb.append("apiVersionInUri");
            sb.append('=');
            sb.append(((this.apiVersionInUri == null) ? "<null>" : this.apiVersionInUri));
            sb.append(',');
            sb.append("apiFullVersion");
            sb.append('=');
            sb.append(((this.apiFullVersion == null) ? "<null>" : this.apiFullVersion));
            sb.append(',');

            if (sb.charAt((sb.length() - 1)) == ',')
                sb.setCharAt((sb.length() - 1), ']');
            else
                sb.append(']');

            return sb.toString();
        }

        @Override
        public int hashCode()
        {
            int result = 1;
            result = ((result * 31) + ((this.apiVersionInUri == null) ? 0 : this.apiVersionInUri.hashCode()));
            result = ((result * 31) + ((this.apiFullVersion == null) ? 0 : this.apiFullVersion.hashCode()));
            return result;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
                return true;

            if ((other instanceof Version) == false)
                return false;

            final Version rhs = (Version) other;

            return (this.apiVersionInUri == rhs.apiVersionInUri || this.apiVersionInUri != null && this.apiVersionInUri.equals(rhs.apiVersionInUri))
                   && (this.apiFullVersion == rhs.apiFullVersion || this.apiFullVersion != null && this.apiFullVersion.equals(rhs.apiFullVersion));
        }
    }

    /**
     * Service address on which the SCP listens for incoming requests
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Service address on which the SCP listens for incoming requests")
    private List<String> serviceAddressRef = new ArrayList<String>();
    /**
     * Name uniquely identifying this service instance. (Required)
     * 
     */
    @JsonProperty("service-instance-id")
    @JsonPropertyDescription("Name uniquely identifying this service instance.")
    private String serviceInstanceId;
    /**
     * Name identifying the type of NF service. (Required)
     * 
     */
    @JsonProperty("service-name")
    @JsonPropertyDescription("Name identifying the type of NF service.")
    private NfService.ServiceName serviceName;
    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    @JsonPropertyDescription("Specifies the URI scheme")
    private NfService.Scheme scheme;
    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    @JsonPropertyDescription("Administrative state on NFService level")
    private NfService.AdminState adminState;
    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    @JsonPropertyDescription("Optional path used to construct the API URI for this service")
    private String apiPrefix;
    /**
     * PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN
     * are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-plmn")
    @JsonPropertyDescription("PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN are allowed to access the NF")
    private List<AllowedPlmn> allowedPlmn = new ArrayList<AllowedPlmn>();
    /**
     * NF types which are allowed to access the NF service. If not specified, NFs of
     * any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    @JsonPropertyDescription("NF types which are allowed to access the NF service. If not specified, NFs of any type are allowed to access the NF service.")
    private List<AllowedNfType> allowedNfType = new ArrayList<AllowedNfType>();
    /**
     * NF domain names which are allowed to access the NF service. If not specified,
     * If not specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    @JsonPropertyDescription("NF domain names which are allowed to access the NF service. If not specified, If not specified, NFs in any domain are allowed to access the NF")
    private List<String> allowedNfDomain = new ArrayList<String>();
    /**
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai")
    @JsonPropertyDescription("NSSAIs which are allowed to access the NF instance. If not specified, NFs in any NSSAI are allowed to access the NF")
    private List<AllowedNssai> allowedNssai = new ArrayList<AllowedNssai>();
    /**
     * Service priority of this NF service relative to other NF services of the same
     * type, to be used for NF service selection. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("service-priority")
    @JsonPropertyDescription("Service priority of this NF service relative to other NF services of the same type, to be used for NF service selection. Lower values indicate a higher priority")
    private Integer servicePriority;
    /**
     * Static capacity information of this NF service expressed as a weight relative
     * to other services of the same type, to be used for NF selection
     * 
     */
    @JsonProperty("capacity")
    @JsonPropertyDescription("Static capacity information of this NF service expressed as a weight relative to other services of the same type, to be used for NF selection")
    private Integer capacity;
    /**
     * Timestamp when the NF was (re)started
     * 
     */
    @JsonProperty("recovery-time")
    @JsonPropertyDescription("Timestamp when the NF was (re)started")
    private Date recoveryTime;

    /**
     * Supported features of this NF service
     * <p>
     * This attribute is not displayed to the user, hence not sent/received to/from
     * CMM. For use in the NRF registration, it must be set programmatically.
     */
    @JsonIgnore
    private String supportedFeatures = null;

    /**
     * The versions of this NF service
     * <p>
     * This attribute is not displayed to the user, hence not sent/received to/from
     * CMM. For use in the NRF registration, it must be set programmatically.
     */
    @JsonIgnore
    private List<Version> versions = null;

    /**
     * Service address of the BSF service
     * 
     */
    @JsonProperty("service-address-ref")
    public List<String> getServiceAddressRef()
    {
        return serviceAddressRef;
    }

    /**
     * Service address on which the SCP listens for incoming requests
     * 
     */
    @JsonProperty("service-address-ref")
    public void setServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
    }

    public NfService withServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    /**
     * Name uniquely identifying this service instance. (Required)
     * 
     */
    @JsonProperty("service-instance-id")
    public String getServiceInstanceId()
    {
        return serviceInstanceId;
    }

    /**
     * Name uniquely identifying this service instance. (Required)
     * 
     */
    @JsonProperty("service-instance-id")
    public void setServiceInstanceId(String serviceInstanceId)
    {
        this.serviceInstanceId = serviceInstanceId;
    }

    public NfService withServiceInstanceId(String serviceInstanceId)
    {
        this.serviceInstanceId = serviceInstanceId;
        return this;
    }

    /**
     * Name identifying the type of NF service. (Required)
     * 
     */
    @JsonProperty("service-name")
    public NfService.ServiceName getServiceName()
    {
        return serviceName;
    }

    /**
     * Name identifying the type of NF service. (Required)
     * 
     */
    @JsonProperty("service-name")
    public void setServiceName(NfService.ServiceName serviceName)
    {
        this.serviceName = serviceName;
    }

    public NfService withServiceName(NfService.ServiceName serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    public NfService.Scheme getScheme()
    {
        return scheme;
    }

    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    public void setScheme(NfService.Scheme scheme)
    {
        this.scheme = scheme;
    }

    public NfService withScheme(NfService.Scheme scheme)
    {
        this.scheme = scheme;
        return this;
    }

    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    public NfService.AdminState getAdminState()
    {
        return adminState;
    }

    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    public void setAdminState(NfService.AdminState adminState)
    {
        this.adminState = adminState;
    }

    public NfService withAdminState(NfService.AdminState adminState)
    {
        this.adminState = adminState;
        return this;
    }

    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    public String getApiPrefix()
    {
        return apiPrefix;
    }

    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    public void setApiPrefix(String apiPrefix)
    {
        this.apiPrefix = apiPrefix;
    }

    public NfService withApiPrefix(String apiPrefix)
    {
        this.apiPrefix = apiPrefix;
        return this;
    }

    /**
     * PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN
     * are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-plmn")
    public List<AllowedPlmn> getAllowedPlmn()
    {
        return allowedPlmn;
    }

    /**
     * PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN
     * are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-plmn")
    public void setAllowedPlmn(List<AllowedPlmn> allowedPlmn)
    {
        this.allowedPlmn = allowedPlmn;
    }

    public NfService withAllowedPlmn(List<AllowedPlmn> allowedPlmn)
    {
        this.allowedPlmn = allowedPlmn;
        return this;
    }

    /**
     * NF types which are allowed to access the NF service. If not specified, NFs of
     * any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    public List<AllowedNfType> getAllowedNfType()
    {
        return allowedNfType;
    }

    /**
     * NF types which are allowed to access the NF service. If not specified, NFs of
     * any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    public void setAllowedNfType(List<AllowedNfType> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
    }

    public NfService withAllowedNfType(List<AllowedNfType> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
        return this;
    }

    /**
     * NF domain names which are allowed to access the NF service. If not specified,
     * If not specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    public List<String> getAllowedNfDomain()
    {
        return allowedNfDomain;
    }

    /**
     * NF domain names which are allowed to access the NF service. If not specified,
     * If not specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    public void setAllowedNfDomain(List<String> allowedNfDomain)
    {
        this.allowedNfDomain = allowedNfDomain;
    }

    public NfService withAllowedNfDomain(List<String> allowedNfDomain)
    {
        this.allowedNfDomain = allowedNfDomain;
        return this;
    }

    /**
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai")
    public List<AllowedNssai> getAllowedNssai()
    {
        return allowedNssai;
    }

    /**
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai")
    public void setAllowedNssai(List<AllowedNssai> allowedNssai)
    {
        this.allowedNssai = allowedNssai;
    }

    public NfService withAllowedNssai(List<AllowedNssai> allowedNssai)
    {
        this.allowedNssai = allowedNssai;
        return this;
    }

    /**
     * Service priority of this NF service relative to other NF services of the same
     * type, to be used for NF service selection. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("service-priority")
    public Integer getServicePriority()
    {
        return servicePriority;
    }

    /**
     * Service priority of this NF service relative to other NF services of the same
     * type, to be used for NF service selection. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("service-priority")
    public void setServicePriority(Integer servicePriority)
    {
        this.servicePriority = servicePriority;
    }

    public NfService withServicePriority(Integer servicePriority)
    {
        this.servicePriority = servicePriority;
        return this;
    }

    /**
     * Static capacity information of this NF service expressed as a weight relative
     * to other services of the same type, to be used for NF selection
     * 
     */
    @JsonProperty("capacity")
    public Integer getCapacity()
    {
        return capacity;
    }

    /**
     * Static capacity information of this NF service expressed as a weight relative
     * to other services of the same type, to be used for NF selection
     * 
     */
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public NfService withCapacity(Integer capacity)
    {
        this.capacity = capacity;
        return this;
    }

    /**
     * Timestamp when the NF was (re)started
     * 
     */
    @JsonProperty("recovery-time")
    public Date getRecoveryTime()
    {
        return recoveryTime;
    }

    /**
     * Timestamp when the NF was (re)started
     * 
     */
    @JsonProperty("recovery-time")
    public void setRecoveryTime(Date recoveryTime)
    {
        this.recoveryTime = recoveryTime;
    }

    public NfService withRecoveryTime(Date recoveryTime)
    {
        this.recoveryTime = recoveryTime;
        return this;
    }

    @JsonIgnore
    public String getSupportedFeatures()
    {
        return this.supportedFeatures;
    }

    @JsonIgnore
    public void setSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
    }

    @JsonIgnore
    public NfService withSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
        return this;
    }

    @JsonIgnore
    public List<Version> getVersions()
    {
        return this.versions;
    }

    @JsonIgnore
    public void setVersions(List<Version> versions)
    {
        this.versions = versions;
    }

    @JsonIgnore
    public NfService withVersions(List<Version> versions)
    {
        this.versions = versions;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfService.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("serviceAddressRef");
        sb.append('=');
        sb.append(((this.serviceAddressRef == null) ? "<null>" : this.serviceAddressRef));
        sb.append(',');
        sb.append("serviceInstanceId");
        sb.append('=');
        sb.append(((this.serviceInstanceId == null) ? "<null>" : this.serviceInstanceId));
        sb.append(',');
        sb.append("serviceName");
        sb.append('=');
        sb.append(((this.serviceName == null) ? "<null>" : this.serviceName));
        sb.append(',');
        sb.append("scheme");
        sb.append('=');
        sb.append(((this.scheme == null) ? "<null>" : this.scheme));
        sb.append(',');
        sb.append("adminState");
        sb.append('=');
        sb.append(((this.adminState == null) ? "<null>" : this.adminState));
        sb.append(',');
        sb.append("apiPrefix");
        sb.append('=');
        sb.append(((this.apiPrefix == null) ? "<null>" : this.apiPrefix));
        sb.append(',');
        sb.append("allowedPlmn");
        sb.append('=');
        sb.append(((this.allowedPlmn == null) ? "<null>" : this.allowedPlmn));
        sb.append(',');
        sb.append("allowedNfType");
        sb.append('=');
        sb.append(((this.allowedNfType == null) ? "<null>" : this.allowedNfType));
        sb.append(',');
        sb.append("allowedNfDomain");
        sb.append('=');
        sb.append(((this.allowedNfDomain == null) ? "<null>" : this.allowedNfDomain));
        sb.append(',');
        sb.append("allowedNssai");
        sb.append('=');
        sb.append(((this.allowedNssai == null) ? "<null>" : this.allowedNssai));
        sb.append(',');
        sb.append("servicePriority");
        sb.append('=');
        sb.append(((this.servicePriority == null) ? "<null>" : this.servicePriority));
        sb.append(',');
        sb.append("capacity");
        sb.append('=');
        sb.append(((this.capacity == null) ? "<null>" : this.capacity));
        sb.append(',');
        sb.append("recoveryTime");
        sb.append('=');
        sb.append(((this.recoveryTime == null) ? "<null>" : this.recoveryTime));
        sb.append(',');
        sb.append("supportedFeatures");
        sb.append('=');
        sb.append(((this.supportedFeatures == null) ? "<null>" : this.supportedFeatures));
        sb.append(',');
        sb.append("versions");
        sb.append('=');
        sb.append(((this.versions == null) ? "<null>" : this.versions));
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
        result = ((result * 31) + ((this.recoveryTime == null) ? 0 : this.recoveryTime.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.scheme == null) ? 0 : this.scheme.hashCode()));
        result = ((result * 31) + ((this.allowedNfType == null) ? 0 : this.allowedNfType.hashCode()));
        result = ((result * 31) + ((this.serviceInstanceId == null) ? 0 : this.serviceInstanceId.hashCode()));
        result = ((result * 31) + ((this.serviceName == null) ? 0 : this.serviceName.hashCode()));
        result = ((result * 31) + ((this.allowedPlmn == null) ? 0 : this.allowedPlmn.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        result = ((result * 31) + ((this.allowedNfDomain == null) ? 0 : this.allowedNfDomain.hashCode()));
        result = ((result * 31) + ((this.apiPrefix == null) ? 0 : this.apiPrefix.hashCode()));
        result = ((result * 31) + ((this.allowedNssai == null) ? 0 : this.allowedNssai.hashCode()));
        result = ((result * 31) + ((this.adminState == null) ? 0 : this.adminState.hashCode()));
        result = ((result * 31) + ((this.servicePriority == null) ? 0 : this.servicePriority.hashCode()));
        result = ((result * 31) + ((this.supportedFeatures == null) ? 0 : this.supportedFeatures.hashCode()));
        result = ((result * 31) + ((this.versions == null) ? 0 : this.versions.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfService) == false)
        {
            return false;
        }
        NfService rhs = ((NfService) other);
        return ((((((((((((((((this.recoveryTime == rhs.recoveryTime) || ((this.recoveryTime != null) && this.recoveryTime.equals(rhs.recoveryTime)))
                             && ((this.serviceAddressRef == rhs.serviceAddressRef)
                                 || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))))
                            && ((this.scheme == rhs.scheme) || ((this.scheme != null) && this.scheme.equals(rhs.scheme))))
                           && ((this.allowedNfType == rhs.allowedNfType) || ((this.allowedNfType != null) && this.allowedNfType.equals(rhs.allowedNfType))))
                          && ((this.serviceInstanceId == rhs.serviceInstanceId)
                              || ((this.serviceInstanceId != null) && this.serviceInstanceId.equals(rhs.serviceInstanceId))))
                         && ((this.serviceName == rhs.serviceName) || ((this.serviceName != null) && this.serviceName.equals(rhs.serviceName))))
                        && ((this.allowedPlmn == rhs.allowedPlmn) || ((this.allowedPlmn != null) && this.allowedPlmn.equals(rhs.allowedPlmn))))
                       && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))))
                      && ((this.allowedNfDomain == rhs.allowedNfDomain)
                          || ((this.allowedNfDomain != null) && this.allowedNfDomain.equals(rhs.allowedNfDomain))))
                     && ((this.apiPrefix == rhs.apiPrefix) || ((this.apiPrefix != null) && this.apiPrefix.equals(rhs.apiPrefix))))
                    && ((this.allowedNssai == rhs.allowedNssai) || ((this.allowedNssai != null) && this.allowedNssai.equals(rhs.allowedNssai))))
                   && ((this.adminState == rhs.adminState) || ((this.adminState != null) && this.adminState.equals(rhs.adminState))))
                  && ((this.servicePriority == rhs.servicePriority) || ((this.servicePriority != null) && this.servicePriority.equals(rhs.servicePriority))))
                 && ((this.supportedFeatures == rhs.supportedFeatures)
                     || ((this.supportedFeatures != null) && this.supportedFeatures.equals(rhs.supportedFeatures))))
                && ((this.versions == rhs.versions) || ((this.versions != null) && this.versions.equals(rhs.versions))));
    }

    public enum AdminState
    {

        ACTIVE("active"),
        UNDISCOVERABLE("undiscoverable");

        private final String value;
        private final static Map<String, NfService.AdminState> CONSTANTS = new HashMap<String, NfService.AdminState>();

        static
        {
            for (NfService.AdminState c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private AdminState(String value)
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
        public static NfService.AdminState fromValue(String value)
        {
            NfService.AdminState constant = CONSTANTS.get(value);
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

    public enum Scheme
    {

        HTTP("http"),
        HTTPS("https");

        private final String value;
        private final static Map<String, NfService.Scheme> CONSTANTS = new HashMap<String, NfService.Scheme>();

        static
        {
            for (NfService.Scheme c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Scheme(String value)
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
        public static NfService.Scheme fromValue(String value)
        {
            NfService.Scheme constant = CONSTANTS.get(value);
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

    public enum ServiceName
    {

        NCHF_SPENDINGLIMITCONTROL("nchf-spendinglimitcontrol"),
        NCHF_CONVERGEDCHARGING("nchf-convergedcharging"),
        NBSF_MANAGEMENT("nbsf-management");

        private final String value;
        private final static Map<String, NfService.ServiceName> CONSTANTS = new HashMap<String, NfService.ServiceName>();

        static
        {
            for (NfService.ServiceName c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private ServiceName(String value)
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
        public static NfService.ServiceName fromValue(String value)
        {
            NfService.ServiceName constant = CONSTANTS.get(value);
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
