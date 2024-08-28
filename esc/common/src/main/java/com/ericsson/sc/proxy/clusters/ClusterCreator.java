/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy.clusters;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfDnsProfile;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.endpoints.EndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.sepp.model.AsymmetricKey;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;

public abstract class ClusterCreator
{
    private static final Logger log = LoggerFactory.getLogger(ClusterCreator.class);
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME") + "-sepp-extcert-";

    protected final IfNfPool pool;
    protected final IfNfInstance configInst;
    protected EndpointCollector endpointCollector;
    protected String suffix;
    protected ProxyCluster cluster;
    protected String altStatPoolName;

    /**
     * Basic ClusterCreator constructor. All child classes of the cluster creator
     * are responsible of calling their implementations of
     * {@link #generateAltStatName() GenerateAltStatName()} and
     * {@link #generateSuffix() GenerateSuffix()} to avoid invocations of
     * overridable methods
     * 
     * @param pool              the nf-pool for which the ProxyCluster will be made
     * @param configInst        the configuration instance
     * @param endpointCollector The @see EndpointCollector containing the endpoints
     *                          for this cluster
     */
    protected ClusterCreator(IfNfPool pool,
                             IfNfInstance configInst,
                             EndpointCollector endpointCollector)
    {
        this.pool = pool;
        this.configInst = configInst;
        this.endpointCollector = endpointCollector;

    }

    /**
     * This constructor is only used by the @see AggregateClusterCreator and @see
     * DfwClusterCreator where endpoint collectors make no sense
     * 
     * @param pool       the nf-pool for which the ProxyCluster will be made
     * @param configInst the configuration instance
     */
    protected ClusterCreator(IfNfPool pool,
                             IfNfInstance configInst)
    {
        this.pool = pool;
        this.configInst = configInst;
        this.endpointCollector = null;
    }

    /**
     * @return the pool
     */
    public IfNfPool getPool()
    {
        return this.pool;
    }

    /**
     * @return the suffix
     */
    public String getSuffix()
    {
        return suffix;
    }

    /**
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    /**
     * Create the ProxyCluster. Needs to be called before {@link #getCluster()}. The
     * endpoints have to be collected by calling first
     * {@link EndpointCollector#createEndpoints()}
     */
    public abstract void createCluster();

    /**
     * Generate the instance name for cluster counters.
     *
     * @return
     */
    public abstract String generateAltStatName();

    /**
     * Generate a cluster suffix to differentiate different clusters for the same
     * pool. Depends on the cluster type.
     *
     * @return String suffix
     */
    public abstract String generateSuffix();

    /**
     * Get the generated ProxyCluster. Need to call
     * {@link ClusterCreator#createCluster()} first.
     * 
     * @return
     */
    public ProxyCluster getCluster()
    {
        return this.cluster;
    }

    /**
     * To avoid cluster duplicates, every cluster creator must implement this
     * function, called from Egress
     * 
     * @param clusters A set containing all the already created clusters from
     *                 processing previous rules
     */
    public abstract void appendClusters(Set<ProxyCluster> clusters);

    /**
     * Configure ProxyCluster with the configured Egress Connection Profile
     * referenced by the pool.
     * 
     * @param cluster
     */
    protected void addEgressConnectionProfile(ProxyCluster cluster)
    {
        var epc = CommonConfigUtils.getReferencedEgressConnectionProfile(this.pool, this.configInst);
        CommonConfigUtils.setHpackTableSize(cluster, epc);
        CommonConfigUtils.setMaxConcurrentStreams(cluster, epc);
        CommonConfigUtils.setMaxConnectionDuration(cluster, epc);
        CommonConfigUtils.setTcpConnectTimeout(cluster, epc);

        CommonConfigUtils.setPoolRetryBudgetForCluster(pool, cluster);
        CommonConfigUtils.setCircuitBreakerForCluster(cluster, epc);
        CommonConfigUtils.setTcpKeepalive(cluster, epc);
        CommonConfigUtils.setIdleTimeout(cluster, epc);
        CommonConfigUtils.setTrackClusterStats(cluster, epc);
        CommonConfigUtils.setDscpMarking(cluster, epc);
    }

