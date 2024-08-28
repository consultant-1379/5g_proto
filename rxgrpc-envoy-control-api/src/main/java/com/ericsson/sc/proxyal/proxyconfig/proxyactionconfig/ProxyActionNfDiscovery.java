/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 4, 2023
 *     Author: emavoni
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig;

import java.util.Objects;

import com.ericsson.utilities.common.Triplet;

import java.util.List;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KeyVarOrStringValuePair;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NfDiscoveryAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.OptDSelectNfOnPriority;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.StringList;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarOrString;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;

/**
 * 
 * This routing action is used to perform delegated NF discovery by querying the
 * NRF with the parameters from the received request. Envoy is configured
 * accordingly
 * 
 */
public class ProxyActionNfDiscovery implements ProxyAction
{
    private static final String NLF_SERVICE_CLUSTERNAME = "internal-nlf";
    private final String nrfGroup;
    private final List<String> useDiscoveryParameter;
    private final Boolean useAllDiscoveryParameters;

    // List of triplets to hold the discovery parameters to add
    // first: parameter name
    // second: true if third is value, false if third is variable name
    // third: parameter value or variable name
    private final List<Triplet<String, Boolean, String>> addDiscoveryParameter;

    private final Integer requestTimeout;
    private final IPFamily ipVersion;
    private final String selectedHostVarName;
    private final String nfSetidVarName;

    /**
     * @param nrfGroup
     * @param requestTimeout,
     * @param useDiscoveryParameter,
     * @param useAllDiscoveryParameters,
     * @param addDiscoveryParameter,
     * @param selectedHostVarName,
     * @param nfSetidVarName)
     * 
     */
    public ProxyActionNfDiscovery(String nrfGroup,
                                  Integer requestTimeout,
                                  IPFamily ipVersion,
                                  List<String> useDiscoveryParameter,
                                  Boolean useAllDiscoveryParameters,
                                  List<Triplet<String, Boolean, String>> addDiscoveryParameter,
                                  String selectedHostVarName,
                                  String nfSetidVarName)
    {
        this.nrfGroup = nrfGroup;
        this.useDiscoveryParameter = useDiscoveryParameter;
        this.useAllDiscoveryParameters = useAllDiscoveryParameters;
        this.addDiscoveryParameter = addDiscoveryParameter;
        this.requestTimeout = requestTimeout != null ? requestTimeout : 10000;
        this.selectedHostVarName = selectedHostVarName;
        this.nfSetidVarName = nfSetidVarName;
        this.ipVersion = ipVersion;
    }

    public ProxyActionNfDiscovery(ProxyActionNfDiscovery that)
    {
        this.nrfGroup = that.getNrfGroup();
        this.useDiscoveryParameter = that.getUseDiscoveryParameter();
        this.useAllDiscoveryParameters = that.getUseAllDiscoveryParameters();
        this.addDiscoveryParameter = that.getAddDiscoveryParameter();
        this.requestTimeout = that.getRequestTimeout();
        this.selectedHostVarName = that.getSelectedHostVarName();
        this.nfSetidVarName = that.getNfSetidVarName();
        this.ipVersion = that.getIpVersion();
    }

    /**
     * @return the nrfGroup
     */
    public String getNrfGroup()
    {
        return nrfGroup;
    }

    /**
     * @return the useDiscoveryParameter
     */
    public List<String> getUseDiscoveryParameter()
    {
        return useDiscoveryParameter;
    }

    /**
     * @return the useDAllDiscoveryParameters
     */
    public Boolean getUseAllDiscoveryParameters()
    {
        return useAllDiscoveryParameters;
    }

    /**
     * @return the addDiscoveryParameter
     */
    public List<Triplet<String, Boolean, String>> getAddDiscoveryParameter()
    {
        return addDiscoveryParameter;
    }

    /**
     * @return the requestTimeout
     */
    public int getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * @return the preferredIpVersion
     */
    public IPFamily getIpVersion()
    {
        return ipVersion;
    }

    /**
     * @return the preferredHostVarName
     */
    public String getSelectedHostVarName()
    {
        return selectedHostVarName;
    }

