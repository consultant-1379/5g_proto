/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 1, 2021
 *     Author: epitgio
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig;

import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.SlfLookupAction;

/**
 *
 */
public class ProxyActionSlfLookup implements ProxyAction
{
    public enum IdentityType
    {
        SUPI("supi"),
        GPSI("gpsi");

        private final String value;

        private IdentityType(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }

    private static final String SLF_SERVICE_CLUSTERNAME = "internal-slf";
    public static final String DEFAULT_ROUTING_CASE_IDENTITY_NOT_FOUND = "#!_#default-routing-case-identity-not-found";
    public static final String DEFAULT_ROUTING_CASE_DESTINATION_UNKNOWN = "#!_#default-routing-case-destination-unknown";
    public static final String DEFAULT_ROUTING_CASE_IDENTITY_MISSING = "#!_#default-routing-case-identity-missing";
    public static final String DEFAULT_ROUTING_CASE_LOOKUP_FAILURE = "#!_#default-routing-case-lookup-failure";

    private final String name;
    private final String requesterNfType;
    private final String targetNfType;
    private final IdentityType identityType;
    private final String nrfGroupName;
    private final Integer requestTimeout;
    private final String result;
    private String routingCaseIdentityNotFound = DEFAULT_ROUTING_CASE_IDENTITY_NOT_FOUND;
    private String routingCaseDestinationUnknown = DEFAULT_ROUTING_CASE_DESTINATION_UNKNOWN;
    private String routingCaseIdentityMissing = DEFAULT_ROUTING_CASE_IDENTITY_MISSING;
    private String routingCaseLookupFailure = DEFAULT_ROUTING_CASE_LOOKUP_FAILURE;
    private final String identityVar;

    /**
     * @param name
     * @param value
     * @param messageDataRef
     * @param nrfGroupName
     * @param result
     * @param routingCaseIdentityNotFound
     * @param routingCaseDestinationUnknown
     * @param routingCaseIdentityMissing
     * @param routingCaseLookupFailure
     */
    public ProxyActionSlfLookup(String name,
                                String requesterNfType,
                                String targetNfType,
                                String nrfGroupName,
                                IdentityType identityType,
                                Integer requestTimeout,
                                String identityVar,
                                String result,
                                String routingCaseIdentityNotFound,
                                String routingCaseDestinationUnknown,
                                String routingCaseIdentityMissing,
                                String routingCaseLookupFailure)
    {
        this.name = name;
        this.requesterNfType = requesterNfType != null ? requesterNfType : "SMF";  // Backward compatibility
        this.targetNfType = targetNfType != null ? targetNfType : "CHF"; // Backward compatibility
        this.nrfGroupName = nrfGroupName;
        this.identityType = identityType;
        this.requestTimeout = requestTimeout != null ? requestTimeout : 10000; // Backward compatibility
        this.identityVar = identityVar;
        this.result = result;

        if (routingCaseIdentityNotFound != null && !routingCaseIdentityNotFound.isEmpty())
        {
            this.routingCaseIdentityNotFound = routingCaseIdentityNotFound;
        }
        if (routingCaseDestinationUnknown != null && !routingCaseDestinationUnknown.isEmpty())
        {
            this.routingCaseDestinationUnknown = routingCaseDestinationUnknown;
        }
        if (routingCaseIdentityMissing != null && !routingCaseIdentityMissing.isEmpty())
        {
            this.routingCaseIdentityMissing = routingCaseIdentityMissing;
        }
        if (routingCaseLookupFailure != null && !routingCaseLookupFailure.isEmpty())
        {
            this.routingCaseLookupFailure = routingCaseLookupFailure;
        }
    }

    public ProxyActionSlfLookup(ProxyActionSlfLookup that)
    {
        this.name = that.name;
        this.requesterNfType = that.requesterNfType;
        this.targetNfType = that.targetNfType;
        this.nrfGroupName = that.nrfGroupName;
        this.identityType = that.identityType;
        this.requestTimeout = that.requestTimeout;
        this.result = that.result;
        this.identityVar = that.identityVar;

        if (that.routingCaseIdentityNotFound != null && !that.routingCaseIdentityNotFound.isEmpty())
        {
            this.routingCaseIdentityNotFound = that.routingCaseIdentityNotFound;
        }
        if (that.routingCaseDestinationUnknown != null && !that.routingCaseDestinationUnknown.isEmpty())
        {
            this.routingCaseDestinationUnknown = that.routingCaseDestinationUnknown;
        }
        if (that.routingCaseIdentityMissing != null && !that.routingCaseIdentityMissing.isEmpty())
        {
            this.routingCaseIdentityMissing = that.routingCaseIdentityMissing;
        }
        if (that.routingCaseLookupFailure != null && !that.routingCaseLookupFailure.isEmpty())
        {
            this.routingCaseLookupFailure = that.routingCaseLookupFailure;
        }
    }

    /**
     * @return the routingCaseIdentityNotFound
     */
    public String getRoutingCaseIdentityNotFound()
    {
        return routingCaseIdentityNotFound;
    }

    /**
     * @param routingCaseIdentityNotFound the routingCaseIdentityNotFound to set
     */
    public void setRoutingCaseIdentityNotFound(String routingCaseIdentityNotFound)
    {
        if (routingCaseIdentityNotFound != null && !routingCaseIdentityNotFound.isEmpty())
        {
            this.routingCaseIdentityNotFound = routingCaseIdentityNotFound;
        }
    }

    /**
     * @return the routingCaseDestinationUnknown
     */
    public String getRoutingCaseDestinationUnknown()
    {
        return routingCaseDestinationUnknown;
    }

