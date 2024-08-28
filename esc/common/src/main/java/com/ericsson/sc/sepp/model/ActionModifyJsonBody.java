
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Performs modifications on JSON body elements by either adding new elements,
 * replacing the value of existing elements, appending/prepending strings to
 * existing elements or removing the elements from the JSON body
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "json-pointer",
                     "add-value",
                     "add-from-var-name",
                     "replace-value",
                     "replace-from-var-name",
                     "prepend-value",
                     "prepend-from-var-name",
                     "append-value",
                     "append-from-var-name",
                     "search-replace-string",
                     "search-replace-regex",
                     "remove" })
public class ActionModifyJsonBody
{

    /**
     * Specifies the path inside the JSON body where an element will be added or
     * modified or removed (Required)
     * 
     */
    @JsonProperty("json-pointer")
    @JsonPropertyDescription("Specifies the path inside the JSON body where an element will be added or modified or removed")
    private String jsonPointer;
    /**
     * Adds a new element to the path specified by the json-pointer attribute. If
     * the element already exists in the JSON body, it provides the possibility to
     * either replace the existing value or not. If the path where the element is to
     * be added does not already exist in the JSON body, it provides the possibility
     * to either create the path and add the element or do nothing and not add the
     * element to the JSON body
     * 
     */
    @JsonProperty("add-value")
    @JsonPropertyDescription("Adds a new element to the path specified by the json-pointer attribute. If the element already exists in the JSON body, it provides the possibility to either replace the existing value or not. If the path where the element is to be added does not already exist in the JSON body, it provides the possibility to either create the path and add the element or do nothing and not add the element to the JSON body")
    private AddValue addValue;
    /**
     * Adds a new element to the path specified by the json-pointer attribute. The
     * value of the new element is stored on a variable. If the element already
     * exists in the JSON body, it provides the possibility to either replace the
     * existing value or not. If the path where the element is to be added does not
     * already exist in the JSON body it provides the possibility to either create
     * the path and add the element or do nothing and not add the element to the
     * JSON body
     * 
     */
    @JsonProperty("add-from-var-name")
    @JsonPropertyDescription("Adds a new element to the path specified by the json-pointer attribute. The value of the new element is stored on a variable. If the element already exists in the JSON body, it provides the possibility to either replace the existing value or not. If the path where the element is to be added does not already exist in the JSON body it provides the possibility to either create the path and add the element or do nothing and not add the element to the JSON body")
    private AddFromVarName addFromVarName;
    /**
     * Replaces an element's old value with a new value specified in the value
     * attribute
     * 
     */
    @JsonProperty("replace-value")
    @JsonPropertyDescription("Replaces an element's old value with a new value specified in the value attribute")
    private ReplaceValue replaceValue;
    /**
     * Replaces an element's old value with a new value that is stored on a variable
     * 
     */
    @JsonProperty("replace-from-var-name")
    @JsonPropertyDescription("Replaces an element's old value with a new value that is stored on a variable")
    private ReplaceFromVarName replaceFromVarName;
    /**
     * Prepends a string to an element's old value
     * 
     */
    @JsonProperty("prepend-value")
    @JsonPropertyDescription("Prepends a string to an element's old value")
    private PrependValue prependValue;
    /**
     * Prepends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    @JsonPropertyDescription("Prepends a string stored in a variable to an element's old value")
    private PrependFromVarName prependFromVarName;
    /**
     * Appends a string to an element's old value
     * 
     */
    @JsonProperty("append-value")
    @JsonPropertyDescription("Appends a string to an element's old value")
    private AppendValue appendValue;
    /**
     * Appends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    @JsonPropertyDescription("Appends a string stored in a variable to an element's old value")
    private AppendFromVarName appendFromVarName;
    /**
     * Searches for a string using the path specified by the json-pointer attribute.
     * If the string is found in the JSON body, it is being replaced with the string
     * specified using the configured options
     * 
     */
    @JsonProperty("search-replace-string")
    @JsonPropertyDescription("Searches for a string using the path specified by the json-pointer attribute. If the string is found in the JSON body, it is being replaced with the string specified using the configured options")
    private SearchReplaceString searchReplaceString;
    /**
     * Searches for a pattern using the path specified by the json-pointer
     * attribute. If the pattern matches in the JSON body, it is being replaced with
     * the matched text specified using the configured options
     * 
     */
    @JsonProperty("search-replace-regex")
    @JsonPropertyDescription("Searches for a pattern using the path specified by the json-pointer attribute. If the pattern matches in the JSON body, it is being replaced with the matched text specified using the configured options")
    private SearchReplaceRegex searchReplaceRegex;
    /**
     * Removes the element, including its contents if any, that is specified by the
     * json-pointer attribute
     * 
     */
    @JsonProperty("remove")
    @JsonPropertyDescription("Removes the element, including its contents if any, that is specified by the json-pointer attribute")
    private Remove remove;

