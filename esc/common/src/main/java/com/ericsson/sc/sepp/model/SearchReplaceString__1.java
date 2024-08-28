
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Searches for a string using the path specified by the json-pointer attribute.
 * If the string is found in the JSON body, it is being replaced with the string
 * specified using the configured options
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "case-sensitive",
                     "full-match",
                     "search-backwards",
                     "replace-all-occurrences",
                     "search-value",
                     "search-from-var-name",
                     "replace-value",
                     "replace-from-var-name" })
public class SearchReplaceString__1
{

    /**
     * If true, the search operation will be perfomed in case sensitive mode.
     * 
     */
    @JsonProperty("case-sensitive")
    @JsonPropertyDescription("If true, the search operation will be perfomed in case sensitive mode.")
    private Boolean caseSensitive = true;
    /**
     * If true, the search operation requires full match to proceed with the
     * replace.
     * 
     */
    @JsonProperty("full-match")
    @JsonPropertyDescription("If true, the search operation requires full match to proceed with the replace.")
    private Boolean fullMatch = false;
    /**
     * If true, the search operation will start from the end of the json body toward
     * the top.
     * 
     */
    @JsonProperty("search-backwards")
    @JsonPropertyDescription("If true, the search operation will start from the end of the json body toward the top.")
    private Boolean searchBackwards = false;
    /**
     * If true, the replace operation will be applied on all occurrences of the
     * search item. Otherwise, only the first occurrence will be replaced
     * 
     */
    @JsonProperty("replace-all-occurrences")
    @JsonPropertyDescription("If true, the replace operation will be applied on all occurrences of the search item. Otherwise, only the first occurrence will be replaced")
    private Boolean replaceAllOccurrences = false;
    /**
     * Specifies specific string as the search value
     * 
     */
    @JsonProperty("search-value")
    @JsonPropertyDescription("Specifies specific string as the search value")
    private String searchValue;
    /**
     * Specifies the variable name where the search string is stored.
     * 
     */
    @JsonProperty("search-from-var-name")
    @JsonPropertyDescription("Specifies the variable name where the search string is stored.")
    private String searchFromVarName;
    /**
     * Specifies specific string as the replace value
     * 
     */
    @JsonProperty("replace-value")
    @JsonPropertyDescription("Specifies specific string as the replace value")
    private String replaceValue;
    /**
     * Specifies the variable name where the replace string is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    @JsonPropertyDescription("Specifies the variable name where the replace string is stored.")
    private String replaceFromVarName;

    /**
     * If true, the search operation will be perfomed in case sensitive mode.
     * 
     */
    @JsonProperty("case-sensitive")
    public Boolean getCaseSensitive()
    {
        return caseSensitive;
    }

    /**
     * If true, the search operation will be perfomed in case sensitive mode.
     * 
     */
    @JsonProperty("case-sensitive")
    public void setCaseSensitive(Boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public SearchReplaceString__1 withCaseSensitive(Boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
        return this;
    }

    /**
     * If true, the search operation requires full match to proceed with the
     * replace.
     * 
     */
    @JsonProperty("full-match")
    public Boolean getFullMatch()
    {
        return fullMatch;
    }

    /**
     * If true, the search operation requires full match to proceed with the
     * replace.
     * 
     */
    @JsonProperty("full-match")
    public void setFullMatch(Boolean fullMatch)
    {
        this.fullMatch = fullMatch;
    }

    public SearchReplaceString__1 withFullMatch(Boolean fullMatch)
    {
        this.fullMatch = fullMatch;
        return this;
    }

    /**
     * If true, the search operation will start from the end of the json body toward
     * the top.
     * 
     */
    @JsonProperty("search-backwards")
    public Boolean getSearchBackwards()
    {
        return searchBackwards;
    }

    /**
     * If true, the search operation will start from the end of the json body toward
     * the top.
     * 
     */
    @JsonProperty("search-backwards")
    public void setSearchBackwards(Boolean searchBackwards)
    {
        this.searchBackwards = searchBackwards;
    }

    public SearchReplaceString__1 withSearchBackwards(Boolean searchBackwards)
    {
        this.searchBackwards = searchBackwards;
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

    public SearchReplaceString__1 withReplaceAllOccurrences(Boolean replaceAllOccurrences)
    {
        this.replaceAllOccurrences = replaceAllOccurrences;
        return this;
    }

    /**
     * Specifies specific string as the search value
     * 
     */
    @JsonProperty("search-value")
    public String getSearchValue()
    {
        return searchValue;
    }

    /**
     * Specifies specific string as the search value
     * 
     */
    @JsonProperty("search-value")
    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }

    public SearchReplaceString__1 withSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
        return this;
    }

    /**
     * Specifies the variable name where the search string is stored.
     * 
     */
    @JsonProperty("search-from-var-name")
    public String getSearchFromVarName()
    {
        return searchFromVarName;
    }

