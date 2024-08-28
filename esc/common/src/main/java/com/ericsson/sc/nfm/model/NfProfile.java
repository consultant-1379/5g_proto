
package com.ericsson.sc.nfm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ericsson.sc.scp.model.ScpInfo;
import com.ericsson.sc.sepp.model.SeppInfo;
import com.ericsson.sc.bsf.model.BsfInfo;
import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "service-address-ref",
                     "associated-nf-pool-ref",
                     "chf-info",
                     "scp-info",
                     "sepp-info",
                     "nf-service",
                     "bsf-info",
                     "nf-instance-name",
                     "nf-type",
                     "admin-state",
                     "nf-specific-info",
                     "requested-heartbeat-timer",
                     "plmn",
                     "snssai",
                     "snssai-1",
                     "allowed-plmn",
                     "allowed-nf-type",
                     "allowed-nf-domain",
                     "allowed-nssai",
                     "allowed-nssai-1",
                     "nf-set-id",
                     "nsi",
                     "service-priority",
                     "capacity",
                     "locality",
                     "recovery-time",
                     "scp-domain" })
public class NfProfile implements IfNamedListItem
{

    /**
     * Name identifying the NF profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the NF profile")
    private String name;
    /**
     * Service address on which the SCP listens for incoming requests (Required)
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Service address on which the SCP listens for incoming requests")
    private String serviceAddressRef;
    /**
     * Reference to the associated nf-pool to be considered at NRF registration for
     * discovered NF instances capacity aggregation. If it is not present, the
     * configured capacity will be used instead.
     * 
     */
    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("associated-nf-pool-ref")
    @JsonPropertyDescription("Reference to the associated nf-pool to be considered at NRF registration for discovered NF instances capacity aggregation. If it is not present, the configured capacity will be used instead.")
    private List<String> associatedNfPoolRef = new ArrayList<String>();
    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    @JsonPropertyDescription("Specific data for the CHF NF")
    private ChfInfo chfInfo;
    /**
     * Specific data for the SCP NF
     * 
     */
    @JsonProperty("scp-info")
    @JsonPropertyDescription("Specific data for the SCP NF")
    private ScpInfo scpInfo;
    /**
     * Specific data for the SEPP NF
     * 
     */
    @JsonProperty("sepp-info")
    @JsonPropertyDescription("Specific data for the SEPP NF")
    private SeppInfo seppInfo;
    /**
     * The configuration of NF services, proxied through the SCP NF instance, which
     * can be discovered by other NFs
     * 
     */
    @JsonProperty("nf-service")
    @JsonPropertyDescription("The configuration of NF services, proxied through the SCP NF instance, which can be discovered by other NFs")
    private List<NfService> nfService = new ArrayList<NfService>();
    /**
     * Specific data for the BSF NF
     * 
     */
    @JsonProperty("bsf-info")
    @JsonPropertyDescription("Specific data for the BSF NF")
    private BsfInfo bsfInfo;
    /**
     * Human readable name of the NF instance
     * 
     */
    @JsonProperty("nf-instance-name")
    @JsonPropertyDescription("Human readable name of the NF instance")
    private String nfInstanceName;
    /**
     * The type of the NF instance (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The type of the NF instance (according to TS 29.510)")
    private String nfType;
    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    @JsonPropertyDescription("Administrative state on NF level")
    private AdminState adminState;
    /**
     * Deprecated, use nf-profile->chf-info and nf-profile->bsf-info instead,
     * specific data for the NF instance type
     * 
     */
    @JsonProperty("nf-specific-info")
    @JsonPropertyDescription("Deprecated, use nf-profile->chf-info and nf-profile->bsf-info instead, specific data for the NF instance type")
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
     * Deprecated, use snssai-1 instead. S-NSSAIs of the NF instance. If present,
     * this attribute represents the list of S-NSSAIs supported in all the PLMNs
     * configured for this NF instance. If not provided, the NF instance can serve
     * any S-NSSAI
     * 
     */
    @JsonProperty("snssai")
    @JsonPropertyDescription("Deprecated, use snssai-1 instead. S-NSSAIs of the NF instance. If present, this attribute represents the list of S-NSSAIs supported in all the PLMNs configured for this NF instance. If not provided, the NF instance can serve any S-NSSAI")
    private List<Snssai> snssai = new ArrayList<Snssai>();
    /**
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai-1")
    @JsonPropertyDescription("S-NSSAIs of the NF instance. If present, this attribute represents the list of S-NSSAIs supported in all the PLMNs configured for this NF instance. If not provided, the NF instance can serve any S-NSSAI")
    private List<Snssai1> snssai1 = new ArrayList<Snssai1>();
    /**
     * PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN
     * are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-plmn")
    @JsonPropertyDescription("PLMNs allowed to access the NF instance. If not specified, NFs of any PLMN are allowed to access the NF")
    private List<AllowedPlmn> allowedPlmn = new ArrayList<AllowedPlmn>();
    /**
     * NF types (according to TS 29.510) which are allowed to access the NF
     * instance. If not specified, NFs of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    @JsonPropertyDescription("NF types (according to TS 29.510) which are allowed to access the NF instance. If not specified, NFs of any type are allowed to access the NF")
    private List<String> allowedNfType = new ArrayList<String>();
    /**
     * NF domain names which are allowed to access the NF instance. If not
     * specified, NFs in any domain are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-domain")
    @JsonPropertyDescription("NF domain names which are allowed to access the NF instance. If not specified, NFs in any domain are allowed to access the NF")
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
     * NF Set Identifiers for this NF used for NF registration
     * 
     */
    @JsonProperty("nf-set-id")
    @JsonPropertyDescription("NF Set Identifiers for this NF used for NF registration")
    private List<String> nfSetId = new ArrayList<String>();
    /**
     * NSI identities of the Network Function. If not provided, the NF can serve any
     * NSI
     * 
     */
    @JsonProperty("nsi")
    @JsonPropertyDescription("NSI identities of the Network Function. If not provided, the NF can serve any NSI")
    private List<String> nsi = new ArrayList<String>();
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
     * The SCP domain(s) the SCP belongs to or the SCP domain the NF (other than
     * SCP) belongs to
     * 
     */
    @JsonProperty("scp-domain")
    @JsonPropertyDescription("The SCP domain(s) the SCP belongs to or the SCP domain the NF (other than SCP) belongs to")
    private List<String> scpDomain = new ArrayList<String>();

