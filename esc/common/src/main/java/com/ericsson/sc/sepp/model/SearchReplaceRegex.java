
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Searches for a pattern using the path specified by the json-pointer
 * attribute. If the pattern matches in the JSON body, it is being replaced with
 * the matched text specified using the configured options
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "search-regex", "replace-all-occurrences", "replace-value", "replace-from-var-name" })
public class SearchReplaceRegex
{

    /**
     * Specifies the regular expression used to search for string patterns.
     * 
     */
    @JsonProperty("search-regex")
    @JsonPropertyDescription("Specifies the regular expression used to search for string patterns.")
    private String searchRegex;
    /**
     * If true, the replace operation will be applied on all occurrences of the
     * search item. Otherwise, only the first occurrence will be replaced
     * 
     */
    @JsonProperty("replace-all-occurrences")
    @JsonPropertyDescription("If true, the replace operation will be applied on all occurrences of the search item. Otherwise, only the first occurrence will be replaced")
    private Boolean replaceAllOccurrences = false;
    /**
     * Specifies how the replacement will be performed based on the matched text as
     * result of search-regex attribute. If the regular expression did not match, no
     * replacement is applied.
     * 
     */
    @JsonProperty("replace-value")
    @JsonPropertyDescription("Specifies how the replacement will be performed based on the matched text as result of search-regex attribute. If the regular expression did not match, no replacement is applied.")
    private String replaceValue;
    /**
     * Specifies the variable name where the replace value is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    @JsonPropertyDescription("Specifies the variable name where the replace value is stored.")
    private String replaceFromVarName;

    /**
     * Specifies the regular expression used to search for string patterns.
     * 
     */
    @JsonProperty("search-regex")
    public String getSearchRegex()
    {
        return searchRegex;
    }

    /**
     * Specifies the regular expression used to search for string patterns.
     * 
     */
    @JsonProperty("search-regex")
    public void setSearchRegex(String searchRegex)
    {
        this.searchRegex = searchRegex;
    }

    public SearchReplaceRegex withSearchRegex(String searchRegex)
    {
        this.searchRegex = searchRegex;
        return this;
    }

    /**
     * If true, the replace operation will be applied on all occurrences of the
     * search item. Otherwise, only the first occurrence will be replaced
     * 
     */
    @JsonProperty("replace-all-occurrences")
    public Boolean getReplaceAllOccurrences()
    {
        return replaceAllOccurrences;
    }

    /**
     * If true, the replace operation will be applied on all occurrences of the
     * search item. Otherwise, only the first occurrence will be replaced
     * 
     */
    @JsonProperty("replace-all-occurrences")
    public void setReplaceAllOccurrences(Boolean replaceAllOccurrences)
    {
        this.replaceAllOccurrences = replaceAllOccurrences;
    }

    public SearchReplaceRegex withReplaceAllOccurrences(Boolean replaceAllOccurrences)
    {
        this.replaceAllOccurrences = replaceAllOccurrences;
        return this;
    }

    /**
     * Specifies how the replacement will be performed based on the matched text as
     * result of search-regex attribute. If the regular expression did not match, no
     * replacement is applied.
     * 
     */
    @JsonProperty("replace-value")
    public String getReplaceValue()
    {
        return replaceValue;
    }

    /**
     * Specifies how the replacement will be performed based on the matched text as
     * result of search-regex attribute. If the regular expression did not match, no
     * replacement is applied.
     * 
     */
    @JsonProperty("replace-value")
    public void setReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
    }

    public SearchReplaceRegex withReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
        return this;
    }

    /**
     * Specifies the variable name where the replace value is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    public String getReplaceFromVarName()
    {
        return replaceFromVarName;
    }

    /**
     * Specifies the variable name where the replace value is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    public void setReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
    }

    public SearchReplaceRegex withReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchReplaceRegex.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("searchRegex");
        sb.append('=');
        sb.append(((this.searchRegex == null) ? "<null>" : this.searchRegex));
        sb.append(',');
        sb.append("replaceAllOccurrences");
        sb.append('=');
        sb.append(((this.replaceAllOccurrences == null) ? "<null>" : this.replaceAllOccurrences));
        sb.append(',');
        sb.append("replaceValue");
        sb.append('=');
        sb.append(((this.replaceValue == null) ? "<null>" : this.replaceValue));
        sb.append(',');
        sb.append("replaceFromVarName");
        sb.append('=');
        sb.append(((this.replaceFromVarName == null) ? "<null>" : this.replaceFromVarName));
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
        result = ((result * 31) + ((this.replaceValue == null) ? 0 : this.replaceValue.hashCode()));
        result = ((result * 31) + ((this.searchRegex == null) ? 0 : this.searchRegex.hashCode()));
        result = ((result * 31) + ((this.replaceAllOccurrences == null) ? 0 : this.replaceAllOccurrences.hashCode()));
        result = ((result * 31) + ((this.replaceFromVarName == null) ? 0 : this.replaceFromVarName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SearchReplaceRegex) == false)
        {
            return false;
        }
        SearchReplaceRegex rhs = ((SearchReplaceRegex) other);
        return (((((this.replaceValue == rhs.replaceValue) || ((this.replaceValue != null) && this.replaceValue.equals(rhs.replaceValue)))
                  && ((this.searchRegex == rhs.searchRegex) || ((this.searchRegex != null) && this.searchRegex.equals(rhs.searchRegex))))
                 && ((this.replaceAllOccurrences == rhs.replaceAllOccurrences)
                     || ((this.replaceAllOccurrences != null) && this.replaceAllOccurrences.equals(rhs.replaceAllOccurrences))))
                && ((this.replaceFromVarName == rhs.replaceFromVarName)
                    || ((this.replaceFromVarName != null) && this.replaceFromVarName.equals(rhs.replaceFromVarName))));
    }

}
