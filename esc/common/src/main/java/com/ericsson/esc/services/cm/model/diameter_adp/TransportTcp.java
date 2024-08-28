
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Used to assign TCP transport capability for a Local Endpoint.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "port", "rank", "backlog", "no-delay", "address", "tls-profile", "tls-host-name-validation" })
public class TransportTcp
{

    /**
     * Used to specify the port to be used by the TCP transport capability of a
     * Local Endpoint. Update Apply: Immediate. Update Effect: All established
     * diameter peer connections assigned to local endpoint are dropped and
     * reestablished with updated information. Depending on local endpoint role, it
     * defaults to: 0: if local endpoint is configured in connection initiation
     * (client) mode. 3868: if local endpoint is configured in connection
     * termination (server) mode.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("Used to specify the port to be used by the TCP transport capability of a Local Endpoint. Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated information. Depending on local endpoint role, it defaults to: 0: if local endpoint is configured in connection initiation (client) mode. 3868: if local endpoint is configured in connection termination (server) mode.")
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
     * Used to specify the backlog size of incoming connections handled by a Local
     * Endpoint (the queue size of sockets in LISTEN state, see
     * http://man7.org/linux/man-pages/man2/listen.2.html). This is only valid for
     * Local Endpoints configured in connection termination (server) role. The value
     * applied also depends on underlying OS settings (might be constrained by OS to
     * less than configured). Update Apply: Immediate. Update Effect: All
     * established diameter peer connections assigned to local endpoint are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("backlog")
    @JsonPropertyDescription("Used to specify the backlog size of incoming connections handled by a Local Endpoint (the queue size of sockets in LISTEN state, see http://man7.org/linux/man-pages/man2/listen.2.html). This is only valid for Local Endpoints configured in connection termination (server) role. The value applied also depends on underlying OS settings (might be constrained by OS to less than configured). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated information.")
    private Long backlog = 1024L;
    /**
     * Used to enable or disable the use of the Nagle algorithm for the TCP
     * transport capability of a Local Endpoint. By default, TCP_NODELAY socket
     * option is applied, the use of Nagle algorithm (see
     * https://tools.ietf.org/html/rfc3539#section-3.2) is disabled. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are preserved and updated with new configuration.
     * 
     */
    @JsonProperty("no-delay")
    @JsonPropertyDescription("Used to enable or disable the use of the Nagle algorithm for the TCP transport capability of a Local Endpoint. By default, TCP_NODELAY socket option is applied, the use of Nagle algorithm (see https://tools.ietf.org/html/rfc3539#section-3.2) is disabled. Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are preserved and updated with new configuration.")
    private Boolean noDelay = true;
    /**
     * Offers the possibility to assign an IP address to a transport capability.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Offers the possibility to assign an IP address to a transport capability. Update Apply: Immediate. Update Effect: All established diameter peer connections for the transport capability assigned to local endpoint are dropped and reestablished by need with updated information.")
    private List<String> address = new ArrayList<String>();
    /**
     * If defined, endpoint uses TLS to secure the communication with properties of
     * the referred tls-profile. If not defined, alarm DIA Diameter Transport
     * Vulnerability is raised for the related endpoint. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-profile")
    @JsonPropertyDescription("If defined, endpoint uses TLS to secure the communication with properties of the referred tls-profile. If not defined, alarm DIA Diameter Transport Vulnerability is raised for the related endpoint. Update Effect: All established Diameter Peer connections assigned to local tcp endpoint are dropped and reestablished with updated information.")
    private String tlsProfile;
    /**
     * Can be used to turn off hostname validation on TLS secured static
     * connections. By default, peer certificate received during TLS handshake must
     * contain the id defined by peer-origin-host of referred static-peer instance.
     * If turned off, alarm DIA Diameter Transport Vulnerability is raised for the
     * related endpoint. Note, there is no hostname validation for dynamic Peers
     * allowed by filters in dynamic-peer-acceptor instances. Has no effect on
     * connections without TLS, that is if tls-profile is empty. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-host-name-validation")
    @JsonPropertyDescription("Can be used to turn off hostname validation on TLS secured static connections. By default, peer certificate received during TLS handshake must contain the id defined by peer-origin-host of referred static-peer instance. If turned off, alarm DIA Diameter Transport Vulnerability is raised for the related endpoint. Note, there is no hostname validation for dynamic Peers allowed by filters in dynamic-peer-acceptor instances. Has no effect on connections without TLS, that is if tls-profile is empty. Update Effect: All established Diameter Peer connections assigned to local tcp endpoint are dropped and reestablished with updated information.")
    private Boolean tlsHostNameValidation = true;

    /**
     * Used to specify the port to be used by the TCP transport capability of a
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
     * Used to specify the port to be used by the TCP transport capability of a
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

    public TransportTcp withPort(Long port)
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

    public TransportTcp withRank(Long rank)
    {
        this.rank = rank;
        return this;
    }

    /**
     * Used to specify the backlog size of incoming connections handled by a Local
     * Endpoint (the queue size of sockets in LISTEN state, see
     * http://man7.org/linux/man-pages/man2/listen.2.html). This is only valid for
     * Local Endpoints configured in connection termination (server) role. The value
     * applied also depends on underlying OS settings (might be constrained by OS to
     * less than configured). Update Apply: Immediate. Update Effect: All
     * established diameter peer connections assigned to local endpoint are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("backlog")
    public Long getBacklog()
    {
        return backlog;
    }

    /**
     * Used to specify the backlog size of incoming connections handled by a Local
     * Endpoint (the queue size of sockets in LISTEN state, see
     * http://man7.org/linux/man-pages/man2/listen.2.html). This is only valid for
     * Local Endpoints configured in connection termination (server) role. The value
     * applied also depends on underlying OS settings (might be constrained by OS to
     * less than configured). Update Apply: Immediate. Update Effect: All
     * established diameter peer connections assigned to local endpoint are dropped
     * and reestablished with updated information.
     * 
     */
    @JsonProperty("backlog")
    public void setBacklog(Long backlog)
    {
        this.backlog = backlog;
    }

