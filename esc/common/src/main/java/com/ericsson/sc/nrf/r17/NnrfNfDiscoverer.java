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

package com.ericsson.sc.nrf.r17;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.nrf.r17.NrfAdapter.RequestContext;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Result;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ServiceName;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext.PollingQuery;
import com.ericsson.sc.nrf.r17.Nrf.Pool;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Encapsulates all handling for NnrfNfDiscovery.
 * <ul>
 * <li>Input: flow of {@code Optional<Map<String, Set<Query>>>}
 * <li>Output: flow of {@code Optional<Map<String, Map<Query, SearchResult>>>}
 * </ul>
 * In the above, String is the name of an entity the queries (and their results)
 * are associated with (e.g the name of a pool).
 */
public class NnrfNfDiscoverer
{
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder({ "data" })
    public static class SearchingContext
    {
        public enum OutputStrategy
        {
            /**
             * Return the aggregated result only if ALL queries were answered successfully.
             * Otherwise {@code Optional.empty()} is returned as aggregated result.
             */
            ALL_SUCCESS,

            /**
             * Return the aggregated result also if only some queries were answered
             * successfully. For the unsuccessful queries the result is
             * {@code Optional.empty()}.
             */
            BEST_EFFORT
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonPropertyOrder({ "query", "pollingIntervalSecs", "lastPollingTimeMillis" })
        public static class PollingQuery implements Comparable<PollingQuery>
        {
            public static PollingQuery of(final TargetedQuery query,
                                          final int pollingIntervalSecs)
            {
                return new PollingQuery(query, pollingIntervalSecs);
            }

            @JsonProperty("query")
            private final TargetedQuery query;

            @JsonProperty("pollingIntervalSecs")
            private final Integer pollingIntervalSecs;

            @JsonProperty("lastPollingTimeMillis")
            private long lastPollingTimeMillis;

            private PollingQuery(final TargetedQuery query,
                                 final int pollingIntervalSecs)
            {
                this.query = query;
                this.pollingIntervalSecs = pollingIntervalSecs;
                this.lastPollingTimeMillis = 0l;
            }

            @Override
            public int compareTo(PollingQuery o)
            {
                log.debug("compareTo");
                int result = this.query.compareTo(o.query);

                if (result == 0)
                    result = this.pollingIntervalSecs.compareTo(o.pollingIntervalSecs);

                return result;
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == this)
                    return true;

                if (!(other instanceof PollingQuery))
                    return false;

                final PollingQuery that = ((PollingQuery) other);
                return Objects.equals(this.query, that.query) && Objects.equals(this.pollingIntervalSecs, that.pollingIntervalSecs);
            }

            public TargetedQuery getQuery()
            {
                return query;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(this.query, this.pollingIntervalSecs);
            }

