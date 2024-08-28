
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Properties of the running scan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "current-status", "started", "scanned-bindings", "stale-bindings", "deleted-bindings" })
public class EricssonBsfCurrentScan
{

    /**
     * Current scan status
     * 
     */
    @JsonProperty("current-status")
    @JsonPropertyDescription("Current scan status")
    private EricssonBsfCurrentScan.CurrentStatus currentStatus;
    /**
     * Starting time of the current scan
     * 
     */
    @JsonProperty("started")
    @JsonPropertyDescription("Starting time of the current scan")
    private String started;
    /**
     * Number of scanned bindings for the current run
     * 
     */
    @JsonProperty("scanned-bindings")
    @JsonPropertyDescription("Number of scanned bindings for the current run")
    private Long scannedBindings;
    /**
     * Number of stale bindings for the current run
     * 
     */
    @JsonProperty("stale-bindings")
    @JsonPropertyDescription("Number of stale bindings for the current run")
    private Long staleBindings;
    /**
     * Number of deleted bindings current for the current run
     * 
     */
    @JsonProperty("deleted-bindings")
    @JsonPropertyDescription("Number of deleted bindings current for the current run")
    private Long deletedBindings;

    /**
     * Current scan status
     * 
     */
    @JsonProperty("current-status")
    public EricssonBsfCurrentScan.CurrentStatus getCurrentStatus()
    {
        return currentStatus;
    }

    /**
     * Current scan status
     * 
     */
    @JsonProperty("current-status")
    public void setCurrentStatus(EricssonBsfCurrentScan.CurrentStatus currentStatus)
    {
        this.currentStatus = currentStatus;
    }

    public EricssonBsfCurrentScan withCurrentStatus(EricssonBsfCurrentScan.CurrentStatus currentStatus)
    {
        this.currentStatus = currentStatus;
        return this;
    }

    /**
     * Starting time of the current scan
     * 
     */
    @JsonProperty("started")
    public String getStarted()
    {
        return started;
    }

    /**
     * Starting time of the current scan
     * 
     */
    @JsonProperty("started")
    public void setStarted(String started)
    {
        this.started = started;
    }

    public EricssonBsfCurrentScan withStarted(String started)
    {
        this.started = started;
        return this;
    }

    /**
     * Number of scanned bindings for the current run
     * 
     */
    @JsonProperty("scanned-bindings")
    public Long getScannedBindings()
    {
        return scannedBindings;
    }

    /**
     * Number of scanned bindings for the current run
     * 
     */
    @JsonProperty("scanned-bindings")
    public void setScannedBindings(Long scannedBindings)
    {
        this.scannedBindings = scannedBindings;
    }

    public EricssonBsfCurrentScan withScannedBindings(Long scannedBindings)
    {
        this.scannedBindings = scannedBindings;
        return this;
    }

    /**
     * Number of stale bindings for the current run
     * 
     */
    @JsonProperty("stale-bindings")
    public Long getStaleBindings()
    {
        return staleBindings;
    }

    /**
     * Number of stale bindings for the current run
     * 
     */
    @JsonProperty("stale-bindings")
    public void setStaleBindings(Long staleBindings)
    {
        this.staleBindings = staleBindings;
    }

    public EricssonBsfCurrentScan withStaleBindings(Long staleBindings)
    {
        this.staleBindings = staleBindings;
        return this;
    }

    /**
     * Number of deleted bindings current for the current run
     * 
     */
    @JsonProperty("deleted-bindings")
    public Long getDeletedBindings()
    {
        return deletedBindings;
    }

    /**
     * Number of deleted bindings current for the current run
     * 
     */
    @JsonProperty("deleted-bindings")
    public void setDeletedBindings(Long deletedBindings)
    {
        this.deletedBindings = deletedBindings;
    }

    public EricssonBsfCurrentScan withDeletedBindings(Long deletedBindings)
    {
        this.deletedBindings = deletedBindings;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfCurrentScan.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("currentStatus");
        sb.append('=');
        sb.append(((this.currentStatus == null) ? "<null>" : this.currentStatus));
        sb.append(',');
        sb.append("started");
        sb.append('=');
        sb.append(((this.started == null) ? "<null>" : this.started));
        sb.append(',');
        sb.append("scannedBindings");
        sb.append('=');
        sb.append(((this.scannedBindings == null) ? "<null>" : this.scannedBindings));
        sb.append(',');
        sb.append("staleBindings");
        sb.append('=');
        sb.append(((this.staleBindings == null) ? "<null>" : this.staleBindings));
        sb.append(',');
        sb.append("deletedBindings");
        sb.append('=');
        sb.append(((this.deletedBindings == null) ? "<null>" : this.deletedBindings));
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
        result = ((result * 31) + ((this.started == null) ? 0 : this.started.hashCode()));
        result = ((result * 31) + ((this.scannedBindings == null) ? 0 : this.scannedBindings.hashCode()));
        result = ((result * 31) + ((this.deletedBindings == null) ? 0 : this.deletedBindings.hashCode()));
        result = ((result * 31) + ((this.currentStatus == null) ? 0 : this.currentStatus.hashCode()));
        result = ((result * 31) + ((this.staleBindings == null) ? 0 : this.staleBindings.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfCurrentScan) == false)
        {
            return false;
        }
        EricssonBsfCurrentScan rhs = ((EricssonBsfCurrentScan) other);
        return ((((((this.started == rhs.started) || ((this.started != null) && this.started.equals(rhs.started)))
                   && ((this.scannedBindings == rhs.scannedBindings) || ((this.scannedBindings != null) && this.scannedBindings.equals(rhs.scannedBindings))))
                  && ((this.deletedBindings == rhs.deletedBindings) || ((this.deletedBindings != null) && this.deletedBindings.equals(rhs.deletedBindings))))
                 && ((this.currentStatus == rhs.currentStatus) || ((this.currentStatus != null) && this.currentStatus.equals(rhs.currentStatus))))
                && ((this.staleBindings == rhs.staleBindings) || ((this.staleBindings != null) && this.staleBindings.equals(rhs.staleBindings))));
    }

    public enum CurrentStatus
    {

        RUNNING("running"),
        NOT_RUNNING("not-running");

        private final String value;
        private final static Map<String, EricssonBsfCurrentScan.CurrentStatus> CONSTANTS = new HashMap<String, EricssonBsfCurrentScan.CurrentStatus>();

        static
        {
            for (EricssonBsfCurrentScan.CurrentStatus c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private CurrentStatus(String value)
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
        public static EricssonBsfCurrentScan.CurrentStatus fromValue(String value)
        {
            EricssonBsfCurrentScan.CurrentStatus constant = CONSTANTS.get(value);
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
