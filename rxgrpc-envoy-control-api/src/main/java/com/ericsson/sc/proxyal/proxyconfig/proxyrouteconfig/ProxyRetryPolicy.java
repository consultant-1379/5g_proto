package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.annotations.NonNull;

/**
 * Simple container class. Holds data for retries.
 */
public class ProxyRetryPolicy
{
    public final String ERIC_OMIT_HOST_MD_DYNAMIC = "envoy.retry_host_predicates.eric_omit_host_metadata_dynamic";
    public final String PREVIOUS_HOSTS = "envoy.retry_host_predicates.previous_hosts";

    public final String ERIC_RESELECT_PRIORITIES = "envoy.retry_priorities.eric_reselect_priorities";

    private Optional<String> name;
    private String retryOn;
    private int numRetries;
    private Double perTryTimeoutSeconds;
    private Double requestTimeoutSeconds;
    private String retryHostPredicate;
    private String retryPriority;
    private int hostSelectionRetryMaxAttempts;
    private int preferredHostRetries = 0;
    private int failoverReselects = 0;
    private int lastResortReselects = 0;
    private String omitHostMetaData;
    private boolean SupportTemporaryBlocking = false;
    private boolean SupportLoopPrevention = false;

    /**
     * @return the supprtTemporaryBlocking
     */
    public boolean isSupportTemporaryBlocking()
    {
        return SupportTemporaryBlocking;
    }

    /**
     * @param supprtTemporaryBlocking the supprtTemporaryBlocking to set
     */
    public void setSupportTemporaryBlocking(boolean supprtTemporaryBlocking)
    {
        SupportTemporaryBlocking = supprtTemporaryBlocking;
    }

    /**
     * @return the SupportLoopPrevention
     */
    public boolean isSupportLoopPrevention()
    {
        return this.SupportLoopPrevention;
    }

    /**
     * @param supprtLoopPrevention the supprtLoopPrevention to set
     */
    public void setSupportLoopPrevention(boolean supprtLoopPrevention)
    {
        this.SupportLoopPrevention = supprtLoopPrevention;
    }

    /**
     * @param omitHostMetaData the omitHostMetaData to set
     */
    public void setOmitHostMetaData(String omitHostMetaData)
    {
        this.omitHostMetaData = omitHostMetaData;
    }

    private List<Integer> retriableStatusCodes = new ArrayList<>();

    public int getPreferredHostRetries()
    {
        return preferredHostRetries;
    }

    public void setPreferredHostRetries(int preferredHostRetries)
    {
        this.preferredHostRetries = preferredHostRetries;
    }

    public int getFailoverReselects()
    {
        return failoverReselects;
    }

    public void setFailoverReselects(int failoverReselects)
    {
        this.failoverReselects = failoverReselects;
    }

    public int getLastResortReselects()
    {
        return lastResortReselects;
    }

    public void setLastResortReselects(int lastResortReselects)
    {
        this.lastResortReselects = lastResortReselects;
    }

    public ProxyRetryPolicy()
    {
        // empty/use defaults
    }

    public ProxyRetryPolicy(String retryOn,
                            int numRetries,
                            Double perTryTimeoutSeconds,
                            int hostSelectionRetryMaxAttempts)
    {
        this.retryOn = retryOn;
        this.numRetries = numRetries;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.hostSelectionRetryMaxAttempts = hostSelectionRetryMaxAttempts;
    }

    public ProxyRetryPolicy(String retryOn,
                            int numRetries,
                            Double perTryTimeoutSeconds,
                            String retryHostPredicate,
                            String retryPriority,
                            int updatefreq,
                            int hostSelectionRetryMaxAttempts)
    {
        this.retryOn = retryOn;
        this.numRetries = numRetries;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.retryHostPredicate = retryHostPredicate;
        this.retryPriority = retryPriority;
        this.hostSelectionRetryMaxAttempts = hostSelectionRetryMaxAttempts;
    }

    public ProxyRetryPolicy(@NonNull Optional<String> name,
                            String retryOn,
                            Double perTryTimeoutSeconds,
                            Double requestTimeoutSeconds,
                            String retryHostPredicate,
                            String retryPriority)
    {
        this.name = name;
        this.retryOn = retryOn;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        this.retryHostPredicate = retryHostPredicate;
        this.retryPriority = retryPriority;
    }

