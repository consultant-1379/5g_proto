
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfIngressConnectionProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "user-label",
                     "max-connection-duration",
                     "hpack-table-size",
                     "max-concurrent-streams",
                     "connection-idle-timeout",
                     "tcp-keepalive",
                     "dscp-marking" })
public class IngressConnectionProfile implements IfIngressConnectionProfile
{

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the ingress-connection-profile")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * The maximum duration of an incoming TCP connection. The duration is defined
     * as a period since a connection was established. Default value zero means that
     * no time limit is imposed
     * 
     */
    @JsonProperty("max-connection-duration")
    @JsonPropertyDescription("The maximum duration of an incoming TCP connection. The duration is defined as a period since a connection was established. Default value zero means that no time limit is imposed")
    private Integer maxConnectionDuration = 0;
    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 2147483647 (2^31 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    @JsonPropertyDescription("Maximum table size (in octets) that the encoder is permitted to use for the dynamic HPACK table. Valid values range from 0 to 2147483647 (2^31 - 1). 0 effectively disables header compression.")
    private Integer hpackTableSize = 4096;
    /**
     * Maximum concurrent streams allowed for peer on one HTTP/2 connection. If the
     * limit is reached, it is treated as stream error
     * 
     */
    @JsonProperty("max-concurrent-streams")
    @JsonPropertyDescription("Maximum concurrent streams allowed for peer on one HTTP/2 connection. If the limit is reached, it is treated as stream error")
    private Integer maxConcurrentStreams = 2147483647;
    /**
     * The period in which there are no active HTTP requests. When the idle timeout
     * is reached the connection will be closed. To disable idle timeouts explicitly
     * set this to 0.
     * 
     */
    @JsonProperty("connection-idle-timeout")
    @JsonPropertyDescription("The period in which there are no active HTTP requests. When the idle timeout is reached the connection will be closed. To disable idle timeouts explicitly set this to 0.")
    private Integer connectionIdleTimeout = 3600;
    /**
     * TCP-keepalive settings
     * 
     */
    @JsonProperty("tcp-keepalive")
    @JsonPropertyDescription("TCP-keepalive settings")
    private TcpKeepalive tcpKeepalive;
    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    @JsonPropertyDescription("DSCP value used for IP packets sent over this connection")
    private Integer dscpMarking;

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public IngressConnectionProfile withName(String name)
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

    public IngressConnectionProfile withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * The maximum duration of an incoming TCP connection. The duration is defined
     * as a period since a connection was established. Default value zero means that
     * no time limit is imposed
     * 
     */
    @JsonProperty("max-connection-duration")
    public Integer getMaxConnectionDuration()
    {
        return maxConnectionDuration;
    }

    /**
     * The maximum duration of an incoming TCP connection. The duration is defined
     * as a period since a connection was established. Default value zero means that
     * no time limit is imposed
     * 
     */
    @JsonProperty("max-connection-duration")
    public void setMaxConnectionDuration(Integer maxConnectionDuration)
    {
        this.maxConnectionDuration = maxConnectionDuration;
    }

    public IngressConnectionProfile withMaxConnectionDuration(Integer maxConnectionDuration)
    {
        this.maxConnectionDuration = maxConnectionDuration;
        return this;
    }

    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 2147483647 (2^31 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    public Integer getHpackTableSize()
    {
        return hpackTableSize;
    }

    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 2147483647 (2^31 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    public void setHpackTableSize(Integer hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
    }

    public IngressConnectionProfile withHpackTableSize(Integer hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
        return this;
    }

    /**
     * Maximum concurrent streams allowed for peer on one HTTP/2 connection. If the
     * limit is reached, it is treated as stream error
     * 
     */
    @JsonProperty("max-concurrent-streams")
    public Integer getMaxConcurrentStreams()
    {
        return maxConcurrentStreams;
    }

    /**
     * Maximum concurrent streams allowed for peer on one HTTP/2 connection. If the
     * limit is reached, it is treated as stream error
     * 
     */
    @JsonProperty("max-concurrent-streams")
    public void setMaxConcurrentStreams(Integer maxConcurrentStreams)
    {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public IngressConnectionProfile withMaxConcurrentStreams(Integer maxConcurrentStreams)
    {
        this.maxConcurrentStreams = maxConcurrentStreams;
        return this;
    }

    /**
     * The period in which there are no active HTTP requests. When the idle timeout
     * is reached the connection will be closed. To disable idle timeouts explicitly
     * set this to 0.
     * 
     */
    @JsonProperty("connection-idle-timeout")
    public Integer getConnectionIdleTimeout()
    {
        return connectionIdleTimeout;
    }

    /**
     * The period in which there are no active HTTP requests. When the idle timeout
     * is reached the connection will be closed. To disable idle timeouts explicitly
     * set this to 0.
     * 
     */
    @JsonProperty("connection-idle-timeout")
    public void setConnectionIdleTimeout(Integer connectionIdleTimeout)
    {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    public IngressConnectionProfile withConnectionIdleTimeout(Integer connectionIdleTimeout)
    {
        this.connectionIdleTimeout = connectionIdleTimeout;
        return this;
    }

    /**
     * TCP-keepalive settings
     * 
     */
    @JsonProperty("tcp-keepalive")
    public TcpKeepalive getTcpKeepalive()
    {
        return tcpKeepalive;
    }

    /**
     * TCP-keepalive settings
     * 
     */
    @JsonProperty("tcp-keepalive")
    public void setTcpKeepalive(TcpKeepalive tcpKeepalive)
    {
        this.tcpKeepalive = tcpKeepalive;
    }

    public IngressConnectionProfile withTcpKeepalive(TcpKeepalive tcpKeepalive)
    {
        this.tcpKeepalive = tcpKeepalive;
        return this;
    }

    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    public Integer getDscpMarking()
    {
        return dscpMarking;
    }

    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    public void setDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
    }

    public IngressConnectionProfile withDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IngressConnectionProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("maxConnectionDuration");
        sb.append('=');
        sb.append(((this.maxConnectionDuration == null) ? "<null>" : this.maxConnectionDuration));
        sb.append(',');
        sb.append("hpackTableSize");
        sb.append('=');
        sb.append(((this.hpackTableSize == null) ? "<null>" : this.hpackTableSize));
        sb.append(',');
        sb.append("maxConcurrentStreams");
        sb.append('=');
        sb.append(((this.maxConcurrentStreams == null) ? "<null>" : this.maxConcurrentStreams));
        sb.append(',');
        sb.append("connectionIdleTimeout");
        sb.append('=');
        sb.append(((this.connectionIdleTimeout == null) ? "<null>" : this.connectionIdleTimeout));
        sb.append(',');
        sb.append("tcpKeepalive");
        sb.append('=');
        sb.append(((this.tcpKeepalive == null) ? "<null>" : this.tcpKeepalive));
        sb.append(',');
        sb.append("dscpMarking");
        sb.append('=');
        sb.append(((this.dscpMarking == null) ? "<null>" : this.dscpMarking));
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
        result = ((result * 31) + ((this.hpackTableSize == null) ? 0 : this.hpackTableSize.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.tcpKeepalive == null) ? 0 : this.tcpKeepalive.hashCode()));
        result = ((result * 31) + ((this.dscpMarking == null) ? 0 : this.dscpMarking.hashCode()));
        result = ((result * 31) + ((this.maxConnectionDuration == null) ? 0 : this.maxConnectionDuration.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.connectionIdleTimeout == null) ? 0 : this.connectionIdleTimeout.hashCode()));
        result = ((result * 31) + ((this.maxConcurrentStreams == null) ? 0 : this.maxConcurrentStreams.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IngressConnectionProfile) == false)
        {
            return false;
        }
        IngressConnectionProfile rhs = ((IngressConnectionProfile) other);
        return (((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                      && ((this.hpackTableSize == rhs.hpackTableSize) || ((this.hpackTableSize != null) && this.hpackTableSize.equals(rhs.hpackTableSize))))
                     && ((this.tcpKeepalive == rhs.tcpKeepalive) || ((this.tcpKeepalive != null) && this.tcpKeepalive.equals(rhs.tcpKeepalive))))
                    && ((this.dscpMarking == rhs.dscpMarking) || ((this.dscpMarking != null) && this.dscpMarking.equals(rhs.dscpMarking))))
                   && ((this.maxConnectionDuration == rhs.maxConnectionDuration)
                       || ((this.maxConnectionDuration != null) && this.maxConnectionDuration.equals(rhs.maxConnectionDuration))))
                  && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                 && ((this.connectionIdleTimeout == rhs.connectionIdleTimeout)
                     || ((this.connectionIdleTimeout != null) && this.connectionIdleTimeout.equals(rhs.connectionIdleTimeout))))
                && ((this.maxConcurrentStreams == rhs.maxConcurrentStreams)
                    || ((this.maxConcurrentStreams != null) && this.maxConcurrentStreams.equals(rhs.maxConcurrentStreams))));
    }

}