    /**
     * Name identifying the NF profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the NF profile (Required)
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
     * Reference to the associated nf-pool to be considered at NRF registration for
     * discovered NF instances capacity aggregation. If it is not present, the
     * configured capacity will be used instead.
     * 
     */
    @JsonProperty("associated-nf-pool-ref")
    public List<String> getAssociatedNfPoolRef()
    {
        return associatedNfPoolRef;
    }

    /**
     * Reference to the associated nf-pool to be considered at NRF registration for
     * discovered NF instances capacity aggregation. If it is not present, the
     * configured capacity will be used instead.
     * 
     */
    @JsonProperty("associated-nf-pool-ref")
    public void setAssociatedNfPoolRef(List<String> associatedNfPoolRef)
    {
        this.associatedNfPoolRef = associatedNfPoolRef;
    }

    public NfProfile withAssociatedNfPoolRef(List<String> associatedNfPoolRef)
    {
        this.associatedNfPoolRef = associatedNfPoolRef;
        return this;
    }

    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    public ChfInfo getChfInfo()
    {
        return chfInfo;
    }

    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    public void setChfInfo(ChfInfo chfInfo)
    {
        this.chfInfo = chfInfo;
    }

    public NfProfile withChfInfo(ChfInfo chfInfo)
    {
        this.chfInfo = chfInfo;
        return this;
    }

    /**
     * Specific data for the SCP NF
     * 
     */
    @JsonProperty("scp-info")
    public ScpInfo getScpInfo()
    {
        return scpInfo;
    }

    /**
     * Specific data for the SCP NF
     * 
     */
    @JsonProperty("scp-info")
    public void setScpInfo(ScpInfo scpInfo)
    {
        this.scpInfo = scpInfo;
    }

    public NfProfile withScpInfo(ScpInfo scpInfo)
    {
        this.scpInfo = scpInfo;
        return this;
    }

    /**
     * Specific data for the SEPP NF
     * 
     */
    @JsonProperty("sepp-info")
    public SeppInfo getSeppInfo()
    {
        return seppInfo;
    }

    /**
     * Specific data for the SEPP NF
     * 
     */
    @JsonProperty("sepp-info")
    public void setSeppInfo(SeppInfo seppInfo)
    {
        this.seppInfo = seppInfo;
    }