    /**
     * Add ProxyTls configuration to the cluster. SCP adds an empty/default
     * ProxyTls. SEPP has different ProxyTls for NFs and RPs
     * 
     * @param cluster
     */
    protected void addTlsConfiguration(ProxyCluster cluster)
    {
        if (CommonConfigUtils.isMutualTlsEnabledForCluster(endpointCollector.getEndpoints()))
        {
            if (ConfigHelper.isScpConfiguration(configInst))
            {
                var tls = new ProxyTls();
                cluster.setTls(tls);
            }
            else
            {
                createTlsForSeppCluster(cluster, (com.ericsson.sc.sepp.model.NfPool) pool, ConfigHelper.convertToSeppConfiguration(configInst));
            }
        }
    }

    protected Optional<String> getAsymetricKeyForSvcAddress(final NfInstance seppInst,
                                                            final ServiceAddress svcAddress)
    {
        AtomicReference<Optional<String>> asymmetricKeyString = new AtomicReference<>(Optional.empty());

        Optional.ofNullable(Utils.getByName(seppInst.getAsymKeyList(), svcAddress.getAsymKeyInRef())).ifPresentOrElse(akIn ->
        {
            asymmetricKeyString.set(Optional.of(CR_PREFIX + akIn.getAsymmetricKey() + "-" + akIn.getCertificate() + "-certificate"));

        }, () ->
        {
            Optional<AsymmetricKey> asymKey = Optional.ofNullable(Utils.getByName(seppInst.getAsymmetricKey(), svcAddress.getAsymmetricKeyRef()));
            asymKey.ifPresent(ak ->
            {
                var asymKeyJoiner = new StringJoiner("#!_#");
                asymmetricKeyString.set(Optional.of(asymKeyJoiner.add(ak.getPrivateKey()).add(ak.getCertificate()).toString()));
            });
        });

        return asymmetricKeyString.get();
    }

    protected Optional<String> getTrustedCaList(final NfInstance seppInst,
                                                final RoamingPartner rp)
    {
        AtomicReference<Optional<String>> trustedCaListString = new AtomicReference<>(Optional.empty());

        Optional.ofNullable(Utils.getByName(seppInst.getTrustedCertList(), rp.getTrustedCertInListRef())).ifPresentOrElse(tcInList ->
        {
            trustedCaListString.set(Optional.of(CR_PREFIX + tcInList.getTrustedCertListRef() + "-ca-certificate"));

        }, () -> Optional.ofNullable(rp.getTrustedCertificateList()).ifPresent(tcaList -> trustedCaListString.set(Optional.of(tcaList))));

        return trustedCaListString.get();
    }

    protected static Optional<String> getTrustedCaList(final NfInstance seppInst,
                                                       final OwnNetwork own)
    {
        AtomicReference<Optional<String>> trustedCaListString = new AtomicReference<>(Optional.empty());

        Optional.ofNullable(Utils.getByName(seppInst.getTrustedCertList(), own.getTrustedCertInListRef())).ifPresentOrElse(tcInList ->
        {
            trustedCaListString.set(Optional.of(CR_PREFIX + tcInList.getTrustedCertListRef() + "-ca-certificate"));

        }, () -> Optional.ofNullable(own.getTrustedCertificateList()).ifPresent(tcaList -> trustedCaListString.set(Optional.of(tcaList))));

        return trustedCaListString.get();
    }

