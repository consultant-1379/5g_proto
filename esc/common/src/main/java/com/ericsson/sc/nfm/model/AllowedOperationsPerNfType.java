
package com.ericsson.sc.nfm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-type", "operation" })
public class AllowedOperationsPerNfType
{

    /**
     * The NF type for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The NF type for which the operations are allowed")
    private String nfType;
    /**
     * The operations allowed for the NF type
     * 
     */
    @JsonProperty("operation")
    @JsonPropertyDescription("The operations allowed for the NF type")
    private List<String> operation = new ArrayList<String>();

    /**
     * The NF type for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The NF type for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public AllowedOperationsPerNfType withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * The operations allowed for the NF type
     * 
     */
    @JsonProperty("operation")
    public List<String> getOperation()
    {
        return operation;
    }

    /**
     * The operations allowed for the NF type
     * 
     */
    @JsonProperty("operation")
    public void setOperation(List<String> operation)
    {
        this.operation = operation;
    }

    public AllowedOperationsPerNfType withOperation(List<String> operation)
    {
        this.operation = operation;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AllowedOperationsPerNfType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("operation");
        sb.append('=');
        sb.append(((this.operation == null) ? "<null>" : this.operation));
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
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.operation == null) ? 0 : this.operation.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AllowedOperationsPerNfType) == false)
        {
            return false;
        }
        AllowedOperationsPerNfType rhs = ((AllowedOperationsPerNfType) other);
        return (((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType)))
                && ((this.operation == rhs.operation) || ((this.operation != null) && this.operation.equals(rhs.operation))));
    }

}
