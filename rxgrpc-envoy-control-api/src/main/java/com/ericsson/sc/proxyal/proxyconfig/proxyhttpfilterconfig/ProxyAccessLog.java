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

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.envoyproxy.envoy.config.accesslog.v3.AccessLog;
import io.envoyproxy.envoy.config.accesslog.v3.AccessLogFilter;
import io.envoyproxy.envoy.config.accesslog.v3.MetadataFilter;
import io.envoyproxy.envoy.config.core.v3.SubstitutionFormatString;
import io.envoyproxy.envoy.extensions.access_loggers.stream.v3.StdoutAccessLog;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher.PathSegment;
import io.envoyproxy.envoy.type.matcher.v3.ValueMatcher;

/**
 * 
 */
public class ProxyAccessLog implements IfHttpFilter
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#appendConfig(
     * com.ericsson.sc.proxyal.proxyconfig.ProxyListener,
     * io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.
     * HttpConnectionManager.Builder)
     */
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        // Fields to include in the access log, json-formatted.
        // The timeformat does not allow to specify the timezone as "+00:00", %z gives
        // "+0000"

        SubstitutionFormatString jsonFormatSource = SubstitutionFormatString.newBuilder()
                                                                            .setOmitEmptyValues(true)
                                                                            .setJsonFormat(Struct.newBuilder()
                                                                                                 .putFields("version",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(LOG_VERSION)%")
                                                                                                                 .build())
                                                                                                 .putFields("timestamp",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%START_TIME(%FT%T.%3f%z)%")
                                                                                                                 .build())
                                                                                                 .putFields("severity",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(SEVERITY)%")
                                                                                                                 .build())
                                                                                                 .putFields("message",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(MSG)%")
                                                                                                                 .build())
                                                                                                 .putFields("metadata",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStructValue(Struct.newBuilder()
                                                                                                                                       .putFields("application_id",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStringValue("%EVENT(APPL_ID)%")
                                                                                                                                                       .build())
                                                                                                                                       .putFields("function",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStringValue("sepp-function")
                                                                                                                                                       .build())
                                                                                                                                       .putFields("proc_id",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStringValue("envoy")
                                                                                                                                                       .build())
                                                                                                                                       .putFields("ul_id",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStringValue("%EVENT(ULID)%")
                                                                                                                                                       .build())
                                                                                                                                       .putFields("category",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStringValue("%EVENT(CATEGORY)%")
                                                                                                                                                       .build())
                                                                                                                                       .build())

                                                                                                                 .build())
                                                                                                 .putFields("service_id",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(SRC_TYPE)%")
                                                                                                                 .build())

                                                                                                 .putFields("extra_data",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStructValue(Struct.newBuilder()
                                                                                                                                       .putFields("sc_event",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStructValue(Struct.newBuilder()
                                                                                                                                                                             .putFields("id",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("SC_EVENT_SEPP_%STREAM_ID%-%EVENT(INDEX)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("type",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("%EVENT(TYPE)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("version",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("%EVENT(VERSION)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("log_type",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("sc-event")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("action",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("%EVENT(ACTION)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("roaming_partner",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("%EVENT(RP)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .putFields("sub_spec",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("%EVENT(SUB_SPEC)%")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .build())
                                                                                                                                                       .build())
                                                                                                                                       .putFields("onap",
                                                                                                                                                  Value.newBuilder()
                                                                                                                                                       .setStructValue(Struct.newBuilder()
                                                                                                                                                                             .putFields("nfVendorName",
                                                                                                                                                                                        Value.newBuilder()
                                                                                                                                                                                             .setStringValue("Ericsson AB")
                                                                                                                                                                                             .build())
                                                                                                                                                                             .build())
                                                                                                                                                       .build())
                                                                                                                                       .build())
                                                                                                                 .build())
                                                                                                 .putFields("subject",
                                                                                                            Value.newBuilder().setStringValue("admin").build())
                                                                                                 .putFields("resp_message",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(RESP_MSG)%")
                                                                                                                 .build())
                                                                                                 .putFields("resp_code",
                                                                                                            Value.newBuilder()
                                                                                                                 .setStringValue("%EVENT(RESP_CODE)%")
                                                                                                                 .build())
                                                                                                 .build())
                                                                            .build();

        var accessLogTypedConfig = StdoutAccessLog.newBuilder() //
                                                  .setLogFormat(jsonFormatSource)
                                                  .build();
        builder.addAccessLog(AccessLog.newBuilder() //
                                      .setName("envoy.access_loggers.file") //
                                      .setFilter(AccessLogFilter.newBuilder()
                                                                .setMetadataFilter(MetadataFilter.newBuilder()
                                                                                                 .setMatchIfKeyNotFound(BoolValue.newBuilder().setValue(false))
                                                                                                 .setMatcher(MetadataMatcher.newBuilder()
                                                                                                                            .setFilter("eric_event")
                                                                                                                            .setValue(ValueMatcher.newBuilder()
                                                                                                                                                  .setPresentMatch(true)
                                                                                                                                                  .build())
                                                                                                                            .addPath(PathSegment.newBuilder()
                                                                                                                                                .setKey("is_event"))
                                                                                                                            .build())))
                                      .setTypedConfig(Any.pack(accessLogTypedConfig)) //
                                      .build());

    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.ACCESS_LOG;
    }

}
