
package com.ericsson.esc.services.cm.model;

import java.util.ArrayList;
import java.util.Date;
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
                     "service-address-ref",
                     "nf-service",
                     "nf-type",
                     "admin-state",
                     "nf-specific-info",
                     "requested-heartbeat-timer",
                     "plmn",
                     "snssai",
                     "allowed-plmn",
                     "allowed-nf-type",
                     "allowed-nf-domain",
                     "allowed-nssai",
                     "service-priority",
                     "capacity",
                     "locality",
                     "recovery-time" })
public class NfProfile implements IfNamedListItem
{

    /**
     * Name uniquely identifying the NF profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the NF profile")
    private String name;
    /**
     * Service address on which the SCP listens for incoming requests (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Service address on which the SCP listens for incoming requests")
    private String serviceAddressRef;
    /**
     * The configuration of NF services, proxied through the SCP NF instance, which
     * can be discovered by other NFs
     * 
     */
    @JsonProperty("nf-service")
    @JsonPropertyDescription("The configuration of NF services, proxied through the SCP NF instance, which can be discovered by other NFs")
    private List<NfService> nfService = new ArrayList<NfService>();
    /**
     * NF types (Required)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("NF types")
    private NfProfile.NfType nfType;
    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    @JsonPropertyDescription("Administrative state on NF level")
    private NfProfile.AdminState adminState;
    /**
     * Specific data for the NF instance type
     * 
     */
    @JsonProperty("nf-specific-info")
    @JsonPropertyDescription("Specific data for the NF instance type")
    private NfSpecificInfo nfSpecificInfo;
    /**
     * Requested time, in seconds, expected between two heart-beat messages from the
     * NF instance to the NRF
     * 
     */
    @JsonProperty("requested-heartbeat-timer")
    @JsonPropertyDescription("Requested time, in seconds, expected between two heart-beat messages from the NF instance to the NRF")
    private Integer requestedHeartbeatTimer;
    /**
     * PLMN(s) of the NF instance. If not provided, the PLMN ID(s) of the PLMN of
     * the NRF are assumed for this NF
     * 
     */
    @JsonProperty("plmn")
    @JsonPropertyDescription("PLMN(s) of the NF instance. If not provided, the PLMN ID(s) of the PLMN of the NRF are assumed for this NF")
    private List<Plmn> plmn = new ArrayList<Plmn>();
    /**
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai")
    @JsonPropertyDescription("S-NSSAIs of the NF instance. If present, this attribute represents the list of S-NSSAIs supported in all the PLMNs configured for this NF instance. If not provided, the NF instance can serve any S-NSSAI")
    private List<Snssai> snssai = new ArrayList<Snssai>();
    /**
     * PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN
     * are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-plmn")
    @JsonPropertyDescription("PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN are allowed to access the NF")
    private List<AllowedPlmn> allowedPlmn = new ArrayList<AllowedPlmn>();
    /**
     * NF types which are allowed to access the NF instance. If not specified, NFs
     * of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    @JsonPropertyDescription("NF types which are allowed to access the NF instance. If not specified, NFs of any type are allowed to access the NF")
    private List<AllowedNfType> allowedNfType = new ArrayList<AllowedNfType>();
    /**
     * NF domain names which are allowed to access the NF instance. If not
     * specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    @JsonPropertyDescription("NF domain names which are allowed to access the NF instance. If not specified, NFs in any domain are allowed to access the NF")
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
     * Service priority of this NF instance, in the range of 0-65535, relative to
     * other NF instances of the same type, to be used for NF selection. Lower
     * values indicate a higher priority
     * 
     */
    @JsonProperty("service-priority")
    @JsonPropertyDescription("Service priority of this NF instance, in the range of 0-65535, relative to other NF instances of the same type, to be used for NF selection. Lower values indicate a higher priority")
    private Integer servicePriority;
    /**
     * Static capacity information of this NF instance, in the range of 0-65535,
     * expressed as a weight relative to other NF instances of the same type
     * 
     */
    @JsonProperty("capacity")
    @JsonPropertyDescription("Static capacity information of this NF instance, in the range of 0-65535, expressed as a weight relative to other NF instances of the same type")
    private Integer capacity;
    /**
     * Operator defined information about the location of the NF instance. This
     * information can be used by requesting NFs which prefer services of those NFs
     * in the same geographical location or data center
     * 
     */
    @JsonProperty("locality")
    @JsonPropertyDescription("Operator defined information about the location of the NF instance. This information can be used by requesting NFs which prefer services of those NFs in the same geographical location or data center")
    private String locality;
    /**
     * Timestamp when the NF was (re)started
     * 
     */
    @JsonProperty("recovery-time")
    @JsonPropertyDescription("Timestamp when the NF was (re)started")
    private Date recoveryTime;

