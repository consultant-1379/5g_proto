
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "enabled", "rank", "application", "local-endpoint", "user-label" })
public class LocalEndpointReference
{

    /**
     * Used to specify the key of the local-endpoint-reference instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the local-endpoint-reference instance.")
    private String id;
    /**
     * Used to enable or disable the use of the referred transport local endpoint
     * for the related AAA Service (that is, it disables the related
     * local-endpoint-reference instance). true: The use of the referred transport
     * local endpoint is allowed. false: The use of the referred transport local
     * endpoint is disallowed. Disabling a local-endpoint-reference instance will
     * result in dropping all the peer connections established through the referred
     * local-endpoint instance between parent AAA Service and related Diameter
     * Peers. When disabled, the following alarm is raised: ADP Diameter, Managed
     * Object Disabled Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to referred endpoint are dropped if value
     * set to false.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable the use of the referred transport local endpoint for the related AAA Service (that is, it disables the related local-endpoint-reference instance). true: The use of the referred transport local endpoint is allowed. false: The use of the referred transport local endpoint is disallowed. Disabling a local-endpoint-reference instance will result in dropping all the peer connections established through the referred local-endpoint instance between parent AAA Service and related Diameter Peers. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: All established diameter peer connections linked to referred endpoint are dropped if value set to false.")
    private Boolean enabled = true;
    /**
     * Used to express precedence for referred transport local endpoint selection
     * during diameter egress request message routing. The rank value is considered
     * during diameter egress request message routing when references towards
     * multiple transport local endpoints are defined for a AAA Service (that is,
     * multiple local-endpoint-reference are defined for same AAA Service) and the
     * referred transport local endpoints are pointing to either: static peers with
     * no connection restriction: Diameter Peer Nodes represented by dedicated
     * static-peer in the diameter MIM with connection amount restriction towards
     * same peer disabled (see also restrict-connections of static-peer) dynamic
     * peers with or without connection restriction: Diameter Peer Nodes allowed to
     * connect to own Diameter Node as result of matching conditions expressed
     * through a related dynamic-peer-acceptor with or without restriction on
     * connection amount (see also restrict-connections of dynamic-peer-acceptor).
     * The lowest the value provided the highest the priority assigned to the
     * referred endpoint in related AAA Service configuration context. That is, the
     * highest priority is assigned to a referred transport local endpoint by
     * setting the rank value to 0. Multiple local-endpoint-reference instances of a
     * AAA Service presenting same rank value and pointing to transport local
     * endpoints falling into one of the conditions above defined will have equal
     * priority during routing evaluation. That is, in such conditions the Diameter
     * Service will perform
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] hashing based
     * message load balancing (load sharing) between the relevant transport
     * connections. If the
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] is not
     * present in the related diameter request message connections are selected in
     * random order. Update Apply: Immediate. Update Effect: Depending on configured
     * value a link fail-over might be triggered.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("Used to express precedence for referred transport local endpoint selection during diameter egress request message routing. The rank value is considered during diameter egress request message routing when references towards multiple transport local endpoints are defined for a AAA Service (that is, multiple local-endpoint-reference are defined for same AAA Service) and the referred transport local endpoints are pointing to either: static peers with no connection restriction: Diameter Peer Nodes represented by dedicated static-peer in the diameter MIM with connection amount restriction towards same peer disabled (see also restrict-connections of static-peer) dynamic peers with or without connection restriction: Diameter Peer Nodes allowed to connect to own Diameter Node as result of matching conditions expressed through a related dynamic-peer-acceptor with or without restriction on connection amount (see also restrict-connections of dynamic-peer-acceptor). The lowest the value provided the highest the priority assigned to the referred endpoint in related AAA Service configuration context. That is, the highest priority is assigned to a referred transport local endpoint by setting the rank value to 0. Multiple local-endpoint-reference instances of a AAA Service presenting same rank value and pointing to transport local endpoints falling into one of the conditions above defined will have equal priority during routing evaluation. That is, in such conditions the Diameter Service will perform https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] hashing based message load balancing (load sharing) between the relevant transport connections. If the https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] is not present in the related diameter request message connections are selected in random order. Update Apply: Immediate. Update Effect: Depending on configured value a link fail-over might be triggered.")
    private Long rank;
    /**
     * Used to restrict the use of referred transport local endpoint to a limited
     * set of Diameter Applications implemented by the AAA Service. That is, the
     * referred Diameter Applications must be a full-set or a sub-set of the
     * Diameter Applications implemented by the parent AAA Service. The values of
     * this attribute shall refer to those applications that are allowed to use the
     * referred transport local endpoint. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections linked to related AAA Service are
     * dropped and reestablished with updated Diameter Application information.
     * 
     */
    @JsonProperty("application")
    @JsonPropertyDescription("Used to restrict the use of referred transport local endpoint to a limited set of Diameter Applications implemented by the AAA Service. That is, the referred Diameter Applications must be a full-set or a sub-set of the Diameter Applications implemented by the parent AAA Service. The values of this attribute shall refer to those applications that are allowed to use the referred transport local endpoint. Update Apply: Immediate. Update Effect: All established diameter peer connections linked to related AAA Service are dropped and reestablished with updated Diameter Application information.")
    private List<String> application = new ArrayList<String>();
    /**
     * Used to specify the reference towards the transport local endpoint the AAA
     * Service should use. That is, the relevant local-endpoint MO. (Required)
     * 
     */
    @JsonProperty("local-endpoint")
    @JsonPropertyDescription("Used to specify the reference towards the transport local endpoint the AAA Service should use. That is, the relevant local-endpoint MO.")
    private String localEndpoint;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the local-endpoint-reference instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the local-endpoint-reference instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public LocalEndpointReference withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to enable or disable the use of the referred transport local endpoint
     * for the related AAA Service (that is, it disables the related
     * local-endpoint-reference instance). true: The use of the referred transport
     * local endpoint is allowed. false: The use of the referred transport local
     * endpoint is disallowed. Disabling a local-endpoint-reference instance will
     * result in dropping all the peer connections established through the referred
     * local-endpoint instance between parent AAA Service and related Diameter
     * Peers. When disabled, the following alarm is raised: ADP Diameter, Managed
     * Object Disabled Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to referred endpoint are dropped if value
     * set to false.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable the use of the referred transport local endpoint
     * for the related AAA Service (that is, it disables the related
     * local-endpoint-reference instance). true: The use of the referred transport
     * local endpoint is allowed. false: The use of the referred transport local
     * endpoint is disallowed. Disabling a local-endpoint-reference instance will
     * result in dropping all the peer connections established through the referred
     * local-endpoint instance between parent AAA Service and related Diameter
     * Peers. When disabled, the following alarm is raised: ADP Diameter, Managed
     * Object Disabled Update Apply: Immediate. Update Effect: All established
     * diameter peer connections linked to referred endpoint are dropped if value
     * set to false.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public LocalEndpointReference withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Used to express precedence for referred transport local endpoint selection
     * during diameter egress request message routing. The rank value is considered
     * during diameter egress request message routing when references towards
     * multiple transport local endpoints are defined for a AAA Service (that is,
     * multiple local-endpoint-reference are defined for same AAA Service) and the
     * referred transport local endpoints are pointing to either: static peers with
     * no connection restriction: Diameter Peer Nodes represented by dedicated
     * static-peer in the diameter MIM with connection amount restriction towards
     * same peer disabled (see also restrict-connections of static-peer) dynamic
     * peers with or without connection restriction: Diameter Peer Nodes allowed to
     * connect to own Diameter Node as result of matching conditions expressed
     * through a related dynamic-peer-acceptor with or without restriction on
     * connection amount (see also restrict-connections of dynamic-peer-acceptor).
     * The lowest the value provided the highest the priority assigned to the
     * referred endpoint in related AAA Service configuration context. That is, the
     * highest priority is assigned to a referred transport local endpoint by
     * setting the rank value to 0. Multiple local-endpoint-reference instances of a
     * AAA Service presenting same rank value and pointing to transport local
     * endpoints falling into one of the conditions above defined will have equal
     * priority during routing evaluation. That is, in such conditions the Diameter
     * Service will perform
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] hashing based
     * message load balancing (load sharing) between the relevant transport
     * connections. If the
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] is not
     * present in the related diameter request message connections are selected in
     * random order. Update Apply: Immediate. Update Effect: Depending on configured
     * value a link fail-over might be triggered.
     * 
     */
    @JsonProperty("rank")
    public Long getRank()
    {
        return rank;
    }

    /**
     * Used to express precedence for referred transport local endpoint selection
     * during diameter egress request message routing. The rank value is considered
     * during diameter egress request message routing when references towards
     * multiple transport local endpoints are defined for a AAA Service (that is,
     * multiple local-endpoint-reference are defined for same AAA Service) and the
     * referred transport local endpoints are pointing to either: static peers with
     * no connection restriction: Diameter Peer Nodes represented by dedicated
     * static-peer in the diameter MIM with connection amount restriction towards
     * same peer disabled (see also restrict-connections of static-peer) dynamic
     * peers with or without connection restriction: Diameter Peer Nodes allowed to
     * connect to own Diameter Node as result of matching conditions expressed
     * through a related dynamic-peer-acceptor with or without restriction on
     * connection amount (see also restrict-connections of dynamic-peer-acceptor).
     * The lowest the value provided the highest the priority assigned to the
     * referred endpoint in related AAA Service configuration context. That is, the
     * highest priority is assigned to a referred transport local endpoint by
     * setting the rank value to 0. Multiple local-endpoint-reference instances of a
     * AAA Service presenting same rank value and pointing to transport local
     * endpoints falling into one of the conditions above defined will have equal
     * priority during routing evaluation. That is, in such conditions the Diameter
     * Service will perform
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] hashing based
     * message load balancing (load sharing) between the relevant transport
     * connections. If the
     * https://tools.ietf.org/html/rfc6733#section-8.8[Session-Id AVP] is not
     * present in the related diameter request message connections are selected in
     * random order. Update Apply: Immediate. Update Effect: Depending on configured
     * value a link fail-over might be triggered.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Long rank)
    {
        this.rank = rank;
    }

    public LocalEndpointReference withRank(Long rank)
    {
        this.rank = rank;
        return this;
    }

    /**
     * Used to restrict the use of referred transport local endpoint to a limited
     * set of Diameter Applications implemented by the AAA Service. That is, the
     * referred Diameter Applications must be a full-set or a sub-set of the
     * Diameter Applications implemented by the parent AAA Service. The values of
     * this attribute shall refer to those applications that are allowed to use the
     * referred transport local endpoint. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections linked to related AAA Service are
     * dropped and reestablished with updated Diameter Application information.
     * 
     */
    @JsonProperty("application")
    public List<String> getApplication()
    {
        return application;
    }

    /**
     * Used to restrict the use of referred transport local endpoint to a limited
     * set of Diameter Applications implemented by the AAA Service. That is, the
     * referred Diameter Applications must be a full-set or a sub-set of the
     * Diameter Applications implemented by the parent AAA Service. The values of
     * this attribute shall refer to those applications that are allowed to use the
     * referred transport local endpoint. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections linked to related AAA Service are
     * dropped and reestablished with updated Diameter Application information.
     * 
     */
    @JsonProperty("application")
    public void setApplication(List<String> application)
    {
        this.application = application;
    }

    public LocalEndpointReference withApplication(List<String> application)
    {
        this.application = application;
        return this;
    }

    /**
     * Used to specify the reference towards the transport local endpoint the AAA
     * Service should use. That is, the relevant local-endpoint MO. (Required)
     * 
     */
    @JsonProperty("local-endpoint")
    public String getLocalEndpoint()
    {
        return localEndpoint;
    }

    /**
     * Used to specify the reference towards the transport local endpoint the AAA
     * Service should use. That is, the relevant local-endpoint MO. (Required)
     * 
     */
    @JsonProperty("local-endpoint")
    public void setLocalEndpoint(String localEndpoint)
    {
        this.localEndpoint = localEndpoint;
    }

    public LocalEndpointReference withLocalEndpoint(String localEndpoint)
    {
        this.localEndpoint = localEndpoint;
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

    public LocalEndpointReference withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalEndpointReference.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null) ? "<null>" : this.rank));
        sb.append(',');
        sb.append("application");
        sb.append('=');
        sb.append(((this.application == null) ? "<null>" : this.application));
        sb.append(',');
        sb.append("localEndpoint");
        sb.append('=');
        sb.append(((this.localEndpoint == null) ? "<null>" : this.localEndpoint));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.localEndpoint == null) ? 0 : this.localEndpoint.hashCode()));
        result = ((result * 31) + ((this.application == null) ? 0 : this.application.hashCode()));
        result = ((result * 31) + ((this.rank == null) ? 0 : this.rank.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof LocalEndpointReference) == false)
        {
            return false;
        }
        LocalEndpointReference rhs = ((LocalEndpointReference) other);
        return (((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                    && ((this.localEndpoint == rhs.localEndpoint) || ((this.localEndpoint != null) && this.localEndpoint.equals(rhs.localEndpoint))))
                   && ((this.application == rhs.application) || ((this.application != null) && this.application.equals(rhs.application))))
                  && ((this.rank == rhs.rank) || ((this.rank != null) && this.rank.equals(rhs.rank))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