    /**
     * Specifies the variable name where the search string is stored.
     * 
     */
    @JsonProperty("search-from-var-name")
    public void setSearchFromVarName(String searchFromVarName)
    {
        this.searchFromVarName = searchFromVarName;
    }

    public SearchReplaceString__1 withSearchFromVarName(String searchFromVarName)
    {
        this.searchFromVarName = searchFromVarName;
        return this;
    }

    /**
     * Specifies specific string as the replace value
     * 
     */
    @JsonProperty("replace-value")
    public String getReplaceValue()
    {
        return replaceValue;
    }

    /**
     * Specifies specific string as the replace value
     * 
     */
    @JsonProperty("replace-value")
    public void setReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
    }

    public SearchReplaceString__1 withReplaceValue(String replaceValue)
    {
        this.replaceValue = replaceValue;
        return this;
    }

    /**
     * Specifies the variable name where the replace string is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    public String getReplaceFromVarName()
    {
        return replaceFromVarName;
    }

    /**
     * Specifies the variable name where the replace string is stored.
     * 
     */
    @JsonProperty("replace-from-var-name")
    public void setReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
    }

    public SearchReplaceString__1 withReplaceFromVarName(String replaceFromVarName)
    {
        this.replaceFromVarName = replaceFromVarName;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchReplaceString__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("caseSensitive");
        sb.append('=');
        sb.append(((this.caseSensitive == null) ? "<null>" : this.caseSensitive));
        sb.append(',');
        sb.append("fullMatch");
        sb.append('=');
        sb.append(((this.fullMatch == null) ? "<null>" : this.fullMatch));
        sb.append(',');
        sb.append("searchBackwards");
        sb.append('=');
        sb.append(((this.searchBackwards == null) ? "<null>" : this.searchBackwards));
        sb.append(',');
        sb.append("replaceAllOccurrences");
        sb.append('=');
        sb.append(((this.replaceAllOccurrences == null) ? "<null>" : this.replaceAllOccurrences));
        sb.append(',');
        sb.append("searchValue");
        sb.append('=');
        sb.append(((this.searchValue == null) ? "<null>" : this.searchValue));
        sb.append(',');
        sb.append("searchFromVarName");
        sb.append('=');
        sb.append(((this.searchFromVarName == null) ? "<null>" : this.searchFromVarName));
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
        result = ((result * 31) + ((this.searchBackwards == null) ? 0 : this.searchBackwards.hashCode()));
        result = ((result * 31) + ((this.fullMatch == null) ? 0 : this.fullMatch.hashCode()));
        result = ((result * 31) + ((this.caseSensitive == null) ? 0 : this.caseSensitive.hashCode()));
        result = ((result * 31) + ((this.replaceValue == null) ? 0 : this.replaceValue.hashCode()));
        result = ((result * 31) + ((this.searchFromVarName == null) ? 0 : this.searchFromVarName.hashCode()));
        result = ((result * 31) + ((this.replaceAllOccurrences == null) ? 0 : this.replaceAllOccurrences.hashCode()));
        result = ((result * 31) + ((this.searchValue == null) ? 0 : this.searchValue.hashCode()));
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
        if ((other instanceof SearchReplaceString__1) == false)
        {
            return false;
        }
        SearchReplaceString__1 rhs = ((SearchReplaceString__1) other);
        return (((((((((this.searchBackwards == rhs.searchBackwards) || ((this.searchBackwards != null) && this.searchBackwards.equals(rhs.searchBackwards)))
                      && ((this.fullMatch == rhs.fullMatch) || ((this.fullMatch != null) && this.fullMatch.equals(rhs.fullMatch))))
                     && ((this.caseSensitive == rhs.caseSensitive) || ((this.caseSensitive != null) && this.caseSensitive.equals(rhs.caseSensitive))))
                    && ((this.replaceValue == rhs.replaceValue) || ((this.replaceValue != null) && this.replaceValue.equals(rhs.replaceValue))))
                   && ((this.searchFromVarName == rhs.searchFromVarName)
                       || ((this.searchFromVarName != null) && this.searchFromVarName.equals(rhs.searchFromVarName))))
                  && ((this.replaceAllOccurrences == rhs.replaceAllOccurrences)
                      || ((this.replaceAllOccurrences != null) && this.replaceAllOccurrences.equals(rhs.replaceAllOccurrences))))
                 && ((this.searchValue == rhs.searchValue) || ((this.searchValue != null) && this.searchValue.equals(rhs.searchValue))))
                && ((this.replaceFromVarName == rhs.replaceFromVarName)
                    || ((this.replaceFromVarName != null) && this.replaceFromVarName.equals(rhs.replaceFromVarName))));
    }

}
