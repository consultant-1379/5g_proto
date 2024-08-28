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
 * Created on: Feb 8, 2022
 *     Author: eedstl
 */

package com.ericsson.sc.ratelimiting;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.ratelimiting.service.grpc.PullTokensRequest;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensResponse;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult.ResultCode;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * 
 */
public class RateLimitingService
{
    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private static class BucketConfig
    {
        final String name;
        final double fillRate;
        final long capacity;

        public BucketConfig(final String name,
                            final double fillRate,
                            final long capacity)
        {
            this.name = name;
            this.fillRate = fillRate;
            this.capacity = capacity;
        }

        public long getCapacity()
        {
            return this.capacity;
        }

        public double getFillRate()
        {
            return this.fillRate;
        }

        public String getName()
        {
            return this.name;
        }
    }

    private static class TokenBucket
    {
        private final BucketConfig config;
        private final String id;
        private double tokens;
        private Instant lastPull;
        private Object lock = new Object();

        public TokenBucket(final String namespace,
                           final BucketConfig config)
        {
            this.config = config;
            this.id = toId(namespace, config.getName());
            this.tokens = this.config.getCapacity();
            this.lastPull = Instant.now();
        }

        public BucketConfig getConfig()
        {
            return this.config;
        }

        /**
         * @return The fill grade [%] of the bucket.
         */
        public double getFillGrade()
        {
            synchronized (this.lock)
            {
                return 100d * this.tokens / this.config.getCapacity();
            }
        }

        /**
         * @return The bucket's name.
         */
        public String getName()
        {
            return this.config.getName();
        }

        /**
         * Pull given amount tokens if the bucket's fill grade is not smaller than the
         * given water mark [%] and return the result of the operation.
         * 
         * @param watermark [%] The water mark below which the bucket is considered
         *                  empty.
         * @param amount    The amount of tokens to be pulled.
         * @return PullTokensResult
         */
        public Single<PullTokensResult> pull(final double watermark,
                                             final int amount)
        {
            return Single.fromCallable(() ->
            {
                synchronized (this.lock)
                {
                    // Retrieve data from backend:

                    final Instant lastPull = this.lastPull;
                    double tokens = this.tokens;

                    // Work with the data:

                    final Instant now = Instant.now();
                    final Duration duration = Duration.between(lastPull, Instant.now());
                    final double timeSpan = duration.getSeconds() + duration.getNano() / 1e9; // [s]

                    tokens = Math.max(0, Math.min(tokens + this.config.getFillRate() * timeSpan, this.config.getCapacity()) - amount);

//                    log.info("lastPull={}, tokens={}, fillGrade={}", lastPull, tokens, 100 * tokens / this.config.getCapacity());

                    if (watermark > 100 * tokens / this.config.getCapacity())
                        return PullTokensResult.newBuilder().setRc(ResultCode.TooManyRequests).setRa(3).build();

                    // Store data in backend:

                    this.tokens = tokens;
                    this.lastPull = now;

//                    log.info("{}", this);

                    return PullTokensResult.newBuilder().setRc(ResultCode.Ok).build();
                }
            });
        }

        @Override
        public String toString()
        {
            return new StringBuilder("{").append("id=")
                                         .append(this.id)
                                         .append(", lastPull=")
                                         .append(this.lastPull)
                                         .append(", tokens=")
                                         .append(this.tokens)
                                         .append(", fillGrade=")
                                         .append(this.getFillGrade())
                                         .append("}")
                                         .toString();
        }
    }

    private Map<String, TokenBucket> buckets = new ConcurrentSkipListMap<>();

    private static String toId(final String namespace,
                               final String name)
    {
        return new StringBuilder(namespace).append(":").append(name).toString();
    }

    public RateLimitingService()
    {
        this.buckets.put("scp:bucket-1", new TokenBucket("scp", new BucketConfig("bucket-1", 10d, 100l)));
    }

    public Single<PullTokensResponse> pullTokens(final PullTokensRequest request)
    {
//        log.info("pullTokens");

        return Flowable.fromIterable(request.getContextsList())
                       .concatMapSingle(ctx -> this.buckets.get(toId(request.getNamespace(), ctx.getName())).pull(ctx.getWatermark(), ctx.getAmount()))
                       .reduce(PullTokensResponse.newBuilder(),
                               (r,
                                v) -> r.addResult(v))
                       .map(PullTokensResponse.Builder::build);
    }
}