    public NfProfile withSeppInfo(SeppInfo seppInfo)
    {
        this.seppInfo = seppInfo;
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

    @JsonProperty("bsf-info")
    public BsfInfo getBsfInfo()
    {
        return bsfInfo;
    }

    @JsonProperty("bsf-info")
    public void setBsfInfo(BsfInfo bsfInfo)
    {
        this.bsfInfo = bsfInfo;
    }

    public NfProfile withBsfInfo(BsfInfo bsfInfo)
    {
        this.bsfInfo = bsfInfo;
        return this;
    }

    /**
     * Human readable name of the NF instance
     * 
     */
    @JsonProperty("nf-instance-name")
    public String getNfInstanceName()
    {
        return nfInstanceName;
    }

    /**
     * Human readable name of the NF instance
     * 
     */
    @JsonProperty("nf-instance-name")
    public void setNfInstanceName(String nfInstanceName)
    {
        this.nfInstanceName = nfInstanceName;
    }

    public NfProfile withNfInstanceName(String nfInstanceName)
    {
        this.nfInstanceName = nfInstanceName;
        return this;
    }

    /**
     * The type of the NF instance (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The type of the NF instance (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public NfProfile withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    public AdminState getAdminState()
    {
        return adminState;
    }

    /**
     * Administrative state on NF level
     * 
     */
    @JsonProperty("admin-state")
    public void setAdminState(AdminState adminState)
    {
        this.adminState = adminState;
    }

    public NfProfile withAdminState(AdminState adminState)
    {
        this.adminState = adminState;
        return this;
    }

    /**
     * Deprecated, use nf-profile->chf-info and nf-profile->bsf-info instead,
     * specific data for the NF instance type
     * 
     */
    @JsonProperty("nf-specific-info")
    public NfSpecificInfo getNfSpecificInfo()
    {
        return nfSpecificInfo;
    }

    /**
     * Deprecated, use nf-profile->chf-info and nf-profile->bsf-info instead,
     * specific data for the NF instance type
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
     * Deprecated, use snssai-1 instead. S-NSSAIs of the NF instance. If present,
     * this attribute represents the list of S-NSSAIs supported in all the PLMNs
     * configured for this NF instance. If not provided, the NF instance can serve
     * any S-NSSAI
     * 
     */
    @JsonProperty("snssai")
    public List<Snssai> getSnssai()
    {
        return snssai;
    }

    /**
     * Deprecated, use snssai-1 instead. S-NSSAIs of the NF instance. If present,
     * this attribute represents the list of S-NSSAIs supported in all the PLMNs
     * configured for this NF instance. If not provided, the NF instance can serve
     * any S-NSSAI
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
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai-1")
    public List<Snssai1> getSnssai1()
    {
        return snssai1;
    }

    /**
     * S-NSSAIs of the NF instance. If present, this attribute represents the list
     * of S-NSSAIs supported in all the PLMNs configured for this NF instance. If
     * not provided, the NF instance can serve any S-NSSAI
     * 
     */
    @JsonProperty("snssai-1")
    public void setSnssai1(List<Snssai1> snssai1)
    {
        this.snssai1 = snssai1;
    }

    public NfProfile withSnssai1(List<Snssai1> snssai1)
    {
        this.snssai1 = snssai1;
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
     * NF types (according to TS 29.510) which are allowed to access the NF
     * instance. If not specified, NFs of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    public List<String> getAllowedNfType()
    {
        return allowedNfType;
    }

    /**
     * NF types (according to TS 29.510) which are allowed to access the NF
     * instance. If not specified, NFs of any type are allowed to access the NF
     * 
     */
    @JsonProperty("allowed-nf-type")
    public void setAllowedNfType(List<String> allowedNfType)
    {
        this.allowedNfType = allowedNfType;
    }

    public NfProfile withAllowedNfType(List<String> allowedNfType)
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

    public NfProfile withAllowedNssai(List<AllowedNssai> allowedNssai)
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

    public NfProfile withAllowedNssai1(List<AllowedNssai1> allowedNssai1)
    {
        this.allowedNssai1 = allowedNssai1;
        return this;
    }

    /**
     * NF Set Identifiers for this NF used for NF registration
     * 
     */
    @JsonProperty("nf-set-id")
    public List<String> getNfSetId()
    {
        return nfSetId;
    }

    /**
     * NF Set Identifiers for this NF used for NF registration
     * 
     */
    @JsonProperty("nf-set-id")
    public void setNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
    }

    public NfProfile withNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
        return this;
    }

    /**
     * NSI identities of the Network Function. If not provided, the NF can serve any
     * NSI
     * 
     */
    @JsonProperty("nsi")
    public List<String> getNsi()
    {
        return nsi;
    }

    /**
     * NSI identities of the Network Function. If not provided, the NF can serve any
     * NSI
     * 
     */
    @JsonProperty("nsi")
    public void setNsi(List<String> nsi)
    {
        this.nsi = nsi;
    }

