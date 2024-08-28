
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Used to assign SCTP transport capability for a Local Endpoint.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "port", "rank", "outbound-streams", "max-inbound-streams", "address" })
public class TransportSctp
{

    /**
     * Used to specify the port to be used by the E-SCTP transport capability of a
     * Local Endpoint. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections assigned to local endpoint are dropped and
     * reestablished with updated information. Depending on local endpoint role, it
     * defaults to: 0: if local endpoint is configured in connection initiation
     * (client) mode. 3868: if local endpoint is configured in connection
     * termination (server) mode.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("Used to specify the port to be used by the E-SCTP transport capability of a Local Endpoint. Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated information. Depending on local endpoint role, it defaults to: 0: if local endpoint is configured in connection initiation (client) mode. 3868: if local endpoint is configured in connection termination (server) mode.")
    private Long port = 0L;
    /**
     * Used to express precedence for transport capability selection for a Local
     * Endpoint. That transport capability will be selected for a Local Endpoint
     * that is valid to use (for instance, diameter can bind to specified address)
     * and contains the lowest rank. In case of multiple valid transport
     * capabilities assigned to a Local Endpoint with same rank one will be selected
     * by random. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections for related Local Endpoint. The new value is
     * considered for newly established peer connections.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("Used to express precedence for transport capability selection for a Local Endpoint. That transport capability will be selected for a Local Endpoint that is valid to use (for instance, diameter can bind to specified address) and contains the lowest rank. In case of multiple valid transport capabilities assigned to a Local Endpoint with same rank one will be selected by random. Update Apply: Immediate. Update Effect: No effect on already established peer connections for related Local Endpoint. The new value is considered for newly established peer connections.")
    private Long rank;
    /**
     * Used to configure the Number of Outbound Streams (OS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("outbound-streams")
    @JsonPropertyDescription("Used to configure the Number of Outbound Streams (OS) (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the associations created for the peer connections assigned with the local endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF: STANDARD). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated information. Unit: Count")
    private Long outboundStreams = 1L;
    /**
     * Used to configure the Number of Inbound Streams (MIS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("max-inbound-streams")
    @JsonPropertyDescription("Used to configure the Number of Inbound Streams (MIS) (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the associations created for the peer connections assigned with the local endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF: STANDARD). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated information. Unit: Count")
    private Long maxInboundStreams = 1L;
    /**
     * Gives the possibility to assign an IP address to an E-SCTP transport
     * capability. Update Apply: Immediate. Update Effect: All established diameter
     * peer connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Gives the possibility to assign an IP address to an E-SCTP transport capability. Update Apply: Immediate. Update Effect: All established diameter peer connections for the transport capability assigned to local endpoint are dropped and reestablished by need with updated information.")
    private List<String> address = new ArrayList<String>();

    /**
     * Used to specify the port to be used by the E-SCTP transport capability of a
     * Local Endpoint. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections assigned to local endpoint are dropped and
     * reestablished with updated information. Depending on local endpoint role, it
     * defaults to: 0: if local endpoint is configured in connection initiation
     * (client) mode. 3868: if local endpoint is configured in connection
     * termination (server) mode.
     * 
     */
    @JsonProperty("port")
    public Long getPort()
    {
        return port;
    }

    /**
     * Used to specify the port to be used by the E-SCTP transport capability of a
     * Local Endpoint. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections assigned to local endpoint are dropped and
     * reestablished with updated information. Depending on local endpoint role, it
     * defaults to: 0: if local endpoint is configured in connection initiation
     * (client) mode. 3868: if local endpoint is configured in connection
     * termination (server) mode.
     * 
     */
    @JsonProperty("port")
    public void setPort(Long port)
    {
        this.port = port;
    }

    public TransportSctp withPort(Long port)
    {
        this.port = port;
        return this;
    }

    /**
     * Used to express precedence for transport capability selection for a Local
     * Endpoint. That transport capability will be selected for a Local Endpoint
     * that is valid to use (for instance, diameter can bind to specified address)
     * and contains the lowest rank. In case of multiple valid transport
     * capabilities assigned to a Local Endpoint with same rank one will be selected
     * by random. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections for related Local Endpoint. The new value is
     * considered for newly established peer connections.
     * 
     */
    @JsonProperty("rank")
    public Long getRank()
    {
        return rank;
    }