    /**
     * Name uniquely identifying the NF profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the NF profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Service address on which the SCP listens for incoming requests (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    public String getServiceAddressRef()
    {
        return serviceAddressRef;
    }

    /**
     * Service address on which the SCP listens for incoming requests (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    public void setServiceAddressRef(String serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
    }

    public NfProfile withServiceAddressRef(String serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    /**
     * The configuration of NF services, proxied through the SCP NF instance, which
     * can be discovered by other NFs
     * 
     */
    @JsonProperty("nf-service")
    public List<NfService> getNfService()
    {
        return nfService;
    }

    /**
     * The configuration of NF services, proxied through the SCP NF instance, which
     * can be discovered by other NFs
     * 
     */
    @JsonProperty("nf-service")
    public void setNfService(List<NfService> nfService)
    {
        this.nfService = nfService;
    }

    public NfProfile withNfService(List<NfService> nfService)
    {
        this.nfService = nfService;
        return this;
    }

    /**
     * NF types (Required)
     * 
     */
    @JsonProperty("nf-type")
    public NfProfile.NfType getNfType()
    {
        return nfType;
    }

    /**
     * NF types (Required)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(NfProfile.NfType nfType)
    {
        this.nfType = nfType;
    }

    public NfProfile withNfType(NfProfile.NfType nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    public NfProfile.AdminState getAdminState()
    {
        return adminState;
    }

    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    public void setAdminState(NfProfile.AdminState adminState)
    {
        this.adminState = adminState;
    }

    public NfProfile withAdminState(NfProfile.AdminState adminState)
    {
        this.adminState = adminState;
        return this;
    }

    /**
     * Specific data for the NF instance type
     * 
     */
    @JsonProperty("nf-specific-info")
    public NfSpecificInfo getNfSpecificInfo()
    {
        return nfSpecificInfo;
    }

    /**
     * Specific data for the NF instance type
     * 
     */
    @JsonProperty("nf-specific-info")
    public void setNfSpecificInfo(NfSpecificInfo nfSpecificInfo)
    {
        this.nfSpecificInfo = nfSpecificInfo;
    }

    public NfProfile withNfSpecificInfo(NfSpecificInfo nfSpecificInfo)
    {
        this.nfSpecificInfo = nfSpecificInfo;
        return this;
    }

    /**
     * Requested time, in seconds, expected between two heart-beat messages from the
     * NF instance to the NRF
     * 
     */
    @JsonProperty("requested-heartbeat-timer")
    public Integer getRequestedHeartbeatTimer()
    {
        return requestedHeartbeatTimer;
    }

    /**
     * Requested time, in seconds, expected between two heart-beat messages from the
     * NF instance to the NRF
     * 
     */
    @JsonProperty("requested-heartbeat-timer")
    public void setRequestedHeartbeatTimer(Integer requestedHeartbeatTimer)
    {
        this.requestedHeartbeatTimer = requestedHeartbeatTimer;
    }

    public NfProfile withRequestedHeartbeatTimer(Integer requestedHeartbeatTimer)
    {
        this.requestedHeartbeatTimer = requestedHeartbeatTimer;
        return this;
    }

    /**
     * PLMN(s) of the NF instance. If not provided, the PLMN ID(s) of the PLMN of
     * the NRF are assumed for this NF
     * 
     */
    @JsonProperty("plmn")
    public List<Plmn> getPlmn()
    {
        return plmn;
    }

    /**
     * PLMN(s) of the NF instance. If not provided, the PLMN ID(s) of the PLMN of
     * the NRF are assumed for this NF
     * 
     */
    @JsonProperty("plmn")
    public void setPlmn(List<Plmn> plmn)
    {
        this.plmn = plmn;
    }

    public NfProfile withPlmn(List<Plmn> plmn)
    {
        this.plmn = plmn;
        return this;
    }

