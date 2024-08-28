
package com.ericsson.sc.nfm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "service-instance-id",
                     "oauth2-required",
                     "allowed-operations-per-nf-type",
                     "allowed-operations-per-nf-instance",
                     "service-version",
                     "service-address-ref",
                     "service-instance-id",
                     "service-name",
                     "scheme",
                     "admin-state",
                     "api-prefix",
                     "allowed-plmn",
                     "allowed-nf-type",
                     "allowed-nf-domain",
                     "allowed-nssai",
                     "allowed-nssai-1",
                     "service-priority",
                     "capacity",
                     "recovery-time" })
public class NfService
{

    /**
     * Flag indicating whether the NF instance requires OAuth2-based authorization
     * 
     */
    @JsonProperty("oauth2-required")
    @JsonPropertyDescription("Flag indicating whether the NF instance requires OAuth2-based authorization")
    private Boolean oauth2Required;
    /**
     * The operations allowed per NF type
     * 
     */
    @JsonProperty("allowed-operations-per-nf-type")
    @JsonPropertyDescription("The operations allowed per NF type")
    private List<AllowedOperationsPerNfType> allowedOperationsPerNfType = new ArrayList<AllowedOperationsPerNfType>();
    /**
     * The operations allowed per NF instance
     * 
     */
    @JsonProperty("allowed-operations-per-nf-instance")
    @JsonPropertyDescription("The operations allowed per NF instance")
    private List<AllowedOperationsPerNfInstance> allowedOperationsPerNfInstance = new ArrayList<AllowedOperationsPerNfInstance>();
    /**
     * The supported versions of this NF service. Detailed information on the
     * default versions used can be found in CPI document 'Configuring the SCP'.
     * 
     */
    @JsonProperty("service-version")
    @JsonPropertyDescription("The supported versions of this NF service. Detailed information on the default versions used can be found in CPI document 'Configuring the SCP'.")
    private List<ServiceVersion> serviceVersion = null;
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
    private ServiceName serviceName;
    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    @JsonPropertyDescription("Specifies the URI scheme")
    private Scheme scheme;
    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    @JsonPropertyDescription("Administrative state on NFService level")
    private AdminState adminState;
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
     * NF types (according to TS 29.510) which are allowed to access the NF service.
     * If not specified, NFs of any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    @JsonPropertyDescription("NF types (according to TS 29.510) which are allowed to access the NF service. If not specified, NFs of any type are allowed to access the NF service.")
    private List<String> allowedNfType = new ArrayList<String>();
    /**
     * NF domain names which are allowed to access the NF service. If not specified,
     * If not specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    @JsonPropertyDescription("NF domain names which are allowed to access the NF service. If not specified, If not specified, NFs in any domain are allowed to access the NF")
    private List<String> allowedNfDomain = new ArrayList<String>();
    /**
     * Deprecated, use allowed-nssai-1 instead. NSSAIs which are allowed to access
     * the NF instance. If not specified, NFs in any NSSAI are allowed to access the
     * NF
     * 
     */
    @JsonProperty("allowed-nssai")
    @JsonPropertyDescription("Deprecated, use allowed-nssai-1 instead. NSSAIs which are allowed to access the NF instance. If not specified, NFs in any NSSAI are allowed to access the NF")
    private List<AllowedNssai> allowedNssai = new ArrayList<AllowedNssai>();
    /**
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai-1")
    @JsonPropertyDescription("NSSAIs which are allowed to access the NF instance. If not specified, NFs in any NSSAI are allowed to access the NF")
    private List<AllowedNssai1> allowedNssai1 = new ArrayList<AllowedNssai1>();
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
     * Flag indicating whether the NF instance requires OAuth2-based authorization
     * 
     */
    @JsonProperty("oauth2-required")
    public Boolean getOauth2Required()
    {
        return oauth2Required;
    }

    /**
     * Flag indicating whether the NF instance requires OAuth2-based authorization
     * 
     */
    @JsonProperty("oauth2-required")
    public void setOauth2Required(Boolean oauth2Required)
    {
        this.oauth2Required = oauth2Required;
    }

    public NfService withOauth2Required(Boolean oauth2Required)
    {
        this.oauth2Required = oauth2Required;
        return this;
    }

    /**
     * The operations allowed per NF type
     * 
     */
    @JsonProperty("allowed-operations-per-nf-type")
    public List<AllowedOperationsPerNfType> getAllowedOperationsPerNfType()
    {
        return allowedOperationsPerNfType;
    }

    /**
     * The operations allowed per NF type
     * 
     */
    @JsonProperty("allowed-operations-per-nf-type")
    public void setAllowedOperationsPerNfType(List<AllowedOperationsPerNfType> allowedOperationsPerNfType)
    {
        this.allowedOperationsPerNfType = allowedOperationsPerNfType;
    }

    public NfService withAllowedOperationsPerNfType(List<AllowedOperationsPerNfType> allowedOperationsPerNfType)
    {
        this.allowedOperationsPerNfType = allowedOperationsPerNfType;
        return this;
    }