    /**
     * @return the nfSetidVarName
     */
    public String getNfSetidVarName()
    {
        return nfSetidVarName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionNfDiscovery [nrfGroup=" + nrfGroup + ", useDiscoveryParameter=" + useDiscoveryParameter + ", useAllDiscoveryParameters="
               + useAllDiscoveryParameters + ", requestTimeout=" + requestTimeout + ", ipVersion=" + ipVersion + ", preferredHostVarName=" + selectedHostVarName
               + ", nfSetidVarName=" + nfSetidVarName + ", addDiscoveryParameter=" + addDiscoveryParameter + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(nrfGroup,
                            useDiscoveryParameter,
                            useAllDiscoveryParameters,
                            addDiscoveryParameter,
                            requestTimeout,
                            ipVersion,
                            selectedHostVarName,
                            nfSetidVarName);
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

        ProxyActionNfDiscovery other = (ProxyActionNfDiscovery) obj;

        return Objects.equals(nrfGroup, other.nrfGroup) && Objects.equals(useDiscoveryParameter, other.useDiscoveryParameter)
               && Objects.equals(requestTimeout, other.requestTimeout) && Objects.equals(ipVersion, other.ipVersion)
               && Objects.equals(addDiscoveryParameter, other.addDiscoveryParameter) && Objects.equals(selectedHostVarName, other.selectedHostVarName)
               && Objects.equals(nfSetidVarName, other.nfSetidVarName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.proxyal.proxyconfig.ProxyAction#buildAction()
     */
    @Override
    public Action buildAction()
    {

        var nfDiscoveryBuilder = NfDiscoveryAction.newBuilder()
                                                  .setClusterName(NLF_SERVICE_CLUSTERNAME) // Name of the cluster to the reach the NLF
                                                  .setTimeout(this.getRequestTimeout()) // NRF/NLF query timeout in ms
                                                  .setIpVersion(this.getIpVersion()) // IP versions (IPv4 or IPv6 or both) to take into account while
                                                                                     // creating endpoints when FQDN is not present
                                                  .setNrfGroupName(this.getNrfGroup()); // name of the NRF-Group that shall handle this discovery

        // Variables needed in order to perform delegated discovery based on NF
        // priority.
        if (this.getNfSetidVarName() != null || this.getSelectedHostVarName() != null)
        {
            var OptDSelectNfOnPriorityBuilder = OptDSelectNfOnPriority.newBuilder();

            if (this.getNfSetidVarName() != null)
                OptDSelectNfOnPriorityBuilder.setVarNameNfSet(this.getNfSetidVarName());
            if (this.getSelectedHostVarName() != null)
                OptDSelectNfOnPriorityBuilder.setVarNamePreferredHost(this.getSelectedHostVarName());

            nfDiscoveryBuilder.setNfSelectionOnPriority(OptDSelectNfOnPriorityBuilder.build());
        }

        // Discovery parameters to add to the request to the NLF.
        this.getAddDiscoveryParameter()
            .stream()
            .forEach(entry -> nfDiscoveryBuilder.addAddParametersIfMissing(entry.getSecond().equals(Boolean.TRUE) ? KeyVarOrStringValuePair.newBuilder()

                                                                                                                                           .setKey(entry.getFirst())
                                                                                                                                           .setValue(VarOrString.newBuilder()
                                                                                                                                                                .setTermString(entry.getThird())
                                                                                                                                                                .build())
                                                                                                                                           .build()

                                                                                                                  : KeyVarOrStringValuePair.newBuilder()

                                                                                                                                           .setKey(entry.getFirst())
                                                                                                                                           .setValue(VarOrString.newBuilder()
                                                                                                                                                                .setTermVar(entry.getThird())
                                                                                                                                                                .build())
                                                                                                                                           .build()));

        // Flag to indicate if all received 3gpp-Sbi-Discovery-* parameters
        if (this.getUseAllDiscoveryParameters().equals(Boolean.TRUE))
            nfDiscoveryBuilder.setUseAllParameters(true);
        else
            nfDiscoveryBuilder.setUseAllParameters(false);

        // Envoy will use the listed parameters only. If the parameter is both in this
        // list and in the add_parameters_if_missing list, then the value from the
        // request is used
        if (this.getUseDiscoveryParameter() != null && !this.getUseDiscoveryParameter().isEmpty())
            nfDiscoveryBuilder.setUseParameters(StringList.newBuilder().addAllValues(this.getUseDiscoveryParameter()).build());

        return Action.newBuilder().setActionNfDiscovery(nfDiscoveryBuilder.build()).build();
    }

}
