
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ericsson.sc.glue.IfNfPoolDiscovery;
import com.ericsson.sc.nfm.model.UpdateInterval;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nrf-group-ref",
                     "nrf-query",
                     "static-nf-instance-data-ref",
                     "update-interval",
                     "last-update",
                     "discovered-nf-instance",
                     "discovered-scp-instance" })
public class NfPoolDiscovery implements IfNfPoolDiscovery
{

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the nf-pool-discovery")
    private String name;
    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("Reference to a list of NRF groups to be used for the NF discovery. If unspecified, the NRF group referenced by nrf-service/nf-discovery/nrf-group-ref applies")
    private List<String> nrfGroupRef = new ArrayList<String>();
    /**
     * NRF queries used to discover NF instances that then get stored in attribute
     * discovered-nf-instances of this nf-pool
     * 
     */
    @JsonProperty("nrf-query")
    @JsonPropertyDescription("NRF queries used to discover NF instances that then get stored in attribute discovered-nf-instances of this nf-pool")
    private List<NrfQuery> nrfQuery = new ArrayList<NrfQuery>();
    /**
     * Reference to a list of statically configured NF instances
     * 
     */
    @JsonProperty("static-nf-instance-data-ref")
    @JsonPropertyDescription("Reference to a list of statically configured NF instances")
    private List<String> staticNfInstanceDataRef = new ArrayList<String>();
    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    @JsonPropertyDescription("Time span between two nf-pool updates. Use special value infinite to stop regular nf-pool updates")
    private UpdateInterval updateInterval = UpdateInterval.fromValue("1h");
    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    @JsonPropertyDescription("Date and time of the last update of the discovered NF instances in this nf-pool")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date lastUpdate;
    /**
     * Discovered NF instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-nf-instance")
    @JsonPropertyDescription("Discovered NF instances in this nf-pool")
    private List<DiscoveredNfInstance> discoveredNfInstance = null; // Initial value must be null. Otherwise there will be inconsistencies with what
                                                                    // is stored in CMM.
    /**
     * Discovered SCP instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-scp-instance")
    @JsonPropertyDescription("Discovered SCP instances in this nf-pool")
    private List<DiscoveredScpInstance> discoveredScpInstance = new ArrayList<DiscoveredScpInstance>();

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfPoolDiscovery withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public List<String> getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public NfPoolDiscovery withNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    /**
     * NRF queries used to discover NF instances that then get stored in attribute
     * discovered-nf-instances of this nf-pool
     * 
     */
    @JsonProperty("nrf-query")
    public List<NrfQuery> getNrfQuery()
    {
        return nrfQuery;
    }

    /**
     * NRF queries used to discover NF instances that then get stored in attribute
     * discovered-nf-instances of this nf-pool
     * 
     */
    @JsonProperty("nrf-query")
    public void setNrfQuery(List<NrfQuery> nrfQuery)
    {
        this.nrfQuery = nrfQuery;
    }

    public NfPoolDiscovery withNrfQuery(List<NrfQuery> nrfQuery)
    {
        this.nrfQuery = nrfQuery;
        return this;
    }

    /**
     * Reference to a list of statically configured NF instances
     * 
     */
    @JsonProperty("static-nf-instance-data-ref")
    public List<String> getStaticNfInstanceDataRef()
    {
        return staticNfInstanceDataRef;
    }

    /**
     * Reference to a list of statically configured NF instances
     * 
     */
    @JsonProperty("static-nf-instance-data-ref")
    public void setStaticNfInstanceDataRef(List<String> staticNfInstanceDataRef)
    {
        this.staticNfInstanceDataRef = staticNfInstanceDataRef;
    }

    public NfPoolDiscovery withStaticNfInstanceDataRef(List<String> staticNfInstanceDataRef)
    {
        this.staticNfInstanceDataRef = staticNfInstanceDataRef;
        return this;
    }

    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    public UpdateInterval getUpdateInterval()
    {
        return updateInterval;
    }

    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    public void setUpdateInterval(UpdateInterval updateInterval)
    {
        this.updateInterval = updateInterval;
    }

    public NfPoolDiscovery withUpdateInterval(UpdateInterval updateInterval)
    {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public NfPoolDiscovery withLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
        return this;
    }