    public TransportTcp withBacklog(Long backlog)
    {
        this.backlog = backlog;
        return this;
    }

    /**
     * Used to enable or disable the use of the Nagle algorithm for the TCP
     * transport capability of a Local Endpoint. By default, TCP_NODELAY socket
     * option is applied, the use of Nagle algorithm (see
     * https://tools.ietf.org/html/rfc3539#section-3.2) is disabled. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are preserved and updated with new configuration.
     * 
     */
    @JsonProperty("no-delay")
    public Boolean getNoDelay()
    {
        return noDelay;
    }

    /**
     * Used to enable or disable the use of the Nagle algorithm for the TCP
     * transport capability of a Local Endpoint. By default, TCP_NODELAY socket
     * option is applied, the use of Nagle algorithm (see
     * https://tools.ietf.org/html/rfc3539#section-3.2) is disabled. Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are preserved and updated with new configuration.
     * 
     */
    @JsonProperty("no-delay")
    public void setNoDelay(Boolean noDelay)
    {
        this.noDelay = noDelay;
    }

    public TransportTcp withNoDelay(Boolean noDelay)
    {
        this.noDelay = noDelay;
        return this;
    }

    /**
     * Offers the possibility to assign an IP address to a transport capability.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    public List<String> getAddress()
    {
        return address;
    }

    /**
     * Offers the possibility to assign an IP address to a transport capability.
     * Update Apply: Immediate. Update Effect: All established diameter peer
     * connections for the transport capability assigned to local endpoint are
     * dropped and reestablished by need with updated information.
     * 
     */
    @JsonProperty("address")
    public void setAddress(List<String> address)
    {
        this.address = address;
    }

    public TransportTcp withAddress(List<String> address)
    {
        this.address = address;
        return this;
    }

