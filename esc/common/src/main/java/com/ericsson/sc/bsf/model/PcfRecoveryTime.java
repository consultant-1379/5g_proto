
package com.ericsson.sc.bsf.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration settings for cleanups based on pcf recovery time feature.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "check-upon-lookup", "binding-database-scan" })
public class PcfRecoveryTime
{

    /**
     * Configuration regarding pcf recovery time during traffic.
     * 
     */
    @JsonProperty("check-upon-lookup")
    @JsonPropertyDescription("Configuration regarding pcf recovery time during traffic.")
    private CheckUponLookup checkUponLookup;
    /**
     * Configuration for full table scan feature.
     * 
     */
    @JsonProperty("binding-database-scan")
    @JsonPropertyDescription("Configuration for full table scan feature.")
    private BindingDatabaseScan bindingDatabaseScan;

    /**
     * Configuration regarding pcf recovery time during traffic.
     * 
     */
    @JsonProperty("check-upon-lookup")
    public CheckUponLookup getCheckUponLookup()
    {
        return checkUponLookup;
    }

    /**
     * Configuration regarding pcf recovery time during traffic.
     * 
     */
    @JsonProperty("check-upon-lookup")
    public void setCheckUponLookup(CheckUponLookup checkUponLookup)
    {
        this.checkUponLookup = checkUponLookup;
    }

    public PcfRecoveryTime withCheckUponLookup(CheckUponLookup checkUponLookup)
    {
        this.checkUponLookup = checkUponLookup;
        return this;
    }

    /**
     * Configuration for full table scan feature.
     * 
     */
    @JsonProperty("binding-database-scan")
    public BindingDatabaseScan getBindingDatabaseScan()
    {
        return bindingDatabaseScan;
    }

    /**
     * Configuration for full table scan feature.
     * 
     */
    @JsonProperty("binding-database-scan")
    public void setBindingDatabaseScan(BindingDatabaseScan bindingDatabaseScan)
    {
        this.bindingDatabaseScan = bindingDatabaseScan;
    }

    public PcfRecoveryTime withBindingDatabaseScan(BindingDatabaseScan bindingDatabaseScan)
    {
        this.bindingDatabaseScan = bindingDatabaseScan;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PcfRecoveryTime.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("checkUponLookup");
        sb.append('=');
        sb.append(((this.checkUponLookup == null) ? "<null>" : this.checkUponLookup));
        sb.append(',');
        sb.append("bindingDatabaseScan");
        sb.append('=');
        sb.append(((this.bindingDatabaseScan == null) ? "<null>" : this.bindingDatabaseScan));
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
        return Objects.hash(bindingDatabaseScan, checkUponLookup);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PcfRecoveryTime other = (PcfRecoveryTime) obj;
        return Objects.equals(bindingDatabaseScan, other.bindingDatabaseScan) && Objects.equals(checkUponLookup, other.checkUponLookup);
    }

}