    public NfProfile withNsi(List<String> nsi)
    {
        this.nsi = nsi;
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

    public NfProfile withRecoveryTime(Date recoveryTime)
    {
        this.recoveryTime = recoveryTime;
        return this;
    }

    /**
     * The SCP domain(s) the SCP belongs to or the SCP domain the NF (other than
     * SCP) belongs to
     * 
     */
    @JsonProperty("scp-domain")
    public List<String> getScpDomain()
    {
        return scpDomain;
    }

    /**
     * The SCP domain(s) the SCP belongs to or the SCP domain the NF (other than
     * SCP) belongs to
     * 
     */
    @JsonProperty("scp-domain")
    public void setScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
    }

    public NfProfile withScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
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
        sb.append("associatedNfPoolRef");
        sb.append('=');
        sb.append(((this.associatedNfPoolRef == null) ? "<null>" : this.associatedNfPoolRef));
        sb.append(',');
        sb.append("chfInfo");
        sb.append('=');
        sb.append(((this.chfInfo == null) ? "<null>" : this.chfInfo));
        sb.append(',');
        sb.append("scpInfo");
        sb.append('=');
        sb.append(((this.scpInfo == null) ? "<null>" : this.scpInfo));
        sb.append(',');
        sb.append("seppInfo");
        sb.append('=');
        sb.append(((this.seppInfo == null) ? "<null>" : this.seppInfo));
        sb.append(',');
        sb.append("nfService");
        sb.append('=');
        sb.append(((this.nfService == null) ? "<null>" : this.nfService));
        sb.append(',');
        sb.append("bsfInfo");
        sb.append('=');
        sb.append(((this.bsfInfo == null) ? "<null>" : this.bsfInfo));
        sb.append(',');
        sb.append("nfInstanceName");
        sb.append('=');
        sb.append(((this.nfInstanceName == null) ? "<null>" : this.nfInstanceName));
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
        sb.append("snssai1");
        sb.append('=');
        sb.append(((this.snssai1 == null) ? "<null>" : this.snssai1));
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
        sb.append("nfSetId");
        sb.append('=');
        sb.append(((this.nfSetId == null) ? "<null>" : this.nfSetId));
        sb.append(',');
        sb.append("nsi");
        sb.append('=');
        sb.append(((this.nsi == null) ? "<null>" : this.nsi));
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
        sb.append("scpDomain");
        sb.append('=');
        sb.append(((this.scpDomain == null) ? "<null>" : this.scpDomain));
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
        result = ((result * 31) + ((this.snssai1 == null) ? 0 : this.snssai1.hashCode()));
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.bsfInfo == null) ? 0 : this.bsfInfo.hashCode()));
        result = ((result * 31) + ((this.allowedNfType == null) ? 0 : this.allowedNfType.hashCode()));
        result = ((result * 31) + ((this.nfService == null) ? 0 : this.nfService.hashCode()));
        result = ((result * 31) + ((this.allowedNssai1 == null) ? 0 : this.allowedNssai1.hashCode()));
        result = ((result * 31) + ((this.seppInfo == null) ? 0 : this.seppInfo.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.locality == null) ? 0 : this.locality.hashCode()));
        result = ((result * 31) + ((this.nfInstanceName == null) ? 0 : this.nfInstanceName.hashCode()));
        result = ((result * 31) + ((this.plmn == null) ? 0 : this.plmn.hashCode()));
        result = ((result * 31) + ((this.nfSpecificInfo == null) ? 0 : this.nfSpecificInfo.hashCode()));
        result = ((result * 31) + ((this.chfInfo == null) ? 0 : this.chfInfo.hashCode()));
        result = ((result * 31) + ((this.requestedHeartbeatTimer == null) ? 0 : this.requestedHeartbeatTimer.hashCode()));
        result = ((result * 31) + ((this.allowedPlmn == null) ? 0 : this.allowedPlmn.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        result = ((result * 31) + ((this.allowedNfDomain == null) ? 0 : this.allowedNfDomain.hashCode()));
        result = ((result * 31) + ((this.allowedNssai == null) ? 0 : this.allowedNssai.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.adminState == null) ? 0 : this.adminState.hashCode()));
        result = ((result * 31) + ((this.nfSetId == null) ? 0 : this.nfSetId.hashCode()));
        result = ((result * 31) + ((this.nsi == null) ? 0 : this.nsi.hashCode()));
        result = ((result * 31) + ((this.associatedNfPoolRef == null) ? 0 : this.associatedNfPoolRef.hashCode()));
        result = ((result * 31) + ((this.servicePriority == null) ? 0 : this.servicePriority.hashCode()));
        result = ((result * 31) + ((this.scpInfo == null) ? 0 : this.scpInfo.hashCode()));
        result = ((result * 31) + ((this.scpDomain == null) ? 0 : this.scpDomain.hashCode()));

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
        return (((((((((((((((((((((((((((((this.recoveryTime == rhs.recoveryTime)
                                           || ((this.recoveryTime != null) && this.recoveryTime.equals(rhs.recoveryTime)))
                                          && ((this.snssai == rhs.snssai) || ((this.snssai != null) && this.snssai.equals(rhs.snssai))))
                                         && ((this.snssai1 == rhs.snssai1) || ((this.snssai1 != null) && this.snssai1.equals(rhs.snssai1))))
                                        && ((this.serviceAddressRef == rhs.serviceAddressRef)
                                            || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))))
                                       && ((this.allowedNfType == rhs.allowedNfType)
                                           || ((this.allowedNfType != null) && this.allowedNfType.equals(rhs.allowedNfType))))
                                      && ((this.nfService == rhs.nfService) || ((this.nfService != null) && this.nfService.equals(rhs.nfService))))
                                     && ((this.allowedNssai1 == rhs.allowedNssai1)
                                         || ((this.allowedNssai1 != null) && this.allowedNssai1.equals(rhs.allowedNssai1))))
                                    && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                                   && ((this.locality == rhs.locality) || ((this.locality != null) && this.locality.equals(rhs.locality))))
                                  && ((this.nfInstanceName == rhs.nfInstanceName)
                                      || ((this.nfInstanceName != null) && this.nfInstanceName.equals(rhs.nfInstanceName))))
                                 && ((this.plmn == rhs.plmn) || ((this.plmn != null) && this.plmn.equals(rhs.plmn))))
                                && ((this.nfSpecificInfo == rhs.nfSpecificInfo)
                                    || ((this.nfSpecificInfo != null) && this.nfSpecificInfo.equals(rhs.nfSpecificInfo))))
                               && ((this.requestedHeartbeatTimer == rhs.requestedHeartbeatTimer)
                                   || ((this.requestedHeartbeatTimer != null) && this.requestedHeartbeatTimer.equals(rhs.requestedHeartbeatTimer))))
                              && ((this.allowedPlmn == rhs.allowedPlmn) || ((this.allowedPlmn != null) && this.allowedPlmn.equals(rhs.allowedPlmn))))
                             && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))))
                            && ((this.allowedNfDomain == rhs.allowedNfDomain)
                                || ((this.allowedNfDomain != null) && this.allowedNfDomain.equals(rhs.allowedNfDomain))))
                           && ((this.allowedNssai == rhs.allowedNssai) || ((this.allowedNssai != null) && this.allowedNssai.equals(rhs.allowedNssai))))
                          && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                         && ((this.adminState == rhs.adminState) || ((this.adminState != null) && this.adminState.equals(rhs.adminState))))
                        && ((this.nfSetId == rhs.nfSetId) || ((this.nfSetId != null) && this.nfSetId.equals(rhs.nfSetId))))
                       && ((this.nsi == rhs.nsi) || ((this.nsi != null) && this.nsi.equals(rhs.nsi))))
                      && ((this.associatedNfPoolRef == rhs.associatedNfPoolRef)
                          || ((this.associatedNfPoolRef != null) && this.associatedNfPoolRef.equals(rhs.associatedNfPoolRef))))
                     && ((this.servicePriority == rhs.servicePriority) || ((this.servicePriority != null) && this.servicePriority.equals(rhs.servicePriority))))
                    && ((this.chfInfo == rhs.chfInfo) || ((this.chfInfo != null) && this.chfInfo.equals(rhs.chfInfo))))
                   && ((this.scpInfo == rhs.scpInfo) || ((this.scpInfo != null) && this.scpInfo.equals(rhs.scpInfo))))
                  && ((this.scpDomain == rhs.scpDomain) || ((this.scpDomain != null) && this.scpDomain.equals(rhs.scpDomain))))
                 && ((this.bsfInfo == rhs.bsfInfo) || ((this.bsfInfo != null) && this.bsfInfo.equals(rhs.bsfInfo))))
                && ((this.seppInfo == rhs.seppInfo) || ((this.seppInfo != null) && this.seppInfo.equals(rhs.seppInfo))));
    }
}
