
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "user-label", "linux-env-var", "ip-family" })
public class HostAddressResolver
{

    /**
     * Used to specify the key of the host-address-resolver instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the host-address-resolver instance.")
    private String id;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * Used to specify a Linux environment variable name the Diameter Service
     * instances must use to resolve the related local IP address they should bind
     * to. The Linux environment variable specified must be injected in related
     * Diameter Service containers as well. Update Apply: Immediate Update Effect:
     * All established diameter peer connections linked to Local Endpoints referring
     * to the host-address-resolver are dropped and reestablished using the updated
     * information. (Required)
     * 
     */
    @JsonProperty("linux-env-var")
    @JsonPropertyDescription("Used to specify a Linux environment variable name the Diameter Service instances must use to resolve the related local IP address they should bind to. The Linux environment variable specified must be injected in related Diameter Service containers as well. Update Apply: Immediate Update Effect: All established diameter peer connections linked to Local Endpoints referring to the host-address-resolver are dropped and reestablished using the updated information.")
    private String linuxEnvVar;

    /**
     * Used to specify the key of the host-address-resolver instance. (Required)
     * 
     */

    @JsonProperty("ip-family")
    @JsonPropertyDescription("Used to specify the IP family of the IP address to be selected in case two IP addresses with different IP families are provided through the Linux Environment Variable configured as value for the linux-env-var attribute.")
    private HostAddressResolver.IpFamily ipFamily = HostAddressResolver.IpFamily.fromValue("any");

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the host-address-resolver instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public HostAddressResolver withId(String id)
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

    public HostAddressResolver withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Used to specify a Linux environment variable name the Diameter Service
     * instances must use to resolve the related local IP address they should bind
     * to. The Linux environment variable specified must be injected in related
     * Diameter Service containers as well. Update Apply: Immediate Update Effect:
     * All established diameter peer connections linked to Local Endpoints referring
     * to the host-address-resolver are dropped and reestablished using the updated
     * information. (Required)
     * 
     */
    @JsonProperty("linux-env-var")
    public String getLinuxEnvVar()
    {
        return linuxEnvVar;
    }

    /**
     * Used to specify a Linux environment variable name the Diameter Service
     * instances must use to resolve the related local IP address they should bind
     * to. The Linux environment variable specified must be injected in related
     * Diameter Service containers as well. Update Apply: Immediate Update Effect:
     * All established diameter peer connections linked to Local Endpoints referring
     * to the host-address-resolver are dropped and reestablished using the updated
     * information. (Required)
     * 
     */
    @JsonProperty("linux-env-var")
    public void setLinuxEnvVar(String linuxEnvVar)
    {
        this.linuxEnvVar = linuxEnvVar;
    }

    public HostAddressResolver withLinuxEnvVar(String linuxEnvVar)
    {
        this.linuxEnvVar = linuxEnvVar;
        return this;
    }

    @JsonProperty("ip-family")
    public HostAddressResolver.IpFamily getIpFamily()
    {
        return ipFamily;
    }

    /**
     * Used to specify the IP family of the IP address.
     * 
     */
    @JsonProperty("ip-family")
    public void setIpFamily(HostAddressResolver.IpFamily ipFamily)
    {
        this.ipFamily = ipFamily;
    }

    public HostAddressResolver withIpFamily(HostAddressResolver.IpFamily ipFamily)
    {
        this.ipFamily = ipFamily;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(HostAddressResolver.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("linuxEnvVar");
        sb.append('=');
        sb.append(((this.linuxEnvVar == null) ? "<null>" : this.linuxEnvVar));
        sb.append(',');
        sb.append("ipFamily");
        sb.append('=');
        sb.append(((this.ipFamily == null) ? "<null>" : this.ipFamily));
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
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.linuxEnvVar == null) ? 0 : this.linuxEnvVar.hashCode()));
        result = ((result * 31) + ((this.ipFamily == null) ? 0 : this.ipFamily.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof HostAddressResolver) == false)
        {
            return false;
        }
        HostAddressResolver rhs = ((HostAddressResolver) other);
        return ((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.linuxEnvVar == rhs.linuxEnvVar) || ((this.linuxEnvVar != null) && this.linuxEnvVar.equals(rhs.linuxEnvVar)))
                && ((this.ipFamily == rhs.ipFamily) || ((this.ipFamily != null) && this.ipFamily.equals(rhs.ipFamily))));
    }

    public enum IpFamily
    {

        ANY("any"),
        IPV4("ipv4"),
        IPV6("ipv6");

        private final String value;
        private static final Map<String, HostAddressResolver.IpFamily> CONSTANTS = new HashMap<>();

        static
        {
            for (HostAddressResolver.IpFamily c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private IpFamily(String value)
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
        public static HostAddressResolver.IpFamily fromValue(String value)
        {
            HostAddressResolver.IpFamily constant = CONSTANTS.get(value);
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
