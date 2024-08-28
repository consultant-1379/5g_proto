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
 * Created on: Dec 4, 2019
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Leader Election implementation based on ETCD API. The user of the API is
 * responsible to enforce the same interval parameters for all contender
 * entities and define a common leaderKey. Additionally, it is crucial that the
 * uniqueness of the IDs of all contender entities is guaranteed.
 */
public class RxLeaderElection
{
    public enum LeaderStatus
    {
        LEADER,
        CONTENDER
    }

    private static final Logger log = LoggerFactory.getLogger(RxLeaderElection.class);
    private static final Function<String, ByteSequence> btSqn = s -> ByteSequence.from(s.getBytes());

    private static final String LEADERSHIP_LOST_MSG = "The leaderKey has expired or has been modified by an external entity. Leadership is lost.";
    private static final String CREATE_LEASE_MSG = "[ID: {}] Created lease with id: {}";
    private static final String REVOKE_LEASE_MSG = "[ID: {}] Revoked lease with id: {}";
    private static final String LEADER_MSG = "[ID: {}] Became leader!";
    private static final String CLAIM_MSG = "[ID: {}] Claiming leadership.";
    private static final String RENEW_MSG = "[ID: {}] Renewing leadership.";
    private static final String ERROR_MSG = "[ID: {}] Error during leader election";

    private final RxEtcd rxEtcd;
    private final String ownId;
    private final String leaderKey;
    private final int leaderInterval;
    private final int renewInterval;
    private final int claimInterval;
    private final int recoveryDelay;
    private final Long retries;
    private final double jitter;
    private final Completable electionChain;
    private final BehaviorSubject<LeaderStatus> leaderStatus = BehaviorSubject.create();

    private RxLeaderElection(Builder builder)
    {
        this.rxEtcd = builder.rxEtcd;
        this.ownId = builder.ownId;
        this.leaderKey = builder.leaderKey;
        this.leaderInterval = builder.leaderInterval;
        this.renewInterval = builder.renewInterval;
        this.claimInterval = builder.claimInterval;
        this.recoveryDelay = builder.recoveryDelay;
        this.retries = builder.retries != null ? builder.retries : 0l;
        this.jitter = Math.random();

        electionChain = electionPhase().flatMap(etcdResponse -> emitStatus(LeaderStatus.LEADER).toSingleDefault(etcdResponse))
                                       .flatMapCompletable(this::leadershipPhase);
    }

    /**
     * Start the leader election process on subscription.
     */
    public Completable run()
    {
        log.info("RxLeaderElection.run starting");
        final CompletableTransformer applyRetries = upstream -> this.retries == null ? upstream.retry() : upstream.retry(this.retries);
        return emitStatus(LeaderStatus.CONTENDER).andThen(applyJitter())
                                                 .andThen(electionChain)
                                                 .andThen(Completable.error(new RuntimeException(LEADERSHIP_LOST_MSG)))
                                                 .doOnError(e -> log.warn(ERROR_MSG, ownId, e))
                                                 .onErrorResumeNext(e -> emitStatus(LeaderStatus.CONTENDER).andThen(Observable.timer(recoveryDelay,
                                                                                                                                     TimeUnit.SECONDS)
                                                                                                                              .ignoreElements())
                                                                                                           .andThen(Completable.error(e)))
                                                 .compose(applyRetries);
    }

    /**
     * Emit the latest leader status updates on subscription.
     * 
     * @return Observable<LeaderStatus>
     */
    public Observable<LeaderStatus> leaderStatusUpdates()
    {
        return leaderStatus.hide();
    }

    /**
     * Election phase. Attempt to put ownId with a transaction in the leader key on
     * each claimInterval. When the transaction succeeds, the election phase
     * completes and emits the EtcdResponse of the last transaction.
     * 
     * @return Single<EtcdResponse> The last emitted etcdResponse.
     */
    private Single<EtcdResponse> electionPhase()
    {
        return Observable.interval(0, claimInterval, TimeUnit.SECONDS)
                         .concatMapSingle(tick -> claimLeadership())
                         .takeUntil((EtcdResponse etcdResponse) -> etcdResponse.getTxnResponse().isSucceeded())
                         .lastOrError()
                         .doOnSuccess(etcdResponse -> log.debug(LEADER_MSG, ownId));
    }

    /**
     * Leadership phase. Renew the leader key with ownId on each renewInterval. When
     * the renewal of the key fails, complete. The renewal of the key will fail when
     * ownId is not written in the leader key.
     * 
     * @return Completable A completable for the leadership phase.
     */
    private Completable leadershipPhase(EtcdResponse initialResponse)
    {
        return Observable.interval(renewInterval, TimeUnit.SECONDS)
                         .concatMapSingle(tick -> renewLeadership())
                         .scan(initialResponse,
                               (previousResponse,
                                newResponse) ->
                               {
                                   var staleLeaseId = previousResponse.getActiveLeaseId();
                                   var activeLeaseId = newResponse.getActiveLeaseId();
                                   var newTxnResponse = newResponse.getTxnResponse();
                                   return new EtcdResponse(activeLeaseId, staleLeaseId, newTxnResponse);
                               })
                         // Revoke stale lease of previous renewal.
                         .concatMapSingle(etcdResponse -> Single.just(etcdResponse).compose(revokeStaleLease()))
                         .takeWhile(etcdResponse -> etcdResponse.getTxnResponse().isSucceeded())
                         .ignoreElements();
    }

