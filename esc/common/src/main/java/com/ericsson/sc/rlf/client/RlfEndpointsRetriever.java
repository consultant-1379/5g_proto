/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 25, 2022
 *     Author: eedstl
 */

package com.ericsson.sc.rlf.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.google.gson.Gson;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1EndpointAddress;
import io.kubernetes.client.openapi.models.V1EndpointsList;
import io.kubernetes.client.util.Config;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.reactivex.core.dns.DnsClient;
import okhttp3.OkHttpClient;

/**
 * Encapsulates the periodic retrieval of endpoints of eric-sc-rlf pods.
 */
public class RlfEndpointsRetriever
{
    private static class NameResolver
    {
        private static class NettyUtils
        {
            private static final String ETC_RESOLV_CONF_FILE = "/etc/resolv.conf";
            private static final String NAMESERVER_ROW_LABEL = "nameserver";
            private static final String OPTIONS_ROW_LABEL = "options";
            private static final String DOMAIN_ROW_LABEL = "domain";
            private static final String SEARCH_ROW_LABEL = "search";
            private static final String PORT_ROW_LABEL = "port";
            private static final String NDOTS_LABEL = "ndots:";
            private static final int DEFAULT_NDOTS = 1;
            private static final int DNS_PORT = 53;
            private static final Pattern SEARCH_DOMAIN_PATTERN = Pattern.compile("\\s+");

            /**
             * Code from netty common/src/main/java/io/netty/util/internal/StringUtil.java
             * Find the index of the first non-white space character in {@code s} starting
             * at {@code offset}.
             *
             * @param seq    The string to search.
             * @param offset The offset to start searching at.
             * @return the index of the first non-white space character or &lt;{@code -1} if
             *         none was found.
             */
            public static int indexOfNonWhiteSpace(CharSequence seq,
                                                   int offset)
            {
                for (; offset < seq.length(); ++offset)
                {
                    if (!Character.isWhitespace(seq.charAt(offset)))
                    {
                        return offset;
                    }
                }
                return -1;
            }

            // ---------------------------------------------------------------------------------------------------------
            /**
             * Code from netty
             * resolver-dns/src/main/java/io/netty/resolver/dns/UnixResolverDnsServerAddressStreamProvider.java
             *
             * Parse a file of the format
             * <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> and
             * return the value corresponding to the first ndots in an options
             * configuration.
             * 
             * @return the value corresponding to the first ndots in an options
             *         configuration, or {@link #DEFAULT_NDOTS} if not found.
             * @throws IOException If a failure occurs parsing the file.
             */
            public static int parseEtcResolverFirstNdots()
            {
                try
                {
                    return parseEtcResolverFirstNdots(new File(ETC_RESOLV_CONF_FILE));
                }
                catch (IOException e)
                {
                    log.error("Error parsing file '{}'. Cause: {}", ETC_RESOLV_CONF_FILE, e.toString());
                    return DEFAULT_NDOTS;
                }
            }

            /**
             * Code from netty
             * resolver-dns/src/main/java/io/netty/resolver/dns/UnixResolverDnsServerAddressStreamProvider.java
             *
             * Parse a file of the format
             * <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> and
             * return the value corresponding to the first ndots in an options
             * configuration.
             * 
             * @param etcResolvConf a file of the format <a href=
             *                      "https://linux.die.net/man/5/resolver">/etc/resolv.conf</a>.
             * @return the value corresponding to the first ndots in an options
             *         configuration, or {@link #DEFAULT_NDOTS} if not found.
             * @throws IOException If a failure occurs parsing the file.
             */
            public static int parseEtcResolverFirstNdots(File etcResolvConf) throws IOException
            {
                try (var br = new BufferedReader(new FileReader(etcResolvConf)))
                {
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        if (line.startsWith(OPTIONS_ROW_LABEL))
                        {
                            int i = line.indexOf(NDOTS_LABEL);

                            if (i >= 0)
                            {
                                i += NDOTS_LABEL.length();
                                final int j = line.indexOf(' ', i);

                                return Integer.parseInt(line.substring(i, j < 0 ? line.length() : j));
                            }

                            break;
                        }
                    }
                }