    /**
     * The operations allowed per NF instance
     * 
     */
    @JsonProperty("allowed-operations-per-nf-instance")
    public List<AllowedOperationsPerNfInstance> getAllowedOperationsPerNfInstance()
    {
        return allowedOperationsPerNfInstance;
    }

    /**
     * The operations allowed per NF instance
     * 
     */
    @JsonProperty("allowed-operations-per-nf-instance")
    public void setAllowedOperationsPerNfInstance(List<AllowedOperationsPerNfInstance> allowedOperationsPerNfInstance)
    {
        this.allowedOperationsPerNfInstance = allowedOperationsPerNfInstance;
    }

    public NfService withAllowedOperationsPerNfInstance(List<AllowedOperationsPerNfInstance> allowedOperationsPerNfInstance)
    {
        this.allowedOperationsPerNfInstance = allowedOperationsPerNfInstance;
        return this;
    }

    /**
     * Supported features of this NF service
     * <p>
     * This attribute is not displayed to the user, hence not sent/received to/from
     * CMM. For use in the NRF registration, it must be set programmatically.
     */
    @JsonIgnore
    private String supportedFeatures = null;

    /**
     * The supported versions of this NF service
     * 
     */
    @JsonProperty("service-version")
    public List<ServiceVersion> getServiceVersion()
    {
        return serviceVersion;
    }

    /**
     * The supported versions of this NF service. Detailed information on the
     * default versions used can be found in CPI document 'Configuring the SCP'.
     * 
     */
    @JsonProperty("service-version")
    public void setServiceVersion(List<ServiceVersion> serviceVersion)
    {
        this.serviceVersion = serviceVersion;
    }

