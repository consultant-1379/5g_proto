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
 * Created on: Apr 12, 2022
 *     Author: estoioa
 */
package com.ericsson.sc.bsf.etcd;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.etcd.jetcd.Txn;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Enables persisting objects to the PcfRt etcd database
 */
public final class PcfRtDbEtcd
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtDbEtcd.class);
    private final RxEtcd etcd;
    public final PcfRtDbSerializer serializer;
    private final long ttl;
    private final RetryFunction retryFunction;
    private final AtomicLong transactionRetries = new AtomicLong();

    public enum State
    {
        OK,
        OK_EQUAL,
        OLD,
        FAIL
    }

    /**
     * 
     * @param etcd An etcd client instance, ttl
     */
    public PcfRtDbEtcd(RxEtcd etcd,
                       long ttl)

    {
        this.retryFunction = new RetryFunction().withRetries(200) //
                                                .withDelay(10) //
                                                .withJitter(30) //
                                                .withPredicate(RtTransactionException.class::isInstance) //
                                                .withRetryAction((ex,
                                                                  cnt) ->
                                                {
                                                    var total = transactionRetries.incrementAndGet();
                                                    log.debug("Retrying ETCD transaction({}), total transaction retries {}", cnt, total);
                                                }); //
        this.etcd = etcd;
        this.ttl = ttl;
        this.serializer = new PcfRtDbSerializer();
    }

    private Txn createTrans(Txn txn,
                            PcfRt pcfRt,
                            long id)
    {
        final var key = this.serializer.pcfRt().keyBytes(pcfRt.getId());
        final var value = this.serializer.pcfRt().valueBytes(pcfRt.getRecoverytime());

        var cmp = new Cmp(key, Cmp.Op.EQUAL, CmpTarget.createRevision(0));
        var op = Op.put(key, value, PutOption.newBuilder().withLeaseId(id).withPrevKV().build());
        return txn.If(cmp).Then(op);
    }

    private Txn updateTrans(Txn txn,
                            PcfRt pcfRt,
                            long leaseId)
    {
        final var key = this.serializer.pcfRt().keyBytes(pcfRt.getId());
        final var value = this.serializer.pcfRt().valueBytes(pcfRt.getRecoverytime());

        final var putOption = PutOption.newBuilder().withPrevKV().withLeaseId(leaseId).build();

        final var cmp = new Cmp(key, Cmp.Op.LESS, CmpTarget.value(value));
        final var succOp = Op.put(key, value, putOption);

        final var failCmp = new Cmp[] { new Cmp(key, Cmp.Op.EQUAL, CmpTarget.value(value)) };
        final var equalSuccOp = new Op[] { Op.put(key, value, putOption) };
        final var equalFailOp = new Op[] { Op.get(key, GetOption.DEFAULT) };

        final var failOp = Op.txn(failCmp, equalSuccOp, equalFailOp);

        return txn.If(cmp).Then(succOp).Else(failOp);
    }

    private Single<TxnResponse> createIfAbsent(PcfRt pcfRt,
                                               long leaseId)
    {
        return etcd.txn(args -> createTrans(args, pcfRt, leaseId));
    }

    public Single<TxnResponse> updateIfNewer(PcfRt pcfRt,
                                             long leaseId)
    {
        return etcd.txn(args -> updateTrans(args, pcfRt, leaseId));
    }

    /**
     * Applies the chain of createIfAbsent and updateIfNewer transactions. If
     * failed, retries.
     * 
     * @return A Single<State> which indicates the state of the transaction chain
     * @param PcfRt pcfRt
     */
    public Single<State> createOrUpdate(PcfRt pcfRt)
    {

        return this.getLeaseId(pcfRt.getId()) //
                   .flatMap(leaseId -> leaseId.isPresent() ? updateWithLease(pcfRt, leaseId.get()) : createWithLease(pcfRt))
                   .map(state ->
                   {
                       if (state.equals(State.FAIL))
                           throw new RtTransactionException();
                       else
                           return state;
                   })
                   .retryWhen(retryFunction.create())
                   .onErrorReturnItem(State.FAIL);
    }

    /**
     * Applies updateIfNewer transaction with provided leaseId. If the transaction
     * succeed, renews the attached lease.
     * 
     * @return Single<State> which indicates the state of the transaction
     * @param PcfRt pcfRt, long leaseId
     */
    public Single<State> updateWithLease(PcfRt pcfRt,
                                         long leaseId)
    {
        return this.updateIfNewer(pcfRt, leaseId) //
                   .flatMap(res -> Boolean.TRUE.equals(res.isSucceeded()) ? etcd.leaseRenewOnce(leaseId).map(response -> State.OK).onErrorReturnItem(State.FAIL)
                                                                          : failedUpdateHandler(res, pcfRt, leaseId));
    }

    /**
     * Creates a lease and applies createIfAbsent transaction. If error occur,
     * deletes the created lease.
     * 
     * @return Single<State> which indicates the state of the transaction
     * @param PcfRt pcfRt
     */
    public Single<State> createWithLease(PcfRt pcfRt)
    {
        return etcd.leaseCreate(ttl) //
                   .map(LeaseGrantResponse::getID)
                   .flatMap(id -> createIfAbsent(pcfRt, id).doOnError(err -> etcd.leaseRevoke(id).subscribe(revokeRes ->
                   {
                   }, revokeErr -> log.warn("Revocation of created lease failed", revokeErr))) //
                                                           .map(res ->
                                                           {
                                                               if (Boolean.TRUE.equals(res.isSucceeded()))
                                                                   return State.OK;
                                                               else
                                                               {
                                                                   etcd.leaseRevoke(id).subscribe(revokeRes ->
                                                                   {
                                                                   }, revokeErr -> log.warn("Revocation of created lease failed", revokeErr));
                                                                   return State.FAIL;
                                                               }
                                                           }));

    }

    /**
     * 
     * @return All the PcfRt key-values stored in database
     */
    public Single<EtcdEntries<UUID, Long>> getAll()
    {
        final var options = GetOption.newBuilder()
                                     .isPrefix(true) //
                                     .withLimit(0) // zero means no limit
                                     .build();

        return etcd.get(serializer.pcfRt().getPrefixBytes(), options) //
                   .map(getResp -> new EtcdEntries<>(getResp.getHeader().getRevision(),
                                                     getResp.getKvs() //
                                                            .stream()
                                                            .map(kv ->
                                                            {
                                                                try
                                                                {
                                                                    return new EtcdEntry<>(serializer.pcfRt(), kv);
                                                                }
                                                                catch (Exception e)
                                                                {
                                                                    log.warn("Ignored invalid long value, key: {}", kv.getKey(), e);
                                                                    return null;
                                                                }
                                                            })
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList())));
    }

    Single<Optional<Long>> getLeaseId(UUID id)
    {
        return this.find(id).map(EtcdEntry::getLease).map(Optional::of).toSingle(Optional.empty());
    }

    /**
     * Get the recovery time entry having the PcfRt object
     * 
     * @param UUID id
     * @return An {@link Optional} that contains the entry or is empty if the entry
     *         was not found
     */
    public Single<Optional<PcfRt>> get(UUID id)
    {
        Objects.requireNonNull(id);
        return find(id) //
                       .map(entry -> new PcfRt(entry.getKey(), entry.getValue())) //
                       .map(Optional::of) //
                       .toSingle(Optional.empty());
    }

    /**
     * Get a etcd Entry from database, by key
     * 
     * @param id The PcfRt id key
     * @return The etcd Entry or an empty Maybe if the entry was not found in the
     *         database
     */
    private Maybe<EtcdEntry<UUID, Long>> find(UUID id)
    {
        return this.etcd.get(serializer.pcfRt().keyBytes(id)) //
                        .flatMapMaybe(resp ->
                        {
                            switch (resp.getKvs().size())
                            {
                                case 0:
                                    return Maybe.empty();
                                case 1:
                                    final var kv = resp.getKvs().get(0);
                                    return Maybe.just(new EtcdEntry<UUID, Long>(serializer.pcfRt(), kv));

                                default:
                                    log.warn("Invalid number of entries for nfInstanceId {}", id);
                                    return Maybe.error(new RuntimeException());
                            }
                        });
    }

    /**
     * Deletes a PcfRt from etcd Database based on its id
     * 
     * @param PcfRt pcfRt
     * @return A completable of the deletion action
     */
    public Completable delete(PcfRt pcfRt)
    {
        return this.etcd.delete(this.serializer.pcfRt().keyBytes(pcfRt.getId())).ignoreElement();
    }

    /**
     * Deletes all PcfRt entries from the etcd Database.
     * 
     * @return A completable of the deletion action
     */
    public Completable truncate()
    {
        final var options = DeleteOption.newBuilder().isPrefix(true).build();

        return this.etcd.delete(this.serializer.pcfRt().getPrefixBytes(), options).ignoreElement();
    }

    RxEtcd getEtcd()
    {
        return this.etcd;
    }

    private Single<State> failedUpdateHandler(TxnResponse response,
                                              PcfRt pcfRt,
                                              long leaseId)
    {

        final var success = response.getTxnResponses().get(0).isSucceeded();
        if (success)
            return etcd.leaseRenewOnce(leaseId).map(res -> State.OK_EQUAL).onErrorReturnItem(State.FAIL);
        else
        {
            final var count = response.getTxnResponses().get(0).getGetResponses().get(0).getCount();
            if (count == 0)
                return this.createWithLease(pcfRt);
            else
                return Single.just(State.OLD);
        }
    }

    public class RtTransactionException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public RtTransactionException()
        {
            super("Etcd transaction failed");
        }
    }

}
