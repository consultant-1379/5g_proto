
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Either replaces the value of an existing HTTP header with a new one or
 * prepends/appends a value to the existing value of a header
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "replace-value", "replace-from-var-name", "prepend-value", "prepend-from-var-name", "append-value", "append-from-var-name" })
public class ActionModifyHeader__1
{

    /**
     * Specifies the header to be modified (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Specifies the header to be modified")
    private String name;
    /**
     * Specifies the value to replace the header's old value
     * 
     */
    @JsonProperty("replace-value")
    @JsonPropertyDescription("Specifies the value to replace the header's old value")
    private String replaceValue;
    /**
     * Specifies the name of the variable from which to fetch the value to replace
     * the header's old value
     * 
     */
    @JsonProperty("replace-from-var-name")
    @JsonPropertyDescription("Specifies the name of the variable from which to fetch the value to replace the header's old value")
    private String replaceFromVarName;
    /**
     * Specifies the value to be prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-value")
    @JsonPropertyDescription("Specifies the value to be prepended to the header's old value")
    private String prependValue;
    /**
     * Specifies the name of the variable from which to fetch the value to be
     * prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    @JsonPropertyDescription("Specifies the name of the variable from which to fetch the value to be prepended to the header's old value")
    private String prependFromVarName;
    /**
     * Specifies the value to be appended to the header's old value
     * 
     */
    @JsonProperty("append-value")
    @JsonPropertyDescription("Specifies the value to be appended to the header's old value")
    private String appendValue;
    /**
     * Specifies the name of the variable from which to fetch the value to be
     * appended to the header's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    @JsonPropertyDescription("Specifies the name of the variable from which to fetch the value to be appended to the header's old value")
    private String appendFromVarName;

    /**
     * Specifies the header to be modified (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Specifies the header to be modified (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ActionModifyHeader__1 withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Specifies the value to replace the header's old value
     * 
     */
    @JsonProperty("replace-value")
    public String getReplaceValue()
    {
        return replaceValue;
    }

    /**
     * Specifies the value to replace the header's old value
     * 
     */
    @JsonProperty("replace-value")
    public void setReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
    }

    public ActionModifyHeader__1 withReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
        return this;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to replace
     * the header's old value
     * 
     */
    @JsonProperty("replace-from-var-name")
    public String getReplaceFromVarName()
    {
        return replaceFromVarName;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to replace
     * the header's old value
     * 
     */
    @JsonProperty("replace-from-var-name")
    public void setReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
    }

    public ActionModifyHeader__1 withReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
        return this;
    }

    /**
     * Specifies the value to be prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-value")
    public String getPrependValue()
    {
        return prependValue;
    }

    /**
     * Specifies the value to be prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-value")
    public void setPrependValue(String prependValue)
    {
        this.prependValue = prependValue;
    }

    public ActionModifyHeader__1 withPrependValue(String prependValue)
    {
        this.prependValue = prependValue;
        return this;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to be
     * prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    public String getPrependFromVarName()
    {
        return prependFromVarName;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to be
     * prepended to the header's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    public void setPrependFromVarName(String prependFromVarName)
    {
        this.prependFromVarName = prependFromVarName;
    }

    public ActionModifyHeader__1 withPrependFromVarName(String prependFromVarName)
    {
        this.prependFromVarName = prependFromVarName;
        return this;
    }

    /**
     * Specifies the value to be appended to the header's old value
     * 
     */
    @JsonProperty("append-value")
    public String getAppendValue()
    {
        return appendValue;
    }

    /**
     * Specifies the value to be appended to the header's old value
     * 
     */
    @JsonProperty("append-value")
    public void setAppendValue(String appendValue)
    {
        this.appendValue = appendValue;
    }

    public ActionModifyHeader__1 withAppendValue(String appendValue)
    {
        this.appendValue = appendValue;
        return this;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to be
     * appended to the header's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    public String getAppendFromVarName()
    {
        return appendFromVarName;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to be
     * appended to the header's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    public void setAppendFromVarName(String appendFromVarName)
    {
        this.appendFromVarName = appendFromVarName;
    }

    public ActionModifyHeader__1 withAppendFromVarName(String appendFromVarName)
    {
        this.appendFromVarName = appendFromVarName;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionModifyHeader__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("replaceValue");
        sb.append('=');
        sb.append(((this.replaceValue == null) ? "<null>" : this.replaceValue));
        sb.append(',');
        sb.append("replaceFromVarName");
        sb.append('=');
        sb.append(((this.replaceFromVarName == null) ? "<null>" : this.replaceFromVarName));
        sb.append(',');
        sb.append("prependValue");
        sb.append('=');
        sb.append(((this.prependValue == null) ? "<null>" : this.prependValue));
        sb.append(',');
        sb.append("prependFromVarName");
        sb.append('=');
        sb.append(((this.prependFromVarName == null) ? "<null>" : this.prependFromVarName));
        sb.append(',');
        sb.append("appendValue");
        sb.append('=');
        sb.append(((this.appendValue == null) ? "<null>" : this.appendValue));
        sb.append(',');
        sb.append("appendFromVarName");
        sb.append('=');
        sb.append(((this.appendFromVarName == null) ? "<null>" : this.appendFromVarName));
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.replaceValue == null) ? 0 : this.replaceValue.hashCode()));
        result = ((result * 31) + ((this.appendValue == null) ? 0 : this.appendValue.hashCode()));
        result = ((result * 31) + ((this.prependFromVarName == null) ? 0 : this.prependFromVarName.hashCode()));
        result = ((result * 31) + ((this.replaceFromVarName == null) ? 0 : this.replaceFromVarName.hashCode()));
        result = ((result * 31) + ((this.prependValue == null) ? 0 : this.prependValue.hashCode()));
        result = ((result * 31) + ((this.appendFromVarName == null) ? 0 : this.appendFromVarName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionModifyHeader__1) == false)
        {
            return false;
        }
        ActionModifyHeader__1 rhs = ((ActionModifyHeader__1) other);
        return ((((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                     && ((this.replaceValue == rhs.replaceValue) || ((this.replaceValue != null) && this.replaceValue.equals(rhs.replaceValue))))
                    && ((this.appendValue == rhs.appendValue) || ((this.appendValue != null) && this.appendValue.equals(rhs.appendValue))))
                   && ((this.prependFromVarName == rhs.prependFromVarName)
                       || ((this.prependFromVarName != null) && this.prependFromVarName.equals(rhs.prependFromVarName))))
                  && ((this.replaceFromVarName == rhs.replaceFromVarName)
                      || ((this.replaceFromVarName != null) && this.replaceFromVarName.equals(rhs.replaceFromVarName))))
                 && ((this.prependValue == rhs.prependValue) || ((this.prependValue != null) && this.prependValue.equals(rhs.prependValue))))
                && ((this.appendFromVarName == rhs.appendFromVarName)
                    || ((this.appendFromVarName != null) && this.appendFromVarName.equals(rhs.appendFromVarName))));
    }

}
