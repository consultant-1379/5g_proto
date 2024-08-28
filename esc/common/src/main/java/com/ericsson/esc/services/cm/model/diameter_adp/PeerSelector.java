
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "user-label", "static-peer-group", "dynamic-peer-group" })
public class PeerSelector
{

    /**
     * Used to specify the key of the peer-selector instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the peer-selector instance.")
    private String id;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * When set to true in linux service-execution-environment, Diameter routing
     * function executing route-to-peer-selection routing action checks if the
     * Destination-Host and Destination-Realm AVPs are included in the egress
     * request message. If they are included, Diameter ensures that they do match
     * the peer-origin-host of the selected static-peer and its realm, respectively.
     * This function is to be used by diameter user applications utilizing
     * peer-selector to implement fail-over between multiple directly connected Peer
     * Nodes (probably serving different realms) while Destination-Host and
     * Destination-Realm AVP pair is included into the request by the user
     * application. Message destination updates performed by Diameter routing
     * ensures that the request won't get rejected by the message destination
     * validation even at alternative Diameter Peer Nodes.
     * 
     */
    @JsonProperty("update-message-destination")
    @JsonPropertyDescription("When set to true in linux service-execution-environment,\n"
                             + "        Diameter routing function executing route-to-peer-selection routing\n"
                             + "        action checks if the Destination-Host and Destination-Realm AVPs are\n"
                             + "        included in the egress request message. If they are included, Diameter\n"
                             + "        ensures that they do match the peer-origin-host of the selected\n"
                             + "        static-peer and its realm, respectively.\n"
                             + "        This function is to be used by diameter user applications utilizing\n"
                             + "        peer-selector to implement fail-over between multiple directly\n"
                             + "        connected Peer Nodes (probably serving different realms) while\n"
                             + "        Destination-Host and Destination-Realm AVP pair is included into the\n"
                             + "        request by the user application. Message destination updates performed\n"
                             + "        by Diameter routing ensures that the request won't get rejected by the\n"
                             + "        message destination validation even at alternative Diameter Peer\n" + "        Nodes.")
    private Boolean updateMessageDestination;
    /**
     * A static-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their explicit representation expressed through related
     * static-peer instances. Any change in the integer of static peers assigned to
     * the static-peer-group instance will be applied immediately impacting in this
     * way the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("static-peer-group")
    @JsonPropertyDescription("A static-peer-group instance is used to specify a group of Diameter Peer Nodes by referring to their explicit representation expressed through related static-peer instances. Any change in the integer of static peers assigned to the static-peer-group instance will be applied immediately impacting in this way the related routing entries evaluated by the routing logic of diameter.")
    private List<StaticPeerGroup> staticPeerGroup = new ArrayList<StaticPeerGroup>();
    /**
     * A dynamic-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their pattern based representation expressed through
     * related dynamic-peer-acceptor instances. Any change in the integer of
     * diameter Peer Nodes assigned dynamically (pattern based) to the
     * dynamic-peer-group instance will be applied immediately impacting in this way
     * the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("dynamic-peer-group")
    @JsonPropertyDescription("A dynamic-peer-group instance is used to specify a group of Diameter Peer Nodes by referring to their pattern based representation expressed through related dynamic-peer-acceptor instances. Any change in the integer of diameter Peer Nodes assigned dynamically (pattern based) to the dynamic-peer-group instance will be applied immediately impacting in this way the related routing entries evaluated by the routing logic of diameter.")
    private List<DynamicPeerGroup> dynamicPeerGroup = new ArrayList<DynamicPeerGroup>();

    /**
     * Used to specify the key of the peer-selector instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the peer-selector instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public PeerSelector withId(String id)
    {
        this.id = id;
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

    public PeerSelector withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A static-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their explicit representation expressed through related
     * static-peer instances. Any change in the integer of static peers assigned to
     * the static-peer-group instance will be applied immediately impacting in this
     * way the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("static-peer-group")
    public List<StaticPeerGroup> getStaticPeerGroup()
    {
        return staticPeerGroup;
    }

    /**
     * A static-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their explicit representation expressed through related
     * static-peer instances. Any change in the integer of static peers assigned to
     * the static-peer-group instance will be applied immediately impacting in this
     * way the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("static-peer-group")
    public void setStaticPeerGroup(List<StaticPeerGroup> staticPeerGroup)
    {
        this.staticPeerGroup = staticPeerGroup;
    }

    public PeerSelector withStaticPeerGroup(List<StaticPeerGroup> staticPeerGroup)
    {
        this.staticPeerGroup = staticPeerGroup;
        return this;
    }

    /**
     * A dynamic-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their pattern based representation expressed through
     * related dynamic-peer-acceptor instances. Any change in the integer of
     * diameter Peer Nodes assigned dynamically (pattern based) to the
     * dynamic-peer-group instance will be applied immediately impacting in this way
     * the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("dynamic-peer-group")
    public List<DynamicPeerGroup> getDynamicPeerGroup()
    {
        return dynamicPeerGroup;
    }

    /**
     * A dynamic-peer-group instance is used to specify a group of Diameter Peer
     * Nodes by referring to their pattern based representation expressed through
     * related dynamic-peer-acceptor instances. Any change in the integer of
     * diameter Peer Nodes assigned dynamically (pattern based) to the
     * dynamic-peer-group instance will be applied immediately impacting in this way
     * the related routing entries evaluated by the routing logic of diameter.
     * 
     */
    @JsonProperty("dynamic-peer-group")
    public void setDynamicPeerGroup(List<DynamicPeerGroup> dynamicPeerGroup)
    {
        this.dynamicPeerGroup = dynamicPeerGroup;
    }

    public PeerSelector withDynamicPeerGroup(List<DynamicPeerGroup> dynamicPeerGroup)
    {
        this.dynamicPeerGroup = dynamicPeerGroup;
        return this;
    }

    public PeerSelector withUpdateMessageDestination(Boolean updateMessageDestination)
    {
        this.updateMessageDestination = updateMessageDestination;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PeerSelector.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("updateMessageDestination");
        sb.append('=');
        sb.append(((this.updateMessageDestination == null) ? "<null>" : this.updateMessageDestination.toString()));
        sb.append(',');
        sb.append("staticPeerGroup");
        sb.append('=');
        sb.append(((this.staticPeerGroup == null) ? "<null>" : this.staticPeerGroup));
        sb.append(',');
        sb.append("dynamicPeerGroup");
        sb.append('=');
        sb.append(((this.dynamicPeerGroup == null) ? "<null>" : this.dynamicPeerGroup));
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
        result = ((result * 31) + ((this.dynamicPeerGroup == null) ? 0 : this.dynamicPeerGroup.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.staticPeerGroup == null) ? 0 : this.staticPeerGroup.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.updateMessageDestination == null) ? 0 : this.updateMessageDestination.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PeerSelector) == false)
        {
            return false;
        }
        PeerSelector rhs = ((PeerSelector) other);
        return (((((this.dynamicPeerGroup == rhs.dynamicPeerGroup) || ((this.dynamicPeerGroup != null) && this.dynamicPeerGroup.equals(rhs.dynamicPeerGroup)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.staticPeerGroup == rhs.staticPeerGroup) || ((this.staticPeerGroup != null) && this.staticPeerGroup.equals(rhs.staticPeerGroup))))
                && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id)))
                && ((this.updateMessageDestination == rhs.updateMessageDestination)
                    || ((this.updateMessageDestination != null) && this.updateMessageDestination.equals(rhs.updateMessageDestination))));
    }

}
