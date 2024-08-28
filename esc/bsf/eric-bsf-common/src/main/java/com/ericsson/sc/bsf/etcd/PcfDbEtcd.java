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
 * Created on: Mar 9, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.json.Smile;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Enables persisting objects to the PCF etcd database
 */
public final class PcfDbEtcd
{

    private static final Logger log = LoggerFactory.getLogger(PcfDbEtcd.class);
    private final ObjectMapper om;
    private final RxEtcd etcd;
    private final long ttl;
    private final RetryFunction watcherRetryFunction;
    public final PcfDbSerializer serializer;

    /**
     * 
     * @param etcd          An etcd client instance
     * @param useBinaryJson True if SMILE data encoding should be used for the
     *                      persisted objects, false for simple JSON encoding
     */
    public PcfDbEtcd(RxEtcd etcd,
                     boolean useBinaryJson,
                     long ttl)

    {
        this.etcd = etcd;
        om = useBinaryJson ? Smile.om() : Jackson.om();
        // etcd watcher retry strategy. Keep trying for 2 minutes, then fail
        this.watcherRetryFunction = new RetryFunction().withRetries(-1) // infinite retries
                                                       .withDelay(5 * 1000L) // 5 second delay between retries
                                                       .withJitter(30) //
                                                       .withRetryAction((ex,
                                                                         cnt) -> log.warn("Etcd watch failed, retry: {}", cnt, ex));

        this.serializer = new PcfDbSerializer(this.om);
        this.ttl = ttl;
    }

    /**
     * 
     * @return All the keys for PcfNf objects stored in database
     */
    public Single<Stream<UUID>> getPcfKeys()
    {
        final var options = GetOption.builder()
                                     .isPrefix(true)
                                     .withKeysOnly(true)
                                     .withLimit(0) // zero means no limit
                                     .build();

        return etcd.get(serializer.pcfNf().getPrefixBytes(), options) //
                   .map(getResp -> getResp.getKvs() //
                                          .stream()
                                          .map(kv ->
                                          {
                                              try
                                              {
                                                  return serializer.pcfNf().key(kv.getKey());
                                              }
                                              catch (Exception e)
                                              {
                                                  log.warn("Ignored invalid DynamicProducer, key: {}", kv.getKey(), e);
                                                  return null;
                                              }
                                          })
                                          .filter(Objects::nonNull));
    }

    /**
     * 
     * @return All the PcfNf objects stored in database
     */
    public Single<EtcdEntries<UUID, PcfNf>> getPcfs()
    {
        final var options = GetOption.builder()
                                     .isPrefix(true)
                                     .withLimit(0) // zero means no limit
                                     .build();

        return etcd.get(serializer.pcfNf().getPrefixBytes(), options) //
                   .map(getResp -> new EtcdEntries<>(getResp.getHeader().getRevision(),
                                                     getResp.getKvs() //
                                                            .stream()
                                                            .map(kv ->
                                                            {
                                                                try
                                                                {
                                                                    return new EtcdEntry<>(serializer.pcfNf(), kv);
                                                                }
                                                                catch (Exception e)
                                                                {
                                                                    log.warn("Ignored invalid DynamicProducer, key: {}", kv.getKey(), e);
                                                                    return null;
                                                                }
                                                            })
                                                            .filter(Objects::nonNull)
                                                            .toList()));
    }

    /**
     * Delete a collection of PcfNf objects, ignoring non existing objects
     * 
     * @param uuids The keys for the objects that should be deleted
     * @return A Completable that performs the operation upon subscription
     */
    public Single<List<Long>> delete(final Collection<UUID> uuids)
    {
        return Flowable.fromIterable(uuids) //
                       .map(this.serializer.pcfNf()::keyBytes)
                       .concatMapSingle(etcd::delete)
                       .map(DeleteResponse::getDeleted)
                       .toList();
    }

    /**
     * Deletes all PcfNf entries from the etcd Database.
     * 
     * @return A completable of the deletion action
     */
    public Completable truncate()
    {
        final var options = DeleteOption.builder().isPrefix(true).build();

        return this.etcd.delete(serializer.pcfNf().getPrefixBytes(), options).ignoreElement();
    }

    /**
     * Creates or Updates PcfNf objects in etcd database
     * 
     * @param pcfs The PCFs to create or update
     * @return A Completable that performs the database operation upon subscription
     */
    public Completable createOrUpdate(final Iterable<PcfNf> pcfs)
    {
        return Flowable.fromIterable(pcfs) //
                       .concatMapCompletable(this::createOrUpdate);
    }

    /**
     * Persist a PcfNf object to database. Existing objects shall be updated
     * 
     * @param pcf The object to persist
     * @return A Completable that performs the operation upon subscription
     */
    public Completable createOrUpdate(final PcfNf pcf)
    {
        final var key = this.serializer.pcfNf().keyBytes(pcf.getNfInstanceId());
        final var value = this.serializer.pcfNf().valueBytes(pcf);

        return this.etcd.leaseCreate(this.ttl)
                        .map(LeaseGrantResponse::getID)
                        .flatMap(id -> this.etcd.put(key, value, PutOption.builder().withPrevKV().withLeaseId(id).build()))
                        .ignoreElement();
    }

    /**
     * Create a database watcher.
     * 
     * @return A newly created database watcher
     */
    public PcfDbWatcher newWatcher()
    {
        return new PcfDbWatcher(this, watcherRetryFunction);
    }

    RxEtcd getEtcd()
    {
        return this.etcd;
    }
}
