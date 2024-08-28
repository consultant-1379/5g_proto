
package com.ericsson.esc.services.cm.model;

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
@JsonPropertyOrder({ "name", "nf-profile-ref", "oauth2-profile", "priority", "fqdn", "scheme", "ip-endpoint", "retry-timeout", "max-retries" })
public class Nrf
{

    /**
     * Name uniquely identifying the NRF (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the NRF")
    private String name;
    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on the
     * individual NRF level will override the corresponding attributes of the NF
     * profile on the NRF group level.
     * 
     */
    @JsonProperty("nf-profile-ref")
    @JsonPropertyDescription("The NF profile with which to register to the NRF for the Nnrf_NFManagement service. If specified, the attributes configured in the NF profile on the individual NRF level will override the corresponding attributes of the NF profile on the NRF group level.")
    private String nfProfileRef;
    /**
     * Key profiles used by the SCP authorization service to verify the signature of
     * access tokens
     * 
     */
    @JsonProperty("oauth2-profile")
    @JsonPropertyDescription("Key profiles used by the SCP authorization service to verify the signature of access tokens")
    private List<Oauth2Profile> oauth2Profile = new ArrayList<Oauth2Profile>();
    /**
     * Priority of this NRF for NF registration and other NRF service requests,
     * relative to other NRFs in the same NRF group. In regard to the
     * Nnrf_NFManagement service, the NRF with the highest priority is the primary
     * NRF to which the NF instance will register. The remaining NRFs are used in
     * case of failover, in order of priority. Lower values indicate a higher
     * priority. (Required)
     * 
     */
    @JsonProperty("priority")
    @JsonPropertyDescription("Priority of this NRF for NF registration and other NRF service requests, relative to other NRFs in the same NRF group. In regard to the Nnrf_NFManagement service, the NRF with the highest priority is the primary NRF to which the NF instance will register. The remaining NRFs are used in case of failover, in order of priority. Lower values indicate a higher priority.")
    private Integer priority;
    /**
     * FQDN of the NRF
     * 
     */
    @JsonProperty("fqdn")
    @JsonPropertyDescription("FQDN of the NRF")
    private String fqdn;
    /**
     * Specifies the URI scheme to be used when contacting this NRF (Required)
     * 
     */
    @JsonProperty("scheme")
    @JsonPropertyDescription("Specifies the URI scheme to be used when contacting this NRF")
    private Nrf.Scheme scheme;
    /**
     * IP endpoint of the NRF (Required)
     * 
     */
    @JsonProperty("ip-endpoint")
    @JsonPropertyDescription("IP endpoint of the NRF")
    private List<IpEndpoint> ipEndpoint = new ArrayList<IpEndpoint>();
    /**
     * The time duration in milliseconds after which a request is considered
     * unsuccessful and a retry to the same NRF is sent.
     * 
     */
    @JsonProperty("retry-timeout")
    @JsonPropertyDescription("The time duration in milliseconds after which a request is considered unsuccessful and a retry to the same NRF is sent.")
    private Integer retryTimeout = 1500;
    /**
     * The integer of retries sent to the same NRF before failover to another NRF.
     * 
     */
    @JsonProperty("max-retries")
    @JsonPropertyDescription("The integer of retries sent to the same NRF before failover to another NRF.")
    private Integer maxRetries = 0;

    /**
     * Name uniquely identifying the NRF (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the NRF (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Nrf withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on the
     * individual NRF level will override the corresponding attributes of the NF
     * profile on the NRF group level.
     * 
     */
    @JsonProperty("nf-profile-ref")
    public String getNfProfileRef()
    {
        return nfProfileRef;
    }

    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on the
     * individual NRF level will override the corresponding attributes of the NF
     * profile on the NRF group level.
     * 
     */
    @JsonProperty("nf-profile-ref")
    public void setNfProfileRef(String nfProfileRef)
    {
        this.nfProfileRef = nfProfileRef;
    }

