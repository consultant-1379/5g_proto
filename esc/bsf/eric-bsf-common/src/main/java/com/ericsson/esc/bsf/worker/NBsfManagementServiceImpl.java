/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 22, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ericsson.esc.bsf.openapi.model.BaseUeAddress;
import com.ericsson.esc.bsf.openapi.model.BindingLevel;
import com.ericsson.esc.bsf.openapi.model.DiameterIdentity;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddr;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsi;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsiDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsiDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupi;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupiDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupiDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.MacAddr48;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.esc.bsf.worker.BsfSchema.pcf_bindings;
import com.ericsson.esc.bsf.worker.StalePcfBinding.Reason;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.ericsson.utilities.cassandra.PreparedStatements;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.cassandra.RxSession.SessionHolder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.net.Inet4Address;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NBsfManagementServiceImpl implements NBsfManagementService
{
    private static final Logger log = LoggerFactory.getLogger(NBsfManagementServiceImpl.class);
    private final RxSession rxSession;
    private final String keyspace;
    private final Flowable<Context> contextFlow;
    private final Flowable<MultipleBindingResolver> multipleBindingLookupFlow;

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        MULTIBLE_BINDINGS_FOUND,
        DEREGISTER
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    private static final class CassandraContext
    {
        private final RxSession.SessionHolder sh;
        private final PreparedStatements<BsfQuery> ps;

        CassandraContext(SessionHolder sh,
                         PreparedStatements<BsfQuery> ps)
        {
            this.sh = sh;
            this.ps = ps;
        }
    }

    private static final class Context
    {
        private final CassandraContext cassCtx;
        private final MultipleBindingResolver mbr;

        public Context(CassandraContext cassCtx,
                       MultipleBindingResolver mbr)
        {
            this.cassCtx = cassCtx;
            this.mbr = mbr;
        }
    }

    /**
     * A concrete implementation of the {@link NbsfManagementService} interface
     * using a Cassandra database backend where the BSF schema is deployed
     * 
     * @param rxSession                   The Cassandra session
     * @param keyspace                    The keyspace in Cassandra database where
     *                                    BSF schema resides
     * @param multipleBindingLookupConfig This variable defines the discovery
     *                                    behavior for each query according to the
     *                                    configuration.
     */

    public NBsfManagementServiceImpl(RxSession rxSession,
                                     String keyspace,
                                     Flowable<MultipleBindingResolver> multipleBindingLookupConfig)
    {
        this.rxSession = rxSession;
        this.keyspace = keyspace;
        this.multipleBindingLookupFlow = multipleBindingLookupConfig.doOnComplete(() -> log.info("MultipleBindingLookup FLow stopped."))
                                                                    .doOnNext(resolver -> log.info("Applying new MultipleBindingResolver: {}", resolver))
                                                                    .mergeWith(Flowable.never());

        final var cassandraContext = this.rxSession.sessionHolder().flatMap(this::createCassandraContext);
        cassandraContext.flatMapPublisher(ctx -> this.multipleBindingLookupFlow.map(cf -> new Context(ctx, cf)));
        this.contextFlow = cassandraContext.flatMapPublisher(ctx -> this.multipleBindingLookupFlow.map(cf -> new Context(ctx, cf)))
                                           .doOnNext(next -> log.info("Context Flow updated"))
                                           .doOnError(err -> log.error("Fatal error during context update", err))
                                           .doFinally(() -> log.info("Context Flow terminated"))
                                           .replay(1)
                                           .autoConnect();
    }

    private Single<CassandraContext> createCassandraContext(SessionHolder sh)
    {
        return registerCustomCassandraCodecs(sh.getCqlSession()).andThen(sh.prepare(BsfQuery.class, BsfQuery.createStatements(keyspace, true))
                                                                           .map(ps -> new CassandraContext(sh, ps)))
                                                                .doOnSuccess(ctx -> log.info("Created new context for SessionHolder {}", sh));
    }

    private Single<Context> latestContext()
    {
        return this.contextFlow.firstOrError();
    }

    /**
     * Initialize cassandra session with BSF schema. Users should subscribe to the
     * returned Completable before calling any other public methods of this class
     * 
     * @return A Completable that completes as soon as the driver has been fully
     *         initialized with the BSF schema
     */
    @Override
    public Completable init()
    {
        return latestContext().ignoreElement()
                              .doOnSubscribe(d -> log.info("Initializing BSF database"))
                              .doOnComplete(() -> log.info("BSF database initialization complete"));
    }

    @Override
    public Completable run()
    {
        return this.contextFlow.ignoreElements();
    }

    @Override
    public Single<DeregisterResult> deregister(UUID bindingId)
    {
        log.debug("Deregister Operation, bindingId: {}", bindingId);
        if (bindingId.version() != 4)
        {
            safeLog.log(Lbl.DEREGISTER, l -> log.warn("Invalid bindingId version, should be type 4 (time based): {}", bindingId));
            return Single.<DeregisterResult>just(DeregisterResult.NOT_FOUND);
        }
        return latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.DEREGISTER).bind(bindingId)).toList()) //
                              .doOnSuccess(result -> log.trace("BsfQuery.DEREGISTER resultSet {}", result))
                              .map(row -> DeregisterResult.OK)
                              .doOnError(error -> safeLog.log(Lbl.DEREGISTER,
                                                              l -> l.warn("Error during deregister operation, bindingId: {}", bindingId, error)));

    }

    @Override
    public Single<RegisterResult> register(PcfBinding binding,
                                           int ttlConfig)
    {
        log.debug("Register Operation, binding: {}", binding);
        final var uuid = Uuids.random();

        return latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(createBatchRegisterStatement(uuid, binding, ctx.cassCtx.ps, ttlConfig)).toList()) //
                              .doOnSuccess(row -> log.trace("BsfQuery.REGISTER row {}", row))
                              .map(row -> new RegisterResult(binding, uuid));
    }

    @Override
    public Single<DiscoveryResult> discovery(DiscoveryQuery query)
    {
        return latestContext().flatMap(ctx ->
        {
            final var visitor = new Visitor()
            {
                private static final String LOG_MSG = "Discovery query {}";

                @Override
                public void visit(UeAddr query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR) //
                                                                               .bind(query.getUeAddress()))
                                                .toList();
                }

                @Override
                public void visit(UeAddrDnn query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_DNN)
                                                                               .bind(query.getUeAddress(), //
                                                                                     query.getDnn()))
                                                .toList();
                }

                @Override
                public void visit(UeAddrDnnSnssai query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_DNN_SNSSAI)
                                                                               .bind(query.getUeAddress(), //
                                                                                     query.getDnn(),
                                                                                     query.getSnssai()))
                                                .toList();
                }

                @Override
                public void visit(UeAddrSupi query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_SUPI)
                                                                               .bind(query.getUeAddress(), //
                                                                                     query.getSupi()))
                                                .toList();
                }

                @Override
                public void visit(UeAddrSupiDnn query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_SUPI_DNN)
                                                                                                              .bind(query.getUeAddress(), //
                                                                                                                    query.getSupi(),
                                                                                                                    query.getDnn()))
                                                                               .toList());
                }

                @Override
                public void visit(UeAddrSupiDnnSnssai query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_SUPI_DNN_SNSSAI)
                                                                                                              .bind(query.getUeAddress(), //
                                                                                                                    query.getSupi(),
                                                                                                                    query.getDnn(),
                                                                                                                    query.getSnssai()))
                                                                               .toList());
                }

                @Override
                public void visit(UeAddrGpsi query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_GPSI)
                                                                                                              .bind(query.getUeAddress(), //
                                                                                                                    query.getGpsi()))
                                                                               .toList());
                }

                @Override
                public void visit(UeAddrGpsiDnn query)
                {
                    log.debug(LOG_MSG, query);
                    this.result = latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_GPSI_DNN)
                                                                                                              .bind(query.getUeAddress(), //
                                                                                                                    query.getGpsi(),
                                                                                                                    query.getDnn()))
                                                                               .toList());
                }

                @Override
                public void visit(UeAddrGpsiDnnSnssai query)
                {
                    log.debug(LOG_MSG, query);

                    this.result = latestContext().flatMap(ctx -> ctx.cassCtx.sh.executeReactive(ctx.cassCtx.ps.get(BsfQuery.UEADDR_GPSI_DNN_SNSSAI)
                                                                                                              .bind(query.getUeAddress(), //
                                                                                                                    query.getGpsi(),
                                                                                                                    query.getDnn(),
                                                                                                                    query.getSnssai()))
                                                                               .toList());
                }
            };

            query.accept(visitor);
            assert (visitor.result != null);

            return processQueryResult(visitor, ctx.mbr, query);
        });
    }

    private Single<DiscoveryResult> processQueryResult(final Visitor visitor,
                                                       final MultipleBindingResolver mbr,
                                                       final DiscoveryQuery query)
    {
        return visitor.result.map(rows ->
        {
            final var bindingCount = rows.size();

            if (bindingCount == 0)
            {
                return DiscoveryResult.notFound();
            }
            else if (bindingCount == 1)
            {
                return DiscoveryResult.found(createBindingFromQueryResult(rows.get(0)));
            }
            else // Multiple binding resolution feature is enabled
            {
                if (bindingCount >= BsfQuery.LIMIT_WITH_WRITE_TIME) // Ensure that maximum binding count is not exceeded
                {
                    safeLog.log(Lbl.MULTIBLE_BINDINGS_FOUND, l -> l.warn("Too many bindings for query: {}, limit: {}", query, BsfQuery.LIMIT_WITH_WRITE_TIME));
                    return DiscoveryResult.tooMany();
                }
                else if (mbr.isQueryApplicable(query))
                {
                    final var lastUpdatedRow = Collections.max(rows,
                                                               (row1,
                                                                row2) -> Long.compare(row1.getLong(BsfQuery.WRITE_TIME_COL),
                                                                                      row2.getLong(BsfQuery.WRITE_TIME_COL)));
                    log.debug("MultipleBindingCleanup: {}", mbr.getDeleteMultipleBindings());
                    if (mbr.getDeleteMultipleBindings())
                    {
                        final var staleMfBindings = collectStaleBindings(rows, lastUpdatedRow);
                        return DiscoveryResult.okMultiple(createBindingFromQueryResult(lastUpdatedRow), staleMfBindings);
                    }
                    else
                    {
                        return DiscoveryResult.okMultiple(createBindingFromQueryResult(lastUpdatedRow));
                    }
                }
                else // Multiple binding resolution is not applicable for this query
                {
                    return DiscoveryResult.rejectMultiple();
                }
            }
        });
    }

    private List<StalePcfBinding> collectStaleBindings(List<ReactiveRow> rows,
                                                       ReactiveRow lastUpdatedRow)
    {
        return rows.stream()
                   .filter(row -> row != lastUpdatedRow)
                   .map(NBsfManagementServiceImpl::createBindingFromQueryResult)
                   .map(staleMfBinding -> new StalePcfBinding(staleMfBinding.getBindingId(), staleMfBinding.getPcfId(), Reason.MULTIPLE_BINDINGS_FOUND))
                   .collect(Collectors.toList());
    }

    private Completable registerCustomCassandraCodecs(CqlSession session)
    {
        return Completable.fromAction(() ->
        {
            final var codecRegistry = session.getContext().getCodecRegistry();
            final var ipv6PrefixUdt = fetchUdt(this.keyspace, session, Ipv6PrefixCodec.UDT_NAME);
            final var ueAddressUdt = fetchUdt(this.keyspace, session, BaseUeAddressCodec.UDT_NAME);
            final var snssaiUdt = fetchUdt(this.keyspace, session, SnssaiCodec.UDT_NAME);
            final var ipEndPointUdt = fetchUdt(this.keyspace, session, IpEndPointCodec.UDT_NAME);

            List<TypeCodec<?>> codecs = List.of(new Ipv6PrefixCodec(codecRegistry.codecFor(ipv6PrefixUdt)), //
                                                new BaseUeAddressCodec(codecRegistry.codecFor(ueAddressUdt)),
                                                new SnssaiCodec(codecRegistry.codecFor(snssaiUdt)),
                                                new IpEndPointCodec(codecRegistry.codecFor(ipEndPointUdt)));

            codecs.forEach(((MutableCodecRegistry) codecRegistry)::register);
        });
    }

    /**
     * Fetch UDT for the given type name
     * 
     * @param keyspace The keyspace where UDT resides
     * @param session  A valid cassandra session
     * @param typeName The name of the UDT
     * @return The UDT that corresponds to given typeName
     * @throws IllegalStateException if the UDT cannot be found, possibly because it
     *                               does not exist in the given keyspace
     */
    private static final UserDefinedType fetchUdt(String keyspace,
                                                  CqlSession session,
                                                  String typeName)
    {
        return session.getMetadata()
                      .getKeyspace(keyspace)
                      .flatMap(ks -> ks.getUserDefinedType(typeName))
                      .orElseThrow(() -> new IllegalStateException("User defined type not found in database schema: " + typeName));
    }

    /**
     * Creates a bound batch statement to be used for PCF binding registration. A
     * single registration comprises multiple batched {@link BsfQuery#REGISTER}
     * statements
     * 
     * @param bindingId The bindingId that shall be assigned to the binding
     * @param binding   The PcfBinding to register
     * @param ps        Holder of BSF schema prepared statements
     * @return The constructed bound statement
     */
    private Statement<?> createBatchRegisterStatement(UUID bindingId,
                                                      PcfBinding binding,
                                                      PreparedStatements<BsfQuery> ps,
                                                      int ttlConfig)
    {
        final var builder = new MatchingAddressBuilder(binding);
        final List<BatchableStatement<?>> boundStatements = builder.build() //
                                                                   .map(matchingAddress -> this.bindRegisterMarkers(bindingId,
                                                                                                                    binding,
                                                                                                                    matchingAddress,
                                                                                                                    ps,
                                                                                                                    ttlConfig))
                                                                   .collect(Collectors.toList());
        return boundStatements.size() == 1 ? //
                                           boundStatements.get(0) // Performance optimization: Do not batch a single statement
                                           : new BatchStatementBuilder(BatchType.LOGGED) //
                                                                                        .addStatements(boundStatements)
                                                                                        .build();
    }

    /**
     * Binds markers to a {@link BsfQuery#REGISTER} prepared statement
     */
    private BoundStatement bindRegisterMarkers(UUID bindingId,
                                               PcfBinding binding,
                                               BaseUeAddress matchingUeAddress,
                                               PreparedStatements<BsfQuery> ps,
                                               int ttlConfig)
    {
        final var ueAddress = BaseUeAddress.create(binding.getIpv4Addr(), binding.getIpv6Prefix(), binding.getIpDomain(), binding.getMacAddr48());

        final var builder = ps.get(BsfQuery.REGISTER).boundStatementBuilder();

        // Bind binding_id and addresses.
        builder.setUuid(pcf_bindings.binding_id.column(), bindingId)
               .set(pcf_bindings.matching_ue_address.column(), matchingUeAddress, BaseUeAddress.class)
               .set(pcf_bindings.ue_address.column(), ueAddress, BaseUeAddress.class);

        this.bindValue(builder, pcf_bindings.add_ipv6_prefixes.column(), binding.getAddIpv6Prefixes(), Ipv6Prefix.class);
        this.bindValue(builder,
                       pcf_bindings.add_mac_addrs.column(),
                       binding.getAddMacAddrs() != null ? binding.getAddMacAddrs().stream().map(MacAddr48::getMacAddr48Str).collect(Collectors.toList())
                                                        : null);

        // Bind subscriber and network info.
        this.bindValue(builder, pcf_bindings.supi.column(), binding.getSupi());
        this.bindValue(builder, pcf_bindings.gpsi.column(), binding.getGpsi());
        this.bindValue(builder, pcf_bindings.dnn.column(), binding.getDnn());
        this.bindValue(builder, pcf_bindings.snssai.column(), binding.getSnssai(), Snssai.class);

        // Bind PCF info.
        this.bindValue(builder, pcf_bindings.pcf_fqdn.column(), binding.getPcfFqdn());
        this.bindValue(builder, pcf_bindings.pcf_ip_end_points.column(), binding.getPcfIpEndPoints(), IpEndPoint.class);
        this.bindValue(builder,
                       pcf_bindings.pcf_diam_host.column(),
                       binding.getPcfDiamHost() != null ? binding.getPcfDiamHost().getDiameterIdentityStr() : null);
        this.bindValue(builder,
                       pcf_bindings.pcf_diam_realm.column(),
                       binding.getPcfDiamRealm() != null ? binding.getPcfDiamRealm().getDiameterIdentityStr() : null);
        this.bindValue(builder, pcf_bindings.pcf_id.column(), binding.getPcfId());
        this.bindValue(builder, pcf_bindings.pcf_set_id.column(), binding.getPcfSetId());
        this.bindValue(builder, pcf_bindings.bind_level.column(), binding.getBindLevel() != null ? binding.getBindLevel().getBindLevelStr() : null);
        this.bindValue(builder, pcf_bindings.recovery_time.column(), binding.getRecoveryTime() != null ? binding.getRecoveryTime().getRecoveryTimeStr() : null);

        return builder.setInt("ttl", ttlConfig).build();
    }

    /**
     * Binds a value to a specific column. If the value is null, it unsets the
     * column to prevent tombstone creation.
     * 
     * @param <T>     The type of the value parameter
     * @param builder The statement builder
     * @param column  The target table column
     * @param value   The value to bind
     * @param clazz   The class of the object value
     */
    private <T> void bindValue(BoundStatementBuilder builder,
                               String column,
                               T value,
                               Class<T> clazz)
    {
        if (value == null)
            builder.unset(column);
        else
            builder.set(column, value, clazz);
    }

    /**
     * Binds a value to a specific column. If the value is null, it unsets the
     * column to prevent tombstone creation.
     * 
     * @param <T>     The type of the value parameter
     * @param builder The statement builder
     * @param column  The target table column
     * @param value   A list of the values to bind
     * @param clazz   The class of the object value
     */
    private <T> void bindValue(BoundStatementBuilder builder,
                               String column,
                               List<T> value,
                               Class<T> clazz)
    {
        if (value == null)
            builder.unset(column);
        else
            builder.setList(column, value, clazz);
    }

    /**
     * Binds a value to a specific column. If the value is null, it unsets the
     * column to prevent tombstone creation.
     * 
     * @param builder The statement builder
     * @param column  The target table column
     * @param value   The string value to bind
     */
    private void bindValue(BoundStatementBuilder builder,
                           String column,
                           String value)
    {
        if (value == null)
            builder.unset(column);
        else
            builder.setString(column, value);
    }

    /**
     * Binds a value to a specific column. If the value is null, it unsets the
     * column to prevent tombstone creation.
     * 
     * @param builder The statement builder
     * @param column  The target table column
     * @param value   A list of strings to bind
     */
    private void bindValue(BoundStatementBuilder builder,
                           String column,
                           List<String> value)
    {
        if (value == null)
            builder.unset(column);
        else
            builder.setList(column, value, String.class);
    }

    /**
     * Binds a value to a specific column. If the value is null, it unsets the
     * column to prevent tombstone creation.
     * 
     * @param builder The statement builder
     * @param column  The target table column
     * @param value   The UUID value to bind
     */
    private void bindValue(BoundStatementBuilder builder,
                           String column,
                           UUID value)
    {
        if (value == null)
            builder.unset(column);
        else
            builder.setUuid(column, value);
    }

    private static DiscoveredPcfBinding createBindingFromQueryResult(Row row)
    {
        try
        {
            final var ueAddress = row.get(BsfSchema.pcf_bindings.ue_address.column(), BaseUeAddress.class);
            final var snssai = row.get(BsfSchema.pcf_bindings.snssai.column(), Snssai.class);
            final var pcfIpEndPoints = row.getList(BsfSchema.pcf_bindings.pcf_ip_end_points.column(), IpEndPoint.class);
            final var dnn = row.getString(BsfSchema.pcf_bindings.dnn.column());
            final var gpsi = row.getString(BsfSchema.pcf_bindings.gpsi.column());
            final var pcfDiamHost = row.getString(BsfSchema.pcf_bindings.pcf_diam_host.column());
            final var pcfDiamRealm = row.getString(BsfSchema.pcf_bindings.pcf_diam_realm.column());
            final var pcfFqdn = row.getString(BsfSchema.pcf_bindings.pcf_fqdn.column());
            final var supi = row.getString(BsfSchema.pcf_bindings.supi.column());
            final var pcfId = row.getUuid(BsfSchema.pcf_bindings.pcf_id.column());
            final var recoveryTime = row.getString(BsfSchema.pcf_bindings.recovery_time.column());
            final var addIpv6Prefixes = row.getList(BsfSchema.pcf_bindings.add_ipv6_prefixes.column(), Ipv6Prefix.class);
            final var addMacAddrs = row.getList(BsfSchema.pcf_bindings.add_mac_addrs.column(), String.class);
            final var pcfSetId = row.getString(BsfSchema.pcf_bindings.pcf_set_id.column());
            final var bindLevel = row.getString(BsfSchema.pcf_bindings.bind_level.column());
            final var bindingId = row.getUuid(BsfSchema.pcf_bindings.binding_id.column());
            final var writeTime = row.getLong(BsfQuery.WRITE_TIME_COL);

            final var pcfBinding = PcfBinding.create(supi,
                                                     gpsi,
                                                     ueAddress.getIpv4Addr().orElse(null),
                                                     ueAddress.getIpv6Prefix().orElse(null),
                                                     ueAddress.getIpDomain().orElse(null),
                                                     ueAddress.getMacAddr48().orElse(null),
                                                     dnn,
                                                     pcfFqdn,
                                                     pcfIpEndPoints != null && !pcfIpEndPoints.isEmpty() ? pcfIpEndPoints : null,
                                                     pcfDiamHost != null ? new DiameterIdentity(pcfDiamHost) : null,
                                                     pcfDiamRealm != null ? new DiameterIdentity(pcfDiamRealm) : null,
                                                     snssai,
                                                     pcfId,
                                                     recoveryTime != null ? new RecoveryTime(recoveryTime) : null,
                                                     null,
                                                     addIpv6Prefixes != null && !addIpv6Prefixes.isEmpty() ? addIpv6Prefixes : null,
                                                     addMacAddrs != null && !addMacAddrs.isEmpty() ? addMacAddrs.stream()
                                                                                                                .map(MacAddr48::new)
                                                                                                                .collect(Collectors.toList())
                                                                                                   : null,
                                                     pcfSetId,
                                                     bindLevel != null ? new BindingLevel(bindLevel) : null);

            return new DiscoveredPcfBinding(pcfBinding, bindingId, Instant.EPOCH.plus(writeTime, ChronoUnit.MICROS));
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + row.getFormattedContents(), e);
        }
    }

    private abstract class Visitor implements DiscoveryQuery.Visitor
    {
        Single<List<ReactiveRow>> result;
    }

    /**
     * Helper class that computes appropriate Matching UE addresses for a given
     * PcfBinding The produced addresses can be used for the construction of batch
     * register queries.
     */
    private final class MatchingAddressBuilder
    {
        private final PcfBinding pcfBinding;
        private final RegisterType registerType;

        MatchingAddressBuilder(PcfBinding pcfBinding)
        {
            this.pcfBinding = pcfBinding;
            this.registerType = findRegisterType();
        }

        Stream<BaseUeAddress> build()
        {
            switch (this.registerType)
            {
                case IPV4:
                    return this.ipv4(this.pcfBinding.getIpv4Addr());
                case IPV4_IPDOMAIN:
                    return this.ipv4IpDomain(this.pcfBinding.getIpv4Addr(), this.pcfBinding.getIpDomain());
                case IPV4_IPDOMAIN_IPV6PREFIX:
                    return this.ipv4ipDomainIpv6(this.pcfBinding.getIpv4Addr(), this.pcfBinding.getIpDomain(), this.pcfBinding.getAllIpv6Prefix());
                case IPV4_IPV6PREFIX:
                    return this.ipv4Ipv6(this.pcfBinding.getIpv4Addr(), this.pcfBinding.getAllIpv6Prefix());
                case IPV6PREFIX:
                    return this.ipv6(this.pcfBinding.getAllIpv6Prefix());
                case MAC_ADDRESS:
                    return this.macAddr(this.pcfBinding.getAllMacAddr48());
            }
            // This can never happen, enum types exhausted
            throw new IllegalArgumentException("Invalid register type");
        }

        private Stream<BaseUeAddress> ipv4IpDomain(Inet4Address ipv4Address,
                                                   String ipDomain)
        {
            return Stream.of(BaseUeAddress.create(ipv4Address, null, null, null), //
                             BaseUeAddress.create(ipv4Address, null, ipDomain, null));

        }

        private Stream<BaseUeAddress> ipv4(Inet4Address ipv4Address)
        {
            return Stream.of(BaseUeAddress.create(ipv4Address, null, null, null));

        }

        private Stream<BaseUeAddress> ipv6(Stream<Ipv6Prefix> ipv6Prefixes)
        {
            return ipv6Prefixes.map(ipv6Prefix -> BaseUeAddress.create(null, ipv6Prefix, null, null));
        }

        private Stream<BaseUeAddress> ipv4Ipv6(Inet4Address ipv4Address,
                                               Stream<Ipv6Prefix> ipv6Prefixes)
        {
            return Stream.concat(Stream.of(BaseUeAddress.create(ipv4Address, null, null, null)), // CONCAT WITH
                                 ipv6(ipv6Prefixes));

        }

        private Stream<BaseUeAddress> ipv4ipDomainIpv6(Inet4Address ipv4Address,
                                                       String ipDomain,
                                                       Stream<Ipv6Prefix> ipv6Prefixes)
        {
            return Stream.concat(ipv4IpDomain(ipv4Address, ipDomain), // CONCAT WITH
                                 ipv6(ipv6Prefixes));

        }

        private Stream<BaseUeAddress> macAddr(Stream<MacAddr48> macAddresses)
        {
            return macAddresses.map(macAddr -> BaseUeAddress.create(null, null, null, macAddr));
        }

        /**
         * Calculate register type according to given PcfBinding
         * 
         * @return
         */
        private RegisterType findRegisterType()
        {
            switch (pcfBinding.getUeAddressType())
            {
                case MAC:
                    return RegisterType.MAC_ADDRESS;
                case INET6:
                    return RegisterType.IPV6PREFIX;
                case INET4_6:
                    return pcfBinding.getIpDomain() != null ? RegisterType.IPV4_IPDOMAIN_IPV6PREFIX : RegisterType.IPV4_IPV6PREFIX;
                case INET4:
                    return pcfBinding.getIpDomain() != null ? RegisterType.IPV4_IPDOMAIN : RegisterType.IPV4;
            }
            // This can never happen, all possible enum types covered
            throw new IllegalArgumentException("Invalid UeAddress type " + pcfBinding.getUeAddressType());
        }

    }

    /**
     * PCF binding registration type. Depends on the PcfBinding to be registered
     */
    private enum RegisterType
    {
        /**
         * Binding with IPv4 UE address, requires single {@link BsfSchema#pcf_bindings}
         * rows
         */
        IPV4,
        /**
         * Binding with IPv4 UE address and an IPv4 domain, requires two
         * {@link BsfSchema#pcf_bindings} rows
         */
        IPV4_IPDOMAIN,
        /**
         * Binding with IPv4 address and IPv6 prefix, requires two
         * {@link BsfSchema#pcf_bindings} rows
         */
        IPV4_IPV6PREFIX,
        /**
         * Binding with IPv4 address, IPv4 domain, requires three
         * {@link BsfSchema#pcf_bindings} rows
         */
        IPV4_IPDOMAIN_IPV6PREFIX,
        /**
         * Binding with IPv6 address, requires single {@link BsfSchema#pcf_bindings} row
         */
        IPV6PREFIX,
        /**
         * Binding with MAC address, requires single {@link BsfSchema#pcf_bindings} row
         */
        MAC_ADDRESS;
    }

    @Override
    public Completable stop()
    {
        return Completable.complete();
    }
}
