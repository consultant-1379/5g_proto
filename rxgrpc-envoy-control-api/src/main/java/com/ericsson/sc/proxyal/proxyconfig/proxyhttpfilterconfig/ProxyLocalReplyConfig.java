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
 * Created on: Dec 10, 2020
 *     Author: eavapsr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.LocalReplyMappingDefaults.ProxyLocalReplyMapping;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Struct;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.Value;

import io.envoyproxy.envoy.config.accesslog.v3.AccessLogFilter;
import io.envoyproxy.envoy.config.accesslog.v3.AndFilter;
import io.envoyproxy.envoy.config.accesslog.v3.ComparisonFilter;
import io.envoyproxy.envoy.config.accesslog.v3.MetadataFilter;
import io.envoyproxy.envoy.config.accesslog.v3.ResponseFlagFilter;
import io.envoyproxy.envoy.config.accesslog.v3.StatusCodeFilter;
import io.envoyproxy.envoy.config.core.v3.RuntimeUInt32;
import io.envoyproxy.envoy.config.core.v3.SubstitutionFormatString;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.LocalReplyConfig;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.ResponseMapper;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher.PathSegment;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.envoyproxy.envoy.type.matcher.v3.ValueMatcher;

/**
 * 
 */

public class ProxyLocalReplyConfig implements IfHttpFilter
{

    /*
     * Based on the data filtering of each case the following filter combinations
     * are used:
     * 
     * 0: No action: In case screening action or routing rule is configured then the
     * result code and message body is kept unchanged.
     * 
     * 1: Status code filter
     * 
     * 2: And filter with Status code filter + Response flag filter
     * 
     * 3: And filter with Status code filter + Response flag filter + Metadata
     * filter (routing behavior)
     * 
     * 4: And filter with Status code filter + Metadata filter (routing behavior)
     * 
     * 5: Response flag filter
     * 
     * 6: No action: In case screening action or routing rule is configured then the
     * result code and message body is kept unchanged.
     * 
     */
    private static final int NO_ACTION = 0;
    private static final int STATUS_CODE_FILTER = 1;
    private static final int STATUS_FLAG_FILTER = 2;
    private static final int STATUS_FLAG_META_FILTER = 3;
    private static final int STATUS_META_FILTER = 4;
    private static final int RESPONSE_FLAG_FILTER = 5;

    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        List<ResponseMapper> mapperList = new ArrayList<>();

        var lrMappingDefaults = new LocalReplyMappingDefaults();
        var filters = lrMappingDefaults.getFilters();

        for (var entry : filters)
        {
            var responseMapperBuilder = ResponseMapper.newBuilder();

            if (entry.getFilterType() == NO_ACTION)
            {
                var metaDataFilter = AccessLogFilter.newBuilder()
                                                    .setMetadataFilter(MetadataFilter.newBuilder()
                                                                                     .setMatcher(MetadataMatcher.newBuilder()
                                                                                                                .setFilter("eric_filter")
                                                                                                                .addPath(PathSegment.newBuilder()
                                                                                                                                    .setKey("local_replied")
                                                                                                                                    .build())
                                                                                                                .setValue(ValueMatcher.newBuilder()
                                                                                                                                      .setStringMatch(StringMatcher.newBuilder()
                                                                                                                                                                   .setExact("true")
                                                                                                                                                                   .build())
                                                                                                                                      .build())
                                                                                                                .build())
                                                                                     .setMatchIfKeyNotFound(BoolValue.newBuilder().setValue(false).build())
                                                                                     .build())
                                                    .build();
                responseMapperBuilder.setFilter(metaDataFilter).build();

            }
            else if (entry.getFilterType() == STATUS_CODE_FILTER)
            {
                var statusCodeFilter = generateStatusCodeFilter(entry.getEnvoyStatusCode());
                var subFormatString = generateSubFormatString(entry);

                responseMapperBuilder.setFilter(statusCodeFilter)
                                     .setStatusCode(UInt32Value.of(entry.getThreeGppStatusCode()))
                                     .setBodyFormatOverride(subFormatString)
                                     .build();

            }
            else if (entry.getFilterType() == STATUS_FLAG_FILTER)
            {
                var statusCodeFilter = generateStatusCodeFilter(entry.getEnvoyStatusCode());
                var respFlagFilter = generateResponseFlagFilter(entry.getEnvoyResponseFlag());
                var subFormatString = generateSubFormatString(entry);

                responseMapperBuilder.setFilter(AccessLogFilter.newBuilder()
                                                               .setAndFilter(AndFilter.newBuilder()
                                                                                      .addFilters(statusCodeFilter)
                                                                                      .addFilters(respFlagFilter)
                                                                                      .build())
                                                               .build())
                                     .setStatusCode(UInt32Value.of(entry.getThreeGppStatusCode()))
                                     .setBodyFormatOverride(subFormatString)
                                     .build();

            }
            else if (entry.getFilterType() == STATUS_FLAG_META_FILTER)
            {
                var statusCodeFilter = generateStatusCodeFilter(entry.getEnvoyStatusCode());
                var respFlagFilter = generateResponseFlagFilter(entry.getEnvoyResponseFlag());
                var metaFilter = generateMetaDataFilter(entry.getRoutingType());
                var subFormatString = generateSubFormatString(entry);

                responseMapperBuilder.setFilter(AccessLogFilter.newBuilder()
                                                               .setAndFilter(AndFilter.newBuilder()
                                                                                      .addFilters(statusCodeFilter)
                                                                                      .addFilters(respFlagFilter)
                                                                                      .addFilters(metaFilter)
                                                                                      .build())
                                                               .build())
                                     .setStatusCode(UInt32Value.of(entry.getThreeGppStatusCode()))
                                     .setBodyFormatOverride(subFormatString)
                                     .build();

            }

            else if (entry.getFilterType() == STATUS_META_FILTER)
            {
                var statusCodeFilter = generateStatusCodeFilter(entry.getEnvoyStatusCode());
                var metaFilter = generateMetaDataFilter(entry.getRoutingType());
                var subFormatString = generateSubFormatString(entry);

                responseMapperBuilder.setFilter(AccessLogFilter.newBuilder()
                                                               .setAndFilter(AndFilter.newBuilder().addFilters(statusCodeFilter).addFilters(metaFilter).build())
                                                               .build())
                                     .setStatusCode(UInt32Value.of(entry.getThreeGppStatusCode()))
                                     .setBodyFormatOverride(subFormatString)
                                     .build();

            }
            else if (entry.getFilterType() == RESPONSE_FLAG_FILTER)
            {
                var respFlagFilter = generateResponseFlagFilter(entry.getEnvoyResponseFlag());
                var subFormatString = generateSubFormatString(entry);

                responseMapperBuilder.setFilter(respFlagFilter)
                                     .setStatusCode(UInt32Value.of(entry.getThreeGppStatusCode()))
                                     .setBodyFormatOverride(subFormatString)
                                     .build();

            }
            else
            {
                throw new BadConfigurationException("No valid filter");
            }

            mapperList.add(responseMapperBuilder.build());

        }