                return DEFAULT_NDOTS;
            }

            /**
             * Code adapted from netty
             * resolver-dns/src/main/java/io/netty/resolver/dns/UnixResolverDnsServerAddressStreamProvider.java
             *
             * Parse a file of the format
             * <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> and
             * return the name server address found in it or null if not found.
             * 
             * @param etcResolvConf a file of the format <a href=
             *                      "https://linux.die.net/man/5/resolver">/etc/resolv.conf</a>.
             * @return Name server address
             * @throws IOException If a failure occurs parsing the file.
             */
            public static Pair<String, Integer> parseEtcResolverNameServer()
            {
                try
                {
                    return parseEtcResolverNameServer(new File(ETC_RESOLV_CONF_FILE));
                }
                catch (IOException e)
                {
                    log.error("Error parsing file '{}'. Cause: {}", ETC_RESOLV_CONF_FILE, e.toString());
                    return Pair.of("", DNS_PORT);
                }
            }

            public static Pair<String, Integer> parseEtcResolverNameServer(File etcResolvConf) throws IOException
            {
                String ip = null;
                int port = DNS_PORT;

                try (var br = new BufferedReader(new FileReader(etcResolvConf)))
                {
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        if (ip == null && line.startsWith(NAMESERVER_ROW_LABEL))
                        {
                            int i = indexOfNonWhiteSpace(line, NAMESERVER_ROW_LABEL.length());

                            if (i >= 0)
                            {
                                ip = line.substring(i);
                            }
                        }

                        if (port == DNS_PORT && line.startsWith(PORT_ROW_LABEL))
                        {
                            int i = indexOfNonWhiteSpace(line, PORT_ROW_LABEL.length());

                            if (i >= 0)
                            {
                                port = Integer.parseInt(line.substring(i));
                            }
                        }
                    }
                }

                // Return what was on the 'domain' line only if there were no 'search' lines
                return Pair.of(ip, port);
            }

            /**
             * Code from netty
             * resolver-dns/src/main/java/io/netty/resolver/dns/UnixResolverDnsServerAddressStreamProvider.java
             *
             * Parse a file of the format
             * <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> and
             * return the list of search domains found in it or an empty list if not found.
             * 
             * @return List of search domains.
             * @throws IOException If a failure occurs parsing the file.
             */
            public static List<String> parseEtcResolverSearchDomains()
            {
                try
                {
                    return parseEtcResolverSearchDomains(new File(ETC_RESOLV_CONF_FILE));
                }
                catch (IOException e)
                {
                    log.error("Error parsing file '{}'. Cause: {}", ETC_RESOLV_CONF_FILE, e.toString());
                    return List.of();
                }
            }

            /**
             * Code adapted from netty
             * resolver-dns/src/main/java/io/netty/resolver/dns/UnixResolverDnsServerAddressStreamProvider.java
             *
             * Parse a file of the format
             * <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> and
             * return the list of search domains found in it or an empty list if not found.
             * 
             * @param etcResolvConf a file of the format <a href=
             *                      "https://linux.die.net/man/5/resolver">/etc/resolv.conf</a>.
             * @return List of search domains.
             * @throws IOException If a failure occurs parsing the file.
             */
            static List<String> parseEtcResolverSearchDomains(File etcResolvConf) throws IOException
            {
                String localDomain = null;
                List<String> searchDomains = Collections.synchronizedList(new ArrayList<>());

                try (var br = new BufferedReader(new FileReader(etcResolvConf)))
                {
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        if (localDomain == null && line.startsWith(DOMAIN_ROW_LABEL))
                        {
                            int i = indexOfNonWhiteSpace(line, DOMAIN_ROW_LABEL.length());

                            if (i >= 0)
                            {
                                localDomain = line.substring(i);
                            }
                        }
                        else if (line.startsWith(SEARCH_ROW_LABEL))
                        {
                            int i = indexOfNonWhiteSpace(line, SEARCH_ROW_LABEL.length());

                            if (i >= 0)
                            {
                                // May contain more then one entry, either separated by whitespace or tab.
                                // See https://linux.die.net/man/5/resolver
                                String[] domains = SEARCH_DOMAIN_PATTERN.split(line.substring(i));
                                Collections.addAll(searchDomains, domains);
                            }
                        }
                    }
                }

