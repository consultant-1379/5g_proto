
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "address", "port", "transport-protocol", "enabled", "user-label" })
public class RemoteEndpoint
{

    /**
     * Used to specify the key of the remote-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the remote-endpoint instance.")
    private String id;
    /**
     * Used to specify the list of IP addresses (IPv4 or IPv6 addresses) of the
     * remote endpoint that can be used by the Own Diameter Node to connect to a
     * Peer Diameter Node (represented by parent static-peer instance). The address
     * should take as value a single IP addresses if the transport protocol to be
     * used is configured to TCP (see also the transport-protocol below). The
     * address can take as value multiple IP addresses if the transport protocol to
     * be used is configured to SCTP. In such a case the use of combined IPv4 and
     * IPv6 addresses is allowed as well. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are dropped and
     * reestablished with updated information. (Required)
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Used to specify the list of IP addresses (IPv4 or IPv6 addresses) of the remote endpoint that can be used by the Own Diameter Node to connect to a Peer Diameter Node (represented by parent static-peer instance). The address should take as value a single IP addresses if the transport protocol to be used is configured to TCP (see also the transport-protocol below). The address can take as value multiple IP addresses if the transport protocol to be used is configured to SCTP. In such a case the use of combined IPv4 and IPv6 addresses is allowed as well. Update Apply: Immediate. Update Effect: All established diameter peer connections towards the remote endpoint represented by the related remote-endpoint instance are dropped and reestablished with updated information.")
    private List<String> address = new ArrayList<String>();
    /**
     * Used to specify the port integer of the remote endpoint that can be used by
     * the Own Diameter Node to connect to a Peer Diameter Node (represented by
     * parent static-peer instance). The port integer specified is valid for all the
     * addresses specified for the related local-endpoint instance. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections towards
     * the remote endpoint represented by the remote-endpoint instance are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("Used to specify the port integer of the remote endpoint that can be used by the Own Diameter Node to connect to a Peer Diameter Node (represented by parent static-peer instance). The port integer specified is valid for all the addresses specified for the related local-endpoint instance. Update Apply: Immediate. Update Effect: All established diameter peer connections towards the remote endpoint represented by the remote-endpoint instance are dropped and reestablished with updated information.")
    private Long port = 3868L;
    /**
     * Used to specify the transport protocol of the remote endpoint that can be
     * used by the Own Diameter Node when initiating diameter peer connections.
     * 
     */
    @JsonProperty("transport-protocol")
    @JsonPropertyDescription("Used to specify the transport protocol of the remote endpoint that can be used by the Own Diameter Node when initiating diameter peer connections.")
    private RemoteEndpoint.TransportProtocol transportProtocol = RemoteEndpoint.TransportProtocol.fromValue("tcp");
    /**
     * Used to enable or disable the remote endpint of the Peer Diameter Node
     * represented by the parent static-peer. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are disconnected upon
     * setting to value false.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable the remote endpint of the Peer Diameter Node represented by the parent static-peer. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: All established diameter peer connections towards the remote endpoint represented by the related remote-endpoint instance are disconnected upon setting to value false.")
    private Boolean enabled = true;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the remote-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the remote-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public RemoteEndpoint withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to specify the list of IP addresses (IPv4 or IPv6 addresses) of the
     * remote endpoint that can be used by the Own Diameter Node to connect to a
     * Peer Diameter Node (represented by parent static-peer instance). The address
     * should take as value a single IP addresses if the transport protocol to be
     * used is configured to TCP (see also the transport-protocol below). The
     * address can take as value multiple IP addresses if the transport protocol to
     * be used is configured to SCTP. In such a case the use of combined IPv4 and
     * IPv6 addresses is allowed as well. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are dropped and
     * reestablished with updated information. (Required)
     * 
     */
    @JsonProperty("address")
    public List<String> getAddress()
    {
        return address;
    }