    /**
     * Claim the leadership. If leader key is empty, put ownId as value.
     * 
     * @return Single<EtcdResponse> The response of the transaction.
     */
    private Single<EtcdResponse> claimLeadership()
    {
        return rxEtcd.leaseCreate(leaderInterval).retry(this.retries).map(leaseResp ->
        {
            log.trace(CREATE_LEASE_MSG, ownId, leaseResp.getID());
            return leaseResp.getID();
        }).flatMap(leaseId ->
        {
            log.debug(CLAIM_MSG, ownId);
            return txnWriteLeaderKey(leaseId);
        }).compose(revokeStaleLease());
    }

    /**
     * Attempt to renew the leadership. Renew only if the leader key already
     * contains ownId.
     * 
     * @return Single<EtcdResponse> The response of the transaction.
     */
    private Single<EtcdResponse> renewLeadership()
    {
        return rxEtcd.leaseCreate(leaderInterval).retry(this.retries).map(leaseResp ->
        {
            log.trace(CREATE_LEASE_MSG, ownId, leaseResp.getID());
            return leaseResp.getID();
        }).flatMap(leaseId ->
        {
            log.debug(RENEW_MSG, ownId);
            return txnRenewLeaderKey(leaseId);
        }).compose(revokeStaleLease());
    }

    /**
     * Write ownID in leaderKey, if the key is empty.
     * 
     * @param leaseId The id of the lease to use when writing
     * @return Single<EtcdResponse> The transaction response.
     */
    private Single<EtcdResponse> txnWriteLeaderKey(long leaseId)
    {
        return rxEtcd.txn(txn -> txn.If(new Cmp(btSqn.apply(leaderKey), Cmp.Op.EQUAL, CmpTarget.version(0)))
                                    .Then(Op.put(btSqn.apply(leaderKey), btSqn.apply(ownId), PutOption.newBuilder().withLeaseId(leaseId).build())))
                     .retry(this.retries)
                     .map(txnResponse -> txnResponse.isSucceeded() ? new EtcdResponse(Optional.of(leaseId), Optional.empty(), txnResponse) //
                                                                   : new EtcdResponse(Optional.empty(), Optional.of(leaseId), txnResponse));
    }

    /**
     * Update leaderKey if the key contains ownId.
     * 
     * @param leaseId The id of the lease to use when writing
     * @return Single<EtcdResponse> The transaction response.
     */
    private Single<EtcdResponse> txnRenewLeaderKey(long leaseId)
    {
        return rxEtcd.txn(txn -> txn.If(new Cmp(btSqn.apply(leaderKey), Cmp.Op.EQUAL, CmpTarget.value(btSqn.apply(ownId))))
                                    .Then(Op.put(btSqn.apply(leaderKey), btSqn.apply(ownId), PutOption.newBuilder().withLeaseId(leaseId).build())))
                     .retry(this.retries)
                     .map(txnResponse -> txnResponse.isSucceeded() ? new EtcdResponse(Optional.of(leaseId), Optional.empty(), txnResponse) //
                                                                   : new EtcdResponse(Optional.empty(), Optional.of(leaseId), txnResponse));
    }

    /**
     * Revoke the stale lease if the input ETCD response has failed.
     * 
     * @return EtcdResponse Returns the input ETCD response.
     */
    private SingleTransformer<EtcdResponse, EtcdResponse> revokeStaleLease()
    {
        return upstream -> upstream.flatMap(etcdResponse -> //
        etcdResponse.getStaleLeaseId().isEmpty() ? Single.just(etcdResponse)
                                                 : rxEtcd.leaseRevoke(etcdResponse.getStaleLeaseId().get()).retry(this.retries).map(revokeResponse ->
                                                 {
                                                     log.trace(REVOKE_LEASE_MSG, ownId, etcdResponse.getStaleLeaseId().get());
                                                     return etcdResponse;
                                                 }).onErrorResumeNext(Single.just(etcdResponse)));
    }

    /**
     * Emit the election status on the leaderStatusUpdates Observable.
     * 
     * @param status The current election status.
     */
    private Completable emitStatus(LeaderStatus status)
    {
        log.info("RxLeaderElection.emitStatus starting");

        return Completable.fromAction(() -> leaderStatus.onNext(status));
    }

    /**
     * Apply a random initial jitter to the election process.
     */
    private Completable applyJitter()
    {
        return Observable.timer((long) (jitter * 1000L), TimeUnit.MILLISECONDS).ignoreElements();
    }

