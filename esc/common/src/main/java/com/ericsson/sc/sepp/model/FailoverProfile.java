
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfFailoverProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "user-label",
                     "request-time-budget",
                     "retry-condition",
                     "target-timeout",
                     "preferred-host-retries-max",
                     "preferred-host-retry-multiple-addresses",
                     "target-nf-pool-reselects-max",
                     "last-resort-nf-pool-reselects-max" })
public class FailoverProfile implements IfFailoverProfile
{

    /**
     * Name identifying the failover-profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the failover-profile")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * The maximum time in milliseconds a single request is allowed to take
     * including retries, before SEPP considers it as timed out. The time starts
     * when the request enters the SCP Worker and ends when the response leaves the
     * SCP Worker. It includes the time for an SLF-lookup if one is required.
     * 
     */
    @JsonProperty("request-time-budget")
    @JsonPropertyDescription("The maximum time in milliseconds a single request is allowed to take including retries, before SEPP considers it as timed out. The time starts when the request enters the SCP Worker and ends when the response leaves the SCP Worker. It includes the time for an SLF-lookup if one is required.")
    private Integer requestTimeBudget = 2000;
    /**
     * Criteria for attempting a retry
     * 
     */
    @JsonProperty("retry-condition")
    @JsonPropertyDescription("Criteria for attempting a retry")
    private RetryCondition retryCondition;
    /**
     * Specifies the maximum time in milliseconds a single request towards a
     * specific target is allowed to take before considered to be timed out
     * 
     */
    @JsonProperty("target-timeout")
    @JsonPropertyDescription("Specifies the maximum time in milliseconds a single request towards a specific target is allowed to take before considered to be timed out")
    private Integer targetTimeout = 2000;
    /**
     * Specifies the integer of times to perform a retry to the preferred host
     * 
     */
    @JsonProperty("preferred-host-retries-max")
    @JsonPropertyDescription("Specifies the integer of times to perform a retry to the preferred host")
    private Integer preferredHostRetriesMax = 3;
    /**
     * Retry multiple addresses of preferred host. If set to true, retries will be
     * spread across all addresses of the selected host.
     * 
     */
    @JsonProperty("preferred-host-retry-multiple-addresses")
    @JsonPropertyDescription("Retry multiple addresses of preferred host. If set to true, retries will be spread across all addresses of the selected host.")
    private Boolean preferredHostRetryMultipleAddresses = true;
    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * target-nf-pool
     * 
     */
    @JsonProperty("target-nf-pool-reselects-max")
    @JsonPropertyDescription("Specifies the integer of reselects to perform among the hosts of the target-nf-pool")
    private Integer targetNfPoolReselectsMax = 3;
    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * last-resort nf-pool
     * 
     */
    @JsonProperty("last-resort-nf-pool-reselects-max")
    @JsonPropertyDescription("Specifies the integer of reselects to perform among the hosts of the last-resort nf-pool")
    private Integer lastResortNfPoolReselectsMax = 3;

    /**
     * @param failoverProfile
     */
    public FailoverProfile(FailoverProfile other)
    {
        this.name = other.name;
        this.lastResortNfPoolReselectsMax = other.lastResortNfPoolReselectsMax;
        this.preferredHostRetriesMax = other.preferredHostRetriesMax;
        this.requestTimeBudget = other.requestTimeBudget;
        this.retryCondition = new RetryCondition(other.retryCondition);
        this.targetNfPoolReselectsMax = other.targetNfPoolReselectsMax;
        this.targetTimeout = other.targetTimeout;
    }

    /**
     * 
     */
    public FailoverProfile()
    {
    }

    /**
     * Name identifying the failover-profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the failover-profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public FailoverProfile withName(String name)
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

    public FailoverProfile withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * The maximum time in milliseconds a single request is allowed to take
     * including retries, before SEPP considers it as timed out. The time starts
     * when the request enters the SCP Worker and ends when the response leaves the
     * SCP Worker. It includes the time for an SLF-lookup if one is required.
     * 
     */
    @JsonProperty("request-time-budget")
    public Integer getRequestTimeBudget()
    {
        return requestTimeBudget;
    }

    /**
     * The maximum time in milliseconds a single request is allowed to take
     * including retries, before SEPP considers it as timed out. The time starts
     * when the request enters the SCP Worker and ends when the response leaves the
     * SCP Worker. It includes the time for an SLF-lookup if one is required.
     * 
     */
    @JsonProperty("request-time-budget")
    public void setRequestTimeBudget(Integer requestTimeBudget)
    {
        this.requestTimeBudget = requestTimeBudget;
    }