    public Nrf withNfProfileRef(String nfProfileRef)
    {
        this.nfProfileRef = nfProfileRef;
        return this;
    }

    /**
     * Key profiles used by the SCP authorization service to verify the signature of
     * access tokens
     * 
     */
    @JsonProperty("oauth2-profile")
    public List<Oauth2Profile> getOauth2Profile()
    {
        return oauth2Profile;
    }

    /**
     * Key profiles used by the SCP authorization service to verify the signature of
     * access tokens
     * 
     */
    @JsonProperty("oauth2-profile")
    public void setOauth2Profile(List<Oauth2Profile> oauth2Profile)
    {
        this.oauth2Profile = oauth2Profile;
    }

    public Nrf withOauth2Profile(List<Oauth2Profile> oauth2Profile)
    {
        this.oauth2Profile = oauth2Profile;
        return this;
    }

    /**
     * Priority of this NRF for NF registration and other NRF service requests,
     * relative to other NRFs in the same NRF group. In regard to the
     * Nnrf_NFManagement service, the NRF with the highest priority is the primary
     * NRF to which the NF instance will register. The remaining NRFs are used in
     * case of failover, in order of priority. Lower values indicate a higher
     * priority. (Required)
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * Priority of this NRF for NF registration and other NRF service requests,
     * relative to other NRFs in the same NRF group. In regard to the
     * Nnrf_NFManagement service, the NRF with the highest priority is the primary
     * NRF to which the NF instance will register. The remaining NRFs are used in
     * case of failover, in order of priority. Lower values indicate a higher
     * priority. (Required)
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public Nrf withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /**
     * FQDN of the NRF
     * 
     */
    @JsonProperty("fqdn")
    public String getFqdn()
    {
        return fqdn;
    }