                // Return what was on the 'domain' line only if there were no 'search' lines
                return localDomain != null && searchDomains.isEmpty() ? Collections.singletonList(localDomain) : searchDomains;
            }

            private NettyUtils()
            {
            }
        }

        private static NameResolver singleton = new NameResolver(NettyUtils.parseEtcResolverNameServer(),
                                                                 NettyUtils.parseEtcResolverFirstNdots(),
                                                                 NettyUtils.parseEtcResolverSearchDomains());

        public static NameResolver singleton()
        {
            return singleton;
        }

        private final int nDots;
        private final List<String> searchDomains;
        private final DnsClient dnsClient;

        private NameResolver(Pair<String, Integer> nameServer,
                             int nDots,
                             List<String> searchDomains)
        {
            this.nDots = nDots;
            this.searchDomains = searchDomains;
            this.dnsClient = VertxInstance.get().createDnsClient(nameServer.getSecond(), nameServer.getFirst());
        }

        public Single<Set<String>> retrieveIpsForName(final String name)
        {
            return Single.defer(() -> Flowable.fromIterable(this.searchDomains)
                                              .map(domain -> new StringBuilder(name).append(".").append(domain).toString())
                                              .flatMap(host -> Single.merge(this.dnsClient.rxResolveAAAA(host), this.dnsClient.rxResolveA(host))
                                                                     .doOnError(e -> log.debug("host={}, e={}", host, e.toString()))
                                                                     .onErrorReturnItem(new ArrayList<>())
                                                                     .doOnNext(l -> log.debug("host={}, ips={}", host, l))
                                                                     .filter(l -> !l.isEmpty()))
                                              .reduce(new TreeSet<String>(),
                                                      (list,
                                                       item) ->
                                                      {
                                                          list.addAll(item);
                                                          return list;
                                                      })
                                              .doOnSuccess(ips -> log.debug("ips={}", ips))
                                              .doOnError(e -> log.error("e={}", e.toString())));
        }
    }

    private static final String ENV_NAMESPACE = "NAMESPACE";

    private static final int POLLING_DELAY_SECS = 5; // [s]
    private static final int RETRY_DELAY_SECS = 1; // [s]

    private static final RlfEndpointsRetriever singleton = new RlfEndpointsRetriever();
    private static final Logger log = LoggerFactory.getLogger(RlfEndpointsRetriever.class);

    public static RlfEndpointsRetriever singleton()
    {
        return singleton;
    }

    private static String extractResourceVersionFromException(final ApiException ex)
    {
        final String body = ex.getResponseBody();

        if (body == null)
            return null;

        final Gson gson = new Gson();
        final Map<?, ?> st = gson.fromJson(body, Map.class);
        final Pattern p = Pattern.compile("Timeout: Too large resource version: (\\d+), current: (\\d+)");
        final String msg = (String) st.get("message");
        final Matcher m = p.matcher(msg);

        if (!m.matches())
            return null;

        return m.group(2);
    }

    private Subject<Pair<Integer /* sessionId */, Set<String>>> endpointsSubject = BehaviorSubject.createDefault(Pair.of(0, Set.<String>of())).toSerialized();

    private List<Disposable> disposables = new ArrayList<>();

    private String resourceVersion = null;

    private RlfEndpointsRetriever()
    {
    }

    public Flowable<Pair<Integer, Set<String>>> getEndpoints()
    {
        return this.endpointsSubject.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged();
    }

    public Completable start()
    {
        return Completable.defer(() ->
        {
            if (this.disposables.isEmpty())
            {
                this.disposables.add(Completable.ambArray(this.poller())
                                                .doOnSubscribe(d -> log.info("Started retrieving endpoints of RLF pods."))
                                                .doOnDispose(() -> log.info("Stopped retrieving endpoints of RLF pods."))
                                                .subscribe(() -> log.info("Stopped retrieving endpoints of RLF pods."),
                                                           t -> log.error("Stopped retrieving endpoints of RLF pods. Cause: {}",
                                                                          Utils.toString(t, log.isDebugEnabled()))));

            }

            return Completable.complete();
        });
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();

            return Completable.complete();
        });
    }

    private Completable poller()
    {
        final AtomicInteger pollingCnt = new AtomicInteger(0);

//        return NameResolver.singleton().retrieveIpsForName("eric-sc-rlf")//
        return this.retrieveRlfEndpoints()
                   .doOnSuccess(ips -> log.debug("ips={}", ips))
                   // Trigger update unconditionally every 6th poll to overcome pod restarts (IPs
                   // are unchanged then).
                   .doOnSuccess(ips -> this.endpointsSubject.onNext(Pair.of(pollingCnt.getAndIncrement() / 6, ips)))
                   .subscribeOn(Schedulers.io())
                   .repeatWhen(handler -> handler.delay(POLLING_DELAY_SECS, TimeUnit.SECONDS))
                   .ignoreElements()
                   .retryWhen(h -> h.delay(RETRY_DELAY_SECS, TimeUnit.SECONDS));
    }

    private Single<Set<String>> retrieveRlfEndpoints()
    {
        return Single.defer(() ->
        {
            final ApiClient client;

            try
            {
                client = Config.defaultClient();
            }
            catch (IOException e)
            {
                log.error("Exception while initializing K8s API", e);
                return Single.error(e);
            }

            // Optional, put helpful during tests: disable client timeout and enable
            // HTTP wire-level logs
//            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> log.info(message));
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            final OkHttpClient newClient = client.getHttpClient().newBuilder().addInterceptor(interceptor).readTimeout(0, TimeUnit.SECONDS).build();
            final OkHttpClient newClient = client.getHttpClient().newBuilder().build();

            client.setHttpClient(newClient);
            final CoreV1Api api = new CoreV1Api(client);

            try
            {
                final V1EndpointsList eps = api.listNamespacedEndpoints(EnvVars.get(ENV_NAMESPACE, "default"), // String namespace
                                                                        null, // String pretty
                                                                        null, // Boolean allowWatchBookmarks
                                                                        null, // String _continue
                                                                        null, // String fieldSelector
                                                                        "app.kubernetes.io/name=eric-sc-rlf", // String labelSelector
                                                                        null, // Integer limit
                                                                        this.resourceVersion, // String resourceVersion
                                                                        null, // String resourceVersionMatch
                                                                        null, // Integer timeoutSeconds
                                                                        null); // Boolean watch

                this.resourceVersion = eps.getMetadata().getResourceVersion();

                return Single.just(Optional.ofNullable(eps.getItems())
                                           .orElse(List.of())
                                           .stream()
                                           .flatMap(l -> Optional.ofNullable(l.getSubsets())
                                                                 .orElse(List.of())
                                                                 .stream()
                                                                 .flatMap(ss -> Optional.ofNullable(ss.getAddresses())
                                                                                        .orElse(List.of())
                                                                                        .stream()
                                                                                        .map(V1EndpointAddress::getIp)))
                                           .collect(Collectors.toCollection(TreeSet::new)));
            }
            catch (ApiException ex)
            {
                log.error("ApiException", ex);

                if (ex.getCode() == 504 || ex.getCode() == 410)
                {
                    this.resourceVersion = extractResourceVersionFromException(ex);
                }
                else
                {
                    // Reset resource version
                    this.resourceVersion = null;
                }

                return Single.error(ex);
            }
        });
    }
}