    public FailoverProfile withRequestTimeBudget(Integer requestTimeBudget)
    {
        this.requestTimeBudget = requestTimeBudget;
        return this;
    }

    /**
     * Criteria for attempting a retry
     * 
     */
    @JsonProperty("retry-condition")
    public RetryCondition getRetryCondition()
    {
        return retryCondition;
    }

    /**
     * Criteria for attempting a retry
     * 
     */
    @JsonProperty("retry-condition")
    public void setRetryCondition(RetryCondition retryCondition)
    {
        this.retryCondition = retryCondition;
    }

    public FailoverProfile withRetryCondition(RetryCondition retryCondition)
    {
        this.retryCondition = retryCondition;
        return this;
    }

    /**
     * Specifies the maximum time in milliseconds a single request towards a
     * specific target is allowed to take before considered to be timed out
     * 
     */
    @JsonProperty("target-timeout")
    public Integer getTargetTimeout()
    {
        return targetTimeout;
    }

    /**
     * Specifies the maximum time in milliseconds a single request towards a
     * specific target is allowed to take before considered to be timed out
     * 
     */
    @JsonProperty("target-timeout")
    public void setTargetTimeout(Integer targetTimeout)
    {
        this.targetTimeout = targetTimeout;
    }

    public FailoverProfile withTargetTimeout(Integer targetTimeout)
    {
        this.targetTimeout = targetTimeout;
        return this;
    }

    /**
     * Specifies the integer of times to perform a retry to the preferred host
     * 
     */
    @JsonProperty("preferred-host-retries-max")
    public Integer getPreferredHostRetriesMax()
    {
        return preferredHostRetriesMax;
    }

    /**
     * Specifies the integer of times to perform a retry to the preferred host
     * 
     */
    @JsonProperty("preferred-host-retries-max")
    public void setPreferredHostRetriesMax(Integer preferredHostRetriesMax)
    {
        this.preferredHostRetriesMax = preferredHostRetriesMax;
    }

    public FailoverProfile withPreferredHostRetriesMax(Integer preferredHostRetriesMax)
    {
        this.preferredHostRetriesMax = preferredHostRetriesMax;
        return this;
    }

    /**
     * Retry multiple addresses of preferred host. If set to true, retries will be
     * spread across all addresses of the selected host.
     * 
     */
    @JsonProperty("preferred-host-retry-multiple-addresses")
    public Boolean getPreferredHostRetryMultipleAddresses()
    {
        return preferredHostRetryMultipleAddresses;
    }

    /**
     * Retry multiple addresses of preferred host. If set to true, retries will be
     * spread across all addresses of the selected host.
     * 
     */
    @JsonProperty("preferred-host-retry-multiple-addresses")
    public void setPreferredHostRetryMultipleAddresses(Boolean preferredHostRetryMultipleAddresses)
    {
        this.preferredHostRetryMultipleAddresses = preferredHostRetryMultipleAddresses;
    }

    public FailoverProfile withPreferredHostRetryMultipleAddresses(Boolean preferredHostRetryMultipleAddresses)
    {
        this.preferredHostRetryMultipleAddresses = preferredHostRetryMultipleAddresses;
        return this;
    }

    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * target-nf-pool
     * 
     */
    @JsonProperty("target-nf-pool-reselects-max")
    public Integer getTargetNfPoolReselectsMax()
    {
        return targetNfPoolReselectsMax;
    }

    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * target-nf-pool
     * 
     */
    @JsonProperty("target-nf-pool-reselects-max")
    public void setTargetNfPoolReselectsMax(Integer targetNfPoolReselectsMax)
    {
        this.targetNfPoolReselectsMax = targetNfPoolReselectsMax;
    }

    public FailoverProfile withTargetNfPoolReselectsMax(Integer targetNfPoolReselectsMax)
    {
        this.targetNfPoolReselectsMax = targetNfPoolReselectsMax;
        return this;
    }

    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * last-resort nf-pool
     * 
     */
    @JsonProperty("last-resort-nf-pool-reselects-max")
    public Integer getLastResortNfPoolReselectsMax()
    {
        return lastResortNfPoolReselectsMax;
    }

