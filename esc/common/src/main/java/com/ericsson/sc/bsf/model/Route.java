
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "lookup-profile-ref", "route-type" })
public class Route
{

    /**
     * The name of route (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of route")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Reference of a defined lookup profile. (Required)
     * 
     */
    @JsonProperty("lookup-profile-ref")
    @JsonPropertyDescription("Reference of a defined lookup profile.")
    private String lookupProfileRef;
    /**
     * The type of the route to configure. (Required)
     * 
     */
    @JsonProperty("route-type")
    @JsonPropertyDescription("The type of the route to configure.")
    private Route.RouteType routeType;

    /**
     * The name of route (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The name of route (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Route withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public Route withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Reference of a defined lookup profile. (Required)
     * 
     */
    @JsonProperty("lookup-profile-ref")
    public String getLookupProfileRef()
    {
        return lookupProfileRef;
    }

    /**
     * Reference of a defined lookup profile. (Required)
     * 
     */
    @JsonProperty("lookup-profile-ref")
    public void setLookupProfileRef(String lookupProfileRef)
    {
        this.lookupProfileRef = lookupProfileRef;
    }

    public Route withLookupProfileRef(String lookupProfileRef)
    {
        this.lookupProfileRef = lookupProfileRef;
        return this;
    }

    /**
     * The type of the route to configure. (Required)
     * 
     */
    @JsonProperty("route-type")
    public Route.RouteType getRouteType()
    {
        return routeType;
    }

    /**
     * The type of the route to configure. (Required)
     * 
     */
    @JsonProperty("route-type")
    public void setRouteType(Route.RouteType routeType)
    {
        this.routeType = routeType;
    }

    public Route withRouteType(Route.RouteType routeType)
    {
        this.routeType = routeType;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Route.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("lookupProfileRef");
        sb.append('=');
        sb.append(((this.lookupProfileRef == null) ? "<null>" : this.lookupProfileRef));
        sb.append(',');
        sb.append("routeType");
        sb.append('=');
        sb.append(((this.routeType == null) ? "<null>" : this.routeType));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.routeType == null) ? 0 : this.routeType.hashCode()));
        result = ((result * 31) + ((this.lookupProfileRef == null) ? 0 : this.lookupProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Route) == false)
        {
            return false;
        }
        Route rhs = ((Route) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.routeType == rhs.routeType) || ((this.routeType != null) && this.routeType.equals(rhs.routeType))))
                && ((this.lookupProfileRef == rhs.lookupProfileRef)
                    || ((this.lookupProfileRef != null) && this.lookupProfileRef.equals(rhs.lookupProfileRef))));
    }

    public enum RouteType
    {

        DEFAULT("default");

        private final String value;
        private final static Map<String, Route.RouteType> CONSTANTS = new HashMap<String, Route.RouteType>();

        static
        {
            for (Route.RouteType c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private RouteType(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Route.RouteType fromValue(String value)
        {
            Route.RouteType constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