    /**
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai")
    public List<Snssai> getSnssai()
    {
        return snssai;
    }

    /**
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai")
    public void setSnssai(List<Snssai> snssai)
    {
        this.snssai = snssai;
    }

    public NfProfile withSnssai(List<Snssai> snssai)
    {
        this.snssai = snssai;
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

    public NfProfile withAllowedPlmn(List<AllowedPlmn> allowedPlmn)
    {
        this.allowedPlmn = allowedPlmn;
        return this;
    }

    /**
     * NF types which are allowed to access the NF instance. If not specified, NFs
     * of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    public List<AllowedNfType> getAllowedNfType()
    {
        return allowedNfType;
    }

    /**
     * NF types which are allowed to access the NF instance. If not specified, NFs
     * of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    public void setAllowedNfType(List<AllowedNfType> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
    }

    public NfProfile withAllowedNfType(List<AllowedNfType> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
        return this;
    }

    /**
     * NF domain names which are allowed to access the NF instance. If not
     * specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    public List<String> getAllowedNfDomain()
    {
        return allowedNfDomain;
    }

    /**
     * NF domain names which are allowed to access the NF instance. If not
     * specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    public void setAllowedNfDomain(List<String> allowedNfDomain)
    {
        this.allowedNfDomain = allowedNfDomain;
    }

    public NfProfile withAllowedNfDomain(List<String> allowedNfDomain)
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

    public NfProfile withAllowedNssai(List<AllowedNssai> allowedNssai)
    {
        this.allowedNssai = allowedNssai;
        return this;
    }

    /**
     * Service priority of this NF instance, in the range of 0-65535, relative to
     * other NF instances of the same type, to be used for NF selection. Lower
     * values indicate a higher priority
     * 
     */
    @JsonProperty("service-priority")
    public Integer getServicePriority()
    {
        return servicePriority;
    }

    /**
     * Service priority of this NF instance, in the range of 0-65535, relative to
     * other NF instances of the same type, to be used for NF selection. Lower
     * values indicate a higher priority
     * 
     */
    @JsonProperty("service-priority")
    public void setServicePriority(Integer servicePriority)
    {
        this.servicePriority = servicePriority;
    }

    public NfProfile withServicePriority(Integer servicePriority)
    {
        this.servicePriority = servicePriority;
        return this;
    }

    /**
     * Static capacity information of this NF instance, in the range of 0-65535,
     * expressed as a weight relative to other NF instances of the same type
     * 
     */
    @JsonProperty("capacity")
    public Integer getCapacity()
    {
        return capacity;
    }

    /**
     * Static capacity information of this NF instance, in the range of 0-65535,
     * expressed as a weight relative to other NF instances of the same type
     * 
     */
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public NfProfile withCapacity(Integer capacity)
    {
        this.capacity = capacity;
        return this;
    }

    /**
     * Operator defined information about the location of the NF instance. This
     * information can be used by requesting NFs which prefer services of those NFs
     * in the same geographical location or data center
     * 
     */
    @JsonProperty("locality")
    public String getLocality()
    {
        return locality;
    }

