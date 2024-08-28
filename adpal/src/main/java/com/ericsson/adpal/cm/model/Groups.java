
package com.ericsson.adpal.cm.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * NETCONF Access Control Groups.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "group" })
public class Groups
{

    /**
     * One NACM Group Entry. This list will only contain configured entries, not any
     * entries learned from any transport protocols.
     * 
     */
    @JsonProperty("group")
    @JsonPropertyDescription("One NACM Group Entry. This list will only contain configured entries, not any entries learned from any transport protocols.")
    private List<Group> group = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Groups()
    {
    }

    /**
     * 
     * @param group
     */
    public Groups(List<Group> group)
    {
        super();
        this.group = group;
    }

    /**
     * One NACM Group Entry. This list will only contain configured entries, not any
     * entries learned from any transport protocols.
     * 
     */
    @JsonProperty("group")
    public Optional<List<Group>> getGroup()
    {
        return Optional.ofNullable(group);
    }

    /**
     * One NACM Group Entry. This list will only contain configured entries, not any
     * entries learned from any transport protocols.
     * 
     */
    @JsonProperty("group")
    public void setGroup(List<Group> group)
    {
        this.group = group;
    }

    public Groups withGroup(List<Group> group)
    {
        this.group = group;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Groups.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("group");
        sb.append('=');
        sb.append(((this.group == null) ? "<null>" : this.group));
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
        result = ((result * 31) + ((this.group == null) ? 0 : this.group.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Groups) == false)
        {
            return false;
        }
        Groups rhs = ((Groups) other);
        return ((this.group == rhs.group) || ((this.group != null) && this.group.equals(rhs.group)));
    }

}
