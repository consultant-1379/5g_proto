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
 * Created on: Feb 5, 2020
 *     Author: eedala
 */

package com.ericsson.sc.utilities.dns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.reactivex.core.dns.DnsClient;

/**
 * Performs DNS lookups on request for a list of host names and refreshes the
 * lookups in regular intervals.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "caches" })
public class DnsCache implements IfDnsCache
{
    /**
     * Client of the DnsCache. Each client controls its own set of hosts to be
     * resolved and will see only the resolved hosts for these.
     * <p>
     * This to overcome the limitation of the DnsCache to allow for only one set of
     * hosts to be resolved at a time.
     */
    public class Client implements IfDnsCacheClient
    {
        private final int id;
        private final BehaviorSubject<Set<IfDnsLookupContext>> input = BehaviorSubject.createDefault(new HashSet<>());
        private final BehaviorSubject<Map<String, IfDnsLookupContext>> output = BehaviorSubject.createDefault(new HashMap<>());
        private final AtomicReference<Set<IfDnsLookupContext>> hostsToResolve = new AtomicReference<>(new HashSet<>());
        private final DnsCache cache;

        public Client(final DnsCache cache,
                      final int id)
        {
            this.cache = cache;
            this.id = id;
            this.input.toSerialized()
                      .toFlowable(BackpressureStrategy.LATEST)
                      .doOnNext(hostsToResolve -> log.debug("Before: hostsToResolve={}", hostsToResolve))
                      .distinctUntilChanged()
                      .doOnNext(hostsToResolve -> log.debug("After : hostsToResolve={}", hostsToResolve))
                      .doOnNext(this.hostsToResolve::set)
                      .doOnNext(hostsToResolve -> this.cache.mergeAllHostsToResolve())
                      .subscribe();
        }

        @Override
        public Flowable<Map<String, IfDnsLookupContext>> getResolvedHosts()
        {
            return this.output.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged();
        }

        @Override
        public void publishHostsToResolve(final Set<IfDnsLookupContext> hostsToResolve)
        {
            this.input.toSerialized().onNext(hostsToResolve);
        }

        @Override
        public Optional<String> toHost(String ip)
        {
            return this.cache.toHost(ip);
        }

        @Override
        public Map<IpFamily, Optional<String>> toIp(String host)
        {
            return this.cache.toIp(host);
        }

        private Set<IfDnsLookupContext> getHostsToResolve()
        {
            final Set<IfDnsLookupContext> hostsToResolve = this.hostsToResolve.get();
            log.info("id={}, hostsToResolve={}", this.id, hostsToResolve);
            return hostsToResolve;
        }

        private synchronized void publishResolvedHosts(final Map<String, IfDnsLookupContext> reply)
        {
            final Map<String, IfDnsLookupContext> resolvedHosts = this.hostsToResolve.get()
                                                                                     .stream()
                                                                                     .map(IfDnsLookupContext::getHost)
                                                                                     .filter(reply::containsKey)
                                                                                     .collect(Collectors.toMap(Function.identity(),
                                                                                                               reply::get,
                                                                                                               // It can happen that there are > 1 contexts for
                                                                                                               // the same host (e.g. one asks for ipv4, the
                                                                                                               // other for ipv6). The result then contains only
                                                                                                               // one entry for the host, so just take the
                                                                                                               // first.
                                                                                                               (ctx1,
                                                                                                                ctx2) -> ctx1));
            log.info("id={}, resolvedHosts={}", this.id, resolvedHosts.values());
            this.output.toSerialized().onNext(resolvedHosts);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "host", "ipAddrByIpFamily" })
    public static class LookupContext implements IfDnsLookupContext
    {
        public static IfDnsLookupContext of(final LookupContext orig,
                                            final IpFamily exclude)
        {
            return new LookupContext(orig, exclude);
        }

        public static IfDnsLookupContext of(final String host,
                                            final Set<IpFamily> ipFamilies)
        {
            return new LookupContext(host, ipFamilies);
        }

        @JsonProperty("host")
        private final String host;

        @JsonProperty("ipAddrByIpFamily")
        private final Map<IpFamily, ResolutionResult> ipAddrByIpFamily;

        @JsonIgnore
        private final boolean isNumericHost;

        private LookupContext(final LookupContext orig,
                              final IpFamily exclude)
        {
            this.host = orig.getHost();
            this.ipAddrByIpFamily = orig.getIpAddrs()
                                        .entrySet()
                                        .stream()
                                        .filter(e -> exclude != null ? !e.getKey().equals(exclude) : true)
                                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            this.isNumericHost = orig.isNumericHost;
        }

        private LookupContext(final String host,
                              final Set<IpFamily> ipFamilies)
        {
            this.host = host;

            if (IPV4_ADDRESS.matcher(host).matches())
            {
                this.isNumericHost = true;
                this.ipAddrByIpFamily = Stream.of(IpFamily.IPV4)
                                              .collect(Collectors.toConcurrentMap(Function.identity(), ipFamily -> ResolutionResult.resolvedOk(host)));
            }
            else if (IPV6_ADDRESS.matcher(host).matches())
            {
                this.isNumericHost = true;
                this.ipAddrByIpFamily = Stream.of(IpFamily.IPV6)
                                              .collect(Collectors.toConcurrentMap(Function.identity(), ipFamily -> ResolutionResult.resolvedOk(host)));
            }
            else
            {
                this.isNumericHost = false;
                this.ipAddrByIpFamily = ipFamilies.stream()
                                                  .collect(Collectors.toConcurrentMap(Function.identity(), ipFamily -> ResolutionResult.notResolvedYet()));
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            final LookupContext that = (LookupContext) o;
            return Objects.equals(this.host, that.host) && Objects.equals(this.ipAddrByIpFamily, that.ipAddrByIpFamily);
        }

        @Override
        public String getHost()
        {
            return this.host;
        }

        @Override
        public ResolutionResult getIpAddr(final IpFamily ipFamily)
        {
            return this.ipAddrByIpFamily.get(ipFamily);
        }

        @JsonIgnore
        @Override
        public Map<IpFamily, ResolutionResult> getIpAddrs()
        {
            return this.ipAddrByIpFamily;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.host, this.ipAddrByIpFamily);
        }

        @JsonIgnore
        @Override
        public boolean isNotResolvedYet()
        {
            return !this.isNumericHost && this.ipAddrByIpFamily.values().stream().anyMatch(ResolutionResult::isNotResolvedYet);
        }

        @JsonIgnore
        @Override
        public boolean isNumericHost()
        {
            return this.isNumericHost;
        }

        @JsonIgnore
        @Override
        public boolean isResolved()
        {
            return this.isNumericHost || this.ipAddrByIpFamily.values().stream().allMatch(ResolutionResult::isResolvedOk);
        }

        @Override
        public IfDnsLookupContext putIpAddr(final IpFamily ipFamily,
                                            final ResolutionResult ipAddr)
        {
            this.ipAddrByIpFamily.put(ipFamily, ipAddr);
            return this;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    public static class NameserverData
    {
        String nameserver;

        int port;

        public NameserverData(String nameserver,
                              int port)
        {
            this.nameserver = nameserver;
            this.port = port;
        }

        @Override
        public String toString()
        {
            return "{ nameserver=" + this.nameserver + ", port=" + this.port + " }";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "ipFamily", "cache", "request" })
    private static class Cache
    {
        @JsonIgnore
        private final DnsCache owner;

        @JsonProperty("ipFamily")
        private final IpFamily ipFamily;

        @JsonIgnore
        private final DnsClient dnsClient;

        /**
         * Number of dots a hostname has to have before trying absolute DNS lookup, from
         * /etc/resolv.conf
         */
        @JsonIgnore
        private final int dnsNdots;

        /**
         * Contains the search-list with domain-suffixes from /etc/resolv.conf
         */
        @JsonIgnore
        private final List<String> dnsSearchlist;

        /**
         * Hostname -> IP-address mappings
         * <p>
         * CAUTION! if the mappings from hostname to only ONE IP address change, then
         * the failover mechanism is highly impacted and need to be adapted as well
         */
        @JsonProperty("cache")
        private Map<String, ResolutionResult> cache = new ConcurrentHashMap<>();

        /**
         * Hostname -> TTL mappings (TTL = expiry time, not time when entered)
         */
        @JsonIgnore
        private Map<String, LocalDateTime> ttl = new ConcurrentHashMap<>();

        /**
         * Holds the current request.
         * <p>
         * !! We can only handle one request at a time !!
         */
        @JsonProperty("request")
        private AtomicReference<Map<String, ResolutionResult>> request = new AtomicReference<>(new ConcurrentHashMap<>());

        public Cache(final DnsCache owner,
                     final IpFamily ipFamily,
                     final DnsClient dnsClient,
                     final int dnsNdots,
                     final List<String> dnsSearchList)
        {
            this.owner = owner;
            this.ipFamily = ipFamily;
            this.dnsClient = dnsClient;
            this.dnsNdots = dnsNdots;
            this.dnsSearchlist = dnsSearchList;

            VertxInstance.get().setPeriodic(REFRESH_INTERVAL_MILLIS, id -> this.refreshCache());
            VertxInstance.get().setPeriodic(TTL_CLEANUP_INTERVAL_MILLIS, id -> this.ttlCleanupCache());
        }

        /**
         * Return the host name for a given IP address from the cache. This is a reverse
         * lookup of our cache. This function <i>does not</i> trigger a DNS lookup. You
         * get whatever is currently in the cache.
         * 
         * @return Optional.empty if the IP address is not found in the cache
         * @return Optional<String> host name if the IP address is found
         */
        public Optional<String> toHost(final String ip)
        {
            return this.cache.entrySet()
                             .stream()
                             .filter(e -> e.getValue().isResolvedOk())
                             .filter(e -> e.getValue().get().equals(ip)) // the IP-address we're looking for
                             .map(Entry::getKey)
                             .findFirst();
        }

        /**
         * Return the IP address for a given host from the cache. This function <i>does
         * not</i> trigger a DNS lookup. You get whatever is currently in the cache.
         * 
         * @return Optional.empty if the lookup failed, timed out, or was never done
         * @return Optional<String> ip-address if the lookup succeeded
         */
        public ResolutionResult toIp(final String host)
        {
            final ResolutionResult resolvedHost = this.cache.get(host);
            return resolvedHost != null ? resolvedHost : ResolutionResult.notResolvedYet();
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }

        /**
         * Recursive function to do a DNS lookup with domain suffixes taken from the
         * given searchlist. When a DNS lookup fails, try with the next domain suffix
         * from the searchlist.
         *
         * @param host
         * @param searchList
         */
        private void dnsLookup(final String host,
                               final List<String> searchList)
        {
            if (searchList.isEmpty()) // No more suffixes to try -> give up
            {
                log.debug("{}: DNS lookup failed for host {} (giving up)", this.ipFamily, host);
                this.processFinalDnsServerReply(host, ResolutionResult.resolvedNotFound());
            }
            else // searchList is not empty, get next suffix and try it:
            {
                final String suffix = searchList.remove(0);
                final String effectiveHost = host + (suffix.isEmpty() ? "" : ".") + suffix;

                log.debug("{}: suffix='{}', effectiveHost='{}'", this.ipFamily, suffix, effectiveHost);

                switch (this.ipFamily)
                {
                    case IPV4:
                        this.dnsClient.lookup4(effectiveHost, resp -> dnsLookupHandler(resp, host, searchList));
                        break;

                    case IPV6:
                        this.dnsClient.lookup6(effectiveHost, resp -> dnsLookupHandler(resp, host, searchList));
                        break;

                    default:
                        log.error("Internal error: unknown IP family {}", this.ipFamily);
                }
            }
        }

        /**
         * Common callback function for IPv4 and IPv6 DNS lookups
         * 
         * @param resp       -- response from resolver
         * @param host       -- host to be looked up
         * @param searchlist -- domain suffix list to try
         */
        private void dnsLookupHandler(final AsyncResult<String> resp,
                                      final String host,
                                      final List<String> searchList)
        {
            log.debug("{}: host={}, resp={}, searchList={}", this.ipFamily, host, resp, searchList);

            if (resp.succeeded())
            {
                this.processFinalDnsServerReply(host, resp.result() != null ? ResolutionResult.resolvedOk(resp.result()) : ResolutionResult.resolvedNotFound());
            }
            else // Error
            {
                final String cause = resp.cause().getMessage();

                if (cause.contains("NXDOMAIN") || cause.contains("SERVFAIL"))
                {
                    this.dnsLookup(host, searchList); // try again with different domain suffix
                }
                else if (cause.contains("timeout")) // timeout is server fail issue
                {
                    this.processFinalDnsServerReply(host, ResolutionResult.notResolvedYet());
                }
                else
                {
                    log.warn("{}: Unhandled DNS error code: {}", this.ipFamily, resp.cause().getMessage());
                    this.dnsLookup(host, searchList); // try again with different domain suffix
                }
            }
        }

        /**
         * Extend the TTL field for a given hostname
         */
        private void extendTtl(final String host)
        {
            this.ttl.replace(host, LocalDateTime.now().plusSeconds(TTL_SECS));
        }

        /**
         * Once we have a final reply from the DNS server, store it in the cache and the
         * reply. The final reply can be positive (= we got an IP address for the host)
         * or negative (= no more domains to try). If this was the last missing lookup,
         * we send the reply to the requester.
         *
         * @param host
         * @param ipAddress
         */
        private void processFinalDnsServerReply(final String host,
                                                final ResolutionResult ipAddress)
        {
            // Use replace() to insert the reply into the cache because it only inserts the
            // mapping if the hostname is still in the cache and has not been removed by
            // the TTL cleanup in the meantime. Otherwise it could happen that the TTL
            // cleanup removes an entry and a DNS reply that was on its way arrives after
            // the cleanup and is then inserted again. This would defeat the TTL cleanup
            // mechanism.

            if (ipAddress.isNotResolvedYet())
            {
                final ResolutionResult current = this.cache.get(host);

                if (current != null && !current.isResolvedOk())
                {
                    this.cache.replace(host, ResolutionResult.resolvedNotFound());
                }
            }
            else
            {
                final ResolutionResult prevIpAddress = this.cache.replace(host, ipAddress);
                log.debug("{}: Updated cache: host={}, newIp={}, prevIp={}", this.ipFamily, host, ipAddress, prevIpAddress);
            }

            final Map<String, ResolutionResult> request = this.request.get();

            if (ipAddress.isNotResolvedYet()) // DNS server failure
            {
                final ResolutionResult resolutionResult = request.get(host);

                if (resolutionResult != null && !resolutionResult.isResolvedOk()) // Update only if it was not resolved already.
                {
                    request.replace(host, ResolutionResult.resolvedNotFound());
                }
            }
            else
            {
                request.replace(host, ipAddress);
            }

            if (request.values().stream().filter(ResolutionResult::isNotResolvedYet).count() == 0) // Request is complete?
            {
                this.owner.mergeAndPublishResult(this.ipFamily, request);
            }
        }

        /**
         * Refresh the current cache. This function is periodically called from a timer.
         */
        private void refreshCache()
        {
            this.update(this.cache.keySet());
        }

        /**
         * A client wants to know the IP addresses of a set of hosts. If we have the IP
         * addresses already, send them immediately via the outgoing Flowable, otherwise
         * insert them into the cache and start DNS lookups for them.
         * 
         * @param hosts a list of hosts to perform DNS lookups for
         */
        private void setHostsToResolve(final Set<String> hosts)
        {
            log.info("{}: Requested hosts to resolve: {}", this.ipFamily, hosts);

            final Map<String, ResolutionResult> request = new ConcurrentHashMap<>();

            boolean haveAllAddresses = true;
            final Set<String> missingHosts = new ConcurrentHashSet<>();

            for (String host : hosts)
            {
                this.setTtl(host);

                final ResolutionResult ipAddr = this.cache.get(host);

                if (ipAddr == null) // no entry yet for this hostname (first time we see this host)
                {
                    // Is "host" a numerical IP address?
                    if (isNumericalIpAddress(host))
                    { // yes -> store IP address immediately
                        this.cache.put(host, ResolutionResult.resolvedOk(host));
                        request.put(host, ResolutionResult.resolvedOk(host));
                    }
                    else // "host" is really a hostname that needs to be looked up:
                    {
                        missingHosts.add(host);
                        this.cache.put(host, ResolutionResult.notResolvedYet());
                        request.put(host, ResolutionResult.notResolvedYet());
                        haveAllAddresses = false;
                    }
                }
                else if (ipAddr.isNotResolvedYet()) // ongoing request but no IP-address yet
                {
                    missingHosts.add(host);
                    request.put(host, ResolutionResult.notResolvedYet());
                    haveAllAddresses = false;
                }
                else // mapping already exists
                {
                    request.put(host, this.cache.get(host));
                }
            }

            this.request.set(request);

            // If we already have all addresses looked up, send a response now:
            if (haveAllAddresses)
            {
                this.owner.mergeAndPublishResult(this.ipFamily, request);
            }
            else // start update of missing hostnames right now, don't wait for next cycle
            {
                this.update(missingHosts);
            }
        }

        /**
         * Set the TTL field for a given host.
         */
        private void setTtl(String host)
        {
            this.ttl.put(host, LocalDateTime.now().plusSeconds(TTL_SECS));
        }

        /**
         * Remove outdated entries (where the TTL has expired) from the cache. This
         * function is periodically called from a timer.
         */
        private void ttlCleanupCache()
        {
            log.debug("{}: TTL cleanup starting... {}", this.ipFamily, this.ttl);

            final LocalDateTime now = LocalDateTime.now();
            final List<String> hostsToRemove = new ArrayList<>();

            this.ttl.entrySet()
                    .stream() //
                    .filter(entry -> entry.getValue().isBefore(now))
                    .map(Entry::getKey)
                    .forEach(host ->
                    {
                        this.cache.remove(host);
                        hostsToRemove.add(host); // cannot remove from the set we're iterating
                    });

            log.debug("{}: Hosts to be removed by TTL cleanup: {}", this.ipFamily, hostsToRemove);

            for (String host : hostsToRemove)
            {
                this.ttl.remove(host);
            }
        }

        /**
         * For each host in the supplied set, first check if there is an entry in
         * /etc/hosts, otherwise send a DNS request and later update the cache with the
         * IP address in the reply. The Vert.x DNS functions do not look at the
         * /etc/hosts file (there is an internal function that can resolve also via
         * /etc/hosts but it's not a public API), so we do it ourselves.
         */
        private void update(final Set<String> hosts)
        {
            VertxInstance.get().fileSystem().readFile("/etc/hosts", result ->
            {
                if (result.succeeded())
                {
                    this.update2(hosts,
                                 DnsCache.parseEtcHosts(result.toString())
                                         .entrySet()
                                         .stream()
                                         .filter(e -> this.ipFamily.equals(IpFamily.IPV4) && IPV4_ADDRESS.matcher(e.getValue()).matches()
                                                      || this.ipFamily.equals(IpFamily.IPV6) && IPV6_ADDRESS.matcher(e.getValue()).matches())
                                         .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
                }
                else // could not read /etc/hosts, continue with only DNS lookup
                {
                    log.error("{}: Cannot open /etc/hosts for reading. Hostname resolution is done without consulting /etc/hosts now", this.ipFamily);
                    this.update2(hosts, null);
                }
            });
        }

        /**
         * Common code for the if and else branch in update().
         *
         * @param hosts
         * @param hostsFromEtcHosts
         */
        private void update2(final Set<String> hosts,
                             final Map<String, String> hostsFromEtcHosts)
        {
            final Map<String, ResolutionResult> request = this.request.get();

            for (String host : hosts)
            {
                // If the hostname is for the current request, then also update its TTL value
                // to avoid it being purged by the TTL updater
                if (request.containsKey(host))
                {
                    this.extendTtl(host);
                }

                // Is "host" a hostname that needs to be resolved?
                if (!isNumericalIpAddress(host))
                {
                    // Apply the ndots rule as described in the man-page of resolv.conf:
                    // If the number of dots in the hostname is >= ndots, then try an absolute name
                    // lookup first. Else try the absolute name lookup last.
                    // "Absolute" means "as the host was given to us". After that try with the
                    // domain suffixes appended.
                    final List<String> searchlist = new ArrayList<>(this.dnsSearchlist);
                    final long numDotsInHostname = host.chars().filter(ch -> ch == '.').count();

                    if (numDotsInHostname >= this.dnsNdots)
                    {
                        searchlist.add(0, ""); // try absolute name first
                    }
                    else
                    {
                        searchlist.add(""); // try absolute name last
                    }

                    // Is the host in /etc/hosts?
                    if ((hostsFromEtcHosts != null) && hostsFromEtcHosts.containsKey(host))
                    {
                        this.processFinalDnsServerReply(host, ResolutionResult.resolvedOk(hostsFromEtcHosts.get(host)));
                    }
                    else  // Not in /etc/hosts -> have to do a DNS lookup
                    {
                        this.dnsLookup(host, searchlist);
                    }
                }
            }
        }
    }

    private static final int TTL_SECS = 5 * 60; // Retention time of cached DNS lookups, entries will be deleted unless
                                                // requested again, then the TTL starts again
    private static final long REFRESH_INTERVAL_MILLIS = 10l * 1000l; // DNS Cache update interval to check if addresses have changed
    private static final long TTL_CLEANUP_INTERVAL_MILLIS = 5l * REFRESH_INTERVAL_MILLIS; // Interval to check for expired cache entries

    // Regex to match valid IPv4 addresses, taken from Android source code
    // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/util/Patterns.java#120
    // Apache 2.0 license
    private static final String IPV4_ADDRESS_PATTERN = "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                                                       + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                                                       + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" + "|[1-9][0-9]|[0-9]))";
    public static final Pattern IPV4_ADDRESS = Pattern.compile(IPV4_ADDRESS_PATTERN);
    // And for IPv6 addresses, taken from:
    // https://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses
    private static final String IPV6_ADDRESS_PATTERN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    public static final Pattern IPV6_ADDRESS = Pattern.compile(IPV6_ADDRESS_PATTERN);

    // BEGIN code from netty
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
    // END code from netty

    private static final ObjectMapper json = Jackson.om();
    private static final Logger log = LoggerFactory.getLogger(DnsCache.class);
    private static IfDnsCache instance = null;

    public static IfDnsCache getInstance()
    {
        if (instance == null)
        {
            try
            {
                instance = new DnsCache();
            }
            catch (IOException e)
            {
                instance = new DnsCache(null, 1, new ArrayList<>());
            }
        }

        return instance;
    }

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
                return offset;
        }

        return -1;
    }

    /**
     * Returns true if the provided host string matches an IPv4 or IPv6 pattern.
     * 
     * @param host a String containing either a hostname of a numerical IP address
     *             (v4 or v6)
     * @return true if numerical address
     */
    public static boolean isNumericalIpAddress(String host)
    {
        return IPV4_ADDRESS.matcher(host).matches() || IPV6_ADDRESS.matcher(host).matches();
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
    public static int parseEtcResolverFirstNdots() throws IOException
    {
        return parseEtcResolverFirstNdots(new File(ETC_RESOLV_CONF_FILE));
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
    public static NameserverData parseEtcResolverNameserver() throws IOException
    {
        return parseEtcResolverNameserver(new File(ETC_RESOLV_CONF_FILE));
    }

    public static NameserverData parseEtcResolverNameserver(File etcResolvConf) throws IOException
    {
        String nameserver = null;
        int port = DNS_PORT;

        try (var br = new BufferedReader(new FileReader(etcResolvConf)))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                if (nameserver == null && line.startsWith(NAMESERVER_ROW_LABEL))
                {
                    int i = indexOfNonWhiteSpace(line, NAMESERVER_ROW_LABEL.length());

                    if (i >= 0)
                    {
                        nameserver = line.substring(i);
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
        return new NameserverData(nameserver, port);
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
    public static List<String> parseEtcResolverSearchDomains() throws IOException
    {
        return parseEtcResolverSearchDomains(new File(ETC_RESOLV_CONF_FILE));
    }

    private static Set<IpFamily> determineDefaultIpFamilies()
    {
        Set<IpFamily> ipFamilies = new HashSet<>();

        for (String ipVersion : RuntimeEnvironment.getDeployedIpVersion().name().split("_")) // Catch "IPV4_IPV6"
            ipFamilies.add(IpFamily.valueOf(ipVersion));

        return ipFamilies;
    }

    /**
     * Parse the contents of /etc/hosts file and return a map of hostname ->
     * IP-address strings. The format is shown in the man-page of hosts(5):
     * <ul>
     * <li>Comments starting with # are ignored
     * <li>Elements in each line are separated with one or more spaces or tabs
     * <li>Each line starts with an IP address followed by one or several names for
     * that address
     * </ul>
     *
     * @return mapping of hostname to IP-address
     */
    private static Map<String, String> parseEtcHosts(final String contents)
    {
        final Map<String, String> hosts = new HashMap<>();

        for (String line : contents.split("[\\n\\r]+"))
        {
            // Remove comments and skip blank lines:

            line = line.replaceAll("#.*$", "");

            if (line.isBlank())
                continue;

            String[] words = line.split("\\s+");

            if (words.length > 1)
            {
                var addr = words[0];

                for (var i = 1; i < words.length; i++)
                    hosts.put(words[i], addr);
            }
        }

        return hosts;
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
    static List<String> parseEtcResolverSearchDomains(final File etcResolvConf) throws IOException
    {
        String localDomain = null;
        List<String> searchDomains = new ArrayList<>();

        try (var br = new BufferedReader(new FileReader(etcResolvConf)))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                if (localDomain == null && line.startsWith(DOMAIN_ROW_LABEL))
                {
                    final int i = indexOfNonWhiteSpace(line, DOMAIN_ROW_LABEL.length());

                    if (i >= 0)
                    {
                        localDomain = line.substring(i);
                    }
                }
                else if (line.startsWith(SEARCH_ROW_LABEL))
                {
                    final int i = indexOfNonWhiteSpace(line, SEARCH_ROW_LABEL.length());

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

    /**
     * Holds the clients requesting DNS resolution. A client sends a resolution
     * request that then is aggregated with the requests of all other clients. The
     * response to a request only contains the host names relevant to that request.
     */
    @JsonIgnore
    private final Map<Integer, Client> clients = new ConcurrentHashMap<>();

    @JsonIgnore
    private final AtomicInteger clientCnt = new AtomicInteger(0);

    /**
     * Default client that is always created, for use with the methods inherited
     * from the client interface:
     * <li>getResolvedHosts()
     * <li>publishHostsToResolve()
     */
    @JsonIgnore
    private final Client defaultClient;

    @JsonProperty("caches")
    private Map<IpFamily, Cache> caches = new HashMap<>();

    @JsonIgnore
    private DnsClient dnsClient;

    @JsonIgnore
    private BehaviorSubject<Map<String /* host */, IfDnsLookupContext>> dnsUpdates = BehaviorSubject.createDefault(new HashMap<>());

    @JsonIgnore
    private Map<String /* host */, IfDnsLookupContext> lastResultPublished = new HashMap<>();

    private DnsCache() throws IOException
    {
        this(parseEtcResolverNameserver(), parseEtcResolverFirstNdots(), parseEtcResolverSearchDomains());
    }

    private DnsCache(final NameserverData nameserverData,
                     final int nDots,
                     final List<String> searchList)
    {
        log.info("nameserverData={}, nDosts={}, searchList={}", nameserverData, nDots, searchList);

        if (nameserverData == null || nameserverData.nameserver == null)
            this.dnsClient = VertxInstance.get().createDnsClient();
        else
            this.dnsClient = VertxInstance.get().createDnsClient(nameserverData.port, nameserverData.nameserver);

        for (IpFamily ipFamily : IpFamily.values())
            this.caches.put(ipFamily, new Cache(this, ipFamily, this.dnsClient, nDots, searchList));

        this.defaultClient = this.createClient();
    }

    /**
     * Creates a new client of this DnsCache and returns it. Users must retain it
     * for future interactions with the DnsCache.
     * 
     * @return The new client.
     */
    public Client createClient()
    {
        final int id = this.clientCnt.getAndIncrement();
        return this.clients.computeIfAbsent(id, v -> new Client(this, id));
    }

    /**
     * @return A Flowable for the results of the DNS lookups.
     */
    @JsonIgnore
    public Flowable<Map<String, IfDnsLookupContext>> getResolvedHosts()
    {
        return this.defaultClient.getResolvedHosts();
    }

    /**
     * @return A Set of unresolved hosts.
     */
    @JsonIgnore
    @Override
    public Flowable<Map<String, IfDnsLookupContext>> getUnresolvedHosts()
    {
        return this.dnsUpdates.toFlowable(BackpressureStrategy.LATEST)
                              .compose(f -> f.map(hosts -> hosts.entrySet()
                                                                .stream()
                                                                .filter(e -> !e.getValue().isResolved())
                                                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))));
    }

    public void publishHostsToResolve(final Set<IfDnsLookupContext> hostsToResolve)
    {
        this.defaultClient.publishHostsToResolve(hostsToResolve);
    }

    /**
     * Return the host name for a given IP address from the cache. This is a reverse
     * lookup of our cache. This function <i>does not</i> trigger a DNS lookup. You
     * get whatever is currently in the cache.
     * 
     * @return Optional.empty if the IP address is not found in the cache
     * @return Optional<String> host name if the IP address is found
     */
    public Optional<String> toHost(final String ip)
    {
        for (Cache cache : this.caches.values())
        {
            final Optional<String> host = cache.toHost(ip);

            if (host.isPresent())
                return host;
        }

        return Optional.empty();
    }

    /**
     * Return the IP address for a given host from the cache. This function <i>does
     * not</i> trigger a DNS lookup. You get whatever is currently in the cache.
     * 
     * @return Optional.empty if the lookup failed, timed out, or was never done
     * @return Optional<String> ip-address if the lookup succeeded
     */
    public Map<IpFamily, Optional<String>> toIp(final String host)
    {
        return this.caches.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e ->
        {
            final ResolutionResult ip = e.getValue().toIp(host);
            return ip.isResolvedOk() ? Optional.of(ip.get()) : Optional.empty();
        }));
    }

    @Override
    public String toString()
    {
        try
        {
            return json.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }

    /**
     * Loop over all clients and collect all their host names to be resolved.
     */
    private synchronized void mergeAllHostsToResolve()
    {
        // Merge LookupContexts obtained from all clients:

        final Map<String, IfDnsLookupContext> mergedContexts = //
                this.clients.values()
                            .stream()
                            .flatMap(client -> client.getHostsToResolve().stream())
                            .collect(Collectors.toMap(ctx -> ctx.getHost(),
                                                      ctx -> ctx,
                                                      (ctx1,
                                                       ctx2) ->
                                                      {
                                                          final IfDnsLookupContext mergedCtx = LookupContext.of((LookupContext) ctx1, null);
                                                          mergedCtx.getIpAddrs().putAll(ctx2.getIpAddrs());
                                                          return mergedCtx;
                                                      }));

        log.debug("mergedContexts={}", mergedContexts);

        // Now gather the hostsToResolve by IpFamily and use them to trigger the
        // appropriate cache:

        final Map<IpFamily, Set<String>> hostsToResolveByIpFamily = new HashMap<>();

        mergedContexts.values()
                      .forEach(ctx -> ctx.getIpAddrs()
                                         .keySet()
                                         .forEach(ipFamily -> hostsToResolveByIpFamily.computeIfAbsent(ipFamily, v -> new HashSet<>()).add(ctx.getHost())));

        log.debug("hostsToResolveByIpFamily={}", hostsToResolveByIpFamily);

        this.caches.entrySet()
                   .stream()
                   .forEach(e -> e.getValue().setHostsToResolve(Optional.ofNullable(hostsToResolveByIpFamily.get(e.getKey())).orElse(Set.of())));
    }

    private synchronized void mergeAndPublishResult(final IpFamily ipFamily,
                                                    final Map<String, ResolutionResult> partialResult)
    {
        // The partialResult is for the IP family passed in ipFamily.
        // Copy only the lastResultPublished of IP families other than the ipFamily
        // passed.

        final Map<String, IfDnsLookupContext> result = this.lastResultPublished.values()
                                                                               .stream()
                                                                               .map(ctx -> LookupContext.of((LookupContext) ctx, ipFamily))
                                                                               .filter(ctx -> !ctx.getIpAddrs().isEmpty())
                                                                               .collect(Collectors.toMap(IfDnsLookupContext::getHost, Function.identity()));

        log.debug("{}: lastResultPublished={}", ipFamily, this.lastResultPublished.values());
        log.debug("{}: partialResult1={}", ipFamily, result.values());
        log.debug("{}: partialResult2={}", ipFamily, partialResult);

        // Then add the partialResult received for the ipFamily passed.

        partialResult.forEach((host,
                               ip) ->
        {
            final IfDnsLookupContext prev = result.get(host);
            result.put(host, (prev != null ? prev : LookupContext.of(host, Set.of(ipFamily))).putIpAddr(ipFamily, ip));
        });

        log.debug("{}: mergedResult={}", ipFamily, result.values());

        // Always publish the merged result to avoid the expiration of the
        // UnresolvableHosts alarm due to inactivity.

        this.dnsUpdates.onNext(result);

        // Publish the merged result to the clients if it differs from the previously
        // published one.

        if (!this.lastResultPublished.equals(result))
        {
            log.info("{}: Publishing result {}", ipFamily, result.values());

            this.lastResultPublished = result;
            this.clients.values().forEach(c -> c.publishResolvedHosts(result));
        }
    }
}