    /**
     * Specifies the integer of reselects to perform among the hosts of the
     * last-resort nf-pool
     * 
     */
    @JsonProperty("last-resort-nf-pool-reselects-max")
    public void setLastResortNfPoolReselectsMax(Integer lastResortNfPoolReselectsMax)
    {
        this.lastResortNfPoolReselectsMax = lastResortNfPoolReselectsMax;
    }

    public FailoverProfile withLastResortNfPoolReselectsMax(Integer lastResortNfPoolReselectsMax)
    {
        this.lastResortNfPoolReselectsMax = lastResortNfPoolReselectsMax;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FailoverProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("requestTimeBudget");
        sb.append('=');
        sb.append(((this.requestTimeBudget == null) ? "<null>" : this.requestTimeBudget));
        sb.append(',');
        sb.append("retryCondition");
        sb.append('=');
        sb.append(((this.retryCondition == null) ? "<null>" : this.retryCondition));
        sb.append(',');
        sb.append("targetTimeout");
        sb.append('=');
        sb.append(((this.targetTimeout == null) ? "<null>" : this.targetTimeout));
        sb.append(',');
        sb.append("preferredHostRetriesMax");
        sb.append('=');
        sb.append(((this.preferredHostRetriesMax == null) ? "<null>" : this.preferredHostRetriesMax));
        sb.append(',');
        sb.append("preferredHostRetryMultipleAddresses");
        sb.append('=');
        sb.append(((this.preferredHostRetryMultipleAddresses == null) ? "<null>" : this.preferredHostRetryMultipleAddresses));
        sb.append(',');
        sb.append("targetNfPoolReselectsMax");
        sb.append('=');
        sb.append(((this.targetNfPoolReselectsMax == null) ? "<null>" : this.targetNfPoolReselectsMax));
        sb.append(',');
        sb.append("lastResortNfPoolReselectsMax");
        sb.append('=');
        sb.append(((this.lastResortNfPoolReselectsMax == null) ? "<null>" : this.lastResortNfPoolReselectsMax));
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
        result = ((result * 31) + ((this.targetTimeout == null) ? 0 : this.targetTimeout.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.preferredHostRetryMultipleAddresses == null) ? 0 : this.preferredHostRetryMultipleAddresses.hashCode()));
        result = ((result * 31) + ((this.lastResortNfPoolReselectsMax == null) ? 0 : this.lastResortNfPoolReselectsMax.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.preferredHostRetriesMax == null) ? 0 : this.preferredHostRetriesMax.hashCode()));
        result = ((result * 31) + ((this.requestTimeBudget == null) ? 0 : this.requestTimeBudget.hashCode()));
        result = ((result * 31) + ((this.retryCondition == null) ? 0 : this.retryCondition.hashCode()));
        result = ((result * 31) + ((this.targetNfPoolReselectsMax == null) ? 0 : this.targetNfPoolReselectsMax.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof FailoverProfile) == false)
        {
            return false;
        }
        FailoverProfile rhs = ((FailoverProfile) other);
        return ((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                       && ((this.targetTimeout == rhs.targetTimeout) || ((this.targetTimeout != null) && this.targetTimeout.equals(rhs.targetTimeout))))
                      && ((this.preferredHostRetryMultipleAddresses == rhs.preferredHostRetryMultipleAddresses)
                          || ((this.preferredHostRetryMultipleAddresses != null)
                              && this.preferredHostRetryMultipleAddresses.equals(rhs.preferredHostRetryMultipleAddresses))))
                     && ((this.lastResortNfPoolReselectsMax == rhs.lastResortNfPoolReselectsMax)
                         || ((this.lastResortNfPoolReselectsMax != null) && this.lastResortNfPoolReselectsMax.equals(rhs.lastResortNfPoolReselectsMax))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.preferredHostRetriesMax == rhs.preferredHostRetriesMax)
                       || ((this.preferredHostRetriesMax != null) && this.preferredHostRetriesMax.equals(rhs.preferredHostRetriesMax))))
                  && ((this.requestTimeBudget == rhs.requestTimeBudget)
                      || ((this.requestTimeBudget != null) && this.requestTimeBudget.equals(rhs.requestTimeBudget))))
                 && ((this.retryCondition == rhs.retryCondition) || ((this.retryCondition != null) && this.retryCondition.equals(rhs.retryCondition))))
                && ((this.targetNfPoolReselectsMax == rhs.targetNfPoolReselectsMax)
                    || ((this.targetNfPoolReselectsMax != null) && this.targetNfPoolReselectsMax.equals(rhs.targetNfPoolReselectsMax))));
    }

}
