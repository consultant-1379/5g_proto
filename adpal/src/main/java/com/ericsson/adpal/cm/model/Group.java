
package com.ericsson.adpal.cm.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-name" })
public class Group
{

    /**
     * Group name associated with this entry. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Group name associated with this entry.")
    private String name;
    /**
     * Each entry identifies the username of a member of the group associated with
     * this entry.
     * 
     */
    @JsonProperty("user-name")
    @JsonPropertyDescription("Each entry identifies the username of a member of the group associated with this entry.")
    private List<String> userName = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Group()
    {
    }

    /**
     * 
     * @param name
     * @param userName
     */
    public Group(String name,
                 List<String> userName)
    {
        super();
        this.name = name;
        this.userName = userName;
    }

    /**
     * Group name associated with this entry. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Group name associated with this entry. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Group withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Each entry identifies the username of a member of the group associated with
     * this entry.
     * 
     */
    @JsonProperty("user-name")
    public Optional<List<String>> getUserName()
    {
        return Optional.ofNullable(userName);
    }

    /**
     * Each entry identifies the username of a member of the group associated with
     * this entry.
     * 
     */
    @JsonProperty("user-name")
    public void setUserName(List<String> userName)
    {
        this.userName = userName;
    }

    public Group withUserName(List<String> userName)
    {
        this.userName = userName;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Group.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userName");
        sb.append('=');
        sb.append(((this.userName == null) ? "<null>" : this.userName));
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
        result = ((result * 31) + ((this.userName == null) ? 0 : this.userName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Group) == false)
        {
            return false;
        }
        Group rhs = ((Group) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.userName == rhs.userName) || ((this.userName != null) && this.userName.equals(rhs.userName))));
    }

}