    public NfService withServiceVersion(List<ServiceVersion> serviceVersion)
    {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Service address on which the SCP listens for incoming requests
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
    public ServiceName getServiceName()
    {
        return serviceName;
    }

    /**
     * Name identifying the type of NF service. (Required)
     * 
     */
    @JsonProperty("service-name")
    public void setServiceName(ServiceName serviceName)
    {
        this.serviceName = serviceName;
    }

    public NfService withServiceName(ServiceName serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    public Scheme getScheme()
    {
        return scheme;
    }

    /**
     * Specifies the URI scheme (Required)
     * 
     */
    @JsonProperty("scheme")
    public void setScheme(Scheme scheme)
    {
        this.scheme = scheme;
    }

    public NfService withScheme(Scheme scheme)
    {
        this.scheme = scheme;
        return this;
    }

    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    public AdminState getAdminState()
    {
        return adminState;
    }

    /**
     * Administrative state on NFService level
     * 
     */
    @JsonProperty("admin-state")
    public void setAdminState(AdminState adminState)
    {
        this.adminState = adminState;
    }

    public NfService withAdminState(AdminState adminState)
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
     * NF types (according to TS 29.510) which are allowed to access the NF service.
     * If not specified, NFs of any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    public List<String> getAllowedNfType()
    {
        return allowedNfType;
    }

    /**
     * NF types (according to TS 29.510) which are allowed to access the NF service.
     * If not specified, NFs of any type are allowed to access the NF service.
     * 
     */
    @JsonProperty("allowed-nf-type")
    public void setAllowedNfType(List<String> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
    }

    public NfService withAllowedNfType(List<String> allowedNfType)
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
     * Deprecated, use allowed-nssai-1 instead. NSSAIs which are allowed to access
     * the NF instance. If not specified, NFs in any NSSAI are allowed to access the
     * NF
     * 
     */
    @JsonProperty("allowed-nssai")
    public List<AllowedNssai> getAllowedNssai()
    {
        return allowedNssai;
    }

    /**
     * Deprecated, use allowed-nssai-1 instead. NSSAIs which are allowed to access
     * the NF instance. If not specified, NFs in any NSSAI are allowed to access the
     * NF
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
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai-1")
    public List<AllowedNssai1> getAllowedNssai1()
    {
        return allowedNssai1;
    }

    /**
     * NSSAIs which are allowed to access the NF instance. If not specified, NFs in
     * any NSSAI are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nssai-1")
    public void setAllowedNssai1(List<AllowedNssai1> allowedNssai1)
    {
        this.allowedNssai1 = allowedNssai1;
    }

    public NfService withAllowedNssai1(List<AllowedNssai1> allowedNssai1)
    {
        this.allowedNssai1 = allowedNssai1;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfService.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("oauth2Required");
        sb.append('=');
        sb.append(((this.oauth2Required == null) ? "<null>" : this.oauth2Required));
        sb.append(',');
        sb.append("allowedOperationsPerNfType");
        sb.append('=');
        sb.append(((this.allowedOperationsPerNfType == null) ? "<null>" : this.allowedOperationsPerNfType));
        sb.append(',');
        sb.append("allowedOperationsPerNfInstance");
        sb.append('=');
        sb.append(((this.allowedOperationsPerNfInstance == null) ? "<null>" : this.allowedOperationsPerNfInstance));
        sb.append(',');
        sb.append("serviceVersion");
        sb.append('=');
        sb.append(((this.serviceVersion == null) ? "<null>" : this.serviceVersion));
        sb.append(',');
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
        sb.append("allowedNssai1");
        sb.append('=');
        sb.append(((this.allowedNssai1 == null) ? "<null>" : this.allowedNssai1));
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
        result = ((result * 31) + ((this.allowedOperationsPerNfInstance == null) ? 0 : this.allowedOperationsPerNfInstance.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.scheme == null) ? 0 : this.scheme.hashCode()));
        result = ((result * 31) + ((this.allowedNfType == null) ? 0 : this.allowedNfType.hashCode()));
        result = ((result * 31) + ((this.allowedNssai1 == null) ? 0 : this.allowedNssai1.hashCode()));
        result = ((result * 31) + ((this.serviceInstanceId == null) ? 0 : this.serviceInstanceId.hashCode()));
        result = ((result * 31) + ((this.serviceName == null) ? 0 : this.serviceName.hashCode()));
        result = ((result * 31) + ((this.allowedPlmn == null) ? 0 : this.allowedPlmn.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        result = ((result * 31) + ((this.serviceVersion == null) ? 0 : this.serviceVersion.hashCode()));
        result = ((result * 31) + ((this.allowedNfDomain == null) ? 0 : this.allowedNfDomain.hashCode()));
        result = ((result * 31) + ((this.apiPrefix == null) ? 0 : this.apiPrefix.hashCode()));
        result = ((result * 31) + ((this.allowedNssai == null) ? 0 : this.allowedNssai.hashCode()));
        result = ((result * 31) + ((this.adminState == null) ? 0 : this.adminState.hashCode()));
        result = ((result * 31) + ((this.oauth2Required == null) ? 0 : this.oauth2Required.hashCode()));
        result = ((result * 31) + ((this.servicePriority == null) ? 0 : this.servicePriority.hashCode()));
        result = ((result * 31) + ((this.allowedOperationsPerNfType == null) ? 0 : this.allowedOperationsPerNfType.hashCode()));
        result = ((result * 31) + ((this.supportedFeatures == null) ? 0 : this.supportedFeatures.hashCode()));
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
        return (this.recoveryTime == rhs.recoveryTime || this.recoveryTime != null && this.recoveryTime.equals(rhs.recoveryTime))
               && (this.serviceAddressRef == rhs.serviceAddressRef || this.serviceAddressRef != null && this.serviceAddressRef.equals(rhs.serviceAddressRef))
               && (this.scheme == rhs.scheme || this.scheme != null && this.scheme.equals(rhs.scheme))
               && (this.allowedNfType == rhs.allowedNfType || this.allowedNfType != null && this.allowedNfType.equals(rhs.allowedNfType))
               && (this.serviceInstanceId == rhs.serviceInstanceId || this.serviceInstanceId != null && this.serviceInstanceId.equals(rhs.serviceInstanceId))
               && (this.serviceName == rhs.serviceName || this.serviceName != null && this.serviceName.equals(rhs.serviceName))
               && (this.allowedPlmn == rhs.allowedPlmn || this.allowedPlmn != null && this.allowedPlmn.equals(rhs.allowedPlmn))
               && (this.capacity == rhs.capacity || this.capacity != null && this.capacity.equals(rhs.capacity))
               && (this.serviceVersion == rhs.serviceVersion || this.serviceVersion != null && this.serviceVersion.equals(rhs.serviceVersion))
               && (this.allowedNfDomain == rhs.allowedNfDomain || this.allowedNfDomain != null && this.allowedNfDomain.equals(rhs.allowedNfDomain))
               && (this.apiPrefix == rhs.apiPrefix || this.apiPrefix != null && this.apiPrefix.equals(rhs.apiPrefix))
               && (this.allowedNssai == rhs.allowedNssai || this.allowedNssai != null && this.allowedNssai.equals(rhs.allowedNssai))
               && (this.allowedNssai1 == rhs.allowedNssai1 || this.allowedNssai1 != null && this.allowedNssai1.equals(rhs.allowedNssai1))
               && (this.adminState == rhs.adminState || this.adminState != null && this.adminState.equals(rhs.adminState))
               && (this.servicePriority == rhs.servicePriority || this.servicePriority != null && this.servicePriority.equals(rhs.servicePriority))
               && (this.allowedOperationsPerNfInstance == rhs.allowedOperationsPerNfInstance
                   || this.allowedOperationsPerNfInstance != null && this.allowedOperationsPerNfInstance.equals(rhs.allowedOperationsPerNfInstance))
               && (this.allowedOperationsPerNfType == rhs.allowedOperationsPerNfType
                   || this.allowedOperationsPerNfType != null && this.allowedOperationsPerNfType.equals(rhs.allowedOperationsPerNfType))
               && (this.oauth2Required == rhs.oauth2Required || this.oauth2Required != null && this.oauth2Required.equals(rhs.oauth2Required))
               && (this.supportedFeatures == rhs.supportedFeatures || this.supportedFeatures != null && this.supportedFeatures.equals(rhs.supportedFeatures));
    }
}
