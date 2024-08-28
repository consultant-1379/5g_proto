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
 * Created on: Mar 1, 2021
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.glue.IfGenericNfInstance;
import com.ericsson.sc.glue.IfNfFunction;
import com.ericsson.sc.glue.IfNrfGroup;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.json.Json.Patch;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.reactivex.functions.BiPredicate;

/**
 * Utility class gathering all comparators used for filtering configuration
 * changes. For use e.g. with
 * <code> Observable.distinctUtilChanged(comparator)</code>.
 * <p>
 * Different entities may be interested in different parts of the configuration.
 * Hence, a configuration change may not be applicable to some entities while it
 * is for others. A comparator helps to skip configuration changes that are not
 * applicable to an entity.
 */
public class ConfigComparators
{
    public static class ChangeFlags
    {
        protected enum Flags
        {
            F_ALL,
            F_N32C,
            F_N32C_FOR_RP,
            F_NNLF_DISC,
            F_NNRF_DISC,
            F_NNRF_DISC_BSF,
            F_NNRF_DISC_CAPACITY,
            F_NNRF_NFM,
            F_NNRF_NFM_NRF_GROUP_INST_ID,
            F_NRFL_RATE_LIMITING;
        }

        @JsonProperty("changeFlags")
        protected final TreeMap<Flags, Boolean> changeFlags = new TreeMap<>();

        protected ChangeFlags(final Optional<List<Json.Patch>> diff,
                              final Flags... flags)
        {
            Instant instant = Instant.now();

            for (int i = 0; i < flags.length; ++i)
            {
                final Flags cf = flags[i];

                switch (cf)
                {
                    case F_ALL:
                    default:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedAll).orElse(true));
                        break;

                    case F_N32C:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedN32c).orElse(true));
                        break;

