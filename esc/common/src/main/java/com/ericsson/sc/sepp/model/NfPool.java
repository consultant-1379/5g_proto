
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.sc.glue.IfNfPool;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "user-label",
                     "egress-connection-profile-ref",
                     "dns-profile-ref",
                     "preferred-ip-family",
                     "add-non-matching-as-lowest-priority",
                     "nf-match-condition",
                     "scp-match-condition",
                     "nf-pool-discovery",
                     "priority-group",
                     "static-scp-instance-data-ref",
                     "static-sepp-instance-data-ref",
                     "pool-retry-budget",
                     "temporary-blocking",
                     "active-health-check",
                     "enable-stats-per-nf-instance",
                     "threshold-for-nf-unavailable-alarm",
                     "check-san-on-egress",
                     "out-request-screening-case-ref",
                     "in-response-screening-case-ref",
                     "own-network-ref",
                     "roaming-partner-ref" })
public class NfPool implements IfNfPool
{

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the nf-pool")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    @JsonPropertyDescription("Reference to a defined egress-connection-profile")
    private String egressConnectionProfileRef;
    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of this nf-pool. This setting overrides the global setting.
     * 
     */
    @JsonProperty("dns-profile-ref")
    @JsonPropertyDescription("Reference to a defined dns-profile that will be used for DNS resolution on producers of this nf-pool. This setting overrides the global setting.")
    private String dnsProfileRef;
    /**
     * Preferred IP version for routing.
     * 
     */
    @JsonProperty("preferred-ip-family")
    @JsonPropertyDescription("Preferred IP version for routing.")
    private NfPool.PreferredIpFamily preferredIpFamily;
    /**
     * If true, last priority-group is created with non-matching instances or
     * endpoints.
     * 
     */
    @JsonProperty("add-non-matching-as-lowest-priority")
    @JsonPropertyDescription("If true, last priority-group is created with non-matching instances or endpoints.")
    private Boolean addNonMatchingAsLowestPriority = true;
    /**
     * Only the NF instances that satisfy the nf-match-condition are included in the
     * NF pool. This is applicable only to dynamically discovered NF instances.
     * 
     */
    @JsonProperty("nf-match-condition")
    @JsonPropertyDescription("Only the NF instances that satisfy the nf-match-condition are included in the NF pool. This is applicable only to dynamically discovered NF instances.")
    private String nfMatchCondition;
    /**
     * Only the SCP instances that satisfy the scp-match-condition are included in
     * the NF pool. Applicable only to dynamically discovered SCP instances
     * 
     */
    @JsonProperty("scp-match-condition")
    @JsonPropertyDescription("Only the SCP instances that satisfy the scp-match-condition are included in the NF pool. Applicable only to dynamically discovered SCP instances")
    private String scpMatchCondition;
    /**
     * Attributes to define discovery and grouping of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    @JsonPropertyDescription("Attributes to define discovery and grouping of NF instances")
    private List<NfPoolDiscovery> nfPoolDiscovery = new ArrayList<NfPoolDiscovery>();
    /**
     * Group of NF instances that satisfy specific selection criteria and belong to
     * the same priority
     * 
     */
    @JsonProperty("priority-group")
    @JsonPropertyDescription("Group of NF instances that satisfy specific selection criteria and belong to the same priority")
    private List<PriorityGroup> priorityGroup = new ArrayList<PriorityGroup>();
    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    @JsonPropertyDescription("Reference to an SCP instance data set")
    private List<String> staticScpInstanceDataRef = new ArrayList<String>();
    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    @JsonPropertyDescription("Reference to a SEPP instance data set")
    private List<String> staticSeppInstanceDataRef = new ArrayList<String>();
    /**
     * Specifies a limit per pool on concurrent retries in relation to the integer
     * of active requests
     * 
     */
    @JsonProperty("pool-retry-budget")
    @JsonPropertyDescription("Specifies a limit per pool on concurrent retries in relation to the integer of active requests")
    private PoolRetryBudget poolRetryBudget;
    /**
     * Temporary blocking allows the dynamic blocking of NFs which are not
     * reachable, based on configurable and preset attributes
     * 
     */
    @JsonProperty("temporary-blocking")
    @JsonPropertyDescription("Temporary blocking allows the dynamic blocking of NFs which are not reachable, based on configurable and preset attributes")
    private TemporaryBlocking temporaryBlocking;
    /**
     * Active health checking per nf-pool monitors continuously the health of all
     * static and discovered nf-instances associated with an nf-pool.
     * 
     */
    @JsonProperty("active-health-check")
    @JsonPropertyDescription("Active health checking per nf-pool monitors continuously the health of all static and discovered nf-instances associated with an nf-pool.")
    private ActiveHealthCheck activeHealthCheck;
    /**
     * If set to true, then counters per NF instance are activated
     * 
     */
    @JsonProperty("enable-stats-per-nf-instance")
    @JsonPropertyDescription("If set to true, then counters per NF instance are activated")
    private Boolean enableStatsPerNfInstance = false;
    /**
     * Threshold for the integer of 5xx errors and timeouts received within 15
     * seconds time interval, that lead to raising the alarm: SEPP, NF Instance
     * Unavailable
     * 
     */
    @JsonProperty("threshold-for-nf-unavailable-alarm")
    @JsonPropertyDescription("Threshold for the integer of 5xx errors and timeouts received within 15 seconds time interval, that lead to raising the alarm: SEPP, NF Instance Unavailable")
    private Integer thresholdForNfUnavailableAlarm;
    /**
     * If present, SEPP acting as client checks RP's server certificate matching the
     * SANs presented in it with the configured fqdns of that specific RP and only
     * if they are matched , SEPP will send requests towards RP
     * 
     */
    @JsonProperty("check-san-on-egress")
    @JsonPropertyDescription("If present, SEPP acting as client checks RP's server certificate matching the SANs presented in it with the configured fqdns of that specific RP and only if they are matched , SEPP will send requests towards RP")
    private CheckSanOnEgress checkSanOnEgress;
    /**
     * Reference to the request screening case that is applied when the request is
     * targeting an NF instance in this nf-pool
     * 
     */
    @JsonProperty("out-request-screening-case-ref")
    @JsonPropertyDescription("Reference to the request screening case that is applied when the request is targeting an NF instance in this nf-pool")
    private String outRequestScreeningCaseRef;
    /**
     * Reference to the response screening case that is applied when the response is
     * received from an NF instance in this nf-pool
     * 
     */
    @JsonProperty("in-response-screening-case-ref")
    @JsonPropertyDescription("Reference to the response screening case that is applied when the response is received from an NF instance in this nf-pool")
    private String inResponseScreeningCaseRef;
    /**
     * The own-network to which this nf-pool belongs to.
     * 
     */
    @JsonProperty("own-network-ref")
    @JsonPropertyDescription("The own-network to which this nf-pool belongs to.")
    private String ownNetworkRef;
    /**
     * The roaming-partner to which this pool belongs to.
     * 
     */
    @JsonProperty("roaming-partner-ref")
    @JsonPropertyDescription("The roaming-partner to which this pool belongs to.")
    private String roamingPartnerRef;

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfPool withName(String name)
    {
        this.name = name;
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

    public NfPool withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    public String getEgressConnectionProfileRef()
    {
        return egressConnectionProfileRef;
    }

    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    public void setEgressConnectionProfileRef(String egressConnectionProfileRef)
    {
        this.egressConnectionProfileRef = egressConnectionProfileRef;
    }

    public NfPool withEgressConnectionProfileRef(String egressConnectionProfileRef)
    {
        this.egressConnectionProfileRef = egressConnectionProfileRef;
        return this;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of this nf-pool. This setting overrides the global setting.
     * 
     */
    @JsonProperty("dns-profile-ref")
    public String getDnsProfileRef()
    {
        return dnsProfileRef;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of this nf-pool. This setting overrides the global setting.
     * 
     */
    @JsonProperty("dns-profile-ref")
    public void setDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
    }

    public NfPool withDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
        return this;
    }

    /**
     * Preferred IP version for routing.
     * 
     */
    @JsonProperty("preferred-ip-family")
    public NfPool.PreferredIpFamily getPreferredIpFamily()
    {
        return preferredIpFamily;
    }

    /**
     * Preferred IP version for routing.
     * 
     */
    @JsonProperty("preferred-ip-family")
    public void setPreferredIpFamily(NfPool.PreferredIpFamily preferredIpFamily)
    {
        this.preferredIpFamily = preferredIpFamily;
    }

    public NfPool withPreferredIpFamily(NfPool.PreferredIpFamily preferredIpFamily)
    {
        this.preferredIpFamily = preferredIpFamily;
        return this;
    }

    /**
     * If true, last priority-group is created with non-matching instances or
     * endpoints.
     * 
     */
    @JsonProperty("add-non-matching-as-lowest-priority")
    public Boolean getAddNonMatchingAsLowestPriority()
    {
        return addNonMatchingAsLowestPriority;
    }

    /**
     * If true, last priority-group is created with non-matching instances or
     * endpoints.
     * 
     */
    @JsonProperty("add-non-matching-as-lowest-priority")
    public void setAddNonMatchingAsLowestPriority(Boolean addNonMatchingAsLowestPriority)
    {
        this.addNonMatchingAsLowestPriority = addNonMatchingAsLowestPriority;
    }

    public NfPool withAddNonMatchingAsLowestPriority(Boolean addNonMatchingAsLowestPriority)
    {
        this.addNonMatchingAsLowestPriority = addNonMatchingAsLowestPriority;
        return this;
    }

    /**
     * Only the NF instances that satisfy the nf-match-condition are included in the
     * NF pool. This is applicable only to dynamically discovered NF instances.
     * 
     */
    @JsonProperty("nf-match-condition")
    public String getNfMatchCondition()
    {
        return nfMatchCondition;
    }

    /**
     * Only the NF instances that satisfy the nf-match-condition are included in the
     * NF pool. This is applicable only to dynamically discovered NF instances.
     * 
     */
    @JsonProperty("nf-match-condition")
    public void setNfMatchCondition(String nfMatchCondition)
    {
        this.nfMatchCondition = nfMatchCondition;
    }

    public NfPool withNfMatchCondition(String nfMatchCondition)
    {
        this.nfMatchCondition = nfMatchCondition;
        return this;
    }

    /**
     * Only the SCP instances that satisfy the scp-match-condition are included in
     * the NF pool. Applicable only to dynamically discovered SCP instances
     * 
     */
    @JsonProperty("scp-match-condition")
    public String getScpMatchCondition()
    {
        return scpMatchCondition;
    }

    /**
     * Only the SCP instances that satisfy the scp-match-condition are included in
     * the NF pool. Applicable only to dynamically discovered SCP instances
     * 
     */
    @JsonProperty("scp-match-condition")
    public void setScpMatchCondition(String scpMatchCondition)
    {
        this.scpMatchCondition = scpMatchCondition;
    }

    public NfPool withScpMatchCondition(String scpMatchCondition)
    {
        this.scpMatchCondition = scpMatchCondition;
        return this;
    }

    /**
     * Attributes to define discovery and grouping of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    public List<NfPoolDiscovery> getNfPoolDiscovery()
    {
        return nfPoolDiscovery;
    }

    /**
     * Attributes to define discovery and grouping of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    public void setNfPoolDiscovery(List<NfPoolDiscovery> nfPoolDiscovery)
    {
        this.nfPoolDiscovery = nfPoolDiscovery;
    }

    public NfPool withNfPoolDiscovery(List<NfPoolDiscovery> nfPoolDiscovery)
    {
        this.nfPoolDiscovery = nfPoolDiscovery;
        return this;
    }

    /**
     * Group of NF instances that satisfy specific selection criteria and belong to
     * the same priority
     * 
     */
    @JsonProperty("priority-group")
    public List<PriorityGroup> getPriorityGroup()
    {
        return priorityGroup;
    }

    /**
     * Group of NF instances that satisfy specific selection criteria and belong to
     * the same priority
     * 
     */
    @JsonProperty("priority-group")
    public void setPriorityGroup(List<PriorityGroup> priorityGroup)
    {
        this.priorityGroup = priorityGroup;
    }

    public NfPool withPriorityGroup(List<PriorityGroup> priorityGroup)
    {
        this.priorityGroup = priorityGroup;
        return this;
    }

    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    public List<String> getStaticScpInstanceDataRef()
    {
        return staticScpInstanceDataRef;
    }

    /**
     * Reference to an SCP instance data set
     * 
     */
    @JsonProperty("static-scp-instance-data-ref")
    public void setStaticScpInstanceDataRef(List<String> staticScpInstanceDataRef)
    {
        this.staticScpInstanceDataRef = staticScpInstanceDataRef;
    }

    public NfPool withStaticScpInstanceDataRef(List<String> staticScpInstanceDataRef)
    {
        this.staticScpInstanceDataRef = staticScpInstanceDataRef;
        return this;
    }

    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    public List<String> getStaticSeppInstanceDataRef()
    {
        return staticSeppInstanceDataRef;
    }

    /**
     * Reference to a SEPP instance data set
     * 
     */
    @JsonProperty("static-sepp-instance-data-ref")
    public void setStaticSeppInstanceDataRef(List<String> staticSeppInstanceDataRef)
    {
        this.staticSeppInstanceDataRef = staticSeppInstanceDataRef;
    }

    public NfPool withStaticSeppInstanceDataRef(List<String> staticSeppInstanceDataRef)
    {
        this.staticSeppInstanceDataRef = staticSeppInstanceDataRef;
        return this;
    }

    /**
     * Specifies a limit per pool on concurrent retries in relation to the integer
     * of active requests
     * 
     */
    @JsonProperty("pool-retry-budget")
    public PoolRetryBudget getPoolRetryBudget()
    {
        return poolRetryBudget;
    }

    /**
     * Specifies a limit per pool on concurrent retries in relation to the integer
     * of active requests
     * 
     */
    @JsonProperty("pool-retry-budget")
    public void setPoolRetryBudget(PoolRetryBudget poolRetryBudget)
    {
        this.poolRetryBudget = poolRetryBudget;
    }

    public NfPool withPoolRetryBudget(PoolRetryBudget poolRetryBudget)
    {
        this.poolRetryBudget = poolRetryBudget;
        return this;
    }

    /**
     * Temporary blocking allows the dynamic blocking of NFs which are not
     * reachable, based on configurable and preset attributes
     * 
     */
    @JsonProperty("temporary-blocking")
    public TemporaryBlocking getTemporaryBlocking()
    {
        return temporaryBlocking;
    }

    /**
     * Temporary blocking allows the dynamic blocking of NFs which are not
     * reachable, based on configurable and preset attributes
     * 
     */
    @JsonProperty("temporary-blocking")
    public void setTemporaryBlocking(TemporaryBlocking temporaryBlocking)
    {
        this.temporaryBlocking = temporaryBlocking;
    }

    public NfPool withTemporaryBlocking(TemporaryBlocking temporaryBlocking)
    {
        this.temporaryBlocking = temporaryBlocking;
        return this;
    }

    /**
     * Active health checking per nf-pool monitors continuously the health of all
     * static and discovered nf-instances associated with an nf-pool.
     * 
     */
    @JsonProperty("active-health-check")
    public ActiveHealthCheck getActiveHealthCheck()
    {
        return activeHealthCheck;
    }

    /**
     * Active health checking per nf-pool monitors continuously the health of all
     * static and discovered nf-instances associated with an nf-pool.
     * 
     */
    @JsonProperty("active-health-check")
    public void setActiveHealthCheck(ActiveHealthCheck activeHealthCheck)
    {
        this.activeHealthCheck = activeHealthCheck;
    }

    public NfPool withActiveHealthCheck(ActiveHealthCheck activeHealthCheck)
    {
        this.activeHealthCheck = activeHealthCheck;
        return this;
    }

    /**
     * If set to true, then counters per NF instance are activated
     * 
     */
    @JsonProperty("enable-stats-per-nf-instance")
    public Boolean getEnableStatsPerNfInstance()
    {
        return enableStatsPerNfInstance;
    }

    /**
     * If set to true, then counters per NF instance are activated
     * 
     */
    @JsonProperty("enable-stats-per-nf-instance")
    public void setEnableStatsPerNfInstance(Boolean enableStatsPerNfInstance)
    {
        this.enableStatsPerNfInstance = enableStatsPerNfInstance;
    }

    public NfPool withEnableStatsPerNfInstance(Boolean enableStatsPerNfInstance)
    {
        this.enableStatsPerNfInstance = enableStatsPerNfInstance;
        return this;
    }

    /**
     * Threshold for the integer of 5xx errors and timeouts received within 15
     * seconds time interval, that lead to raising the alarm: SEPP, NF Instance
     * Unavailable
     * 
     */
    @JsonProperty("threshold-for-nf-unavailable-alarm")
    public Integer getThresholdForNfUnavailableAlarm()
    {
        return thresholdForNfUnavailableAlarm;
    }

    /**
     * Threshold for the integer of 5xx errors and timeouts received within 15
     * seconds time interval, that lead to raising the alarm: SEPP, NF Instance
     * Unavailable
     * 
     */
    @JsonProperty("threshold-for-nf-unavailable-alarm")
    public void setThresholdForNfUnavailableAlarm(Integer thresholdForNfUnavailableAlarm)
    {
        this.thresholdForNfUnavailableAlarm = thresholdForNfUnavailableAlarm;
    }

    public NfPool withThresholdForNfUnavailableAlarm(Integer thresholdForNfUnavailableAlarm)
    {
        this.thresholdForNfUnavailableAlarm = thresholdForNfUnavailableAlarm;
        return this;
    }

    /**
     * If present, SEPP acting as client checks RP's server certificate matching the
     * SANs presented in it with the configured fqdns of that specific RP and only
     * if they are matched , SEPP will send requests towards RP
     * 
     */
    @JsonProperty("check-san-on-egress")
    public CheckSanOnEgress getCheckSanOnEgress()
    {
        return checkSanOnEgress;
    }

    /**
     * If present, SEPP acting as client checks RP's server certificate matching the
     * SANs presented in it with the configured fqdns of that specific RP and only
     * if they are matched , SEPP will send requests towards RP
     * 
     */
    @JsonProperty("check-san-on-egress")
    public void setCheckSanOnEgress(CheckSanOnEgress checkSanOnEgress)
    {
        this.checkSanOnEgress = checkSanOnEgress;
    }

    public NfPool withCheckSanOnEgress(CheckSanOnEgress checkSanOnEgress)
    {
        this.checkSanOnEgress = checkSanOnEgress;
        return this;
    }

    /**
     * Reference to the request screening case that is applied when the request is
     * targeting an NF instance in this nf-pool
     * 
     */
    @JsonProperty("out-request-screening-case-ref")
    public String getOutRequestScreeningCaseRef()
    {
        return outRequestScreeningCaseRef;
    }

    /**
     * Reference to the request screening case that is applied when the request is
     * targeting an NF instance in this nf-pool
     * 
     */
    @JsonProperty("out-request-screening-case-ref")
    public void setOutRequestScreeningCaseRef(String outRequestScreeningCaseRef)
    {
        this.outRequestScreeningCaseRef = outRequestScreeningCaseRef;
    }

    public NfPool withOutRequestScreeningCaseRef(String outRequestScreeningCaseRef)
    {
        this.outRequestScreeningCaseRef = outRequestScreeningCaseRef;
        return this;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * received from an NF instance in this nf-pool
     * 
     */
    @JsonProperty("in-response-screening-case-ref")
    public String getInResponseScreeningCaseRef()
    {
        return inResponseScreeningCaseRef;
    }

    /**
     * Reference to the response screening case that is applied when the response is
     * received from an NF instance in this nf-pool
     * 
     */
    @JsonProperty("in-response-screening-case-ref")
    public void setInResponseScreeningCaseRef(String inResponseScreeningCaseRef)
    {
        this.inResponseScreeningCaseRef = inResponseScreeningCaseRef;
    }

    public NfPool withInResponseScreeningCaseRef(String inResponseScreeningCaseRef)
    {
        this.inResponseScreeningCaseRef = inResponseScreeningCaseRef;
        return this;
    }

    /**
     * The own-network to which this nf-pool belongs to.
     * 
     */
    @JsonProperty("own-network-ref")
    public String getOwnNetworkRef()
    {
        return ownNetworkRef;
    }

    /**
     * The own-network to which this nf-pool belongs to.
     * 
     */
    @JsonProperty("own-network-ref")
    public void setOwnNetworkRef(String ownNetworkRef)
    {
        this.ownNetworkRef = ownNetworkRef;
    }

    public NfPool withOwnNetworkRef(String ownNetworkRef)
    {
        this.ownNetworkRef = ownNetworkRef;
        return this;
    }

    /**
     * The roaming-partner to which this pool belongs to.
     * 
     */
    @JsonProperty("roaming-partner-ref")
    public String getRoamingPartnerRef()
    {
        return roamingPartnerRef;
    }

    /**
     * The roaming-partner to which this pool belongs to.
     * 
     */
    @JsonProperty("roaming-partner-ref")
    public void setRoamingPartnerRef(String roamingPartnerRef)
    {
        this.roamingPartnerRef = roamingPartnerRef;
    }

    public NfPool withRoamingPartnerRef(String roamingPartnerRef)
    {
        this.roamingPartnerRef = roamingPartnerRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfPool.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("egressConnectionProfileRef");
        sb.append('=');
        sb.append(((this.egressConnectionProfileRef == null) ? "<null>" : this.egressConnectionProfileRef));
        sb.append(',');
        sb.append("dnsProfileRef");
        sb.append('=');
        sb.append(((this.dnsProfileRef == null) ? "<null>" : this.dnsProfileRef));
        sb.append(',');
        sb.append("preferredIpFamily");
        sb.append('=');
        sb.append(((this.preferredIpFamily == null) ? "<null>" : this.preferredIpFamily));
        sb.append(',');
        sb.append("addNonMatchingAsLowestPriority");
        sb.append('=');
        sb.append(((this.addNonMatchingAsLowestPriority == null) ? "<null>" : this.addNonMatchingAsLowestPriority));
        sb.append(',');
        sb.append("nfMatchCondition");
        sb.append('=');
        sb.append(((this.nfMatchCondition == null) ? "<null>" : this.nfMatchCondition));
        sb.append(',');
        sb.append("scpMatchCondition");
        sb.append('=');
        sb.append(((this.scpMatchCondition == null) ? "<null>" : this.scpMatchCondition));
        sb.append(',');
        sb.append("nfPoolDiscovery");
        sb.append('=');
        sb.append(((this.nfPoolDiscovery == null) ? "<null>" : this.nfPoolDiscovery));
        sb.append(',');
        sb.append("priorityGroup");
        sb.append('=');
        sb.append(((this.priorityGroup == null) ? "<null>" : this.priorityGroup));
        sb.append(',');
        sb.append("staticScpInstanceDataRef");
        sb.append('=');
        sb.append(((this.staticScpInstanceDataRef == null) ? "<null>" : this.staticScpInstanceDataRef));
        sb.append(',');
        sb.append("staticSeppInstanceDataRef");
        sb.append('=');
        sb.append(((this.staticSeppInstanceDataRef == null) ? "<null>" : this.staticSeppInstanceDataRef));
        sb.append(',');
        sb.append("poolRetryBudget");
        sb.append('=');
        sb.append(((this.poolRetryBudget == null) ? "<null>" : this.poolRetryBudget));
        sb.append(',');
        sb.append("temporaryBlocking");
        sb.append('=');
        sb.append(((this.temporaryBlocking == null) ? "<null>" : this.temporaryBlocking));
        sb.append(',');
        sb.append("activeHealthCheck");
        sb.append('=');
        sb.append(((this.activeHealthCheck == null) ? "<null>" : this.activeHealthCheck));
        sb.append(',');
        sb.append("enableStatsPerNfInstance");
        sb.append('=');
        sb.append(((this.enableStatsPerNfInstance == null) ? "<null>" : this.enableStatsPerNfInstance));
        sb.append(',');
        sb.append("thresholdForNfUnavailableAlarm");
        sb.append('=');
        sb.append(((this.thresholdForNfUnavailableAlarm == null) ? "<null>" : this.thresholdForNfUnavailableAlarm));
        sb.append(',');
        sb.append("checkSanOnEgress");
        sb.append('=');
        sb.append(((this.checkSanOnEgress == null) ? "<null>" : this.checkSanOnEgress));
        sb.append(',');
        sb.append("outRequestScreeningCaseRef");
        sb.append('=');
        sb.append(((this.outRequestScreeningCaseRef == null) ? "<null>" : this.outRequestScreeningCaseRef));
        sb.append(',');
        sb.append("inResponseScreeningCaseRef");
        sb.append('=');
        sb.append(((this.inResponseScreeningCaseRef == null) ? "<null>" : this.inResponseScreeningCaseRef));
        sb.append(',');
        sb.append("ownNetworkRef");
        sb.append('=');
        sb.append(((this.ownNetworkRef == null) ? "<null>" : this.ownNetworkRef));
        sb.append(',');
        sb.append("roamingPartnerRef");
        sb.append('=');
        sb.append(((this.roamingPartnerRef == null) ? "<null>" : this.roamingPartnerRef));
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
        result = ((result * 31) + ((this.nfPoolDiscovery == null) ? 0 : this.nfPoolDiscovery.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.scpMatchCondition == null) ? 0 : this.scpMatchCondition.hashCode()));
        result = ((result * 31) + ((this.roamingPartnerRef == null) ? 0 : this.roamingPartnerRef.hashCode()));
        result = ((result * 31) + ((this.addNonMatchingAsLowestPriority == null) ? 0 : this.addNonMatchingAsLowestPriority.hashCode()));
        result = ((result * 31) + ((this.priorityGroup == null) ? 0 : this.priorityGroup.hashCode()));
        result = ((result * 31) + ((this.dnsProfileRef == null) ? 0 : this.dnsProfileRef.hashCode()));
        result = ((result * 31) + ((this.poolRetryBudget == null) ? 0 : this.poolRetryBudget.hashCode()));
        result = ((result * 31) + ((this.thresholdForNfUnavailableAlarm == null) ? 0 : this.thresholdForNfUnavailableAlarm.hashCode()));
        result = ((result * 31) + ((this.activeHealthCheck == null) ? 0 : this.activeHealthCheck.hashCode()));
        result = ((result * 31) + ((this.checkSanOnEgress == null) ? 0 : this.checkSanOnEgress.hashCode()));
        result = ((result * 31) + ((this.outRequestScreeningCaseRef == null) ? 0 : this.outRequestScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.inResponseScreeningCaseRef == null) ? 0 : this.inResponseScreeningCaseRef.hashCode()));
        result = ((result * 31) + ((this.temporaryBlocking == null) ? 0 : this.temporaryBlocking.hashCode()));
        result = ((result * 31) + ((this.staticSeppInstanceDataRef == null) ? 0 : this.staticSeppInstanceDataRef.hashCode()));
        result = ((result * 31) + ((this.ownNetworkRef == null) ? 0 : this.ownNetworkRef.hashCode()));
        result = ((result * 31) + ((this.egressConnectionProfileRef == null) ? 0 : this.egressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.preferredIpFamily == null) ? 0 : this.preferredIpFamily.hashCode()));
        result = ((result * 31) + ((this.staticScpInstanceDataRef == null) ? 0 : this.staticScpInstanceDataRef.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfMatchCondition == null) ? 0 : this.nfMatchCondition.hashCode()));
        result = ((result * 31) + ((this.enableStatsPerNfInstance == null) ? 0 : this.enableStatsPerNfInstance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfPool) == false)
        {
            return false;
        }
        NfPool rhs = ((NfPool) other);
        return (((((((((((((((((((((((this.nfPoolDiscovery == rhs.nfPoolDiscovery)
                                     || ((this.nfPoolDiscovery != null) && this.nfPoolDiscovery.equals(rhs.nfPoolDiscovery)))
                                    && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                                   && ((this.scpMatchCondition == rhs.scpMatchCondition)
                                       || ((this.scpMatchCondition != null) && this.scpMatchCondition.equals(rhs.scpMatchCondition))))
                                  && ((this.roamingPartnerRef == rhs.roamingPartnerRef)
                                      || ((this.roamingPartnerRef != null) && this.roamingPartnerRef.equals(rhs.roamingPartnerRef))))
                                 && ((this.addNonMatchingAsLowestPriority == rhs.addNonMatchingAsLowestPriority)
                                     || ((this.addNonMatchingAsLowestPriority != null)
                                         && this.addNonMatchingAsLowestPriority.equals(rhs.addNonMatchingAsLowestPriority))))
                                && ((this.priorityGroup == rhs.priorityGroup)
                                    || ((this.priorityGroup != null) && this.priorityGroup.equals(rhs.priorityGroup))))
                               && ((this.dnsProfileRef == rhs.dnsProfileRef) || ((this.dnsProfileRef != null) && this.dnsProfileRef.equals(rhs.dnsProfileRef))))
                              && ((this.poolRetryBudget == rhs.poolRetryBudget)
                                  || ((this.poolRetryBudget != null) && this.poolRetryBudget.equals(rhs.poolRetryBudget))))
                             && ((this.thresholdForNfUnavailableAlarm == rhs.thresholdForNfUnavailableAlarm)
                                 || ((this.thresholdForNfUnavailableAlarm != null)
                                     && this.thresholdForNfUnavailableAlarm.equals(rhs.thresholdForNfUnavailableAlarm))))
                            && ((this.activeHealthCheck == rhs.activeHealthCheck)
                                || ((this.activeHealthCheck != null) && this.activeHealthCheck.equals(rhs.activeHealthCheck))))
                           && ((this.checkSanOnEgress == rhs.checkSanOnEgress)
                               || ((this.checkSanOnEgress != null) && this.checkSanOnEgress.equals(rhs.checkSanOnEgress))))
                          && ((this.outRequestScreeningCaseRef == rhs.outRequestScreeningCaseRef)
                              || ((this.outRequestScreeningCaseRef != null) && this.outRequestScreeningCaseRef.equals(rhs.outRequestScreeningCaseRef))))
                         && ((this.inResponseScreeningCaseRef == rhs.inResponseScreeningCaseRef)
                             || ((this.inResponseScreeningCaseRef != null) && this.inResponseScreeningCaseRef.equals(rhs.inResponseScreeningCaseRef))))
                        && ((this.temporaryBlocking == rhs.temporaryBlocking)
                            || ((this.temporaryBlocking != null) && this.temporaryBlocking.equals(rhs.temporaryBlocking))))
                       && ((this.staticSeppInstanceDataRef == rhs.staticSeppInstanceDataRef)
                           || ((this.staticSeppInstanceDataRef != null) && this.staticSeppInstanceDataRef.equals(rhs.staticSeppInstanceDataRef))))
                      && ((this.ownNetworkRef == rhs.ownNetworkRef) || ((this.ownNetworkRef != null) && this.ownNetworkRef.equals(rhs.ownNetworkRef))))
                     && ((this.egressConnectionProfileRef == rhs.egressConnectionProfileRef)
                         || ((this.egressConnectionProfileRef != null) && this.egressConnectionProfileRef.equals(rhs.egressConnectionProfileRef))))
                    && ((this.preferredIpFamily == rhs.preferredIpFamily)
                        || ((this.preferredIpFamily != null) && this.preferredIpFamily.equals(rhs.preferredIpFamily))))
                   && ((this.staticScpInstanceDataRef == rhs.staticScpInstanceDataRef)
                       || ((this.staticScpInstanceDataRef != null) && this.staticScpInstanceDataRef.equals(rhs.staticScpInstanceDataRef))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.nfMatchCondition == rhs.nfMatchCondition)
                     || ((this.nfMatchCondition != null) && this.nfMatchCondition.equals(rhs.nfMatchCondition))))
                && ((this.enableStatsPerNfInstance == rhs.enableStatsPerNfInstance)
                    || ((this.enableStatsPerNfInstance != null) && this.enableStatsPerNfInstance.equals(rhs.enableStatsPerNfInstance))));
    }

    public enum PreferredIpFamily
    {

        IPV4("ipv4"),
        IPV6("ipv6");

        private final String value;
        private final static Map<String, NfPool.PreferredIpFamily> CONSTANTS = new HashMap<String, NfPool.PreferredIpFamily>();

        static
        {
            for (NfPool.PreferredIpFamily c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private PreferredIpFamily(String value)
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
        public static NfPool.PreferredIpFamily fromValue(String value)
        {
            NfPool.PreferredIpFamily constant = CONSTANTS.get(value);
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