    /**
     * If defined, endpoint uses TLS to secure the communication with properties of
     * the referred tls-profile. If not defined, alarm DIA Diameter Transport
     * Vulnerability is raised for the related endpoint. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-profile")
    public String getTlsProfile()
    {
        return tlsProfile;
    }

    /**
     * If defined, endpoint uses TLS to secure the communication with properties of
     * the referred tls-profile. If not defined, alarm DIA Diameter Transport
     * Vulnerability is raised for the related endpoint. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-profile")
    public void setTlsProfile(String tlsProfile)
    {
        this.tlsProfile = tlsProfile;
    }

    public TransportTcp withTlsProfile(String tlsProfile)
    {
        this.tlsProfile = tlsProfile;
        return this;
    }

    /**
     * Can be used to turn off hostname validation on TLS secured static
     * connections. By default, peer certificate received during TLS handshake must
     * contain the id defined by peer-origin-host of referred static-peer instance.
     * If turned off, alarm DIA Diameter Transport Vulnerability is raised for the
     * related endpoint. Note, there is no hostname validation for dynamic Peers
     * allowed by filters in dynamic-peer-acceptor instances. Has no effect on
     * connections without TLS, that is if tls-profile is empty. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-host-name-validation")
    public Boolean getTlsHostNameValidation()
    {
        return tlsHostNameValidation;
    }

    /**
     * Can be used to turn off hostname validation on TLS secured static
     * connections. By default, peer certificate received during TLS handshake must
     * contain the id defined by peer-origin-host of referred static-peer instance.
     * If turned off, alarm DIA Diameter Transport Vulnerability is raised for the
     * related endpoint. Note, there is no hostname validation for dynamic Peers
     * allowed by filters in dynamic-peer-acceptor instances. Has no effect on
     * connections without TLS, that is if tls-profile is empty. Update Effect: All
     * established Diameter Peer connections assigned to local tcp endpoint are
     * dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-host-name-validation")
    public void setTlsHostNameValidation(Boolean tlsHostNameValidation)
    {
        this.tlsHostNameValidation = tlsHostNameValidation;
    }

    public TransportTcp withTlsHostNameValidation(Boolean tlsHostNameValidation)
    {
        this.tlsHostNameValidation = tlsHostNameValidation;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TransportTcp.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null) ? "<null>" : this.rank));
        sb.append(',');
        sb.append("backlog");
        sb.append('=');
        sb.append(((this.backlog == null) ? "<null>" : this.backlog));
        sb.append(',');
        sb.append("noDelay");
        sb.append('=');
        sb.append(((this.noDelay == null) ? "<null>" : this.noDelay));
        sb.append(',');
        sb.append("address");
        sb.append('=');
        sb.append(((this.address == null) ? "<null>" : this.address));
        sb.append(',');
        sb.append("tlsProfile");
        sb.append('=');
        sb.append(((this.tlsProfile == null) ? "<null>" : this.tlsProfile));
        sb.append(',');
        sb.append("tlsHostNameValidation");
        sb.append('=');
        sb.append(((this.tlsHostNameValidation == null) ? "<null>" : this.tlsHostNameValidation));
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
        result = ((result * 31) + ((this.backlog == null) ? 0 : this.backlog.hashCode()));
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
        result = ((result * 31) + ((this.tlsHostNameValidation == null) ? 0 : this.tlsHostNameValidation.hashCode()));
        result = ((result * 31) + ((this.noDelay == null) ? 0 : this.noDelay.hashCode()));
        result = ((result * 31) + ((this.tlsProfile == null) ? 0 : this.tlsProfile.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TransportTcp) == false)
        {
            return false;
        }
        TransportTcp rhs = ((TransportTcp) other);
        return ((((((this.rank == rhs.rank) || ((this.rank != null) && this.rank.equals(rhs.rank)))
                   && ((this.backlog == rhs.backlog) || ((this.backlog != null) && this.backlog.equals(rhs.backlog))))
                  && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                 && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))))
                && ((this.noDelay == rhs.noDelay) || ((this.noDelay != null) && this.noDelay.equals(rhs.noDelay))));
    }

}