    /**
     * Specifies the path inside the JSON body where an element will be added or
     * modified or removed (Required)
     * 
     */
    @JsonProperty("json-pointer")
    public String getJsonPointer()
    {
        return jsonPointer;
    }

    /**
     * Specifies the path inside the JSON body where an element will be added or
     * modified or removed (Required)
     * 
     */
    @JsonProperty("json-pointer")
    public void setJsonPointer(String jsonPointer)
    {
        this.jsonPointer = jsonPointer;
    }

    public ActionModifyJsonBody withJsonPointer(String jsonPointer)
    {
        this.jsonPointer = jsonPointer;
        return this;
    }

    /**
     * Adds a new element to the path specified by the json-pointer attribute. If
     * the element already exists in the JSON body, it provides the possibility to
     * either replace the existing value or not. If the path where the element is to
     * be added does not already exist in the JSON body, it provides the possibility
     * to either create the path and add the element or do nothing and not add the
     * element to the JSON body
     * 
     */
    @JsonProperty("add-value")
    public AddValue getAddValue()
    {
        return addValue;
    }

    /**
     * Adds a new element to the path specified by the json-pointer attribute. If
     * the element already exists in the JSON body, it provides the possibility to
     * either replace the existing value or not. If the path where the element is to
     * be added does not already exist in the JSON body, it provides the possibility
     * to either create the path and add the element or do nothing and not add the
     * element to the JSON body
     * 
     */
    @JsonProperty("add-value")
    public void setAddValue(AddValue addValue)
    {
        this.addValue = addValue;
    }

    public ActionModifyJsonBody withAddValue(AddValue addValue)
    {
        this.addValue = addValue;
        return this;
    }

    /**
     * Adds a new element to the path specified by the json-pointer attribute. The
     * value of the new element is stored on a variable. If the element already
     * exists in the JSON body, it provides the possibility to either replace the
     * existing value or not. If the path where the element is to be added does not
     * already exist in the JSON body it provides the possibility to either create
     * the path and add the element or do nothing and not add the element to the
     * JSON body
     * 
     */
    @JsonProperty("add-from-var-name")
    public AddFromVarName getAddFromVarName()
    {
        return addFromVarName;
    }

    /**
     * Adds a new element to the path specified by the json-pointer attribute. The
     * value of the new element is stored on a variable. If the element already
     * exists in the JSON body, it provides the possibility to either replace the
     * existing value or not. If the path where the element is to be added does not
     * already exist in the JSON body it provides the possibility to either create
     * the path and add the element or do nothing and not add the element to the
     * JSON body
     * 
     */
    @JsonProperty("add-from-var-name")
    public void setAddFromVarName(AddFromVarName addFromVarName)
    {
        this.addFromVarName = addFromVarName;
    }

    public ActionModifyJsonBody withAddFromVarName(AddFromVarName addFromVarName)
    {
        this.addFromVarName = addFromVarName;
        return this;
    }

    /**
     * Replaces an element's old value with a new value specified in the value
     * attribute
     * 
     */
    @JsonProperty("replace-value")
    public ReplaceValue getReplaceValue()
    {
        return replaceValue;
    }

    /**
     * Replaces an element's old value with a new value specified in the value
     * attribute
     * 
     */
    @JsonProperty("replace-value")
    public void setReplaceValue(ReplaceValue replaceValue)
    {
        this.replaceValue = replaceValue;
    }

    public ActionModifyJsonBody withReplaceValue(ReplaceValue replaceValue)
    {
        this.replaceValue = replaceValue;
        return this;
    }

    /**
     * Replaces an element's old value with a new value that is stored on a variable
     * 
     */
    @JsonProperty("replace-from-var-name")
    public ReplaceFromVarName getReplaceFromVarName()
    {
        return replaceFromVarName;
    }

    /**
     * Replaces an element's old value with a new value that is stored on a variable
     * 
     */
    @JsonProperty("replace-from-var-name")
    public void setReplaceFromVarName(ReplaceFromVarName replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
    }

