
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "service", "user-label", "routing-entry" })
public class RoutingTable
{

    /**
     * Used to specify the key of the routing-table instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the routing-table instance.")
    private String id;
    /**
     * Used to refer to a set of AAA Services, represented by related service
     * instances, the routing table should be associated with. A routing table can
     * be assigned to many AAA Services. However, a AAA Service must be assigned
     * with a single routing table only. Update Apply: Immediate. Update Effect: No
     * effect on already established peer connections but on routing evaluation.
     * Introduced change will be applied next time a routing entry is evaluated.
     * (Required)
     * 
     */
    @JsonProperty("service")
    @JsonPropertyDescription("Used to refer to a set of AAA Services, represented by related service instances, the routing table should be associated with. A routing table can be assigned to many AAA Services. However, a AAA Service must be assigned with a single routing table only. Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing evaluation. Introduced change will be applied next time a routing entry is evaluated.")
    private List<String> service = new ArrayList<String>();
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * A routing-entry instance is used to specify a routing rule/entry. Each
     * routing rule is constructed with the help of an expression and an action (see
     * also related expression and action attributes of the routing-entry instance).
     * A routing rule can be enabled (default) or disabled by need. Only the enabled
     * routing rules present in a routing table are considered during request
     * message routing evaluation (disabled ones are just simply skipped). Any
     * change in the content of a routing rule will be applied immediately impacting
     * in this way the next routing rules/entries evaluated by the message routing
     * mechanism of diameter. (Required)
     * 
     */
    @JsonProperty("routing-entry")
    @JsonPropertyDescription("A routing-entry instance is used to specify a routing rule/entry. Each routing rule is constructed with the help of an expression and an action (see also related expression and action attributes of the routing-entry instance). A routing rule can be enabled (default) or disabled by need. Only the enabled routing rules present in a routing table are considered during request message routing evaluation (disabled ones are just simply skipped). Any change in the content of a routing rule will be applied immediately impacting in this way the next routing rules/entries evaluated by the message routing mechanism of diameter.")
    private List<RoutingEntry> routingEntry = new ArrayList<RoutingEntry>();

    /**
     * Used to specify the key of the routing-table instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the routing-table instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public RoutingTable withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to refer to a set of AAA Services, represented by related service
     * instances, the routing table should be associated with. A routing table can
     * be assigned to many AAA Services. However, a AAA Service must be assigned
     * with a single routing table only. Update Apply: Immediate. Update Effect: No
     * effect on already established peer connections but on routing evaluation.
     * Introduced change will be applied next time a routing entry is evaluated.
     * (Required)
     * 
     */
    @JsonProperty("service")
    public List<String> getService()
    {
        return service;
    }

    /**
     * Used to refer to a set of AAA Services, represented by related service
     * instances, the routing table should be associated with. A routing table can
     * be assigned to many AAA Services. However, a AAA Service must be assigned
     * with a single routing table only. Update Apply: Immediate. Update Effect: No
     * effect on already established peer connections but on routing evaluation.
     * Introduced change will be applied next time a routing entry is evaluated.
     * (Required)
     * 
     */
    @JsonProperty("service")
    public void setService(List<String> service)
    {
        this.service = service;
    }

    public RoutingTable withService(List<String> service)
    {
        this.service = service;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public RoutingTable withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A routing-entry instance is used to specify a routing rule/entry. Each
     * routing rule is constructed with the help of an expression and an action (see
     * also related expression and action attributes of the routing-entry instance).
     * A routing rule can be enabled (default) or disabled by need. Only the enabled
     * routing rules present in a routing table are considered during request
     * message routing evaluation (disabled ones are just simply skipped). Any
     * change in the content of a routing rule will be applied immediately impacting
     * in this way the next routing rules/entries evaluated by the message routing
     * mechanism of diameter. (Required)
     * 
     */
    @JsonProperty("routing-entry")
    public List<RoutingEntry> getRoutingEntry()
    {
        return routingEntry;
    }

    /**
     * A routing-entry instance is used to specify a routing rule/entry. Each
     * routing rule is constructed with the help of an expression and an action (see
     * also related expression and action attributes of the routing-entry instance).
     * A routing rule can be enabled (default) or disabled by need. Only the enabled
     * routing rules present in a routing table are considered during request
     * message routing evaluation (disabled ones are just simply skipped). Any
     * change in the content of a routing rule will be applied immediately impacting
     * in this way the next routing rules/entries evaluated by the message routing
     * mechanism of diameter. (Required)
     * 
     */
    @JsonProperty("routing-entry")
    public void setRoutingEntry(List<RoutingEntry> routingEntry)
    {
        this.routingEntry = routingEntry;
    }

    public RoutingTable withRoutingEntry(List<RoutingEntry> routingEntry)
    {
        this.routingEntry = routingEntry;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoutingTable.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("service");
        sb.append('=');
        sb.append(((this.service == null) ? "<null>" : this.service));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("routingEntry");
        sb.append('=');
        sb.append(((this.routingEntry == null) ? "<null>" : this.routingEntry));
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
        result = ((result * 31) + ((this.routingEntry == null) ? 0 : this.routingEntry.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.service == null) ? 0 : this.service.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RoutingTable) == false)
        {
            return false;
        }
        RoutingTable rhs = ((RoutingTable) other);
        return (((((this.routingEntry == rhs.routingEntry) || ((this.routingEntry != null) && this.routingEntry.equals(rhs.routingEntry)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.service == rhs.service) || ((this.service != null) && this.service.equals(rhs.service))));
    }

}
