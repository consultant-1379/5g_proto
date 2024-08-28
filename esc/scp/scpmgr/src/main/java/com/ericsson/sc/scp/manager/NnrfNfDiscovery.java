/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 6, 2020
 *     Author: eedstl
 */

package com.ericsson.sc.scp.manager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFService;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpEndPoint;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpDomainInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.TransportProtocol;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UriScheme;
import com.ericsson.sc.expressionparser.NfConditionParser;
import com.ericsson.sc.expressionparser.ScpConditionParser;
import com.ericsson.sc.glue.IfDiscoveredNfInstance;
import com.ericsson.sc.glue.IfDiscoveredScpInstance;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedScpInstance;
import com.ericsson.sc.nfm.model.NfStatus;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.Transport;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext.PollingQuery;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext.TargetedQuery;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoveryAlarmHandler;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoveryValidator;
import com.ericsson.sc.scp.model.Address;
import com.ericsson.sc.scp.model.DiscoveredNfInstance;
import com.ericsson.sc.scp.model.DiscoveredNfService;
import com.ericsson.sc.scp.model.DiscoveredScpDomainInfo;
import com.ericsson.sc.scp.model.DiscoveredScpInstance;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.MultipleIpEndpoint;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.NrfQuery;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.json.Json.Patch;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Encapsulates all handling for NnrfNfDiscovery.
 */
public class NnrfNfDiscovery
{
    private static class UserContext
    {
        public static UserContext of(final Optional<EricssonScp> currConfig,
                                     final Map<String, Pair<String, String>> filterPerPool)
        {
            return new UserContext(currConfig, filterPerPool);
        }

        private final Map<String, Pair<String, String>> filterPerPool;

        private Optional<EricssonScp> currConfig;
        private Optional<EricssonScp> nextConfig;

        private UserContext(final Optional<EricssonScp> currConfig,
                            final Map<String, Pair<String, String>> filterPerPool)
        {
            this.filterPerPool = filterPerPool;
            this.currConfig = currConfig;
            this.nextConfig = Optional.empty();
        }

        public void applyNextConfig()
        {
            log.debug("Applying {}", this.nextConfig);
            this.currConfig = this.nextConfig;
            this.nextConfig = Optional.empty();
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
                return true;

            if (!(other instanceof UserContext))
                return false;

            final UserContext rhs = ((UserContext) other);
            return Objects.equals(this.filterPerPool, rhs.filterPerPool) && Objects.equals(this.currConfig, rhs.currConfig);
        }

        public Optional<EricssonScp> getCurrConfig()
        {
            return this.currConfig;
        }

        @Override
        public int hashCode()
        {
            int result = 1;
            result = result * 31 + (this.filterPerPool == null ? 0 : this.filterPerPool.hashCode());
            return result;
        }

        // This method is applicable only for discovered NF instances with locality
        // parameter.
        public boolean isMemberOfPool(final String pool,
                                      final IfDiscoveredNfInstance discoveredNfInstance)
        {
            // Extract the nf-match-condition from the input nf-pool
            final String filter = this.filterPerPool.get(pool).getFirst();
            Stream<IfTypedNfInstance> test = Stream.of(discoveredNfInstance);
            Set<String> selectedSvcNames = new HashSet<>();

            // The first condition checks the input pool is valid.
            // The second condition checks the discovered_nf_instance matchs the
            // nf-match-condition or not.
            return filter != null && !filter.isEmpty() ? !(NfConditionParser.parse(filter, test, selectedSvcNames)).isEmpty() : true;
        }

        // This method is applicable only for discovered SCP instances with
        // scp-match-condition parameters
        public boolean isMemberOfPool(final String pool,
                                      final IfDiscoveredScpInstance discoveredNfInstance)
        {
            // Extract the scp-match-condition from the input nf-pool
            final String filter = this.filterPerPool.get(pool).getSecond();
            Stream<IfTypedScpInstance> test = Stream.of(discoveredNfInstance);

            // The first condition checks the input pool is valid.
            // The second condition checks the discovered_nf_instance matchs the
            // nf-match-condition or not.
            return filter != null && !filter.isEmpty() ? !(ScpConditionParser.filterScpDomains(filter, test)).isEmpty() : true;
        }

        public UserContext setNextConfig(final Optional<EricssonScp> nextConfig)
        {
            this.nextConfig = nextConfig;
            return this;
        }
    }

    private static final int DFLT_PORT_HTTP = 80;
    private static final int DFLT_PORT_HTTPS = 443;