    /**
     * Used to express precedence for transport capability selection for a Local
     * Endpoint. That transport capability will be selected for a Local Endpoint
     * that is valid to use (for instance, diameter can bind to specified address)
     * and contains the lowest rank. In case of multiple valid transport
     * capabilities assigned to a Local Endpoint with same rank one will be selected
     * by random. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections for related Local Endpoint. The new value is
     * considered for newly established peer connections.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Long rank)
    {
        this.rank = rank;
    }

    public TransportSctp withRank(Long rank)
    {
        this.rank = rank;
        return this;
    }

    /**
     * Used to configure the Number of Outbound Streams (OS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("outbound-streams")
    public Long getOutboundStreams()
    {
        return outboundStreams;
    }

    /**
     * Used to configure the Number of Outbound Streams (OS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("outbound-streams")
    public void setOutboundStreams(Long outboundStreams)
    {
        this.outboundStreams = outboundStreams;
    }

    public TransportSctp withOutboundStreams(Long outboundStreams)
    {
        this.outboundStreams = outboundStreams;
        return this;
    }

    /**
     * Used to configure the Number of Inbound Streams (MIS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("max-inbound-streams")
    public Long getMaxInboundStreams()
    {
        return maxInboundStreams;
    }

    /**
     * Used to configure the Number of Inbound Streams (MIS)
     * (https://tools.ietf.org/html/rfc4960#section-3.3.2) wished for the
     * associations created for the peer connections assigned with the local
     * endpoint (see also Stream Control Transmission Protocol (RFC 4960) IETF:
     * STANDARD). Update Apply: Immediate. Update Effect: All established diameter
     * peer connections assigned to local endpoint are dropped and reestablished
     * with updated information. Unit: Count
     * 
     */
    @JsonProperty("max-inbound-streams")
    public void setMaxInboundStreams(Long maxInboundStreams)
    {
        this.maxInboundStreams = maxInboundStreams;
    }

    public TransportSctp withMaxInboundStreams(Long maxInboundStreams)
    {
        this.maxInboundStreams = maxInboundStreams;
        return this;
    }

    /**
     * Gives the possibility to assign an IP address to an E-SCTP transport
     * capability. Update Apply: Immediate. Update Effect: All established diameter
     * peer connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    public List<String> getAddress()
    {
        return address;
    }

    /**
     * Gives the possibility to assign an IP address to an E-SCTP transport
     * capability. Update Apply: Immediate. Update Effect: All established diameter
     * peer connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    public void setAddress(List<String> address)
    {
        this.address = address;
    }

    public TransportSctp withAddress(List<String> address)
    {
        this.address = address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TransportSctp.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null) ? "<null>" : this.rank));
        sb.append(',');
        sb.append("outboundStreams");
        sb.append('=');
        sb.append(((this.outboundStreams == null) ? "<null>" : this.outboundStreams));
        sb.append(',');
        sb.append("maxInboundStreams");
        sb.append('=');
        sb.append(((this.maxInboundStreams == null) ? "<null>" : this.maxInboundStreams));
        sb.append(',');
        sb.append("address");
        sb.append('=');
        sb.append(((this.address == null) ? "<null>" : this.address));
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
        result = ((result * 31) + ((this.rank == null) ? 0 : this.rank.hashCode()));
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
        result = ((result * 31) + ((this.maxInboundStreams == null) ? 0 : this.maxInboundStreams.hashCode()));
        result = ((result * 31) + ((this.outboundStreams == null) ? 0 : this.outboundStreams.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TransportSctp) == false)
        {
            return false;
        }
        TransportSctp rhs = ((TransportSctp) other);
        return ((((((this.rank == rhs.rank) || ((this.rank != null) && this.rank.equals(rhs.rank)))
                   && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                  && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))))
                 && ((this.maxInboundStreams == rhs.maxInboundStreams)
                     || ((this.maxInboundStreams != null) && this.maxInboundStreams.equals(rhs.maxInboundStreams))))
                && ((this.outboundStreams == rhs.outboundStreams) || ((this.outboundStreams != null) && this.outboundStreams.equals(rhs.outboundStreams))));
    }

}