    protected void createTlsForSeppCluster(ProxyCluster cluster,
                                           final NfPool pool,
                                           final NfInstance seppInst)
    {
        Optional.ofNullable(pool.getOwnNetworkRef()) //
                .map(nwRef -> Utils.getByName(seppInst.getOwnNetwork(), pool.getOwnNetworkRef()))
                .ifPresentOrElse(ownNw -> getTrustedCaList(seppInst, ownNw).ifPresent(tcaList -> Optional
                                                                                                         .ofNullable(Utils.getByName(seppInst.getServiceAddress(),
                                                                                                                                     ownNw.getServiceAddressRef()))
                                                                                                         .map(svcAddress -> getAsymetricKeyForSvcAddress(seppInst,
                                                                                                                                                         svcAddress))
                                                                                                         .filter(Optional::isPresent)
                                                                                                         .map(asymKey -> new ProxyTls(tcaList, asymKey.get()))
                                                                                                         .ifPresent(cluster::setTls)),
                                 () -> Optional.ofNullable(pool.getRoamingPartnerRef())
                                               .map(rpRef -> Utils.getByName(getAllRoamingPartners(seppInst.getExternalNetwork()), rpRef)) //
                                               .ifPresent(rp -> getTrustedCaList(seppInst, rp).ifPresent(tcaListRp ->
                                               {
                                                   var extNw = fetchNetworkForRp(seppInst.getExternalNetwork(), rp.getName());
                                                   extNw.ifPresent(nw -> Optional.ofNullable(Utils.getByName(seppInst.getServiceAddress(),
                                                                                                             nw.getServiceAddressRef())) //
                                                                                 .map(svcAddress -> getAsymetricKeyForSvcAddress(seppInst, svcAddress))
                                                                                 .filter(Optional::isPresent)
                                                                                 .map(asymKey -> new ProxyTls(tcaListRp, asymKey.get()))
                                                                                 .ifPresent(cluster::setTls));
                                               })));
    }

    protected List<RoamingPartner> getAllRoamingPartners(final List<ExternalNetwork> extNws)
    {
        return extNws.stream().flatMap(nw -> nw.getRoamingPartner().stream()).toList();
    }

    public static Optional<ExternalNetwork> fetchNetworkForRp(List<ExternalNetwork> networks,
                                                              String rpName)
    {
        return networks.stream().filter(on -> on.getRoamingPartner().stream().anyMatch(rp -> rp.getName().equals(rpName))).findFirst();
    }

    /**
     * Set vtap settings for the cluster. If there is allNfPools and the vtap is
     * enabled for that egress, then the cluster is setted with the name and vtap
     * enabled of that egress. On the other hand if the vtap is enabled for at least
     * one of the pools (pool or lastResortPool), then the cluster has the vtap
     * enabled.
     *
     * @param proxyCluster
     * @return
     */
    public void setClusterVtapSettings(ProxyCluster proxyCluster)
    {
        Optional<ProxyVtapSettings> vtapSettings = ConfigHelper.createVtapConfigForPool(this.pool.getName(), this.configInst);

        if (vtapSettings.isPresent() && vtapSettings.get().getVtapEnabled())
        {
            log.debug("Cluster of pool {} has vtap settings {}", this.pool.getName(), vtapSettings);
            proxyCluster.setVtapSettings(vtapSettings);
            return;
        }

        log.debug("Cluster of pool {} has no vtap settings", this.pool.getName());
    }

    /**
     * @return The IP families configured for the cluster or an empty set. The
     *         ProxyCfgMapper will then take care of setting the default IP families
     *         which are obtained from the configured service addresses.
     */
    protected Set<IpFamily> getClusterIpFamilies()
    {
        return Optional.<IfDnsProfile>ofNullable(Utils.getByName(List.copyOf(this.configInst.getDnsProfile()),
                                                                 Optional.ofNullable(this.pool.getDnsProfileRef()).orElse(this.configInst.getDnsProfileRef())))
                       .map(dnsProfile -> dnsProfile.getIpFamilyResolution().stream().map(r -> IpFamily.fromValue(r.value())).collect(Collectors.toSet()))
                       .orElse(new HashSet<>()); // ProxyCfgMappyer is taking care of setting the default IP families
    }
}