    public ActionModifyJsonBody withReplaceFromVarName(ReplaceFromVarName replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
        return this;
    }

    /**
     * Prepends a string to an element's old value
     * 
     */
    @JsonProperty("prepend-value")
    public PrependValue getPrependValue()
    {
        return prependValue;
    }

    /**
     * Prepends a string to an element's old value
     * 
     */
    @JsonProperty("prepend-value")
    public void setPrependValue(PrependValue prependValue)
    {
        this.prependValue = prependValue;
    }

    public ActionModifyJsonBody withPrependValue(PrependValue prependValue)
    {
        this.prependValue = prependValue;
        return this;
    }

    /**
     * Prepends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    public PrependFromVarName getPrependFromVarName()
    {
        return prependFromVarName;
    }

    /**
     * Prepends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("prepend-from-var-name")
    public void setPrependFromVarName(PrependFromVarName prependFromVarName)
    {
        this.prependFromVarName = prependFromVarName;
    }

    public ActionModifyJsonBody withPrependFromVarName(PrependFromVarName prependFromVarName)
    {
        this.prependFromVarName = prependFromVarName;
        return this;
    }

    /**
     * Appends a string to an element's old value
     * 
     */
    @JsonProperty("append-value")
    public AppendValue getAppendValue()
    {
        return appendValue;
    }

    /**
     * Appends a string to an element's old value
     * 
     */
    @JsonProperty("append-value")
    public void setAppendValue(AppendValue appendValue)
    {
        this.appendValue = appendValue;
    }

    public ActionModifyJsonBody withAppendValue(AppendValue appendValue)
    {
        this.appendValue = appendValue;
        return this;
    }

    /**
     * Appends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    public AppendFromVarName getAppendFromVarName()
    {
        return appendFromVarName;
    }

    /**
     * Appends a string stored in a variable to an element's old value
     * 
     */
    @JsonProperty("append-from-var-name")
    public void setAppendFromVarName(AppendFromVarName appendFromVarName)
    {
        this.appendFromVarName = appendFromVarName;
    }

    public ActionModifyJsonBody withAppendFromVarName(AppendFromVarName appendFromVarName)
    {
        this.appendFromVarName = appendFromVarName;
        return this;
    }

    /**
     * Searches for a string using the path specified by the json-pointer attribute.
     * If the string is found in the JSON body, it is being replaced with the string
     * specified using the configured options
     * 
     */
    @JsonProperty("search-replace-string")
    public SearchReplaceString getSearchReplaceString()
    {
        return searchReplaceString;
    }

    /**
     * Searches for a string using the path specified by the json-pointer attribute.
     * If the string is found in the JSON body, it is being replaced with the string
     * specified using the configured options
     * 
     */
    @JsonProperty("search-replace-string")
    public void setSearchReplaceString(SearchReplaceString searchReplaceString)
    {
        this.searchReplaceString = searchReplaceString;
    }

    public ActionModifyJsonBody withSearchReplaceString(SearchReplaceString searchReplaceString)
    {
        this.searchReplaceString = searchReplaceString;
        return this;
    }

    /**
     * Searches for a pattern using the path specified by the json-pointer
     * attribute. If the pattern matches in the JSON body, it is being replaced with
     * the matched text specified using the configured options
     * 
     */
    @JsonProperty("search-replace-regex")
    public SearchReplaceRegex getSearchReplaceRegex()
    {
        return searchReplaceRegex;
    }

    /**
     * Searches for a pattern using the path specified by the json-pointer
     * attribute. If the pattern matches in the JSON body, it is being replaced with
     * the matched text specified using the configured options
     * 
     */
    @JsonProperty("search-replace-regex")
    public void setSearchReplaceRegex(SearchReplaceRegex searchReplaceRegex)
    {
        this.searchReplaceRegex = searchReplaceRegex;
    }

    public ActionModifyJsonBody withSearchReplaceRegex(SearchReplaceRegex searchReplaceRegex)
    {
        this.searchReplaceRegex = searchReplaceRegex;
        return this;
    }

    /**
     * Removes the element, including its contents if any, that is specified by the
     * json-pointer attribute
     * 
     */
    @JsonProperty("remove")
    public Remove getRemove()
    {
        return remove;
    }

    /**
     * Removes the element, including its contents if any, that is specified by the
     * json-pointer attribute
     * 
     */
    @JsonProperty("remove")
    public void setRemove(Remove remove)
    {
        this.remove = remove;
    }

