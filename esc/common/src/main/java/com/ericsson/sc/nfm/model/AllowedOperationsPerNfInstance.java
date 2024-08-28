
package com.ericsson.sc.nfm.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-instance-id", "operation" })
public class AllowedOperationsPerNfInstance
{

    /**
     * The NF instance ID for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("The NF instance ID for which the operations are allowed")
    private String nfInstanceId;
    /**
     * The operations allowed for the NF instance
     * 
     */
    @JsonProperty("operation")
    @JsonPropertyDescription("The operations allowed for the NF instance")
    private List<String> operation = new ArrayList<String>();

    /**
     * The NF instance ID for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * The NF instance ID for which the operations are allowed (Required)
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public AllowedOperationsPerNfInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * The operations allowed for the NF instance
     * 
     */
    @JsonProperty("operation")
    public List<String> getOperation()
    {
        return operation;
    }

    /**
     * The operations allowed for the NF instance
     * 
     */
    @JsonProperty("operation")
    public void setOperation(List<String> operation)
    {
        this.operation = operation;
    }

    public AllowedOperationsPerNfInstance withOperation(List<String> operation)
    {
        this.operation = operation;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AllowedOperationsPerNfInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
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
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
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
        if ((other instanceof AllowedOperationsPerNfInstance) == false)
        {
            return false;
        }
        AllowedOperationsPerNfInstance rhs = ((AllowedOperationsPerNfInstance) other);
        return (((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId)))
                && ((this.operation == rhs.operation) || ((this.operation != null) && this.operation.equals(rhs.operation))));
    }

}