    /**
     * Discovered NF instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-nf-instance")
    public List<DiscoveredNfInstance> getDiscoveredNfInstance()
    {
        return discoveredNfInstance;
    }

    /**
     * Discovered NF instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-nf-instance")
    public void setDiscoveredNfInstance(List<DiscoveredNfInstance> discoveredNfInstance)
    {
        this.discoveredNfInstance = discoveredNfInstance;
    }

    public NfPoolDiscovery withDiscoveredNfInstance(List<DiscoveredNfInstance> discoveredNfInstance)
    {
        this.discoveredNfInstance = discoveredNfInstance;
        return this;
    }

    /**
     * Discovered SCP instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-scp-instance")
    public List<DiscoveredScpInstance> getDiscoveredScpInstance()
    {
        return discoveredScpInstance;
    }

    /**
     * Discovered SCP instances in this nf-pool
     * 
     */
    @JsonProperty("discovered-scp-instance")
    public void setDiscoveredScpInstance(List<DiscoveredScpInstance> discoveredScpInstance)
    {
        this.discoveredScpInstance = discoveredScpInstance;
    }

    public NfPoolDiscovery withDiscoveredScpInstance(List<DiscoveredScpInstance> discoveredScpInstance)
    {
        this.discoveredScpInstance = discoveredScpInstance;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfPoolDiscovery.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nrfGroupRef");
        sb.append('=');
        sb.append(((this.nrfGroupRef == null) ? "<null>" : this.nrfGroupRef));
        sb.append(',');
        sb.append("nrfQuery");
        sb.append('=');
        sb.append(((this.nrfQuery == null) ? "<null>" : this.nrfQuery));
        sb.append(',');
        sb.append("staticNfInstanceDataRef");
        sb.append('=');
        sb.append(((this.staticNfInstanceDataRef == null) ? "<null>" : this.staticNfInstanceDataRef));
        sb.append(',');
        sb.append("updateInterval");
        sb.append('=');
        sb.append(((this.updateInterval == null) ? "<null>" : this.updateInterval));
        sb.append(',');
        sb.append("lastUpdate");
        sb.append('=');
        sb.append(((this.lastUpdate == null) ? "<null>" : this.lastUpdate));
        sb.append(',');
        sb.append("discoveredNfInstance");
        sb.append('=');
        sb.append(((this.discoveredNfInstance == null) ? "<null>" : this.discoveredNfInstance));
        sb.append(',');
        sb.append("discoveredScpInstance");
        sb.append('=');
        sb.append(((this.discoveredScpInstance == null) ? "<null>" : this.discoveredScpInstance));
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
        result = ((result * 31) + ((this.nrfGroupRef == null) ? 0 : this.nrfGroupRef.hashCode()));
        result = ((result * 31) + ((this.updateInterval == null) ? 0 : this.updateInterval.hashCode()));
        result = ((result * 31) + ((this.nrfQuery == null) ? 0 : this.nrfQuery.hashCode()));
        result = ((result * 31) + ((this.discoveredNfInstance == null) ? 0 : this.discoveredNfInstance.hashCode()));
        result = ((result * 31) + ((this.lastUpdate == null) ? 0 : this.lastUpdate.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.discoveredScpInstance == null) ? 0 : this.discoveredScpInstance.hashCode()));
        result = ((result * 31) + ((this.staticNfInstanceDataRef == null) ? 0 : this.staticNfInstanceDataRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfPoolDiscovery) == false)
        {
            return false;
        }
        NfPoolDiscovery rhs = ((NfPoolDiscovery) other);
        return (((((((((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef)))
                      && ((this.updateInterval == rhs.updateInterval) || ((this.updateInterval != null) && this.updateInterval.equals(rhs.updateInterval))))
                     && ((this.nrfQuery == rhs.nrfQuery) || ((this.nrfQuery != null) && this.nrfQuery.equals(rhs.nrfQuery))))
                    && ((this.discoveredNfInstance == rhs.discoveredNfInstance)
                        || ((this.discoveredNfInstance != null) && this.discoveredNfInstance.equals(rhs.discoveredNfInstance))))
                   && ((this.lastUpdate == rhs.lastUpdate) || ((this.lastUpdate != null) && this.lastUpdate.equals(rhs.lastUpdate))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.discoveredScpInstance == rhs.discoveredScpInstance)
                     || ((this.discoveredScpInstance != null) && this.discoveredScpInstance.equals(rhs.discoveredScpInstance))))
                && ((this.staticNfInstanceDataRef == rhs.staticNfInstanceDataRef)
                    || ((this.staticNfInstanceDataRef != null) && this.staticNfInstanceDataRef.equals(rhs.staticNfInstanceDataRef))));
    }

}
