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
 * Created on: Oct 25, 2021
 *     Author: echaias
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.core.v3.HeaderValue;
import io.envoyproxy.envoy.config.core.v3.HeaderValueOption;
import io.envoyproxy.envoy.config.core.v3.HeaderValueOption.HeaderAppendAction;
import io.envoyproxy.envoy.config.core.v3.RuntimeFractionalPercent;
import io.envoyproxy.envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.type.v3.FractionalPercent;
import io.envoyproxy.envoy.type.v3.FractionalPercent.DenominatorType;
import io.envoyproxy.envoy.type.v3.TokenBucket;

/**
 * 
 */
public class ProxyLocalRateLimiter implements IfHttpFilter
{

    private final String statPrefix;
    private final Integer maxTokens;
    private final Integer tokensPerFill;
    private final Integer fillInterval;
    private final Map<String, String> responseHeaders;

    public ProxyLocalRateLimiter(String statPrefix,
                                 Integer maxTokens,
                                 Integer tokensPerFill,
                                 Integer fillInterval,
                                 Map<String, String> headers)
    {
        this.statPrefix = statPrefix;
        this.maxTokens = maxTokens;
        this.tokensPerFill = tokensPerFill;
        this.fillInterval = fillInterval;
        this.responseHeaders = headers;

    }

    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {

        long fillIntervalSeconds = TimeUnit.MILLISECONDS.toSeconds(fillInterval);
        var fillIntervalNanos = (int) TimeUnit.MILLISECONDS.toNanos(fillInterval - TimeUnit.SECONDS.toMillis(fillIntervalSeconds));
        var localRateLimitFilterConfig = LocalRateLimit.newBuilder()
                                                       .setStatPrefix(statPrefix)
                                                       .setTokenBucket(TokenBucket.newBuilder()
                                                                                  .setMaxTokens(maxTokens)
                                                                                  .setFillInterval(Duration.newBuilder()
                                                                                                           .setSeconds(fillIntervalSeconds)
                                                                                                           .setNanos(fillIntervalNanos)
                                                                                                           .build())
                                                                                  .setTokensPerFill(UInt32Value.of(tokensPerFill))
                                                                                  .build())
                                                       .setFilterEnabled(RuntimeFractionalPercent.newBuilder()
                                                                                                 .setRuntimeKey("local_rate_limit_enabled")
                                                                                                 .setDefaultValue(FractionalPercent.newBuilder()
                                                                                                                                   .setNumerator(100)
                                                                                                                                   .setDenominator(DenominatorType.HUNDRED)
                                                                                                                                   .build())
                                                                                                 .build())
                                                       .setFilterEnforced(RuntimeFractionalPercent.newBuilder()
                                                                                                  .setRuntimeKey("local_rate_limit_enforced")
                                                                                                  .setDefaultValue(FractionalPercent.newBuilder()
                                                                                                                                    .setNumerator(100)
                                                                                                                                    .setDenominator(DenominatorType.HUNDRED)
                                                                                                                                    .build())
                                                                                                  .build());

        var index = 0;
        for (var header : responseHeaders.entrySet())
        {
            var headerConfig = HeaderValueOption.newBuilder()
                                                .setHeader(HeaderValue.newBuilder().setKey(header.getKey()).setValue(header.getValue()).build())
                                                .setAppendAction(HeaderAppendAction.APPEND_IF_EXISTS_OR_ADD)
                                                .build();
            localRateLimitFilterConfig.addResponseHeadersToAdd(index++, headerConfig);
        }
        var localRateLimitFilterConfigAny = Any.pack(localRateLimitFilterConfig.build());
        builder.addHttpFilters(HttpFilter.newBuilder().setName("envoy.filters.http.local_ratelimit").setTypedConfig(localRateLimitFilterConfigAny).build());

    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.LOCAL_RATE_LIMIT;
    }

}
