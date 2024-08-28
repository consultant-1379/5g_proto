package com.ericsson.sc.bsf.diameter;

import java.util.List;
import java.util.Objects;

/**
 * BSF Diameter runtime configuration
 */
public class BsfDiameterCfg
{
    private final String fallbackDestHost;
    private final String fallbackDestRealm;
    private final boolean failoverEnabled;
    private final String nfInstanceName;
    private final String staticDestHost;
    private final String staticDestRealm;
    private List<Integer> reRouteError;

    private BsfDiameterCfg(String fallbackDestHost,
                           String fallbackDestRealm,
                           boolean failoverEnabled,
                           String nfInstanceName,
                           String staticDestHost,
                           String staticDestRealm,
                           List<Integer> reRouteError)
    {
        Objects.nonNull(nfInstanceName);
        this.fallbackDestHost = fallbackDestHost;
        this.fallbackDestRealm = fallbackDestRealm;
        this.failoverEnabled = failoverEnabled;
        this.nfInstanceName = nfInstanceName;
        this.staticDestHost = staticDestHost;
        this.staticDestRealm = staticDestRealm;
        this.reRouteError = reRouteError;
    }

    public static BsfDiameterCfg create(String fallbackDestHost,
                                        String fallbackDestRealm,
                                        boolean failoverEnabled,
                                        String nfInstanceName,
                                        String staticDestHost,
                                        String staticDestRealm,
                                        List<Integer> reRouteError)
    {
        return new BsfDiameterCfg(fallbackDestHost, fallbackDestRealm, failoverEnabled, nfInstanceName, staticDestHost, staticDestRealm, reRouteError);
    }

    /**
     * The fallback-destination-host configured for this service
     * 
     * @return
     */
    public String getFallbackDestHost()
    {
        return fallbackDestHost;
    }

    /**
     * The fallback-destination-realm configured for this service
     * 
     * @return
     */
    public String getFallbackDestRealm()
    {
        return fallbackDestRealm;
    }

    public boolean isFailoverEnabled()
    {
        return failoverEnabled;
    }

    public String getNfInstanceName()
    {
        return nfInstanceName;
    }

    /**
     * @return the staticDestHost
     */
    public String getStaticDestHost()
    {
        return staticDestHost;
    }

    /**
     * @return the staticDestRealm
     */
    public String getStaticDestRealm()
    {
        return staticDestRealm;
    }

    /**
     * @return the reRouteError
     */
    public List<Integer> getReRouteError()
    {
        return reRouteError;
    }

    /**
     * @param reRouteError the reRouteError to set
     */
    public void setReRouteError(List<Integer> reRouteError)
    {
        this.reRouteError = reRouteError;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(failoverEnabled, fallbackDestHost, fallbackDestRealm, nfInstanceName, reRouteError, staticDestHost, staticDestRealm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BsfDiameterCfg other = (BsfDiameterCfg) obj;
        return failoverEnabled == other.failoverEnabled && Objects.equals(fallbackDestHost, other.fallbackDestHost)
               && Objects.equals(fallbackDestRealm, other.fallbackDestRealm) && Objects.equals(nfInstanceName, other.nfInstanceName)
               && Objects.equals(reRouteError, other.reRouteError) && Objects.equals(staticDestHost, other.staticDestHost)
               && Objects.equals(staticDestRealm, other.staticDestRealm);
    }

}
