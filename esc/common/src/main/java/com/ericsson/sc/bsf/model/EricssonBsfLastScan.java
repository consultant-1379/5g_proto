
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
 * Properties of the last scan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "status", "started", "stopped", "scanned-bindings", "stale-bindings", "deleted-bindings" })
public class EricssonBsfLastScan
{

    /**
     * Last scan status
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("Last scan status")
    private EricssonBsfLastScan.Status status;
    /**
     * Starting time of the last scan
     * 
     */
    @JsonProperty("started")
    @JsonPropertyDescription("Starting time of the last scan")
    private String started;
    /**
     * Stopping time of the last scan
     * 
     */
    @JsonProperty("stopped")
    @JsonPropertyDescription("Stopping time of the last scan")
    private String stopped;
    /**
     * Number of scanned bindings for the last run
     * 
     */
    @JsonProperty("scanned-bindings")
    @JsonPropertyDescription("Number of scanned bindings for the last run")
    private Long scannedBindings;
    /**
     * Number of stale bindings for the last run
     * 
     */
    @JsonProperty("stale-bindings")
    @JsonPropertyDescription("Number of stale bindings for the last run")
    private Long staleBindings;
    /**
     * Number of deleted bindings for the last run
     * 
     */
    @JsonProperty("deleted-bindings")
    @JsonPropertyDescription("Number of deleted bindings for the last run")
    private Long deletedBindings;

    /**
     * Last scan status
     * 
     */
    @JsonProperty("status")
    public EricssonBsfLastScan.Status getStatus()
    {
        return status;
    }

    /**
     * Last scan status
     * 
     */
    @JsonProperty("status")
    public void setStatus(EricssonBsfLastScan.Status status)
    {
        this.status = status;
    }

    public EricssonBsfLastScan withStatus(EricssonBsfLastScan.Status status)
    {
        this.status = status;
        return this;
    }

    /**
     * Starting time of the last scan
     * 
     */
    @JsonProperty("started")
    public String getStarted()
    {
        return started;
    }

    /**
     * Starting time of the last scan
     * 
     */
    @JsonProperty("started")
    public void setStarted(String started)
    {
        this.started = started;
    }

    public EricssonBsfLastScan withStarted(String started)
    {
        this.started = started;
        return this;
    }

    /**
     * Stopping time of the last scan
     * 
     */
    @JsonProperty("stopped")
    public String getStopped()
    {
        return stopped;
    }

    /**
     * Stopping time of the last scan
     * 
     */
    @JsonProperty("stopped")
    public void setStopped(String stopped)
    {
        this.stopped = stopped;
    }

    public EricssonBsfLastScan withStopped(String stopped)
    {
        this.stopped = stopped;
        return this;
    }

    /**
     * Number of scanned bindings for the last run
     * 
     */
    @JsonProperty("scanned-bindings")
    public Long getScannedBindings()
    {
        return scannedBindings;
    }

    /**
     * Number of scanned bindings for the last run
     * 
     */
    @JsonProperty("scanned-bindings")
    public void setScannedBindings(Long scannedBindings)
    {
        this.scannedBindings = scannedBindings;
    }

    public EricssonBsfLastScan withScannedBindings(Long scannedBindings)
    {
        this.scannedBindings = scannedBindings;
        return this;
    }

    /**
     * Number of stale bindings for the last run
     * 
     */
    @JsonProperty("stale-bindings")
    public Long getStaleBindings()
    {
        return staleBindings;
    }

    /**
     * Number of stale bindings for the last run
     * 
     */
    @JsonProperty("stale-bindings")
    public void setStaleBindings(Long staleBindings)
    {
        this.staleBindings = staleBindings;
    }

    public EricssonBsfLastScan withStaleBindings(Long staleBindings)
    {
        this.staleBindings = staleBindings;
        return this;
    }

    /**
     * Number of deleted bindings for the last run
     * 
     */
    @JsonProperty("deleted-bindings")
    public Long getDeletedBindings()
    {
        return deletedBindings;
    }

    /**
     * Number of deleted bindings for the last run
     * 
     */
    @JsonProperty("deleted-bindings")
    public void setDeletedBindings(Long deletedBindings)
    {
        this.deletedBindings = deletedBindings;
    }

    public EricssonBsfLastScan withDeletedBindings(Long deletedBindings)
    {
        this.deletedBindings = deletedBindings;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfLastScan.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("started");
        sb.append('=');
        sb.append(((this.started == null) ? "<null>" : this.started));
        sb.append(',');
        sb.append("stopped");
        sb.append('=');
        sb.append(((this.stopped == null) ? "<null>" : this.stopped));
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
        result = ((result * 31) + ((this.stopped == null) ? 0 : this.stopped.hashCode()));
        result = ((result * 31) + ((this.scannedBindings == null) ? 0 : this.scannedBindings.hashCode()));
        result = ((result * 31) + ((this.staleBindings == null) ? 0 : this.staleBindings.hashCode()));
        result = ((result * 31) + ((this.started == null) ? 0 : this.started.hashCode()));
        result = ((result * 31) + ((this.deletedBindings == null) ? 0 : this.deletedBindings.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfLastScan) == false)
        {
            return false;
        }
        EricssonBsfLastScan rhs = ((EricssonBsfLastScan) other);
        return (((((((this.stopped == rhs.stopped) || ((this.stopped != null) && this.stopped.equals(rhs.stopped)))
                    && ((this.scannedBindings == rhs.scannedBindings) || ((this.scannedBindings != null) && this.scannedBindings.equals(rhs.scannedBindings))))
                   && ((this.staleBindings == rhs.staleBindings) || ((this.staleBindings != null) && this.staleBindings.equals(rhs.staleBindings))))
                  && ((this.started == rhs.started) || ((this.started != null) && this.started.equals(rhs.started))))
                 && ((this.deletedBindings == rhs.deletedBindings) || ((this.deletedBindings != null) && this.deletedBindings.equals(rhs.deletedBindings))))
                && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status))));
    }

    public enum Status
    {

        COMPLETED("completed"),
        FAILED("failed"),
        ABORTED("aborted");

        private final String value;
        private final static Map<String, EricssonBsfLastScan.Status> CONSTANTS = new HashMap<String, EricssonBsfLastScan.Status>();

        static
        {
            for (EricssonBsfLastScan.Status c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Status(String value)
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
        public static EricssonBsfLastScan.Status fromValue(String value)
        {
            EricssonBsfLastScan.Status constant = CONSTANTS.get(value);
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