    /**
     * @param routingCaseDestinationUnknown the routingCaseDestinationUnknown to set
     */
    public void setRoutingCaseDestinationUnknown(String routingCaseDestinationUnknown)
    {
        if (routingCaseDestinationUnknown != null && !routingCaseDestinationUnknown.isEmpty())
        {
            this.routingCaseDestinationUnknown = routingCaseDestinationUnknown;
        }
    }

    /**
     * @return the routingCaseIdentityMissing
     */
    public String getRoutingCaseIdentityMissing()
    {
        return routingCaseIdentityMissing;
    }

    /**
     * @param routingCaseIdentityMissing the routingCaseIdentityMissing to set
     */
    public void setRoutingCaseIdentityMissing(String routingCaseIdentityMissing)
    {
        if (routingCaseIdentityMissing != null && !routingCaseIdentityMissing.isEmpty())
        {
            this.routingCaseIdentityMissing = routingCaseIdentityMissing;
        }
    }

    /**
     * @return the routingCaseLookupFailure
     */
    public String getRoutingCaseLookupFailure()
    {
        return routingCaseLookupFailure;
    }

    /**
     * @param routingCaseLookupFailure the routingCaseLookupFailure to set
     */
    public void setRoutingCaseLookupFailure(String routingCaseLookupFailure)
    {
        if (routingCaseLookupFailure != null && !routingCaseLookupFailure.isEmpty())
        {
            this.routingCaseLookupFailure = routingCaseLookupFailure;
        }
    }

    /**
     * @return the identityVar
     */
    public String getIdentityVar()
    {
        return identityVar;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the NRF Group Reference
     */
    public String getNrfGroupName()
    {
        return nrfGroupName;
    }

    /**
     * @return the requesterNfType
     */
    public String getRequesterNfType()
    {
        return requesterNfType;
    }

    /**
     * @return the requesterNfType
     */
    public String getTargetNfType()
    {
        return targetNfType;
    }

    /**
     * @return the identityType
     */
    public IdentityType getIdentityType()
    {
        return identityType;
    }

    /**
     * @return the requestTimeout
     */
    public int getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * @return the result
     */
    public String getResult()
    {
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionSlfLookup [name=" + name + ", nrfGroupName=" + nrfGroupName + ", requesterNfType=" + requesterNfType + ", targetNfType="
               + targetNfType + ", identityType=" + identityType + ", requestTimeout=" + requestTimeout + ", result=" + result
               + ", routingCaseIdentityNotFound=" + routingCaseIdentityNotFound + ", routingCaseDestinationUnknown=" + routingCaseDestinationUnknown
               + ", routingCaseIdentityMissing=" + routingCaseIdentityMissing + ", routingCaseLookupFailure=" + routingCaseLookupFailure + ", identityVar="
               + identityVar + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(identityType,
                            identityVar,
                            name,
                            nrfGroupName,
                            requestTimeout,
                            requesterNfType,
                            result,
                            routingCaseDestinationUnknown,
                            routingCaseIdentityMissing,
                            routingCaseIdentityNotFound,
                            routingCaseLookupFailure,
                            targetNfType);
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
        ProxyActionSlfLookup other = (ProxyActionSlfLookup) obj;
        return identityType == other.identityType && Objects.equals(identityVar, other.identityVar) && Objects.equals(name, other.name)
               && Objects.equals(nrfGroupName, other.nrfGroupName) && Objects.equals(requestTimeout, other.requestTimeout)
               && Objects.equals(requesterNfType, other.requesterNfType) && Objects.equals(result, other.result)
               && Objects.equals(routingCaseDestinationUnknown, other.routingCaseDestinationUnknown)
               && Objects.equals(routingCaseIdentityMissing, other.routingCaseIdentityMissing)
               && Objects.equals(routingCaseIdentityNotFound, other.routingCaseIdentityNotFound)
               && Objects.equals(routingCaseLookupFailure, other.routingCaseLookupFailure) && Objects.equals(targetNfType, other.targetNfType);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.sc.proxyal.proxyconfig.ProxyAction#buildAction()
     */
    @Override
    public Action buildAction()
    {
        var slfLookupBuilder = SlfLookupAction.newBuilder()
                                              .setClusterName(SLF_SERVICE_CLUSTERNAME)
                                              .setDestinationVariable(this.getResult())
                                              .setTimeout(this.getRequestTimeout())
                                              .setReqNfType(this.getRequesterNfType())
                                              .setTargetNfType(this.getTargetNfType());

        if (this.getNrfGroupName() != null)
        {
            slfLookupBuilder.setNrfGroupName(this.getNrfGroupName());
        }

        var idType = this.getIdentityType();

        if (idType == ProxyActionSlfLookup.IdentityType.SUPI)
        {
            slfLookupBuilder.setSupiVar(this.getIdentityVar());
        }
        else if (idType == ProxyActionSlfLookup.IdentityType.GPSI)
        {
            slfLookupBuilder.setGpsiVar(this.getIdentityVar());
        }

        slfLookupBuilder.setFcIdNotFound(this.getRoutingCaseIdentityNotFound());
        slfLookupBuilder.setFcDestUnknown(this.getRoutingCaseDestinationUnknown());
        slfLookupBuilder.setFcIdMissing(this.getRoutingCaseIdentityMissing());
        slfLookupBuilder.setFcLookupFailure(this.getRoutingCaseLookupFailure());

        return Action.newBuilder().setActionSlfLookup(slfLookupBuilder.build()).build();
    }
}