    public ActionModifyJsonBody withRemove(Remove remove)
    {
        this.remove = remove;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionModifyJsonBody.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jsonPointer");
        sb.append('=');
        sb.append(((this.jsonPointer == null) ? "<null>" : this.jsonPointer));
        sb.append(',');
        sb.append("addValue");
        sb.append('=');
        sb.append(((this.addValue == null) ? "<null>" : this.addValue));
        sb.append(',');
        sb.append("addFromVarName");
        sb.append('=');
        sb.append(((this.addFromVarName == null) ? "<null>" : this.addFromVarName));
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
        sb.append("searchReplaceString");
        sb.append('=');
        sb.append(((this.searchReplaceString == null) ? "<null>" : this.searchReplaceString));
        sb.append(',');
        sb.append("searchReplaceRegex");
        sb.append('=');
        sb.append(((this.searchReplaceRegex == null) ? "<null>" : this.searchReplaceRegex));
        sb.append(',');
        sb.append("remove");
        sb.append('=');
        sb.append(((this.remove == null) ? "<null>" : this.remove));
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
        result = ((result * 31) + ((this.jsonPointer == null) ? 0 : this.jsonPointer.hashCode()));
        result = ((result * 31) + ((this.addFromVarName == null) ? 0 : this.addFromVarName.hashCode()));
        result = ((result * 31) + ((this.replaceValue == null) ? 0 : this.replaceValue.hashCode()));
        result = ((result * 31) + ((this.searchReplaceRegex == null) ? 0 : this.searchReplaceRegex.hashCode()));
        result = ((result * 31) + ((this.remove == null) ? 0 : this.remove.hashCode()));
        result = ((result * 31) + ((this.replaceFromVarName == null) ? 0 : this.replaceFromVarName.hashCode()));
        result = ((result * 31) + ((this.prependValue == null) ? 0 : this.prependValue.hashCode()));
        result = ((result * 31) + ((this.searchReplaceString == null) ? 0 : this.searchReplaceString.hashCode()));
        result = ((result * 31) + ((this.appendValue == null) ? 0 : this.appendValue.hashCode()));
        result = ((result * 31) + ((this.prependFromVarName == null) ? 0 : this.prependFromVarName.hashCode()));
        result = ((result * 31) + ((this.addValue == null) ? 0 : this.addValue.hashCode()));
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
        if ((other instanceof ActionModifyJsonBody) == false)
        {
            return false;
        }
        ActionModifyJsonBody rhs = ((ActionModifyJsonBody) other);
        return (((((((((((((this.jsonPointer == rhs.jsonPointer) || ((this.jsonPointer != null) && this.jsonPointer.equals(rhs.jsonPointer)))
                          && ((this.addFromVarName == rhs.addFromVarName) || ((this.addFromVarName != null) && this.addFromVarName.equals(rhs.addFromVarName))))
                         && ((this.replaceValue == rhs.replaceValue) || ((this.replaceValue != null) && this.replaceValue.equals(rhs.replaceValue))))
                        && ((this.searchReplaceRegex == rhs.searchReplaceRegex)
                            || ((this.searchReplaceRegex != null) && this.searchReplaceRegex.equals(rhs.searchReplaceRegex))))
                       && ((this.remove == rhs.remove) || ((this.remove != null) && this.remove.equals(rhs.remove))))
                      && ((this.replaceFromVarName == rhs.replaceFromVarName)
                          || ((this.replaceFromVarName != null) && this.replaceFromVarName.equals(rhs.replaceFromVarName))))
                     && ((this.prependValue == rhs.prependValue) || ((this.prependValue != null) && this.prependValue.equals(rhs.prependValue))))
                    && ((this.searchReplaceString == rhs.searchReplaceString)
                        || ((this.searchReplaceString != null) && this.searchReplaceString.equals(rhs.searchReplaceString))))
                   && ((this.appendValue == rhs.appendValue) || ((this.appendValue != null) && this.appendValue.equals(rhs.appendValue))))
                  && ((this.prependFromVarName == rhs.prependFromVarName)
                      || ((this.prependFromVarName != null) && this.prependFromVarName.equals(rhs.prependFromVarName))))
                 && ((this.addValue == rhs.addValue) || ((this.addValue != null) && this.addValue.equals(rhs.addValue))))
                && ((this.appendFromVarName == rhs.appendFromVarName)
                    || ((this.appendFromVarName != null) && this.appendFromVarName.equals(rhs.appendFromVarName))));
    }

}
