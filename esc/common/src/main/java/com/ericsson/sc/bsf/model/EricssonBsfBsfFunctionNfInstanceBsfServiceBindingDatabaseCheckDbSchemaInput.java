
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Input schema for action
 * ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::check-db-schema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "configurationName", "configETag", "context", "input" })
public class EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput
{

    /**
     * Name of Yang Module where action belongs. (Required)
     * 
     */
    @JsonProperty("configurationName")
    @JsonPropertyDescription("Name of Yang Module where action belongs.")
    private String configurationName;
    /**
     * ETag version of configuration valid when action was called. (Required)
     * 
     */
    @JsonProperty("configETag")
    @JsonPropertyDescription("ETag version of configuration valid when action was called.")
    private String configETag;
    /**
     * Json pointer to the configuration context where action is executed.
     * (Required)
     * 
     */
    @JsonProperty("context")
    @JsonPropertyDescription("Json pointer to the configuration context where action is executed.")
    private String context;
    /**
     * Contains the action input parameters from Yang model.
     * 
     */
    @JsonProperty("input")
    @JsonPropertyDescription("Contains the action input parameters from Yang model.")
    private Input input;

    /**
     * Name of Yang Module where action belongs. (Required)
     * 
     */
    @JsonProperty("configurationName")
    public String getConfigurationName()
    {
        return configurationName;
    }

    /**
     * Name of Yang Module where action belongs. (Required)
     * 
     */
    @JsonProperty("configurationName")
    public void setConfigurationName(String configurationName)
    {
        this.configurationName = configurationName;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput withConfigurationName(String configurationName)
    {
        this.configurationName = configurationName;
        return this;
    }

    /**
     * ETag version of configuration valid when action was called. (Required)
     * 
     */
    @JsonProperty("configETag")
    public String getConfigETag()
    {
        return configETag;
    }

    /**
     * ETag version of configuration valid when action was called. (Required)
     * 
     */
    @JsonProperty("configETag")
    public void setConfigETag(String configETag)
    {
        this.configETag = configETag;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput withConfigETag(String configETag)
    {
        this.configETag = configETag;
        return this;
    }

    /**
     * Json pointer to the configuration context where action is executed.
     * (Required)
     * 
     */
    @JsonProperty("context")
    public String getContext()
    {
        return context;
    }

    /**
     * Json pointer to the configuration context where action is executed.
     * (Required)
     * 
     */
    @JsonProperty("context")
    public void setContext(String context)
    {
        this.context = context;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput withContext(String context)
    {
        this.context = context;
        return this;
    }

    /**
     * Contains the action input parameters from Yang model.
     * 
     */
    @JsonProperty("input")
    public Input getInput()
    {
        return input;
    }

    /**
     * Contains the action input parameters from Yang model.
     * 
     */
    @JsonProperty("input")
    public void setInput(Input input)
    {
        this.input = input;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput withInput(Input input)
    {
        this.input = input;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput.class.getName())
          .append('@')
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append('[');
        sb.append("configurationName");
        sb.append('=');
        sb.append(((this.configurationName == null) ? "<null>" : this.configurationName));
        sb.append(',');
        sb.append("configETag");
        sb.append('=');
        sb.append(((this.configETag == null) ? "<null>" : this.configETag));
        sb.append(',');
        sb.append("context");
        sb.append('=');
        sb.append(((this.context == null) ? "<null>" : this.context));
        sb.append(',');
        sb.append("input");
        sb.append('=');
        sb.append(((this.input == null) ? "<null>" : this.input));
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
        result = ((result * 31) + ((this.context == null) ? 0 : this.context.hashCode()));
        result = ((result * 31) + ((this.configurationName == null) ? 0 : this.configurationName.hashCode()));
        result = ((result * 31) + ((this.input == null) ? 0 : this.input.hashCode()));
        result = ((result * 31) + ((this.configETag == null) ? 0 : this.configETag.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput) == false)
        {
            return false;
        }
        EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput rhs = ((EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaInput) other);
        return (((((this.context == rhs.context) || ((this.context != null) && this.context.equals(rhs.context)))
                  && ((this.configurationName == rhs.configurationName)
                      || ((this.configurationName != null) && this.configurationName.equals(rhs.configurationName))))
                 && ((this.input == rhs.input) || ((this.input != null) && this.input.equals(rhs.input))))
                && ((this.configETag == rhs.configETag) || ((this.configETag != null) && this.configETag.equals(rhs.configETag))));
    }

}