    /**
     * FQDN of the NRF
     * 
     */
    @JsonProperty("fqdn")
    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public Nrf withFqdn(String fqdn)
    {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * Specifies the URI scheme to be used when contacting this NRF (Required)
     * 
     */
    @JsonProperty("scheme")
    public Nrf.Scheme getScheme()
    {
        return scheme;
    }

    /**
     * Specifies the URI scheme to be used when contacting this NRF (Required)
     * 
     */
    @JsonProperty("scheme")
    public void setScheme(Nrf.Scheme scheme)
    {
        this.scheme = scheme;
    }

    public Nrf withScheme(Nrf.Scheme scheme)
    {
        this.scheme = scheme;
        return this;
    }

    /**
     * IP endpoint of the NRF (Required)
     * 
     */
    @JsonProperty("ip-endpoint")
    public List<IpEndpoint> getIpEndpoint()
    {
        return ipEndpoint;
    }

    /**
     * IP endpoint of the NRF (Required)
     * 
     */
    @JsonProperty("ip-endpoint")
    public void setIpEndpoint(List<IpEndpoint> ipEndpoint)
    {
        this.ipEndpoint = ipEndpoint;
    }

    public Nrf withIpEndpoint(List<IpEndpoint> ipEndpoint)
    {
        this.ipEndpoint = ipEndpoint;
        return this;
    }

    /**
     * The time duration in milliseconds after which a request is considered
     * unsuccessful and a retry to the same NRF is sent.
     * 
     */
    @JsonProperty("retry-timeout")
    public Integer getRetryTimeout()
    {
        return retryTimeout;
    }

    /**
     * The time duration in milliseconds after which a request is considered
     * unsuccessful and a retry to the same NRF is sent.
     * 
     */
    @JsonProperty("retry-timeout")
    public void setRetryTimeout(Integer retryTimeout)
    {
        this.retryTimeout = retryTimeout;
    }

    public Nrf withRetryTimeout(Integer retryTimeout)
    {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * The integer of retries sent to the same NRF before failover to another NRF.
     * 
     */
    @JsonProperty("max-retries")
    public Integer getMaxRetries()
    {
        return maxRetries;
    }

    /**
     * The integer of retries sent to the same NRF before failover to another NRF.
     * 
     */
    @JsonProperty("max-retries")
    public void setMaxRetries(Integer maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public Nrf withMaxRetries(Integer maxRetries)
    {
        this.maxRetries = maxRetries;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Nrf.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfProfileRef");
        sb.append('=');
        sb.append(((this.nfProfileRef == null) ? "<null>" : this.nfProfileRef));
        sb.append(',');
        sb.append("oauth2Profile");
        sb.append('=');
        sb.append(((this.oauth2Profile == null) ? "<null>" : this.oauth2Profile));
        sb.append(',');
        sb.append("priority");
        sb.append('=');
        sb.append(((this.priority == null) ? "<null>" : this.priority));
        sb.append(',');
        sb.append("fqdn");
        sb.append('=');
        sb.append(((this.fqdn == null) ? "<null>" : this.fqdn));
        sb.append(',');
        sb.append("scheme");
        sb.append('=');
        sb.append(((this.scheme == null) ? "<null>" : this.scheme));
        sb.append(',');
        sb.append("ipEndpoint");
        sb.append('=');
        sb.append(((this.ipEndpoint == null) ? "<null>" : this.ipEndpoint));
        sb.append(',');
        sb.append("retryTimeout");
        sb.append('=');
        sb.append(((this.retryTimeout == null) ? "<null>" : this.retryTimeout));
        sb.append(',');
        sb.append("maxRetries");
        sb.append('=');
        sb.append(((this.maxRetries == null) ? "<null>" : this.maxRetries));
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
        result = ((result * 31) + ((this.maxRetries == null) ? 0 : this.maxRetries.hashCode()));
        result = ((result * 31) + ((this.scheme == null) ? 0 : this.scheme.hashCode()));
        result = ((result * 31) + ((this.oauth2Profile == null) ? 0 : this.oauth2Profile.hashCode()));
        result = ((result * 31) + ((this.fqdn == null) ? 0 : this.fqdn.hashCode()));
        result = ((result * 31) + ((this.retryTimeout == null) ? 0 : this.retryTimeout.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.priority == null) ? 0 : this.priority.hashCode()));
        result = ((result * 31) + ((this.ipEndpoint == null) ? 0 : this.ipEndpoint.hashCode()));
        result = ((result * 31) + ((this.nfProfileRef == null) ? 0 : this.nfProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Nrf) == false)
        {
            return false;
        }
        Nrf rhs = ((Nrf) other);
        return ((((((((((this.maxRetries == rhs.maxRetries) || ((this.maxRetries != null) && this.maxRetries.equals(rhs.maxRetries)))
                       && ((this.scheme == rhs.scheme) || ((this.scheme != null) && this.scheme.equals(rhs.scheme))))
                      && ((this.oauth2Profile == rhs.oauth2Profile) || ((this.oauth2Profile != null) && this.oauth2Profile.equals(rhs.oauth2Profile))))
                     && ((this.fqdn == rhs.fqdn) || ((this.fqdn != null) && this.fqdn.equals(rhs.fqdn))))
                    && ((this.retryTimeout == rhs.retryTimeout) || ((this.retryTimeout != null) && this.retryTimeout.equals(rhs.retryTimeout))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.priority == rhs.priority) || ((this.priority != null) && this.priority.equals(rhs.priority))))
                 && ((this.ipEndpoint == rhs.ipEndpoint) || ((this.ipEndpoint != null) && this.ipEndpoint.equals(rhs.ipEndpoint))))
                && ((this.nfProfileRef == rhs.nfProfileRef) || ((this.nfProfileRef != null) && this.nfProfileRef.equals(rhs.nfProfileRef))));
    }

    public enum Scheme
    {

        HTTP("http"),
        HTTPS("https");

        private final String value;
        private final static Map<String, Nrf.Scheme> CONSTANTS = new HashMap<String, Nrf.Scheme>();

        static
        {
            for (Nrf.Scheme c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Scheme(String value)
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
        public static Nrf.Scheme fromValue(String value)
        {
            Nrf.Scheme constant = CONSTANTS.get(value);
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