    /**
     * Operator defined information about the location of the NF instance. This
     * information can be used by requesting NFs which prefer services of those NFs
     * in the same geographical location or data center
     * 
     */
    @JsonProperty("locality")
    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    public NfProfile withLocality(String locality)
    {
        this.locality = locality;
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

    public NfProfile withRecoveryTime(Date recoveryTime)
    {
        this.recoveryTime = recoveryTime;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("serviceAddressRef");
        sb.append('=');
        sb.append(((this.serviceAddressRef == null) ? "<null>" : this.serviceAddressRef));
        sb.append(',');
        sb.append("nfService");
        sb.append('=');
        sb.append(((this.nfService == null) ? "<null>" : this.nfService));
        sb.append(',');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("adminState");
        sb.append('=');
        sb.append(((this.adminState == null) ? "<null>" : this.adminState));
        sb.append(',');
        sb.append("nfSpecificInfo");
        sb.append('=');
        sb.append(((this.nfSpecificInfo == null) ? "<null>" : this.nfSpecificInfo));
        sb.append(',');
        sb.append("requestedHeartbeatTimer");
        sb.append('=');
        sb.append(((this.requestedHeartbeatTimer == null) ? "<null>" : this.requestedHeartbeatTimer));
        sb.append(',');
        sb.append("plmn");
        sb.append('=');
        sb.append(((this.plmn == null) ? "<null>" : this.plmn));
        sb.append(',');
        sb.append("snssai");
        sb.append('=');
        sb.append(((this.snssai == null) ? "<null>" : this.snssai));
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
        sb.append("locality");
        sb.append('=');
        sb.append(((this.locality == null) ? "<null>" : this.locality));
        sb.append(',');
        sb.append("recoveryTime");
        sb.append('=');
        sb.append(((this.recoveryTime == null) ? "<null>" : this.recoveryTime));
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
        result = ((result * 31) + ((this.snssai == null) ? 0 : this.snssai.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.allowedNfType == null) ? 0 : this.allowedNfType.hashCode()));
        result = ((result * 31) + ((this.nfService == null) ? 0 : this.nfService.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.locality == null) ? 0 : this.locality.hashCode()));
        result = ((result * 31) + ((this.plmn == null) ? 0 : this.plmn.hashCode()));
        result = ((result * 31) + ((this.nfSpecificInfo == null) ? 0 : this.nfSpecificInfo.hashCode()));
        result = ((result * 31) + ((this.requestedHeartbeatTimer == null) ? 0 : this.requestedHeartbeatTimer.hashCode()));
        result = ((result * 31) + ((this.allowedPlmn == null) ? 0 : this.allowedPlmn.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        result = ((result * 31) + ((this.allowedNfDomain == null) ? 0 : this.allowedNfDomain.hashCode()));
        result = ((result * 31) + ((this.allowedNssai == null) ? 0 : this.allowedNssai.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.adminState == null) ? 0 : this.adminState.hashCode()));
        result = ((result * 31) + ((this.servicePriority == null) ? 0 : this.servicePriority.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfProfile) == false)
        {
            return false;
        }
        NfProfile rhs = ((NfProfile) other);
        return ((((((((((((((((((this.recoveryTime == rhs.recoveryTime) || ((this.recoveryTime != null) && this.recoveryTime.equals(rhs.recoveryTime)))
                               && ((this.snssai == rhs.snssai) || ((this.snssai != null) && this.snssai.equals(rhs.snssai))))
                              && ((this.serviceAddressRef == rhs.serviceAddressRef)
                                  || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))))
                             && ((this.allowedNfType == rhs.allowedNfType) || ((this.allowedNfType != null) && this.allowedNfType.equals(rhs.allowedNfType))))
                            && ((this.nfService == rhs.nfService) || ((this.nfService != null) && this.nfService.equals(rhs.nfService))))
                           && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                          && ((this.locality == rhs.locality) || ((this.locality != null) && this.locality.equals(rhs.locality))))
                         && ((this.plmn == rhs.plmn) || ((this.plmn != null) && this.plmn.equals(rhs.plmn))))
                        && ((this.nfSpecificInfo == rhs.nfSpecificInfo) || ((this.nfSpecificInfo != null) && this.nfSpecificInfo.equals(rhs.nfSpecificInfo))))
                       && ((this.requestedHeartbeatTimer == rhs.requestedHeartbeatTimer)
                           || ((this.requestedHeartbeatTimer != null) && this.requestedHeartbeatTimer.equals(rhs.requestedHeartbeatTimer))))
                      && ((this.allowedPlmn == rhs.allowedPlmn) || ((this.allowedPlmn != null) && this.allowedPlmn.equals(rhs.allowedPlmn))))
                     && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))))
                    && ((this.allowedNfDomain == rhs.allowedNfDomain) || ((this.allowedNfDomain != null) && this.allowedNfDomain.equals(rhs.allowedNfDomain))))
                   && ((this.allowedNssai == rhs.allowedNssai) || ((this.allowedNssai != null) && this.allowedNssai.equals(rhs.allowedNssai))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.adminState == rhs.adminState) || ((this.adminState != null) && this.adminState.equals(rhs.adminState))))
                && ((this.servicePriority == rhs.servicePriority) || ((this.servicePriority != null) && this.servicePriority.equals(rhs.servicePriority))));
    }

    public enum AdminState
    {

        ACTIVE("active"),
        UNDISCOVERABLE("undiscoverable");

        private final String value;
        private final static Map<String, NfProfile.AdminState> CONSTANTS = new HashMap<String, NfProfile.AdminState>();

        static
        {
            for (NfProfile.AdminState c : values())
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
        public static NfProfile.AdminState fromValue(String value)
        {
            NfProfile.AdminState constant = CONSTANTS.get(value);
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

    public enum NfType
    {

        NRF("nrf"),
        UDM("udm"),
        AMF("amf"),
        SMF("smf"),
        AUSF("ausf"),
        NEF("nef"),
        PCF("pcf"),
        SMSF("smsf"),
        NSSF("nssf"),
        UDR("udr"),
        LMF("lmf"),
        GMLC("gmlc"),
        _5_G_EIR("5g-eir"),
        SEPP("sepp"),
        UPF("upf"),
        N_3_IWF("n3iwf"),
        AF("af"),
        UDSF("udsf"),
        BSF("bsf"),
        CHF("chf"),
        NWDAF("nwdaf");

        private final String value;
        private final static Map<String, NfProfile.NfType> CONSTANTS = new HashMap<String, NfProfile.NfType>();

        static
        {
            for (NfProfile.NfType c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private NfType(String value)
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
        public static NfProfile.NfType fromValue(String value)
        {
            NfProfile.NfType constant = CONSTANTS.get(value);
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