                    case F_N32C_FOR_RP:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedN32cForRp).orElse(true));
                        break;

                    case F_NNLF_DISC:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnlfDisc).orElse(true));
                        break;

                    case F_NNRF_DISC:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnrfDisc).orElse(true));
                        break;

                    case F_NNRF_DISC_BSF:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnrfDiscBsf).orElse(true));
                        break;

                    case F_NNRF_DISC_CAPACITY:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnrfDiscCapacity).orElse(true));
                        break;

                    case F_NNRF_NFM:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnrfNfm).orElse(true));
                        break;

                    case F_NNRF_NFM_NRF_GROUP_INST_ID:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNnrfNfmNrfGroupInstId).orElse(true));
                        break;

                    case F_NRFL_RATE_LIMITING:
                        this.changeFlags.put(cf, diff.map(ConfigComparators::isChangedNrlfRateLimiting).orElse(true));
                        break;
                }

                log.debug("duration={} ({})", Duration.between(instant, instant = Instant.now()), cf);
            }

            log.info("{}", this);
        }

        @Override
        public String toString()
        {
            return new StringBuilder("changeFlags=").append(this.changeFlags).toString();
        }
    }

    private static final String JP_NF_INSTANCE_ID = "/nf-instance-id";

    private static final Logger log = LoggerFactory.getLogger(ConfigComparators.class);

    /**
     * NRFN_DISC_NRF_GROUP_REF related JSON path selector selecting all paths
     * matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/name
     * <li>/ericsson.+function/nf-instance/i/nrf-service
     * <li>/ericsson.+function/nf-instance/i/nrf-service/nf-discovery.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-group-ref.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-query
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-query/l
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-query/l/nrf-group-ref.*
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_DISC_NRF_GROUP_REF = "^(/$|/ericsson-[^-]+-function)($|(/nf-instance(/\\d++)?+($|/name$|(/nrf-service($|/nf-discovery.*+$))|(/nf-pool(/\\d++)?+($|(/nf-pool-discovery(/\\d++)?+($|/nrf-group-ref.*+$|(/nrf-query(/\\d++)?+($|/nrf-group-ref.*+$)))))))))";

    /**
     * /** NNRF_DISC related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/nrf-service
     * <li>/ericsson.+function/nf-instance/i/nrf-service/nf-discovery.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-match-condition
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/scp-match-condition
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-group-ref.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/nrf-query.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/update-interval
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_DISC = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|(/nrf-service($|/nf-discovery.*+$))|/nf-pool(/\\d++)?+($|(/(nf|scp)-match-condition$|/nf-pool-discovery(/\\d++)?+($|/nrf-group-ref.*+$|/nrf-query.*+$|/update-interval$))))";

    /**
     * /** (BSF) NNRF_DISC related JSON path selector selecting all paths matching
     * <code>
     * <ul>
     * <li>/
     * <li>/ericsson-bsf:bsf-function
     * <li>/ericsson-bsf:bsf-function/nf-instance
     * <li>/ericsson-bsf:bsf-function/nf-instance/i
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/nrf-service
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/nrf-service/nf-discovery.*
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j/nf-pool
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j/nf-pool/k
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j/nf-pool/k/nf-pool-discovery
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j/nf-pool/k/nf-pool-discovery/l
     * <li>/ericsson-bsf:bsf-function/nf-instance/i/bsf-service/j/nf-pool/k/nf-pool-discovery/l/update-interval
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_DISC_BSF = "^(/$|/ericsson-bsf:bsf-function)($|/nf-instance(/\\d++)?+)($|(/nrf-service($|/nf-discovery.*+$))|(/bsf-service(/\\d++)?+)($|/nf-pool(/\\d++)?+($|(/nf-pool-discovery(/\\d++)?+($|/update-interval$)))))";

    /**
     * NNRF_DISC related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/nf-pool
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance.*
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-scp-instance.*
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_DISC_INSTANCES = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|/nf-pool(/\\d++)?+($|(/nf-pool-discovery(/\\d++)?+($|(/discovered-(nf|scp)-instance.*+$)))))";

    /**
     * NNRF_NFM related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/nf-pool
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l/nf-status
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l/discovered-nf-service
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l/discovered-nf-service/m
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l/discovered-nf-service/m/capacity
     * <li>/ericsson.+function/nf-instance/i/nf-pool/j/nf-pool-discovery/k/discovered-nf-instance/l/discovered-nf-service/m/status
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_DISC_CAPACITY = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|/nf-pool(/\\d++)?+($|/nf-pool-discovery(/\\d++)?+($|/discovered-nf-instance(/\\d++)?+($|/nf-status|/discovered-nf-service(/\\d++)?+($|/capacity$|/status$)))))";

    /**
     * N32-C related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/external-network
     * <li>/ericsson.+function/nf-instance/i/external-network/j
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner/k
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner/k/n32-c/
     * </ul>
     */
    static final String PATH_SELECTOR_N32C_RP = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|/external-network(/\\d++)?+($|/roaming-partner(/\\d++)?+($|(/n32-c.*+$))))";

    /**
     * NNRF_NFM related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/nf-peer-info.*
     * <li>/ericsson.+function/nf-instance/i/nf-profile.*
     * <li>/ericsson.+function/nf-instance/i/nrf-group
     * <li>/ericsson.+function/nf-instance/i/nrf-group/j.*(without /nf-instance-id)
     * <li>/ericsson.+function/nf-instance/i/nrf-service.*
     * <li>/ericsson.+function/nf-instance/i/service-address.*
     * </ul>
     */
    private static final String PATH_SELECTOR_NNRF_NFM_ALL = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|(/nf-instance-id$|/nf-peer-info.*+$|/nf-profile.*+$|/nrf-group(?!/\\d++/nf-instance-id).*+$|/nrf-service.*+$|/service-address.*+$))";
    static final String PATH_SELECTOR_NNRF_NFM = "(" + PATH_SELECTOR_NNRF_NFM_ALL + "|" + PATH_SELECTOR_N32C_RP + ")";

    /**
     * NNRF_NFM related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function/nf-instance/i/nrf-group/j/nf-instance-id)
     * </ul>
     */
    static final String PATH_SELECTOR_NNRF_NFM_NRF_GROUP_INST_ID = "^(/ericsson-[^-]+-function/nf-instance/\\d+/nrf-group/\\d+/nf-instance-id)";

    /**
     * NNLF_DISC related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/name
     * <li>/ericsson.+function/nf-instance/i/nrf-group
     * <li>/ericsson.+function/nf-instance/i/nrf-group/j.*(without /nf-instance-id)
     * <li>/ericsson.+function/nf-instance/i/nf-peer-info.*
     * <li>/ericsson.+function/nf-instance/i/nf-profile/j/service-address-ref
     * <li>/ericsson.+function/nf-instance/i/service-address/j/fqdn
     * </ul>
     */
    static final String PATH_SELECTOR_NNLF_DISC = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|/name$|/nf-peer-info.*+$|/nrf-group(?!/\\d++/nf-instance-id).*+$|(/nf-profile/\\d++/service-address-ref$$)|(/service-address/\\d++/fqdn$))";

    /**
     * NRLF_RATELIMITING related JSON path selector selecting all paths matching
     * <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/global-rate-limit-profile.*
     * <li>/ericsson.+function/nf-instance/i/own-network
     * <li>/ericsson.+function/nf-instance/i/own-network/j
     * <li>/ericsson.+function/nf-instance/i/own-network/j/name
     * <li>/ericsson.+function/nf-instance/i/own-network/j/global-ingress-rate-limit-profile-ref.*
     * <li>/ericsson.+function/nf-instance/i/external-network
     * <li>/ericsson.+function/nf-instance/i/external-network/j
     * <li>/ericsson.+function/nf-instance/i/external-network/j/name
     * <li>/ericsson.+function/nf-instance/i/external-network/j/global-ingress-rate-limit-profile-ref.*
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner/k
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner/k/name
     * <li>/ericsson.+function/nf-instance/i/external-network/j/roaming-partner/k/global-ingress-rate-limit-profile-ref.*
     * </ul>
     */
    static final String PATH_SELECTOR_NRLF_RATELIMITING = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|/global-rate-limit-profile.*+$|(/(own-network|external-network)(/\\d++)?+)($|/(name$|global-ingress-rate-limit-profile-ref.*+$)|(/roaming-partner(/\\d++)?+($|/(name$|global-ingress-rate-limit-profile-ref.*+$)))))";

    /**
     * N32-C related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i/n32-c/
     * </ul>
     */
    static final String PATH_SELECTOR_N32C = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+($|(/n32-c.*+$)))";

    @Deprecated
    /**
     * N32-C related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/static-nf-instance-data
     * <li>/ericsson.+function/nf-instance/i/static-nf-instance-data/j
     * </ul>
     */
    static final String PATH_SELECTOR_STATICNFINSTDATA = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|(/static-nf-instance-data.*+$))";

    /**
     * N32-C related JSON path selector selecting all paths matching <code>
     * <ul>
     * <li>/
     * <li>/ericsson.+function
     * <li>/ericsson.+function/nf-instance
     * <li>/ericsson.+function/nf-instance/i
     * <li>/ericsson.+function/nf-instance/i/static-sepp-instance-data
     * <li>/ericsson.+function/nf-instance/i/static-sepp-instance-data/j
     * </ul>
     */
    static final String PATH_SELECTOR_STATICSEPPINSTDATA = "^(/$|/ericsson-[^-]+-function)($|/nf-instance(/\\d++)?+)($|(/static-sepp-instance-data.*+$))";

    /**
     * Generates the diff between prev and curr configurations.
     * <p>
     * Only interested in diffs regarding the NF-Instance ID of the NRF groups that
     * may have been updated by method update().
     * <p>
     * <ul>
     * <li>Get all the NRF groups from prev and curr.
     * <li>Check for updated NF-instance IDs in curr compared to prev.
     * <li>Generate a Json.Patch for each updates NF-instance ID found.
     * </ul>
     * 
     * @param jsonBase The JSON path to the NF function, e.g.,
     *                 "/ericsson-scp:scp-function".
     * @param prev     The configuration prior to calling update().
     * @param curr     The configuration after calling update().
     * @return The diff as generated Json.Patches.
     */
    public static List<Json.Patch> diffNrfGroupsNfInstanceId(final String jsonBase,
                                                             final IfNfFunction prev,
                                                             final IfNfFunction curr)
    {
        final List<Json.Patch> patches = new ArrayList<>();

        final List<IfNrfGroup> prevGroups = getNrfGroups(prev);
        final List<IfNrfGroup> currGroups = getNrfGroups(curr);

        for (int i = 0; i < prevGroups.size() && i < currGroups.size(); ++i) // Sizes should be equal
        {
            final String prevNfInstanceId = prevGroups.get(i).getNfInstanceId();
            final String currNfInstanceId = currGroups.get(i).getNfInstanceId();

            if (currNfInstanceId != null)
            {
                if (!Objects.equals(prevNfInstanceId, currNfInstanceId))
                {
                    patches.add(Json.Patch.of()
                                          .op(Json.Patch.Operation.ADD)
                                          .path(new StringBuilder(jsonBase).append("/nf-instance/0/nrf-group/").append(i).append("/nf-instance-id").toString())
                                          .value(currNfInstanceId));
                }
            }
            else if (prevNfInstanceId != null)
            {
                patches.add(Json.Patch.of()
                                      .op(Json.Patch.Operation.REMOVE)
                                      .path(new StringBuilder(jsonBase).append("/nf-instance/0//nrf-group/").append(i).append("/nf-instance-id").toString()));
            }
        }

        log.info("patches={}", patches);

        return patches;
    }

    /**
     * This comparator is of general interest as it filters out configuration
     * changes consisting only of changes of the NRF-groups' read-only attribute
     * <code>nf-instance-id</code>.
     * 
     * @param <T>
     * @return <code>true</code> if only the <code>nf-instance-id</code> of an
     *         NRF-group has been updated, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorAll()
    {
        return Utils.getComparator("All", ConfigComparators::isChangedAll);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the N32-C own security data.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_N32C}does not have a
     *         match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorN32C()
    {
        return Utils.getComparator("N32-C", ConfigComparators::isChangedN32c);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the N32-C security negotiation data.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_STATICNFINSTDATA} AND
     *         {@link #PATH_SELECTOR_N32C_RP}does not have a match,
     *         <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorN32CForRp()
    {
        return Utils.getComparator("N32-C RP", ConfigComparators::isChangedN32cForRp);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the nnlf-disc.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NNLF_DISC} does not
     *         have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNnlfDisc()
    {
        return Utils.getComparator("NnlfDisc", ConfigComparators::isChangedNnlfDisc);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the nnrf-discovery.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NNRF_DISC} does not
     *         have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNnrfDisc()
    {
        return Utils.getComparator("NnrfDisc", ConfigComparators::isChangedNnrfDisc);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the nnlf-disc in BSF.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NNLF_DISC} does not
     *         have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNnrfDiscBsf()
    {
        return Utils.getComparator("NnrfDiscBsf", ConfigComparators::isChangedNnrfDiscBsf);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the nnrf-nfmanagement.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NNRF_NFM} does not
     *         have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNnrfNfm()
    {
        return Utils.getComparator("NnrfNfm", ConfigComparators::isChangedNnrfNfm);
    }

    /**
     * This comparator filters out all configuration changes that are not capacity
     * related.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NNRF_DISC_CAPACITY}
     *         does not have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNnrfNfmDiscCapacity()
    {
        return Utils.getComparator("NnrfDiscCapacity", ConfigComparators::isChangedNnrfDiscCapacity);
    }

    /**
     * This comparator filters out all configuration changes that are not of
     * interest to the nrlf-ratelimiting.
     * 
     * @param <T>
     * @return <code>true</code> if the {@link #PATH_SELECTOR_NRLF_RATELIMITING}
     *         does not have a match, <code>false</code> otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparatorNrlfRateLimiting()
    {
        return Utils.getComparator("NrlfRateLimiting", ConfigComparators::isChangedNrlfRateLimiting);
    }

    public static boolean isChangedAll(final List<Json.Patch> patches)
    {
        return !patches.stream().allMatch(patch -> patch.getPath().contains(JP_NF_INSTANCE_ID));
    }

    public static boolean isChangedN32c(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_N32C));
    }

    public static boolean isChangedN32cForRp(final List<Json.Patch> patches)
    {
        return patches.stream()
                      .anyMatch(patch -> patch.getPath()
                                              .matches(PATH_SELECTOR_N32C_RP + "|" + PATH_SELECTOR_STATICNFINSTDATA + "|" + PATH_SELECTOR_STATICSEPPINSTDATA));
    }

    public static boolean isChangedNnlfDisc(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNLF_DISC));
    }

    public static boolean isChangedNnrfDisc(final List<Json.Patch> patches)
    {
        log.info("#patches={}", patches.size());

        if (patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_DISC)))
            return true; // Change in configuration data detected.

        if (patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_DISC_INSTANCES)))
        {
            final long nowMillis = System.currentTimeMillis();

            final List<Patch> lastUpdates = patches.stream()
                                                   .filter(patch -> patch.getOp() == Patch.Operation.ADD && patch.getPath().matches("^.+last-update$"))
                                                   .collect(Collectors.toList());

            log.debug("lastUpdates={}", lastUpdates);

            for (Patch lastUpdate : lastUpdates)
            {
                long lastUpdateMillis = 0;

                try
                {
                    lastUpdateMillis = Instant.parse(lastUpdate.getValue().toString().replaceFirst("\\+00:00", "Z")).toEpochMilli();
                }
                catch (Exception e)
                {
                    log.error("Error parsing last-update", e);
                }

                log.debug("lastUpdate={}, lastUpdateMillis={}", lastUpdate.getValue(), lastUpdateMillis);

                // If there is one lastUpdate time stamp that is less than two seconds ago, skip
                // this configuration as it has been updated just before by the NrfNfDiscovery
                // itself.

                if (nowMillis - lastUpdateMillis < 2000)
                {
                    log.info("Skipping configuration update, as it was caused by own update < 2 seconds ago (at {}).",
                             lastUpdate.getValue().toString().replaceFirst("\\+00:00", "Z"));
                    return false; // Change in state data detected, but was caused by own update < 2 s ago.
                }
            }

            return true; // Change in state data detected.
        }

        return false; // No change detected, neither in configuration nor in state data.
    }

    public static boolean isChangedNnrfDiscBsf(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_DISC_BSF));
    }

    public static boolean isChangedNnrfDiscCapacity(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_DISC_CAPACITY));
    }

    public static boolean isChangedNnrfNfm(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_NFM));
    }

    public static boolean isChangedNnrfNfmNrfGroupInstId(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NNRF_NFM_NRF_GROUP_INST_ID));
    }

    public static boolean isChangedNrlfRateLimiting(final List<Json.Patch> patches)
    {
        return patches.stream().anyMatch(patch -> patch.getPath().matches(PATH_SELECTOR_NRLF_RATELIMITING));
    }

    private static List<IfNrfGroup> getNrfGroups(final IfNfFunction function)
    {
        if (function != null && function.getNfInstance() != null && !function.getNfInstance().isEmpty()
            && ((IfGenericNfInstance) function.getNfInstance().get(0)).getNrfGroup() != null)
        {
            return ((IfGenericNfInstance) function.getNfInstance().get(0)).getNrfGroup();
        }

        return new ArrayList<>();
    }

    private ConfigComparators()
    {
    }
}