    public ProxyRetryPolicy(@NonNull Optional<String> name,
                            String retryOn,
                            Double perTryTimeoutSeconds,
                            Double requestTimeoutSeconds)
    {
        this.name = name;
        this.retryOn = retryOn;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public ProxyRetryPolicy(@NonNull Optional<String> name,
                            String retryOn,
                            Double perTryTimeoutSeconds,
                            Double requestTimeoutSeconds,
                            String retryHostPredicate,
                            String retryPriority,
                            int numberOfRetries)
    {
        this.name = name;
        this.retryOn = retryOn;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        this.retryHostPredicate = retryHostPredicate;
        this.retryPriority = retryPriority;
        this.numRetries = numberOfRetries;
    }

    public ProxyRetryPolicy(String retryOn,
                            int numRetries,
                            Double perTryTimeoutSeconds,
                            String retryHostPredicate,
                            int hostSelectionRetryMaxAttempts)
    {
        this.retryOn = retryOn;
        this.numRetries = numRetries;
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
        this.retryHostPredicate = retryHostPredicate;
        this.hostSelectionRetryMaxAttempts = hostSelectionRetryMaxAttempts;
    }

    public ProxyRetryPolicy(ProxyRetryPolicy a)
    {
        this.name = a.getName().map(v -> v);
        this.retryOn = a.retryOn;
        this.numRetries = a.numRetries;
        this.retriableStatusCodes = a.retriableStatusCodes;
        this.perTryTimeoutSeconds = a.perTryTimeoutSeconds;
        this.requestTimeoutSeconds = a.requestTimeoutSeconds;
        this.retryHostPredicate = a.retryHostPredicate;
        this.retryPriority = a.retryPriority;

        this.failoverReselects = a.failoverReselects;
        this.preferredHostRetries = a.preferredHostRetries;
        this.lastResortReselects = a.lastResortReselects;
        this.hostSelectionRetryMaxAttempts = a.hostSelectionRetryMaxAttempts;
        this.SupportTemporaryBlocking = a.SupportTemporaryBlocking;
        this.omitHostMetaData = a.omitHostMetaData;

    }

    public Optional<String> getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = Optional.ofNullable(name);
    }

    public String getRetryOn()
    {
        return retryOn;
    }

    public void setRetryOn(String retryOn)
    {
        this.retryOn = retryOn;
    }

    public int getNumRetries()
    {
        return numRetries;
    }

    public void setNumRetries(int numRetries)
    {
        this.numRetries = numRetries;
    }

    public Double getPerTryTimeoutSeconds()
    {
        return perTryTimeoutSeconds;
    }

    public void setPerTryTimeoutSeconds(Double perTryTimeoutSeconds)
    {
        this.perTryTimeoutSeconds = perTryTimeoutSeconds;
    }

    public Double getRequestTimeoutSeconds()
    {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(Double requestTimeout)
    {
        this.requestTimeoutSeconds = requestTimeout;
    }

    public String getRetryHostPredicate()
    {
        return retryHostPredicate;
    }

    public void setRetryHostPredicate(String retryHostPredicate)
    {
        this.retryHostPredicate = retryHostPredicate;
    }

    public String getRetryPriority()
    {
        return this.retryPriority;
    }

    public void setRetryPriority(String priority)
    {
        this.retryPriority = priority;
    }

    public int getHostSelectionRetryMaxAttempts()
    {
        return hostSelectionRetryMaxAttempts;
    }

    public void setHostSelectionRetryMaxAttempts(int hostSelectionRetryMaxAttempts)
    {
        this.hostSelectionRetryMaxAttempts = hostSelectionRetryMaxAttempts;
    }

    public void setOmitHostMetadata(String omitHostMetaData)
    {
        this.omitHostMetaData = omitHostMetaData;
    }

    public String getOmitHostMetaData()
    {
        return this.omitHostMetaData;
    }

    public void setRetriableStatusCodes(List<Integer> codes)
    {
        this.retriableStatusCodes.addAll(codes);
    }

    public List<Integer> getRetriableStatusCodes()
    {
        return this.retriableStatusCodes;
    }

    @Override
    public String toString()
    {
        return "name=" + name + "retryOn=" + retryOn + ", numRetries=" + numRetries + ", retriableStatusCodes=" + retriableStatusCodes
               + ", perTryTimeoutSeconds=" + perTryTimeoutSeconds + ", requestTimeoutSeconds=" + requestTimeoutSeconds + ", retryHostPredicate="
               + retryHostPredicate + ", hostSelectionRetryMaxAttempts=" + hostSelectionRetryMaxAttempts + ", omitHostMetaData=" + omitHostMetaData
               + ", supportTemporaryBlocking=" + SupportTemporaryBlocking + ", supportLoopPrevention=" + SupportLoopPrevention;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hostSelectionRetryMaxAttempts,
                            name,
                            numRetries,
                            omitHostMetaData,
                            perTryTimeoutSeconds,
                            requestTimeoutSeconds,
                            retriableStatusCodes,
                            retryHostPredicate,
                            retryPriority,
                            retryOn);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        ProxyRetryPolicy other = (ProxyRetryPolicy) obj;

        return Objects.equals(hostSelectionRetryMaxAttempts, other.hostSelectionRetryMaxAttempts) && Objects.equals(numRetries, other.numRetries)
               && Objects.equals(perTryTimeoutSeconds, other.perTryTimeoutSeconds) && Objects.equals(requestTimeoutSeconds, other.requestTimeoutSeconds)
               && Objects.equals(retryHostPredicate, other.retryHostPredicate) && Objects.equals(retryOn, other.retryOn) && Objects.equals(name, other.name)
               && Objects.equals(retriableStatusCodes, other.retriableStatusCodes) && Objects.equals(retryPriority, other.retryPriority)
               && Objects.equals(omitHostMetaData, other.omitHostMetaData);
    }
}