    /**
     * Utility class for the cleanup of stale leases.
     */
    private class EtcdResponse
    {
        private final Optional<Long> activeLeaseId;
        private final Optional<Long> staleLeaseId;
        private final TxnResponse txnResponse;

        private EtcdResponse(Optional<Long> activeLeaseId,
                             Optional<Long> staleLeaseId,
                             TxnResponse txnResponse)
        {
            this.activeLeaseId = activeLeaseId;
            this.staleLeaseId = staleLeaseId;
            this.txnResponse = txnResponse;
        }

        private Optional<Long> getActiveLeaseId()
        {
            return activeLeaseId;
        }

        private Optional<Long> getStaleLeaseId()
        {
            return staleLeaseId;
        }

        private TxnResponse getTxnResponse()
        {
            return txnResponse;
        }
    }

    public static class Builder
    {
        private final RxEtcd rxEtcd;
        private final String ownId;
        private final String leaderKey;
        private int leaderInterval;
        private int renewInterval;
        private int claimInterval;
        private float requestLatency;
        private int recoveryDelay;
        private Long retries = null;

        /**
         * Leader election builder.
         * 
         * @param rxEtcd    RxEtcd client to use for ETCD operations.
         * @param ownId     The identity of the contender entity.
         * @param leaderKey The name of the leader key, which is the contention point of
         *                  the election.
         */
        public Builder(RxEtcd rxEtcd,
                       String ownId,
                       String leaderKey)
        {
            this.rxEtcd = rxEtcd;
            this.ownId = ownId;
            this.leaderKey = leaderKey;
        }

        /**
         * @param leaderInterval The duration before the leaderKey expires.
         * @return Builder The Leader Election builder.
         */
        public Builder leaderInterval(int leaderInterval)
        {
            this.leaderInterval = leaderInterval;
            return this;
        }

        /**
         * @param renewInterval The interval between the renew attempts of the leader.
         * @return Builder The Leader Election builder.
         */
        public Builder renewInterval(int renewInterval)
        {
            this.renewInterval = renewInterval;
            return this;
        }

        /**
         * @param claimInterval The interval between the claim attempts of the
         *                      contender.
         * @return Builder The Leader Election builder.
         */
        public Builder claimInterval(int claimInterval)
        {
            this.claimInterval = claimInterval;
            return this;
        }

        /**
         * @param requestLatency The estimated required time for executing an ETCD
         *                       transaction.
         * @return Builder The Leader Election builder.
         */
        public Builder requestLatency(float requestLatency)
        {
            this.requestLatency = requestLatency;
            return this;
        }

        /**
         * @param recoveryDelay The time needed before starting to claim leadership
         *                      again, after an error occurs during the election
         *                      process.
         * @return Builder The Leader Election builder.
         */
        public Builder recoveryDelay(int recoveryDelay)
        {
            this.recoveryDelay = recoveryDelay;
            return this;
        }

        /**
         * @param retries The number of retries when there is an ETCD failure. Null
         *                retries means infinite retries. Null is the default value.
         * @return Builder The Leader Election builder.
         */
        public Builder retries(Long retries)
        {
            this.retries = retries;
            return this;
        }

        /**
         * Builds the Leader Election object.
         * 
         * @return Single<RxLeaderElection> Returns a Single of the Leader Election
         *         object.
         */
        public Single<RxLeaderElection> build()
        {
            return Single.fromCallable(() ->
            {
                sanitizeProperties();
                validateProperties();
                return new RxLeaderElection(this);
            });
        }

        /**
         * Sanitize class properties and assign default values where needed.
         */
        private void sanitizeProperties()
        {
            this.claimInterval = this.claimInterval > 0 ? claimInterval : 2;
            this.renewInterval = this.renewInterval > 0 ? renewInterval : 8;
            this.leaderInterval = this.leaderInterval > 0 ? leaderInterval : 10;
            this.requestLatency = this.requestLatency > 0 ? requestLatency : 0.2f;
            this.recoveryDelay = this.recoveryDelay > 0 ? recoveryDelay : 10;
        }

        /**
         * Validate class properties to enforce correct behavior of leader election.
         */
        private void validateProperties()
        {
            if (claimInterval + requestLatency >= renewInterval)
                throw new IllegalArgumentException("RenewInterval should be greater than claimInterval plus requestLatency");
            if (renewInterval + requestLatency >= leaderInterval)
                throw new IllegalArgumentException("LeaderInterval should be greater than renewInterval plus requestLatency");
            if (rxEtcd.getRequestTimeoutUnit().toSeconds(rxEtcd.getRequestTimeout()) >= claimInterval)
                throw new IllegalArgumentException("The timeout interval of the RxEtcd client must be less than claimInterval");
            if (rxEtcd.getRequestTimeout() == 0)
                throw new IllegalArgumentException("The timeout interval of the RxEtcd client must be bounded");
        }
    }
}
