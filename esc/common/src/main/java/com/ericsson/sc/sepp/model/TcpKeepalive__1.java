package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfTcpKeepalive;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TCP-keepalive settings
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "probes", "time", "interval" })
public class TcpKeepalive__1 implements IfTcpKeepalive
{

    /**
     * Maximum integer of keepalive probes to send without response before deciding
     * the connection is dead
     * 
     */
    @JsonProperty("probes")
    @JsonPropertyDescription("Maximum integer of keepalive probes to send without response before deciding the connection is dead")
    private Integer probes = 6;
    /**
     * The integer of seconds a connection needs to be idle before keep-alive probes
     * start being sent
     * 
     */
    @JsonProperty("time")
    @JsonPropertyDescription("The integer of seconds a connection needs to be idle before keep-alive probes start being sent")
    private Integer time = 5;
    /**
     * The integer of seconds between keep-alive probes
     * 
     */
    @JsonProperty("interval")
    @JsonPropertyDescription("The integer of seconds between keep-alive probes")
    private Integer interval = 5;

    /**
     * Maximum integer of keepalive probes to send without response before deciding
     * the connection is dead
     * 
     */
    @JsonProperty("probes")
    public Integer getProbes()
    {
        return probes;
    }

    /**
     * Maximum integer of keepalive probes to send without response before deciding
     * the connection is dead
     * 
     */
    @JsonProperty("probes")
    public void setProbes(Integer probes)
    {
        this.probes = probes;
    }

    public TcpKeepalive__1 withProbes(Integer probes)
    {
        this.probes = probes;
        return this;
    }

    /**
     * The integer of seconds a connection needs to be idle before keep-alive probes
     * start being sent
     * 
     */
    @JsonProperty("time")
    public Integer getTime()
    {
        return time;
    }

    /**
     * The integer of seconds a connection needs to be idle before keep-alive probes
     * start being sent
     * 
     */
    @JsonProperty("time")
    public void setTime(Integer time)
    {
        this.time = time;
    }

    public TcpKeepalive__1 withTime(Integer time)
    {
        this.time = time;
        return this;
    }

    /**
     * The integer of seconds between keep-alive probes
     * 
     */
    @JsonProperty("interval")
    public Integer getInterval()
    {
        return interval;
    }

    /**
     * The integer of seconds between keep-alive probes
     * 
     */
    @JsonProperty("interval")
    public void setInterval(Integer interval)
    {
        this.interval = interval;
    }

    public TcpKeepalive__1 withInterval(Integer interval)
    {
        this.interval = interval;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TcpKeepalive__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("probes");
        sb.append('=');
        sb.append(((this.probes == null) ? "<null>" : this.probes));
        sb.append(',');
        sb.append("time");
        sb.append('=');
        sb.append(((this.time == null) ? "<null>" : this.time));
        sb.append(',');
        sb.append("interval");
        sb.append('=');
        sb.append(((this.interval == null) ? "<null>" : this.interval));
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
        result = ((result * 31) + ((this.probes == null) ? 0 : this.probes.hashCode()));
        result = ((result * 31) + ((this.interval == null) ? 0 : this.interval.hashCode()));
        result = ((result * 31) + ((this.time == null) ? 0 : this.time.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TcpKeepalive__1) == false)
        {
            return false;
        }
        TcpKeepalive__1 rhs = ((TcpKeepalive__1) other);
        return ((((this.probes == rhs.probes) || ((this.probes != null) && this.probes.equals(rhs.probes)))
                 && ((this.interval == rhs.interval) || ((this.interval != null) && this.interval.equals(rhs.interval))))
                && ((this.time == rhs.time) || ((this.time != null) && this.time.equals(rhs.time))));
    }

}
