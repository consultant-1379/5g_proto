
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "key", "initial-vector" })

public class FqdnScramblingTable
{

    /**
     * The encryption identifier of the scrambling key (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The encryption identifier of the scrambling key")
    private String id;
    /**
     * The key to be used for FQDN Scrambling functionality stored encrypted
     * (Required)
     * 
     */
    @JsonProperty("key")
    @JsonPropertyDescription("The key to be used for FQDN Scrambling functionality stored encrypted")
    private String key;
    /**
     * The initial vector to be used in AES GCM algorithm. (Required)
     * 
     */
    @JsonProperty("initial-vector")
    @JsonPropertyDescription("The initial vector to be used in AES GCM algorithm.")
    private String initialVector;

    /**
     * The encryption identifier of the scrambling key (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * The encryption identifier of the scrambling key (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public FqdnScramblingTable withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * The key to be used for FQDN Scrambling functionality stored encrypted
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey()
    {
        return key;
    }

    /**
     * The key to be used for FQDN Scrambling functionality stored encrypted
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key)
    {
        this.key = key;
    }

    public FqdnScramblingTable withKey(String key)
    {
        this.key = key;
        return this;
    }

    /**
     * The initial vector to be used in AES GCM algorithm. (Required)
     * 
     */
    @JsonProperty("initial-vector")
    public String getInitialVector()
    {
        return initialVector;
    }

    /**
     * The initial vector to be used in AES GCM algorithm. (Required)
     * 
     */
    @JsonProperty("initial-vector")
    public void setInitialVector(String initialVector)
    {
        this.initialVector = initialVector;
    }

    public FqdnScramblingTable withInitialVector(String initialVector)
    {
        this.initialVector = initialVector;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FqdnScramblingTable.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("key");
        sb.append('=');
        sb.append(((this.key == null) ? "<null>" : this.key));
        sb.append(',');
        sb.append("initialVector");
        sb.append('=');
        sb.append(((this.initialVector == null) ? "<null>" : this.initialVector));
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
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.initialVector == null) ? 0 : this.initialVector.hashCode()));
        result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof FqdnScramblingTable) == false)
        {
            return false;
        }
        FqdnScramblingTable rhs = ((FqdnScramblingTable) other);
        return ((((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id)))
                 && ((this.initialVector == rhs.initialVector) || ((this.initialVector != null) && this.initialVector.equals(rhs.initialVector))))
                && ((this.key == rhs.key) || ((this.key != null) && this.key.equals(rhs.key))));
    }

}
