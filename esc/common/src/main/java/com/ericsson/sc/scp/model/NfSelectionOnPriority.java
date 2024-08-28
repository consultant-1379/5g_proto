
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * From the discovered list of NF-profiles, choose one NF from those with the
 * highest priority and store it as the preferred host in the variable given in
 * ‘variable-name-selected-host’. The nf-set-if of the chosen host is stored in
 * the variable given in ‘variable-name-nfset’.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "variable-name-selected-host", "variable-name-nfset" })
public class NfSelectionOnPriority
{

    /**
     * The name of the variable used for storing the host name:port of the selected
     * NF
     * 
     */
    @JsonProperty("variable-name-selected-host")
    @JsonPropertyDescription("The name of the variable used for storing the host name:port of the selected NF")
    private String variableNameSelectedHost;
    /**
     * The name of the variable used for storing the nf-set-id of the selected NF
     * 
     */
    @JsonProperty("variable-name-nfset")
    @JsonPropertyDescription("The name of the variable used for storing the nf-set-id of the selected NF")
    private String variableNameNfset;

    /**
     * The name of the variable used for storing the host name:port of the selected
     * NF
     * 
     */
    @JsonProperty("variable-name-selected-host")
    public String getVariableNameSelectedHost()
    {
        return variableNameSelectedHost;
    }

    /**
     * The name of the variable used for storing the host name:port of the selected
     * NF
     * 
     */
    @JsonProperty("variable-name-selected-host")
    public void setVariableNameSelectedHost(String variableNameSelectedHost)
    {
        this.variableNameSelectedHost = variableNameSelectedHost;
    }

    public NfSelectionOnPriority withVariableNameSelectedHost(String variableNameSelectedHost)
    {
        this.variableNameSelectedHost = variableNameSelectedHost;
        return this;
    }

    /**
     * The name of the variable used for storing the nf-set-id of the selected NF
     * 
     */
    @JsonProperty("variable-name-nfset")
    public String getVariableNameNfset()
    {
        return variableNameNfset;
    }

    /**
     * The name of the variable used for storing the nf-set-id of the selected NF
     * 
     */
    @JsonProperty("variable-name-nfset")
    public void setVariableNameNfset(String variableNameNfset)
    {
        this.variableNameNfset = variableNameNfset;
    }

    public NfSelectionOnPriority withVariableNameNfset(String variableNameNfset)
    {
        this.variableNameNfset = variableNameNfset;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfSelectionOnPriority.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("variableNameSelectedHost");
        sb.append('=');
        sb.append(((this.variableNameSelectedHost == null) ? "<null>" : this.variableNameSelectedHost));
        sb.append(',');
        sb.append("variableNameNfset");
        sb.append('=');
        sb.append(((this.variableNameNfset == null) ? "<null>" : this.variableNameNfset));
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
        result = ((result * 31) + ((this.variableNameNfset == null) ? 0 : this.variableNameNfset.hashCode()));
        result = ((result * 31) + ((this.variableNameSelectedHost == null) ? 0 : this.variableNameSelectedHost.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfSelectionOnPriority) == false)
        {
            return false;
        }
        NfSelectionOnPriority rhs = ((NfSelectionOnPriority) other);
        return (((this.variableNameNfset == rhs.variableNameNfset)
                 || ((this.variableNameNfset != null) && this.variableNameNfset.equals(rhs.variableNameNfset)))
                && ((this.variableNameSelectedHost == rhs.variableNameSelectedHost)
                    || ((this.variableNameSelectedHost != null) && this.variableNameSelectedHost.equals(rhs.variableNameSelectedHost))));
    }

}
