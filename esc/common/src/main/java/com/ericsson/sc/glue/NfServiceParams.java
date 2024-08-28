package com.ericsson.sc.glue;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NfServiceParams
{

    @JsonIgnore
    private String nfType = "nf";   // Initialized with 'nf' for nfInstances without nfType set.

    @JsonIgnore
    private String nfInstanceId = "";   // Initialized with empty string for nfInstances without nfInstanceId.

    @JsonIgnore
    private String nfInstanceName = "";   // Initialized with empty string for nfInstances without nfInstanceName.

    @JsonIgnore
    private String prefix = "";   // Initialized with empty string for nfServices without prefix.

    /**
     * The NfType of the parent NfInstance. Used to determine indirect routing.
     * 
     */
    public String getNfType()
    {
        return this.nfType;
    }

    /**
     * The nfInstanceId of the parent NfInstance.
     * 
     */
    public String getNfInstanceId()
    {
        return this.nfInstanceId;
    }

    /**
     * The nfInstanceName of the parent NfInstance.
     * 
     */
    public String getNfInstanceName()
    {
        return this.nfInstanceName;
    }

    /**
     * The prefix of the NfService.
     * 
     */
    public String getPrefix()
    {
        return this.prefix;
    }

    /**
     * Set NfType of the service based on the value of the parent NfInstance. Used
     * to determine indirect routing.
     * 
     */
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    /**
     * Set the nfInstanceId based on the value of the parent NfInstance. Used to put
     * the nfInstanceId in the endpoint metadata of the service and add it in the
     * response header "3gpp-Sbi-Producer-Id"
     * 
     */
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    /**
     * Set the nfInstanceName based on the value of the parent NfInstance. Used to
     * put the nfInstanceName in the endpoint metadata of the service and add it in
     * the response header "3gpp-Sbi-Producer-Id"
     * 
     */
    public void setNfInstanceName(String nfInstanceName)
    {
        this.nfInstanceName = nfInstanceName;
    }

    /**
     * Set the prefix based on the value of the corresponding prefix of the
     * NfService. Used to put the prefix in the endpoint metadata of the service
     * 
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.nfInstanceName == null) ? 0 : this.nfInstanceName.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.prefix == null) ? 0 : this.prefix.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfServiceParams) == false)
        {
            return false;
        }
        NfServiceParams rhs = ((NfServiceParams) other);
        return (this.nfInstanceId == rhs.nfInstanceId || this.nfInstanceId != null && this.nfInstanceId.equals(rhs.nfInstanceId))
               && (this.nfInstanceName == rhs.nfInstanceName || this.nfInstanceName != null && this.nfInstanceName.equals(rhs.nfInstanceName))
               && (this.prefix == rhs.prefix || this.prefix != null && this.prefix.equals(rhs.prefix))
               && (this.nfType == rhs.nfType || this.nfType != null && this.nfType.equals(rhs.nfType));
    }

}