        builder.setLocalReplyConfig(LocalReplyConfig.newBuilder().addAllMappers(mapperList).build());
    }

    /*
     * Helper methods for filter creation
     */
    private AccessLogFilter generateResponseFlagFilter(List<String> filterFlags)
    {
        return AccessLogFilter.newBuilder().setResponseFlagFilter(ResponseFlagFilter.newBuilder().addAllFlags(filterFlags).build()).build();

    }

    private AccessLogFilter generateStatusCodeFilter(int envoyStatusCode)
    {

        return AccessLogFilter.newBuilder()
                              .setStatusCodeFilter(StatusCodeFilter.newBuilder() // response flag filter
                                                                   .setComparison(ComparisonFilter.newBuilder()
                                                                                                  .setOp(ComparisonFilter.Op.EQ)
                                                                                                  .setValue(RuntimeUInt32.newBuilder()
                                                                                                                         .setDefaultValue(envoyStatusCode)
                                                                                                                         .setRuntimeKey("null")
                                                                                                                         .build())
                                                                                                  .build())
                                                                   .build())
                              .build();
    }

    private AccessLogFilter generateMetaDataFilter(String routingType)
    {

        return AccessLogFilter.newBuilder()
                              .setMetadataFilter(MetadataFilter.newBuilder()
                                                               .setMatcher(MetadataMatcher.newBuilder()
                                                                                          .setFilter("eric_proxy")
                                                                                          .addPath(PathSegment.newBuilder().setKey("routing-behaviour").build())
                                                                                          .setValue(ValueMatcher.newBuilder()
                                                                                                                .setStringMatch(StringMatcher.newBuilder()
                                                                                                                                             .setContains(routingType)
                                                                                                                                             .build())
                                                                                                                .build())
                                                                                          .build())
                                                               .setMatchIfKeyNotFound(BoolValue.newBuilder().setValue(false).build())
                                                               .build())
                              .build();
    }

    private SubstitutionFormatString generateSubFormatString(ProxyLocalReplyMapping entry)
    {

        var structBuilder = Struct.newBuilder()
                                  .putFields("title", Value.newBuilder().setStringValue(entry.getTitle()).build())
                                  .putFields("status", Value.newBuilder().setNumberValue(entry.getStatus()).build());

        entry.getCause().ifPresentOrElse(cause ->
        {
            structBuilder.putFields("cause", Value.newBuilder().setStringValue(entry.getCause().get()).build());
            structBuilder.putFields("detail",
                                    Value.newBuilder()
                                         .setStringValue(entry.getEnvoyStatusCode() == 418 ? "Route not found (No VHost match)" : "%RESPONSE_CODE_DETAILS%")
                                         .build());

        }, () -> structBuilder.putFields("detail", Value.newBuilder().setStringValue("%RESPONSE_CODE_DETAILS%").build()));

        return SubstitutionFormatString.newBuilder().setContentType("application/problem+json").setJsonFormat(structBuilder.build()).build();
    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.LOCAL_REPLY_CONFIG;
    }

}