            @JsonIgnore
            public boolean isDue()
            {
                long now = System.currentTimeMillis();

                if (now - this.lastPollingTimeMillis > this.pollingIntervalSecs * 1000)
                {
                    log.debug("delta={}", now - this.lastPollingTimeMillis);
                    this.lastPollingTimeMillis = now;
                    return true;
                }

                return false;
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

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonPropertyOrder({ "query", "nrfGroup" })
        public static class TargetedQuery implements Comparable<TargetedQuery>
        {
            public static TargetedQuery of(final Query query,
                                           final String nrfGroup)
            {
                return new TargetedQuery(query, nrfGroup);
            }

            private final Query query;

            private final String nrfGroup;

            private TargetedQuery(final Query query,
                                  final String nrfGroup)
            {
                this.query = query;
                this.nrfGroup = nrfGroup;
            }

            @Override
            public int compareTo(TargetedQuery o)
            {
                int result = this.query.compareTo(o.query);

                if (result == 0)
                    result = this.nrfGroup.compareTo(o.nrfGroup);

                return result;
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == this)
                    return true;

                if (!(other instanceof TargetedQuery))
                    return false;

                final TargetedQuery that = ((TargetedQuery) other);
                return Objects.equals(this.query, that.query) && Objects.equals(this.nrfGroup, that.nrfGroup);
            }

            public String getNrfGroup()
            {
                return this.nrfGroup;
            }

            public Query getQuery()
            {
                return this.query;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(this.query, this.nrfGroup);
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

        private static final ObjectMapper json = Jackson.om();

        /**
         * @return An empty context.
         */
        public static SearchingContext empty()
        {
            return new SearchingContext(Optional.empty(), Optional.empty());
        }

        /**
         * Returns a context with the input for the searching process. Convenience
         * method if not opaque user data are needed.
         * 
         * @param input The input for the searching process.
         * @return A context with the input for the searching process.
         */
        public static SearchingContext of(final Optional<Map<String, Set<PollingQuery>>> input)
        {
            return SearchingContext.of(input, null);
        }

        /**
         * Returns a context with the input for the searching process.
         * 
         * @param input          The input for the searching process.
         * @param opaqueUserData The opaque user data, piped as-is to the output.
         * @return A context with the input for the searching process.
         */
        public static SearchingContext of(final Optional<Map<String, Set<PollingQuery>>> input,
                                          final Optional<Object> opaqueUserData)
        {
            return new SearchingContext(input, opaqueUserData);
        }

        private static SearchingContext of(final Optional<Map<String, Map<TargetedQuery, SearchResult>>> output,
                                           final Set<String> entitiesWithPartialResult,
                                           final SearchingContext input)
        {
            return new SearchingContext(output, entitiesWithPartialResult, input);
        }

        private final Optional<Map<String, Map<TargetedQuery, SearchResult>>> output;

        @JsonIgnore
        private final Optional<Map<String, Set<PollingQuery>>> input;

        @JsonIgnore
        private final Collection<PollingQuery> normalizedInput;

        @JsonIgnore
        private final Map<TargetedQuery, Result<SearchResult>> aggregatedResults;

        @JsonIgnore
        private Set<String> entitiesWithPartialResult;

        @JsonIgnore
        private Optional<Object> opaqueUserData;

        private SearchingContext(final Optional<Map<String, Map<TargetedQuery, SearchResult>>> output,
                                 final Set<String> entitiesWithPartialResult,
                                 final SearchingContext input)
        {
            // Output side.

            this.output = output;
            this.entitiesWithPartialResult = entitiesWithPartialResult;
            this.opaqueUserData = input.opaqueUserData;

            this.input = null;
            this.normalizedInput = null;
            this.aggregatedResults = null;
        }

        private SearchingContext(final Optional<Map<String, Set<PollingQuery>>> input,
                                 final Optional<Object> opaqueUserData)
        {
            // Input side.

            // This is needed later for the input comparator. The normalized input is not
            // sufficient to decide whether or not the input has changed.
            this.input = input;

            // Normalize the input data such that in case of equal queries the one with the
            // smallest update interval is kept.
            this.normalizedInput = input.orElse(Map.of())
                                        .values()
                                        .stream()
                                        .flatMap(Set::stream)
                                        .collect(Collectors.toMap(PollingQuery::getQuery,
                                                                  Function.identity(),
                                                                  (existing,
                                                                   replacement) -> existing.compareTo(replacement) <= 0 ? existing : replacement))
                                        .values();

            // Convert Map<String, Set<PollingQuery>> to Map<String, Map<Query,
            // SearchResult>> and store as preliminary output which will be completed later
            // with the search results.
            // The PollingQuery is converted to Query as the update interval is only used
            // for discovering.
            this.output = input.map(m -> m.entrySet()
                                          .stream()
                                          .collect(Collectors.toMap(Entry::getKey,
                                                                    entry -> entry.getValue()
                                                                                  .stream()
                                                                                  .collect(Collectors.toMap(PollingQuery::getQuery,
                                                                                                            v -> new SearchResult())))));

            this.aggregatedResults = new HashMap<>();
            this.entitiesWithPartialResult = new HashSet<>();
            this.opaqueUserData = opaqueUserData;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
                return true;

            if (!(other instanceof SearchingContext))
                return false;

            final SearchingContext that = (SearchingContext) other;

            return (this.input != null ? Objects.equals(this.input, that.input) : Objects.equals(this.output, that.output))
                   && Objects.equals(this.opaqueUserData, that.opaqueUserData);
        }

        /**
         * @return The output data, whether it is completed or not.
         */
        @JsonProperty("output")
        public Optional<Map<String, Map<TargetedQuery, SearchResult>>> getData()
        {
            return this.output;
        }

        /**
         * @return The opaque user data, piped as-is to the output.
         */
        @SuppressWarnings("unchecked")
        public <T> Optional<T> getOpaqueUserData()
        {
            return (Optional<T>) this.opaqueUserData;
        }

        /**
         * @return {@code !}{@link #isEmpty()}.
         */
        public boolean hasData()
        {
            return !this.isEmpty();
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.input != null ? this.input : this.output);
        }

        public boolean hasPartialResult(final String entity)
        {
            return this.entitiesWithPartialResult.contains(entity);
        }

        /**
         * @return {@code true} if the input/output data are empty, {@code false}
         *         otherwise.
         */
        public boolean isEmpty()
        {
            return this.output.isEmpty() || this.output.get().isEmpty();
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

        private Collection<PollingQuery> getNormalizedInput()
        {
            return this.normalizedInput;
        }

        private SearchingContext mapToOutput(final Map<TargetedQuery, Result<SearchResult>> results,
                                             final OutputStrategy strategy)
        {
            // We are in the context of the input side.
            // Aggregate the results, use them to generate the context for the output side
            // and return it.

            results.forEach(this.aggregatedResults::put);

            log.debug("aggregatedResults={}", this.aggregatedResults);

            final Set<String> entitiesWithPartialResult = new HashSet<>();

            final Map<String /* entity */, Map<TargetedQuery, SearchResult>> output = //
                    this.output.filter(o -> strategy == OutputStrategy.BEST_EFFORT
                                            || strategy == OutputStrategy.ALL_SUCCESS
                                               && this.aggregatedResults.entrySet().stream().allMatch(r -> !r.getValue().hasProblem()))
                               .orElse(Map.of())
                               .entrySet()
                               .stream()
                               .map(i -> Pair.of(i.getKey(), /* entity */
                                                 this.aggregatedResults.entrySet()
                                                                       .stream()
                                                                       .filter(r -> i.getValue().containsKey(r.getKey() /* query */))
                                                                       .map(r ->
                                                                       {
                                                                           // If a query did not work, note the entity it belongs to for partial update.
                                                                           // Entities with partial results shall only be updated partially, i.e., data are
                                                                           // only added/updated but not removed.
                                                                           if (r.getValue().hasProblem())
                                                                               entitiesWithPartialResult.add(i.getKey() /* entity */);

                                                                           return r;
                                                                       })
                                                                       .filter(r -> !r.getValue().hasProblem())
                                                                       .collect(Collectors.toMap(Entry::getKey /* query */, r -> r.getValue().getBody()))))
                               .filter(e -> !e.getSecond().isEmpty()) // Only collect entries with at least one search result.
                               .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

            log.debug("output={}", output);

            return SearchingContext.of(Optional.of(output), entitiesWithPartialResult, this);
        }
    }

    private static class FlowController
    {
        private SearchingContext input;
        private AtomicInteger numJobsPending;

        public FlowController()
        {
            this.input = null;
            this.numJobsPending = new AtomicInteger(0);
        }

        public final BiPredicate<SearchingContext, SearchingContext> getInputComparator()
        {
            // Comparator has to return false to indicate a change.

            return (prev,
                    curr) ->
            {
                if (prev != curr)
                {
                    log.debug("inputComparator: curr={}", curr);

                    return curr.isEmpty(); // Only continue if it makes sense.
                }

                return this.numJobsPending.get() > 0;
            };
        }

        public void release(final Object unused)
        {
            this.numJobsPending.decrementAndGet();
            log.debug("Releasing, numJobsPending={}", this.numJobsPending);
        }

        public void seize(final Object unused)
        {
            this.numJobsPending.getAndIncrement();
            log.debug("Seizing, numJobsPending={}", this.numJobsPending);
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("isBusy=").append(this.numJobsPending.get() > 0).append(", input=").append(this.input).toString();
        }
    }

    private static final String NF_INSTANCES_SEARCH = "SearchNFInstances";

    private static final String ROUTE_NF_STATUS_NOTIFICTAION = "/notifications/nf-status";

    private static final Logger log = LoggerFactory.getLogger(NnrfNfDiscoverer.class);

    private final Function<String, Optional<Nrf.Pool>> nrfGroupForNfDiscovery;
    private final Flowable<SearchingContext> input;
    private final BehaviorSubject<SearchingContext> output;
    private final SearchingContext.OutputStrategy strategy;

    private Disposable disposable;

    public NnrfNfDiscoverer(final RouterHandler server,
                            final Function<String, Optional<Nrf.Pool>> nrfGroupForNfDiscovery,
                            final Flowable<SearchingContext> input,
                            final SearchingContext.OutputStrategy strategy)
    {
        if (server != null)
            server.configureRouter(router -> router.postWithRegex("/" + ServiceName.NNRF_NFM + "/v1" + ROUTE_NF_STATUS_NOTIFICTAION
                                                                  + "\\/(?<subscriptionId>[^\\/]+)")
                                                   .handler(this::handlerNfStatusChange));

        this.nrfGroupForNfDiscovery = nrfGroupForNfDiscovery;
        this.input = input;
        this.output = BehaviorSubject.create();
        this.strategy = strategy;
        this.disposable = null;
    }

    /**
     * @return The output of the searching process.
     */
    public BehaviorSubject<SearchingContext> getOutput()
    {
        return this.output;
    }

    public Completable start()
    {
        return this.start(true);
    }

    public Completable start(boolean doPolling)
    {
        return Completable.defer(() ->
        {
            if (this.disposable == null)
            {
                final Completable handler = doPolling ? this.createUpdater() : this.createSearcher();

                this.disposable = handler.doOnSubscribe(d -> log.info("Started discovering NF instances{}.", (doPolling ? " with polling" : "")))
                                         .doOnDispose(() -> log.info("Stopped discovering NF instances."))
                                         .subscribe(() -> log.info("Stopped discovering NF instances."),
                                                    t -> log.error("Stopped discovering NF instances. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
            }

            return Completable.complete();
        });
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            if (this.disposable != null)
            {
                this.disposable.dispose();
                this.disposable = null;
            }

            return Completable.complete();
        });
    }

    private Completable createSearcher()
    {
        return this.input.onBackpressureBuffer()
                         .observeOn(Schedulers.computation())
                         .concatMap(this::nfInstancesSearch)
                         .doOnNext(this::publish)
                         .ignoreElements()
                         .onErrorComplete();
    }

    private Completable createUpdater()
    {
        final FlowController controller = new FlowController();

        return Flowable.combineLatest(this.input.distinctUntilChanged(),
                                      Flowable.interval(1000, TimeUnit.MILLISECONDS).onBackpressureDrop(),
                                      (input,
                                       tick) -> input)
                       .onBackpressureBuffer()
                       .observeOn(Schedulers.single())
                       .distinctUntilChanged(controller.getInputComparator())
                       .doOnNext(controller::seize)
                       .concatMap(this::nfInstancesSearch)
                       .doOnNext(controller::release)
                       .doOnError(controller::release)
                       .distinctUntilChanged()
                       .doOnNext(x -> log.debug("SearchingContext: change detected"))
                       .filter(SearchingContext::hasData)
                       .doOnNext(x -> log.debug("SearchingContext: will be published"))
                       .doOnNext(this::publish)
                       .ignoreElements()
                       .doOnError(error -> log.error("Error discovering NF instances. Cause: {}", Utils.toString(error, log.isDebugEnabled())))
                       .retryWhen(handler -> handler.delay(1000, TimeUnit.MILLISECONDS));
    }

    private void handlerNfStatusChange(final RoutingContext routingContext)
    {
        final String subscriptionId = routingContext.request().getParam("subscriptionId");
        log.info("Notification received, subscriptionId={}", subscriptionId);
        routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
    }

    private Single<Result<SearchResult>> nfInstancesSearch(final Pair<Optional<Pool>, PollingQuery> context)
    {
        final String query = context.getSecond().getQuery().getQuery().toString();
        final Event event = new Event(NF_INSTANCES_SEARCH, String.class.getName(), query);

        log.debug("Processing {} request, query={}", NF_INSTANCES_SEARCH, query);

        return context.getFirst()//
                      .map(nrfGroup -> nrfGroup.nfInstancesSearch(RequestContext.of(query))//
                                               .doOnSuccess(result ->
                                               {
                                                   if (result.hasProblem())
                                                       log.warn("Problem discovering NF instances. Server responded with unexpected result code '{}'",
                                                                HttpResponseStatus.valueOf(result.getStatusCode()));
                                               }))
                      .orElse(Single.just(new Result<>(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode(), new SearchResult()))
                                    .doOnSuccess(x -> log.warn("Problem discovering NF instance. No NRF group has been configured for NF discovery.")));
    }

    private Flowable<SearchingContext> nfInstancesSearch(final SearchingContext input)
    {
        return Flowable.fromIterable(input.getNormalizedInput())
                       .onBackpressureBuffer()
                       .subscribeOn(Schedulers.io())
                       .map(pq -> Pair.of(this.nrfGroupForNfDiscovery.apply(pq.getQuery().getNrfGroup()), pq))
                       .filter(p -> p.getFirst().isPresent() && p.getSecond().isDue())
                       .flatMapSingle(p -> this.nfInstancesSearch(p).map(result -> Pair.of(p.getSecond().getQuery(), result)))
                       .toMap(Pair::getFirst, Pair::getSecond)
                       .map(searchResults -> input.mapToOutput(searchResults, this.strategy))
                       .doOnSuccess(output -> log.debug("<<<<<<<<<<"))
                       .doOnError(error -> log.error("Error discovering NF instances. Cause: {}", Utils.toString(error, log.isDebugEnabled())))
                       .onErrorReturnItem(SearchingContext.empty())
                       .doOnSubscribe(d -> log.debug(">>>>>>>>>>"))
                       .toFlowable();
    }

    private void publish(SearchingContext output)
    {
        this.output.toSerialized().onNext(output);
    }
}