    private static final String ENV_CM_MEDIATOR = "CM_MEDIATOR";
    private static final Boolean ENV_GLOBAL_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));

    private static final String JP_DISCOVERED_NF_OR_SCP_INSTANCE = "/discovered-(nf|scp)-instance";
    private static final String JP_DISCOVERED_NF_INSTANCE = "/discovered-nf-instance";
    private static final String JP_DISCOVERED_SCP_INSTANCE = "/discovered-scp-instance";
    private static final String JP_LAST_UPDATE = "/last-update";
    private static final String JP_POOL_DISCOVERY = "/nf-pool-discovery";

    private static final String QRY_REQUESTER_NF_TYPE = "requester-nf-type";
    private static final String QRY_REQUESTER_PLMN_LIST = "requester-plmn-list";
    private static final String QRY_REQUESTER_SNSSAIS = "requester-snssais";
    private static final String QRY_PREFERRED_LOCALITY = "preferred-locality";
    private static final String QRY_SCP_DOMAIN_LIST = "scp-domain-list";
    private static final String QRY_SERVICE_NAMES = "service-names";
    private static final String QRY_TARGET_NF_SERVICE_SET_ID = "target-nf-service-set-id";
    private static final String QRY_TARGET_NF_SET_ID = "target-nf-set-id";
    private static final String QRY_TARGET_NF_TYPE = "target-nf-type";

    private static final Logger log = LoggerFactory.getLogger(NnrfNfDiscovery.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    public static SearchingContext mapConfigToInput(final Optional<EricssonScp> o)
    {
        return o.map(config ->
        {
            final String JSON_BASE = "/ericsson-scp:scp-function/nf-instance/0";
            final Map<String, Set<PollingQuery>> queriesPerPool = new HashMap<>();
            final Map<String, Pair<String, String>> filterPerPool = new HashMap<>();

            final AtomicInteger poolIndex = new AtomicInteger();

            final List<String> nrfGroups = getNrfGroupsForDiscovery(config);

            getPools(config).forEach(pool ->
            {
                final int i = poolIndex.getAndIncrement();
                final AtomicInteger poolDiscoveryIndex = new AtomicInteger();

                pool.getNfPoolDiscovery().forEach(pd ->
                {
                    final int j = poolDiscoveryIndex.getAndIncrement();
                    final Set<PollingQuery> queries = pd.getNrfQuery()
                                                        .stream()
                                                        .distinct()
                                                        .flatMap(query -> (query.getNrfGroupRef()
                                                                                .isEmpty() ? (pd.getNrfGroupRef().isEmpty() ? nrfGroups : pd.getNrfGroupRef())
                                                                                           : query.getNrfGroupRef()).stream()
                                                                                                                    .distinct()
                                                                                                                    .map(group -> PollingQuery.of(TargetedQuery.of(convertToQuery(query),
                                                                                                                                                                   group),
                                                                                                                                                  pd.getUpdateInterval()
                                                                                                                                                    .seconds())))
                                                        .collect(Collectors.toSet());
                    final String key = new StringBuilder(JSON_BASE).append("/nf-pool/").append(i).append("/nf-pool-discovery/").append(j).toString();
                    queriesPerPool.put(key, queries);
                    filterPerPool.put(key, Pair.of(pool.getNfMatchCondition(), pool.getScpMatchCondition()));
                });
            });

            log.debug("queriesPerPool={}", queriesPerPool);
            log.debug("filterPerPool={}", filterPerPool);

            return SearchingContext.of(Optional.of(queriesPerPool), Optional.of(UserContext.of(o, filterPerPool)));
        }).orElse(SearchingContext.empty());
    }

    private static List<Patch> addLastUpdateTimeStamp(final List<Json.Patch> patches)
    {
        final String lastUpdate = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return Stream.concat(patches.stream(),
                             patches.stream()
                                    .filter(p -> p.getPath().contains(JP_POOL_DISCOVERY))
                                    .map(p -> p.getPath().replaceAll(new StringBuilder(JP_DISCOVERED_NF_OR_SCP_INSTANCE).append(".*").toString(), ""))
                                    .distinct()
                                    .map(p -> Json.Patch.of()
                                                        .op(Json.Patch.Operation.ADD)
                                                        .path(new StringBuilder(p).append(JP_LAST_UPDATE).toString())
                                                        .value(lastUpdate)))
                     .collect(Collectors.toList());
    }

    private static Query convertToQuery(final NrfQuery query)
    {
        final Query.Builder b = new Query.Builder();

        if (query.getRequesterNfType() != null)
            b.add(QRY_REQUESTER_NF_TYPE, query.getRequesterNfType().toUpperCase());
        else
            b.add(QRY_REQUESTER_NF_TYPE, NFType.SCP);

        if (query.getNfType() != null)
            b.add(QRY_TARGET_NF_TYPE, query.getNfType().toUpperCase());

        if (query.getNfSetId() != null)
            b.add(QRY_TARGET_NF_SET_ID, query.getNfSetId());

        if (query.getNfServiceSetId() != null)
            b.add(QRY_TARGET_NF_SERVICE_SET_ID, query.getNfServiceSetId());

        if (query.getPreferredLocality() != null)
            b.add(QRY_PREFERRED_LOCALITY, query.getPreferredLocality());

        if (query.getScpDomain() != null && !query.getScpDomain().isEmpty())
            b.add(QRY_SCP_DOMAIN_LIST, String.join(",", query.getScpDomain()));

        if (query.getRequesterPlmn() != null && !query.getRequesterPlmn().isEmpty())
        {
            try
            {
                b.add(QRY_REQUESTER_PLMN_LIST,
                      URLEncoder.encode(json.writeValueAsString(query.getRequesterPlmn()
                                                                     .stream()
                                                                     .map(r -> new PlmnId().mcc(r.substring(0, 3)).mnc(r.substring(3)))
                                                                     .collect(Collectors.toSet())),
                                        "UTF-8")
                                .replaceAll("\\+", "%20"));
            }
            catch (JsonProcessingException | UnsupportedEncodingException e)
            {
                log.error("Ignoring invalid query parameter {}='{}'", QRY_REQUESTER_PLMN_LIST, query.getRequesterPlmn().stream().toList());
            }
        }

        if (query.getRequesterSnssai() != null && !query.getRequesterSnssai().isEmpty())
        {
            try
            {
                // Query parameter requester-snssai needs special treatment as property sd has
                // value "-" if undefined. This needs to be turned into null before it is sent
                // to the NRF.
                b.add(QRY_REQUESTER_SNSSAIS,
                      URLEncoder.encode(json.writeValueAsString(query.getRequesterSnssai()
                                                                     .stream()
                                                                     // As the model reflects only the R16 properties of ExtSnssai, i.e. those of
                                                                     // Snssai, set only those.
                                                                     .map(r -> new ExtSnssai().sst(r.getSst()).sd(r.getSd().equals("-") ? null : r.getSd()))
                                                                     .collect(Collectors.toSet())),
                                        "UTF-8")
                                .replaceAll("\\+", "%20"));
            }
            catch (JsonProcessingException | UnsupportedEncodingException e)
            {
                log.error("Ignoring invalid query parameter {}='{}'", QRY_REQUESTER_SNSSAIS, query.getRequesterSnssai().stream().toList());
            }
        }

        if (query.getServiceName() != null && !query.getServiceName().isEmpty())
            b.add(QRY_SERVICE_NAMES, String.join(",", query.getServiceName()));

        if (query.getQueryParameter() != null)
        {
            query.getQueryParameter().forEach(qp ->
            {
                try
                {
                    b.add(qp.getName(), URLEncoder.encode(qp.getValue(), "UTF-8").replaceAll("\\+", "%20"));
                }
                catch (UnsupportedEncodingException e)
                {
                    log.error("Ignoring invalid query parameter {}='{}'", qp.getName(), qp.getValue());
                }
            });
        }

        return b.build();
    }

    private static DiscoveredNfService createDefaultNfService(final int capacity,
                                                              final int priority,
                                                              final String fqdn,
                                                              final List<MultipleIpEndpoint> endpoints,
                                                              final String scheme)
    {
        final Address address = new Address().withFqdn(fqdn).withScheme(Scheme.fromValue(scheme)).withMultipleIpEndpoint(endpoints);
        final String serviceName = (scheme.equals(UriScheme.HTTPS) ? "default-tls" : "default-non-tls");

        return new DiscoveredNfService().withNfServiceId(serviceName).withCapacity(capacity).withPriority(priority).withName(serviceName).withAddress(address);
    }

    private static DiscoveredNfService createDefaultNfService(final NFProfile profile,
                                                              final int port,
                                                              final String scheme)
    {
        return createDefaultNfService(profile.getCapacity(),
                                      profile.getPriority(),
                                      profile.getFqdn(),
                                      List.of(createMultiIpEndpoint(port, profile.getIpv4Addresses(), profile.getIpv6Addresses())),
                                      scheme);
    }

    private static MultipleIpEndpoint createMultiIpEndpoint(final int port,
                                                            final List<String> ipv4Addrs,
                                                            final List<String> ipv6Addrs)
    {
        final String epName = new StringBuilder(Transport.TCP.name()).append("-").append(port).toString(); // Example: "TCP-80"
        return new MultipleIpEndpoint().withName(epName)
                                       .withTransport(Transport.TCP)
                                       .withPort(port)
                                       .withIpv4Address(ipv4Addrs != null ? ipv4Addrs : new ArrayList<>())
                                       .withIpv6Address(ipv6Addrs != null ? ipv6Addrs : new ArrayList<>());
    }

    private static IfDiscoveredNfInstance createNfInstance(final NFProfile profile)
    {
        return new DiscoveredNfInstance().withName(profile.getNfInstanceName() != null ? profile.getNfInstanceName() : profile.getNfInstanceId().toString())
                                         .withNfInstanceId(profile.getNfInstanceId().toString())
                                         .withNfType(profile.getNfType())
                                         .withNfStatus(NfStatus.valueOf(profile.getNfStatus()))
                                         .withLocality(profile.getLocality())
                                         .withNfSetId(profile.getNfSetIdList() != null ? profile.getNfSetIdList() : new ArrayList<>())
                                         .withScpDomain(profile.getScpDomains() != null ? profile.getScpDomains() : new ArrayList<>())
                                         .withDiscoveredNfService(createNfServiceList(profile));
    }

    private static DiscoveredNfService createNfService(final NFProfile profile,
                                                       final NFService service)
    {
        final List<MultipleIpEndpoint> endpoints = new ArrayList<>();

        final SortedMap<String, Set<String>> ipsv4ByEp = new TreeMap<>();
        final SortedMap<String, Set<String>> ipsv6ByEp = new TreeMap<>();

        if (service.getIpEndPoints() != null)
        {
            // Collect all IP addresses per scheme-port:

            service.getIpEndPoints().forEach(ep ->
            {
                final Integer port = ep.getPort() != null ? ep.getPort() : service.getScheme().equals(UriScheme.HTTPS) ? DFLT_PORT_HTTPS : DFLT_PORT_HTTP;
                final String transport = ep.getTransport() != null ? ep.getTransport() : TransportProtocol.TCP;
                final String epName = new StringBuilder(transport).append("-").append(port).toString(); // Example: "TCP-80"

                ipsv4ByEp.computeIfAbsent(epName, v -> new HashSet<>()).add(ep.getIpv4Address() != null ? ep.getIpv4Address() : "");
                ipsv6ByEp.computeIfAbsent(epName, v -> new HashSet<>()).add(ep.getIpv6Address() != null ? ep.getIpv6Address() : "");
            });
        }

        // Note that ipsv4ByEp and ipsv6ByEp have content for the same scheme-port (the
        // set of keys is equal).
        // Hence, by looping over ipsv4ByEp also all contents of ipsv6ByEp may be
        // addressed:

        ipsv4ByEp.keySet().forEach(epName ->
        {
            // Remove empty IP addresses (i.e. no IP addresses were configured on service
            // level). If thereafter the list is empty, take those defined on profile level:

            ipsv4ByEp.get(epName).remove("");
            ipsv6ByEp.get(epName).remove("");
            List<String> ipsv4 = new ArrayList<>(ipsv4ByEp.get(epName));
            List<String> ipsv6 = new ArrayList<>(ipsv6ByEp.get(epName));

            if (ipsv4.isEmpty() && profile.getIpv4Addresses() != null)
                ipsv4 = profile.getIpv4Addresses();

            if (ipsv6.isEmpty() && profile.getIpv6Addresses() != null)
                ipsv6 = profile.getIpv6Addresses();

            endpoints.add(new MultipleIpEndpoint().withName(epName)
                                                  .withPort(Integer.parseInt(epName.split("-")[1]))
                                                  .withTransport(Transport.valueOf(epName.split("-")[0])) // Example: "TCP" -> valueOf()
                                                  .withIpv4Address(ipsv4)
                                                  .withIpv6Address(ipsv6));
        });

        if (endpoints.isEmpty())
        {
            // Create an endpoint for the default port and with the IP addresses defined on
            // profile level:

            final Integer port = service.getScheme().equals(UriScheme.HTTPS) ? DFLT_PORT_HTTPS : DFLT_PORT_HTTP;
            final String transport = TransportProtocol.TCP;
            final String epName = new StringBuilder(transport).append("-").append(port).toString(); // Example: "TCP-80"

            endpoints.add(new MultipleIpEndpoint().withName(epName)
                                                  .withPort(Integer.parseInt(epName.split("-")[1]))
                                                  .withTransport(Transport.valueOf(epName.split("-")[0])) // Example: "TCP" -> valueOf()
                                                  .withIpv4Address(Optional.ofNullable(profile.getIpv4Addresses()).orElse(new ArrayList<>()))
                                                  .withIpv6Address(Optional.ofNullable(profile.getIpv6Addresses()).orElse(new ArrayList<>())));
        }

        final Address address = new Address().withFqdn(service.getFqdn() != null ? service.getFqdn() : profile.getFqdn())
                                             .withInterPlmnFqdn(service.getInterPlmnFqdn() != null ? service.getInterPlmnFqdn() : profile.getInterPlmnFqdn())
                                             .withScheme(Scheme.fromValue(service.getScheme()))
                                             .withMultipleIpEndpoint(endpoints);

        return new DiscoveredNfService().withNfServiceId(service.getServiceInstanceId())
                                        .withCapacity(service.getCapacity() != null ? service.getCapacity() : profile.getCapacity())
                                        .withPriority(service.getPriority() != null ? service.getPriority() : profile.getPriority())
                                        .withName(service.getServiceName())
                                        .withStatus(NfStatus.valueOf(service.getNfServiceStatus()))
                                        .withSetId(service.getNfServiceSetIdList() != null ? service.getNfServiceSetIdList() : new ArrayList<>())
                                        .withApiPrefix(service.getApiPrefix())
                                        .withAddress(address);
    }

    private static DiscoveredScpDomainInfo createScpDomainInfo(final int capacity,
                                                               final int priority,
                                                               final String fqdn,
                                                               final List<MultipleIpEndpoint> endpoints,
                                                               final String scheme,
                                                               final String scpDomain)
    {
        final Address address = new Address().withFqdn(fqdn).withScheme(Scheme.fromValue(scheme)).withMultipleIpEndpoint(endpoints);
        final String name = (scpDomain != null ? scpDomain : "default") + (scheme.equals(UriScheme.HTTPS) ? "-tls" : "-non-tls");

        return new DiscoveredScpDomainInfo().withName(name).withDomain(scpDomain).withCapacity(capacity).withPriority(priority).withAddress(address);
    }

    private static DiscoveredScpDomainInfo createScpDomainInfo(final NFProfile profile,
                                                               final int port,
                                                               final String scheme,
                                                               final String domain)
    {
        return createScpDomainInfo(profile.getCapacity(),
                                   profile.getPriority(),
                                   profile.getFqdn(),
                                   List.of(createMultiIpEndpoint(port, profile.getIpv4Addresses(), profile.getIpv6Addresses())),
                                   scheme,
                                   domain);
    }

    private static IfDiscoveredScpInstance createScpInstance(final NFProfile profile)
    {
        return new DiscoveredScpInstance().withName(profile.getNfInstanceName() != null ? profile.getNfInstanceName() : profile.getNfInstanceId().toString())
                                          .withNfInstanceId(profile.getNfInstanceId().toString())
                                          .withNfType(profile.getNfType())
                                          .withNfStatus(NfStatus.valueOf(profile.getNfStatus()))
                                          .withLocality(profile.getLocality())
                                          .withNfSetId(profile.getNfSetIdList() != null ? profile.getNfSetIdList() : new ArrayList<>())
                                          .withScpDomain(profile.getScpDomains() != null ? profile.getScpDomains() : new ArrayList<>())
                                          .withServedNfSetId(profile.getScpInfo() != null
                                                             && profile.getScpInfo().getServedNfSetIdList() != null
                                                                                                                    ? profile.getScpInfo()
                                                                                                                             .getServedNfSetIdList()
                                                                                                                    : new ArrayList<>())
                                          .withDiscoveredScpDomainInfo(createScpDomainInfoList(profile));
    }

    /**
     * Find and return the sorted set of discovered NF instances pointed to by
     * jsonPath in the configuration passed in config.
     * 
     * @param jsonPath Example:
     *                 "/ericsson-scp:scp-function/nf-instance/0/nf-pool/4/nf-pool-discovery/1/discovered-nf-instance"
     * @param config   The configuration where to search for the discovered NF
     *                 instances.
     * @return The sorted set of discovered NF instances.
     */
    private static SortedSet<DiscoveredNfInstance> getDiscoveredNfInstances(final String jsonPath,
                                                                            final EricssonScp config)
    {
        // The structure of the jsonPath always looks the same, i.e., a simple split on
        // '/' is good enough, then take the indices of nf-instance, nf-pool, and
        // nf-pool-discovery (always at the same position) to locate the wanted list of
        // discovered NF instances.

        final String[] tokens = jsonPath.split("/");

        if (tokens.length > 8)
        {
            final int iNfInstance = Integer.parseInt(tokens[3]);
            final int iNfPool = Integer.parseInt(tokens[5]);
            final int iNfPoolDiscovery = Integer.parseInt(tokens[7]);

            if (config.getEricssonScpScpFunction() != null && config.getEricssonScpScpFunction().getNfInstance().size() > iNfInstance)
            {
                final NfInstance nfInstance = config.getEricssonScpScpFunction().getNfInstance().get(iNfInstance);

                if (nfInstance.getNfPool().size() > iNfPool)
                {
                    final NfPool nfPool = nfInstance.getNfPool().get(iNfPool);

                    if (nfPool.getNfPoolDiscovery().size() > iNfPoolDiscovery)
                    {
                        return nfPool.getNfPoolDiscovery()
                                     .get(iNfPoolDiscovery)
                                     .getDiscoveredNfInstance()
                                     .stream()
                                     .collect(Collectors.toCollection(TreeSet::new));
                    }
                }
            }
        }

        return new TreeSet<>();
    }

    /**
     * Find and return the sorted set of discovered SCP instances pointed to by
     * jsonPath in the configuration passed in config.
     * 
     * @param jsonPath Example:
     *                 "/ericsson-scp:scp-function/nf-instance/0/nf-pool/4/nf-pool-discovery/1/discovered-scp-instance"
     * @param config   The configuration where to search for the discovered SCP
     *                 instances.
     * @return The sorted set of discovered SCP instances.
     */
    private static SortedSet<DiscoveredScpInstance> getDiscoveredScpInstances(final String jsonPath,
                                                                              final EricssonScp config)
    {
        // The structure of the jsonPath always looks the same, i.e., a simple split on
        // '/' is good enough, then take the indices of nf-instance, nf-pool, and
        // nf-pool-discovery (always at the same position) to locate the wanted list of
        // discovered SCP instances.

        final String[] tokens = jsonPath.split("/");

        if (tokens.length > 8)
        {
            final int iNfInstance = Integer.parseInt(tokens[3]);
            final int iNfPool = Integer.parseInt(tokens[5]);
            final int iNfPoolDiscovery = Integer.parseInt(tokens[7]);

            if (config.getEricssonScpScpFunction() != null && config.getEricssonScpScpFunction().getNfInstance().size() > iNfInstance)
            {
                final NfInstance nfInstance = config.getEricssonScpScpFunction().getNfInstance().get(iNfInstance);

                if (nfInstance.getNfPool().size() > iNfPool)
                {
                    final NfPool nfPool = nfInstance.getNfPool().get(iNfPool);

                    if (nfPool.getNfPoolDiscovery().size() > iNfPoolDiscovery)
                    {
                        return nfPool.getNfPoolDiscovery()
                                     .get(iNfPoolDiscovery)
                                     .getDiscoveredScpInstance()
                                     .stream()
                                     .collect(Collectors.toCollection(TreeSet::new));
                    }
                }
            }
        }

        return new TreeSet<>();
    }

    private static List<String> getNrfGroupsForDiscovery(final EricssonScp config)
    {
        final List<String> nrfGroups = new ArrayList<>();

        if (config.getEricssonScpScpFunction() != null && config.getEricssonScpScpFunction().getNfInstance() != null
            && !config.getEricssonScpScpFunction().getNfInstance().isEmpty()
            && config.getEricssonScpScpFunction().getNfInstance().get(0).getNrfService() != null
            && config.getEricssonScpScpFunction().getNfInstance().get(0).getNrfService().getNfDiscovery() != null)
        {
            nrfGroups.add(config.getEricssonScpScpFunction().getNfInstance().get(0).getNrfService().getNfDiscovery().getNrfGroupRef());
        }

        return nrfGroups;
    }

    private static Optional<String> getOwnNfInstanceId(Optional<EricssonScp> curr)
    {
        if (curr.isPresent())
        {
            EricssonScp config = curr.get();

            if (config.getEricssonScpScpFunction() != null && config.getEricssonScpScpFunction().getNfInstance() != null
                && !config.getEricssonScpScpFunction().getNfInstance().isEmpty()
                && config.getEricssonScpScpFunction().getNfInstance().get(0).getNrfService() != null
                && config.getEricssonScpScpFunction().getNfInstance().get(0).getNrfService().getNfDiscovery() != null)
            {
                return Optional.ofNullable(com.ericsson.utilities.common.Utils.getByName(config.getEricssonScpScpFunction()
                                                                                               .getNfInstance()
                                                                                               .get(0)
                                                                                               .getNrfGroup(),
                                                                                         config.getEricssonScpScpFunction()
                                                                                               .getNfInstance()
                                                                                               .get(0)
                                                                                               .getNrfService()
                                                                                               .getNfDiscovery()
                                                                                               .getNrfGroupRef())
                                                                              .getNfInstanceId());
            }
        }
        return Optional.empty();
    }

    private static List<NfPool> getPools(final EricssonScp config)
    {
        if (config.getEricssonScpScpFunction() != null && config.getEricssonScpScpFunction().getNfInstance() != null
            && !config.getEricssonScpScpFunction().getNfInstance().isEmpty() && config.getEricssonScpScpFunction().getNfInstance().get(0).getNfPool() != null)
        {
            return config.getEricssonScpScpFunction().getNfInstance().get(0).getNfPool();
        }

        return new ArrayList<>();
    }

    private static Optional<List<Json.Patch>> mapOutputToPatches(final SearchingContext output)
    {
        return output.getData().map(entities ->
        {
            final Optional<UserContext> userData = output.getOpaqueUserData();
            final Optional<String> ownNfInstanceId = userData.map(ctx -> getOwnNfInstanceId(ctx.getCurrConfig())).orElse(Optional.empty());

            final List<Json.Patch> patches = toPatchesNfInstances(entities, userData, ownNfInstanceId);
            patches.addAll(toPatchesScpInstances(entities, userData, ownNfInstanceId));

            log.debug("patches={}", patches);

            NnrfNfDiscoveryValidator.singleton().publish();

            return userData.isEmpty() ? Optional.ofNullable(patches.isEmpty() ? null : patches)
                                      : userData.flatMap(context -> context.getCurrConfig()//
                                                                           .map(curr ->
                                                                           {
                                                                               updatePatches(patches, entity -> output.hasPartialResult(entity), curr);
                                                                               log.debug("updatedPatches={}", patches);

                                                                               try
                                                                               {
                                                                                   final EricssonScp next = Json.patch(curr, patches, EricssonScp.class);
                                                                                   context.setNextConfig(Optional.of(next));

                                                                                   log.debug("curr={}", curr);
                                                                                   log.debug("next={}", next);

                                                                                   return patches.isEmpty() ? null : patches;
                                                                               }
                                                                               catch (IOException e)
                                                                               {
                                                                                   // Should not happen.
                                                                                   log.error("Error patching configuration. Cause: {}", e.toString());
                                                                                   return null;
                                                                               }
                                                                           }));
        }).orElse(Optional.empty());
    }

    private static Optional<List<Json.Patch>> mapOutputToRefinedPatches(final SearchingContext output)
    {
        return output.getData().map(entities ->
        {
            final Optional<UserContext> userData = output.getOpaqueUserData();
            final Optional<String> ownNfInstanceId = userData.map(ctx -> getOwnNfInstanceId(ctx.getCurrConfig())).orElse(Optional.empty());

            final List<Json.Patch> patches = toPatchesNfInstances(entities, userData, ownNfInstanceId);
            patches.addAll(toPatchesScpInstances(entities, userData, ownNfInstanceId));

            log.debug("patches={}", patches);

            NnrfNfDiscoveryValidator.singleton().publish();

            return userData.isEmpty() ? Optional.ofNullable(patches.isEmpty() ? null : patches)
                                      : userData.flatMap(context -> context.getCurrConfig()//
                                                                           .map(curr ->
                                                                           {
                                                                               updatePatches(patches, entity -> output.hasPartialResult(entity), curr);
                                                                               log.debug("updatedPatches={}", patches);

                                                                               try
                                                                               {
                                                                                   final EricssonScp next = Json.patch(Json.copy(curr, EricssonScp.class),
                                                                                                                       patches,
                                                                                                                       EricssonScp.class);
                                                                                   final List<Patch> refinedPatches = Json.diff(curr, next)
                                                                                                                          .stream()
                                                                                                                          .filter(p -> p.getPath()
                                                                                                                                        .contains(JP_DISCOVERED_NF_INSTANCE)
                                                                                                                                       || p.getPath()
                                                                                                                                           .contains(JP_DISCOVERED_SCP_INSTANCE))
                                                                                                                          .collect(Collectors.toList());

                                                                                   context.setNextConfig(Optional.of(next));

                                                                                   log.debug("curr={}", curr);
                                                                                   log.debug("next={}", next);
                                                                                   log.debug("refinedPatches={}", refinedPatches);

                                                                                   return refinedPatches.isEmpty() ? null : refinedPatches;
                                                                               }
                                                                               catch (IOException e)
                                                                               {
                                                                                   // Should not happen.
                                                                                   log.error("Error patching configuration. Cause: {}", e.toString());
                                                                                   return null;
                                                                               }
                                                                           }));
        }).orElse(Optional.empty());
    }

    private static List<Patch> toPatchesNfInstances(Map<String, Map<TargetedQuery, SearchResult>> entities,
                                                    final Optional<UserContext> userData,
                                                    final Optional<String> ownNfInstanceId)
    {
        return entities.entrySet().stream().map(entity ->
        {
            return Json.Patch.of()
                             .op(Json.Patch.Operation.ADD)
                             .path(new StringBuilder(entity.getKey()).append(JP_DISCOVERED_NF_INSTANCE).toString())
                             .value(new ArrayList<>(entity.getValue()
                                                          .values()
                                                          .stream()
                                                          .flatMap(r -> r.getNfInstances().stream())
                                                          // Make sure that the NFProfiles are unique, take only those with the highest
                                                          // priority.
                                                          .collect(Collectors.groupingBy(p -> Optional.ofNullable(p.getNfInstanceName())
                                                                                                      .orElse(p.getNfInstanceId().toString()),
                                                                                         Collectors.groupingBy(p -> Optional.ofNullable(p.getPriority())
                                                                                                                            .orElse(Integer.MAX_VALUE),
                                                                                                               TreeMap::new,
                                                                                                               Collectors.toSet())))
                                                          .values()
                                                          .stream()
                                                          // Take the NFProfiles of the highest priority only.
                                                          .flatMap(m -> m.values().stream().limit(1))
                                                          // Take the first of identical NFProfiles.
                                                          .flatMap(profiles -> profiles.stream().limit(1))
                                                          // Exclude discovered SCPs, will be treated separately.
                                                          .filter(p -> !p.getNfType().equalsIgnoreCase(NFType.SCP))
                                                          // Exclude our own instanceId since there is no point discovering ourselves.
                                                          .filter(profile -> !profile.getNfInstanceId()
                                                                                     .toString()
                                                                                     .equals(ownNfInstanceId.isPresent() ? ownNfInstanceId.get() : null))
                                                          // Finally create a new DiscoveredNfInstance from the NFProfile.
                                                          .map(NnrfNfDiscovery::createNfInstance)
                                                          // Make sure that the DiscoveredNfInstance is to be considered for the NfPool
                                                          // identified by the entity key.
                                                          .filter(i -> userData.isEmpty() ? true : userData.get().isMemberOfPool(entity.getKey(), i))
                                                          // Only consider valid DiscoveredNfInstances. An alarm is raised displaying the
                                                          // invalid ones.
                                                          .filter(NnrfNfDiscoveryValidator.singleton()::validateNfInstance)
                                                          .collect(Collectors.toCollection(TreeSet::new))));
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Patch> toPatchesScpInstances(Map<String, Map<TargetedQuery, SearchResult>> entities,
                                                     final Optional<UserContext> userData,
                                                     final Optional<String> ownNfInstanceId)
    {
        return entities.entrySet().stream().map(entity ->
        {
            return Json.Patch.of()
                             .op(Json.Patch.Operation.ADD)
                             .path(new StringBuilder(entity.getKey()).append(JP_DISCOVERED_SCP_INSTANCE).toString())
                             .value(new ArrayList<>(entity.getValue()
                                                          .values()
                                                          .stream()
                                                          .flatMap(r -> r.getNfInstances().stream())
                                                          // Make sure that the NFProfiles are unique, take only those with the highest
                                                          // priority.
                                                          .collect(Collectors.groupingBy(p -> Optional.ofNullable(p.getNfInstanceName())
                                                                                                      .orElse(p.getNfInstanceId().toString()),
                                                                                         Collectors.groupingBy(p -> Optional.ofNullable(p.getPriority())
                                                                                                                            .orElse(Integer.MAX_VALUE),
                                                                                                               TreeMap::new,
                                                                                                               Collectors.toSet())))
                                                          .values()
                                                          .stream()
                                                          // Take the NFProfiles of the highest priority only.
                                                          .flatMap(m -> m.values().stream().limit(1))
                                                          // Take the first of identical NFProfiles.
                                                          .flatMap(profiles -> profiles.stream().limit(1))
                                                          // Include discovered SCPs only.
                                                          .filter(p -> p.getNfType().equalsIgnoreCase(NFType.SCP))
                                                          // Exclude our own instanceId since there is no point discovering ourselves.
                                                          .filter(profile -> !profile.getNfInstanceId()
                                                                                     .toString()
                                                                                     .equals(ownNfInstanceId.isPresent() ? ownNfInstanceId.get() : null))
                                                          // Finally create a new DiscoveredScpInstance from the NFProfile.
                                                          .map(NnrfNfDiscovery::createScpInstance)
                                                          // Make sure that the DiscoveredScpInstance is to be considered for the NfPool
                                                          .filter(i -> userData.isEmpty() ? true : userData.get().isMemberOfPool(entity.getKey(), i))
                                                          // Only consider valid DiscoveredScpInstances. An alarm is raised displaying the
                                                          // invalid ones.
                                                          .filter(NnrfNfDiscoveryValidator.singleton()::validateScpInstance)
                                                          .collect(Collectors.toCollection(TreeSet::new))));
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * If a patch contains a partial discovery result, make sure that the
     * potentially missing discovered instances are not removed from the
     * configuration.
     * <p>
     * This is done by merging the discovered instances in the patch with the ones
     * previously discovered. All instances that were previously discovered and are
     * not part of the patch are added to the patch, pretending they had also been
     * discovered again.
     * 
     * @param patches          The patches to be updated if they contain a partial
     *                         discovery result.
     * @param hasPartialResult The predicate to test if a patch has a partial
     *                         result.
     * @param currentConfig    The current configuration with the previously
     *                         discovered instances.
     */
    private static void updatePatches(final List<Json.Patch> patches,
                                      final Predicate<String> hasPartialResult,
                                      final EricssonScp currentConfig)
    {
        patches.stream().forEach(patch ->
        {
            if (hasPartialResult.test(patch.getPath()))
            {
                if (patch.getPath().endsWith("discovered-nf-instance"))
                {
                    @SuppressWarnings("unchecked")
                    final SortedSet<DiscoveredNfInstance> nextInstances = ((List<DiscoveredNfInstance>) patch.getValue()).stream()
                                                                                                                         .collect(Collectors.toCollection(TreeSet::new));

                    getDiscoveredNfInstances(patch.getPath(), currentConfig).forEach(currInstance ->
                    {
                        if (!nextInstances.contains(currInstance))
                            nextInstances.add(currInstance);
                    });

                    patch.setValue(new ArrayList<>(nextInstances));
                }
                else if (patch.getPath().endsWith("discovered-scp-instance"))
                {
                    @SuppressWarnings("unchecked")
                    final SortedSet<DiscoveredScpInstance> nextInstances = ((List<DiscoveredScpInstance>) patch.getValue()).stream()
                                                                                                                           .collect(Collectors.toCollection(TreeSet::new));

                    getDiscoveredScpInstances(patch.getPath(), currentConfig).forEach(currInstance ->
                    {
                        if (!nextInstances.contains(currInstance))
                            nextInstances.add(currInstance);
                    });

                    patch.setValue(new ArrayList<>(nextInstances));
                }
            }
        });
    }

    static List<DiscoveredNfService> createNfServiceList(NFProfile profile)
    {
        final Set<DiscoveredNfService> services = new TreeSet<>();

        // Property service-list takes precedence over deprecated property services.
        final List<NFService> nfServices = profile.getNfServiceList() != null && !profile.getNfServiceList().isEmpty()
                                                                                                                       ? List.copyOf(profile.getNfServiceList()
                                                                                                                                            .values())
                                                                                                                       : profile.getNfServices();

        if (nfServices != null && !nfServices.isEmpty())
        {
            nfServices.forEach(service -> services.add(createNfService(profile, service)));
        }
        else if (profile.getNfType().equalsIgnoreCase(NFType.SEPP))
        {
            // DND-45907: if there are no discovered NF services, do not create a default
            // service, only for a discovered SEPP.
            services.add(createDefaultNfService(profile, 80, UriScheme.HTTP));
        }
        else
        {
            log.debug("No discovered NF service in discovered NF instance, nfInstanceId={}, nfInstanceName={}, nfType={}",
                      profile.getNfInstanceId(),
                      profile.getNfInstanceName(),
                      profile.getNfType());
        }

        return services.stream().toList();
    }

    static List<DiscoveredScpDomainInfo> createScpDomainInfoList(NFProfile profile)
    {
        final Set<DiscoveredScpDomainInfo> domainInfos = new TreeSet<>();

        final java.util.function.BiConsumer<String, NFProfile> createDomainInfoFromScpInfoOrDefault = (scpDomain,
                                                                                                       p) ->
        {
            if (p.getScpInfo() != null)
            {
                if (p.getScpInfo().getScpPorts() != null && !p.getScpInfo().getScpPorts().isEmpty())
                {
                    p.getScpInfo().getScpPorts().entrySet().forEach(e -> domainInfos.add(createScpDomainInfo(p, e.getValue(), e.getKey(), scpDomain)));
                }
                else
                {
                    domainInfos.add(createScpDomainInfo(p, 80, UriScheme.HTTP, scpDomain));
                }
            }
            else
            {
                domainInfos.add(createScpDomainInfo(p, 80, UriScheme.HTTP, scpDomain));
            }
        };

        final java.util.function.BiConsumer<String, NFProfile> createDomainInfoFromScpDomainInfoOrDefault = (scpDomain,
                                                                                                             p) ->
        {
            final ScpDomainInfo domainInfo = p.getScpInfo().getScpDomainInfoList().get(scpDomain);
            final List<IpEndPoint> scpIpEndPoints = domainInfo.getScpIpEndPoints();
            final Map<String, Integer> scpPorts = domainInfo.getScpPorts();
            final String scpFqdn = domainInfo.getScpFqdn();

            // In ScpDomainInfo, if port information exists in scpIpEndPoints, it has to be
            // included in all IpEndPoints.
            // If no port information exists in scpIpEndPoints then take the
            // ScpDomainInfo/scpPorts attribute there, otherwise the ports from scpInfo,
            // otherwise the default ports.

            if (scpIpEndPoints != null && scpIpEndPoints.stream().anyMatch(ep -> ep.getPort() != null))
            {
                // Attribute scpIpEndPoints has port info.

                final Map<Integer, Pair<List<String>, List<String>>> ipAddrsByPort = new TreeMap<>();

                scpIpEndPoints.stream()
                              .filter(endpoint -> endpoint.getPort() != null) // Consider only end points with port info (should not be necessary)
                              .forEach(endpoint ->
                              {
                                  final Pair<List<String>, List<String>> ipAddrs = ipAddrsByPort.computeIfAbsent(endpoint.getPort(),
                                                                                                                 port -> Pair.of(new ArrayList<String>(),
                                                                                                                                 new ArrayList<String>()));

                                  if (endpoint.getIpv4Address() != null && !endpoint.getIpv4Address().isEmpty())
                                      ipAddrs.getFirst().add(endpoint.getIpv4Address());

                                  if (endpoint.getIpv6Address() != null && !endpoint.getIpv6Address().isEmpty())
                                      ipAddrs.getSecond().add(endpoint.getIpv6Address());
                              });

                final Set<MultipleIpEndpoint> endpoints = // Use Set to avoid duplicate endpoints (should not happen).
                        ipAddrsByPort.entrySet()
                                     .stream()
                                     .map(e -> createMultiIpEndpoint(e.getKey(),
                                                                     e.getValue().getFirst() != null
                                                                                 && !e.getValue().getFirst().isEmpty() ? e.getValue().getFirst()
                                                                                                                       : p.getIpv4Addresses(),
                                                                     e.getValue()
                                                                      .getSecond() != null && !e.getValue().getSecond().isEmpty() ? e.getValue().getSecond()
                                                                                                                                  : p.getIpv6Addresses()))
                                     .collect(Collectors.toCollection(LinkedHashSet::new));

                domainInfos.add(createScpDomainInfo(p.getCapacity(),
                                                    p.getPriority(),
                                                    domainInfo.getScpFqdn(), // scpIpEndPoints exists -> also take scpFqdn
                                                    endpoints.stream().collect(Collectors.toList()),
                                                    UriScheme.HTTP,
                                                    scpDomain));

                domainInfos.add(createScpDomainInfo(p.getCapacity(),
                                                    p.getPriority(),
                                                    domainInfo.getScpFqdn(), // scpIpEndPoints exists -> also take scpFqdn
                                                    endpoints.stream().collect(Collectors.toList()),
                                                    UriScheme.HTTPS,
                                                    scpDomain));
            }
            else
            {
                // Attribute scpIpEndPoints is not present or does not have port info.
                // If it has IP addresses, generate a default SCP domain info for this scpDomain
                // taking those IP addresses.
                // Otherwise, generate a general default SCP domain info taking the IP addresses
                // from profile level.

                final List<String> ipv4Addresses = Optional.ofNullable(scpIpEndPoints)
                                                           .orElse(List.of())
                                                           .stream()
                                                           .map(ep -> Optional.ofNullable(ep.getIpv4Address()).orElse(""))
                                                           .filter(ip -> !ip.isEmpty())
                                                           .collect(Collectors.toList());

                final List<String> ipv6Addresses = Optional.ofNullable(scpIpEndPoints)
                                                           .orElse(List.of())
                                                           .stream()
                                                           .map(ep -> Optional.ofNullable(ep.getIpv6Address()).orElse(""))
                                                           .filter(ip -> !ip.isEmpty())
                                                           .collect(Collectors.toList());

                // Note in TS 29.510 regarding the handling of ScpDomainInfo attributes
                // scpIpEndPoints and scpFqdn:
                // "If any of these attributes is present for a given SCP domain, it shall apply
                // instead of the attributes fqdn, Ipv4Addresses and Ipv6Addresses within the
                // NFProfile data type for the corresponding SCP Domain. If none of these
                // attributes is present for a given SCP domain, the attributes fqdn,
                // Ipv4Addresses, and Ipv6Addresses within the NFProfile data type shall apply
                // for the corresponding SCP Domain."
                final boolean fallbackOnProfileLevel = scpIpEndPoints == null && scpFqdn == null;

                if (scpPorts != null && !scpPorts.isEmpty())
                {
                    scpPorts.entrySet()
                            .forEach(e -> domainInfos.add(createScpDomainInfo(p.getCapacity(),
                                                                              p.getPriority(),
                                                                              domainInfo.getScpFqdn() != null ? domainInfo.getScpFqdn()
                                                                                                              : fallbackOnProfileLevel ? p.getFqdn() : null,
                                                                              List.of(createMultiIpEndpoint(e.getValue(),
                                                                                                            !ipv4Addresses.isEmpty() ? ipv4Addresses
                                                                                                                                     : fallbackOnProfileLevel ? p.getIpv4Addresses()
                                                                                                                                                              : null,
                                                                                                            !ipv6Addresses.isEmpty() ? ipv6Addresses
                                                                                                                                     : fallbackOnProfileLevel ? p.getIpv6Addresses()
                                                                                                                                                              : null)),
                                                                              e.getKey(),
                                                                              scpDomain)));
                }
                else if (p.getScpInfo().getScpPorts() != null && !p.getScpInfo().getScpPorts().isEmpty())
                {
                    p.getScpInfo()
                     .getScpPorts()
                     .entrySet()
                     .forEach(e -> domainInfos.add(createScpDomainInfo(p.getCapacity(),
                                                                       p.getPriority(),
                                                                       domainInfo.getScpFqdn() != null ? domainInfo.getScpFqdn()
                                                                                                       : fallbackOnProfileLevel ? p.getFqdn() : null,
                                                                       List.of(createMultiIpEndpoint(e.getValue(),
                                                                                                     !ipv4Addresses.isEmpty() ? ipv4Addresses
                                                                                                                              : fallbackOnProfileLevel ? p.getIpv4Addresses()
                                                                                                                                                       : null,
                                                                                                     !ipv6Addresses.isEmpty() ? ipv6Addresses
                                                                                                                              : fallbackOnProfileLevel ? p.getIpv6Addresses()
                                                                                                                                                       : null)),
                                                                       e.getKey(),
                                                                       !ipv4Addresses.isEmpty() || !ipv6Addresses.isEmpty() ? scpDomain : null)));
                }
                else
                {
                    domainInfos.add(createScpDomainInfo(p, 80, UriScheme.HTTP, scpDomain));
                }
            }
        };

        if (profile.getScpDomains() != null && !profile.getScpDomains().isEmpty())
        {
            log.debug("-- 1.1 --");

            for (String scpDomain : profile.getScpDomains())
            {
                log.debug("-- 2.1 -- scpDomain={}", scpDomain);

                if (profile.getScpInfo() != null)
                {
                    log.debug("-- 3.1 -- scpDomain={}", scpDomain);

                    if (profile.getScpInfo().getScpDomainInfoList() != null && !profile.getScpInfo().getScpDomainInfoList().isEmpty())
                    {
                        log.debug("-- 4.1 -- scpDomain={}", scpDomain);

                        if (profile.getScpInfo().getScpDomainInfoList().containsKey(scpDomain))
                        {
                            log.debug("-- 5.1 -- scpDomain={}", scpDomain);
                            createDomainInfoFromScpDomainInfoOrDefault.accept(scpDomain, profile);
                        }
                        else
                        {
                            log.debug("-- 5.2 -- scpDomain={}", scpDomain);
                            createDomainInfoFromScpInfoOrDefault.accept(null, profile);
                        }
                    }
                    else
                    {
                        log.debug("-- 4.2 -- scpDomain={}", scpDomain);
                        createDomainInfoFromScpInfoOrDefault.accept(null, profile);
                    }
                }
                else
                {
                    log.debug("-- 3.2 -- scpDomain={}", scpDomain);
                    createDomainInfoFromScpInfoOrDefault.accept(null, profile);
                }
            }
        }
        else
        {
            log.debug("-- 1.2 --");
            createDomainInfoFromScpInfoOrDefault.accept(null, profile);
        }

        return domainInfos.stream().toList();
    }

    private final NnrfNfDiscoverer discoverer;
    private final CmmPatch cmPatch;
    private final NnrfNfDiscoveryAlarmHandler ah;

    Disposable updater = null;

    public NnrfNfDiscovery(final NnrfNfDiscoverer discoverer,
                           final CmmPatch cmPatch,
                           final NnrfNfDiscoveryAlarmHandler ah)
    {
        this.discoverer = discoverer;
        this.cmPatch = EnvVars.get(ENV_CM_MEDIATOR) != null ? cmPatch
                                                            : new CmmPatch(55003,
                                                                           "localhost",
                                                                           WebClientProvider.builder().build(VertxInstance.get()),
                                                                           ENV_GLOBAL_TLS_ENABLED /* for main local run */);
        this.ah = ah;
    }

    public Completable start()
    {
        return Completable.defer(() ->
        {
            if (this.updater == null)
            {
                this.updater = this.updaterCreate()
                                   .subscribeOn(Schedulers.io())
                                   .doOnSubscribe(d -> log.info("Started processing discovered NFs."))
                                   .doOnDispose(() -> log.info("Stopped processing discovered NFs."))
                                   .subscribe(() -> log.info("Stopped processing discovered NFs."),
                                              t -> log.error("Stopped processing discovered NFs. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
            }

            return Completable.complete();
        }).mergeWith(this.discoverer.start());
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            if (this.updater != null)
            {
                this.updater.dispose();
                this.updater = null;
            }

            return Completable.complete();
        }).mergeWith(this.discoverer.stop());
    }

    private Completable updaterCreate()
    {
        final Function<List<Patch>, CompletableSource> cmModelPatcher = patches -> this.cmPatch.patchWithNoRetry("/cm/api/v1/configurations/ericsson-scp",
                                                                                                                 patches.stream()
                                                                                                                        .map(p -> new PatchItem(PatchOperation.fromValue(p.getOp()
                                                                                                                                                                          .getValue()),
                                                                                                                                                p.getPath(),
                                                                                                                                                p.getFrom(),
                                                                                                                                                p.getValue()))
                                                                                                                        .collect(Collectors.toList()),
                                                                                                                 100000);

        return this.discoverer.getOutput()//
                              .toFlowable(BackpressureStrategy.BUFFER)
                              .flatMapCompletable(context ->
                              {
                                  final Completable updateCmModelRefined = Flowable.just(context)
                                                                                   .map(NnrfNfDiscovery::mapOutputToRefinedPatches)
                                                                                   .filter(Optional::isPresent)
                                                                                   .map(Optional::get)
                                                                                   .map(NnrfNfDiscovery::addLastUpdateTimeStamp)
                                                                                   .flatMapCompletable(cmModelPatcher);

                                  final Completable updateCmModelNormal = Flowable.just(context)
                                                                                  .map(NnrfNfDiscovery::mapOutputToPatches)
                                                                                  .filter(Optional::isPresent)
                                                                                  .map(Optional::get)
                                                                                  .map(NnrfNfDiscovery::addLastUpdateTimeStamp)
                                                                                  .flatMapCompletable(cmModelPatcher);

                                  return updateCmModelRefined.doOnError(e -> log.error("Refined configuration update failed, falling back to normal configuration update. Cause: {}",
                                                                                       Utils.toString(e, log.isDebugEnabled())))
                                                             .onErrorResumeNext(t -> updateCmModelNormal)
                                                             .doOnComplete(() -> context.getOpaqueUserData()
                                                                                        .ifPresent(u -> ((UserContext) u).applyNextConfig()))
                                                             .doOnComplete(() -> this.ah.ceaseAlarmNfDiscoveryDataUpdateError("nf=scp"))
                                                             .doOnError(e -> log.error("Configuration update failed. Cause: {}",
                                                                                       Utils.toString(e, log.isDebugEnabled())))
                                                             .doOnError(e -> this.ah.raiseAlarmNfDiscoveryDataUpdateError("nf=scp", e))
                                                             .onErrorComplete();
                              });
    }
}