    /**
     * Used to specify the list of IP addresses (IPv4 or IPv6 addresses) of the
     * remote endpoint that can be used by the Own Diameter Node to connect to a
     * Peer Diameter Node (represented by parent static-peer instance). The address
     * should take as value a single IP addresses if the transport protocol to be
     * used is configured to TCP (see also the transport-protocol below). The
     * address can take as value multiple IP addresses if the transport protocol to
     * be used is configured to SCTP. In such a case the use of combined IPv4 and
     * IPv6 addresses is allowed as well. Update Apply: Immediate. Update Effect:
     * All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are dropped and
     * reestablished with updated information. (Required)
     * 
     */
    @JsonProperty("address")
    public void setAddress(List<String> address)
    {
        this.address = address;
    }

    public RemoteEndpoint withAddress(List<String> address)
    {
        this.address = address;
        return this;
    }

    /**
     * Used to specify the port integer of the remote endpoint that can be used by
     * the Own Diameter Node to connect to a Peer Diameter Node (represented by
     * parent static-peer instance). The port integer specified is valid for all the
     * addresses specified for the related local-endpoint instance. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections towards
     * the remote endpoint represented by the remote-endpoint instance are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("port")
    public Long getPort()
    {
        return port;
    }

    /**
     * Used to specify the port integer of the remote endpoint that can be used by
     * the Own Diameter Node to connect to a Peer Diameter Node (represented by
     * parent static-peer instance). The port integer specified is valid for all the
     * addresses specified for the related local-endpoint instance. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections towards
     * the remote endpoint represented by the remote-endpoint instance are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("port")
    public void setPort(Long port)
    {
        this.port = port;
    }

    public RemoteEndpoint withPort(Long port)
    {
        this.port = port;
        return this;
    }

    /**
     * Used to specify the transport protocol of the remote endpoint that can be
     * used by the Own Diameter Node when initiating diameter peer connections.
     * 
     */
    @JsonProperty("transport-protocol")
    public RemoteEndpoint.TransportProtocol getTransportProtocol()
    {
        return transportProtocol;
    }

    /**
     * Used to specify the transport protocol of the remote endpoint that can be
     * used by the Own Diameter Node when initiating diameter peer connections.
     * 
     */
    @JsonProperty("transport-protocol")
    public void setTransportProtocol(RemoteEndpoint.TransportProtocol transportProtocol)
    {
        this.transportProtocol = transportProtocol;
    }

    public RemoteEndpoint withTransportProtocol(RemoteEndpoint.TransportProtocol transportProtocol)
    {
        this.transportProtocol = transportProtocol;
        return this;
    }

    /**
     * Used to enable or disable the remote endpint of the Peer Diameter Node
     * represented by the parent static-peer. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are disconnected upon
     * setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable the remote endpint of the Peer Diameter Node
     * represented by the parent static-peer. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the remote endpoint
     * represented by the related remote-endpoint instance are disconnected upon
     * setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public RemoteEndpoint withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
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

    public RemoteEndpoint withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RemoteEndpoint.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("address");
        sb.append('=');
        sb.append(((this.address == null) ? "<null>" : this.address));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
        sb.append(',');
        sb.append("transportProtocol");
        sb.append('=');
        sb.append(((this.transportProtocol == null) ? "<null>" : this.transportProtocol));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
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
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
        result = ((result * 31) + ((this.transportProtocol == null) ? 0 : this.transportProtocol.hashCode()));
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
        if ((other instanceof RemoteEndpoint) == false)
        {
            return false;
        }
        RemoteEndpoint rhs = ((RemoteEndpoint) other);
        return (((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                    && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                   && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))))
                  && ((this.transportProtocol == rhs.transportProtocol)
                      || ((this.transportProtocol != null) && this.transportProtocol.equals(rhs.transportProtocol))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

    public enum TransportProtocol
    {

        TCP("tcp"),
        SCTP("sctp");

        private final String value;
        private final static Map<String, RemoteEndpoint.TransportProtocol> CONSTANTS = new HashMap<String, RemoteEndpoint.TransportProtocol>();

        static
        {
            for (RemoteEndpoint.TransportProtocol c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private TransportProtocol(String value)
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
        public static RemoteEndpoint.TransportProtocol fromValue(String value)
        {
            RemoteEndpoint.TransportProtocol constant = CONSTANTS.get(value);
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
