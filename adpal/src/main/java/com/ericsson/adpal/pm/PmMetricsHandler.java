/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 20, 2018
 *     Author: xkorpap
 */

package com.ericsson.adpal.pm;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

//FIXME move this class to "utilities"
public class PmMetricsHandler implements Handler<RoutingContext>
{
    private static final Logger log = LoggerFactory.getLogger(PmMetricsHandler.class);

    /**
     * Wrap a Vert.x Buffer as a Writer so it can be used with TextFormat writer
     */
    private static class BufferWriter extends Writer
    {

        private final Buffer buffer = Buffer.buffer();

        @Override
        public void write(char[] cbuf,
                          int off,
                          int len) throws IOException
        {
            buffer.appendString(new String(cbuf, off, len));
        }

        @Override
        public void flush() throws IOException
        {
            // NO-OP
        }

        @Override
        public void close() throws IOException
        {
            // NO-OP
        }

        Buffer getBuffer()
        {
            return buffer;
        }

        @Override
        public String toString()
        {
            return buffer.toString();
        }
    }

    private List<CollectorRegistry> registries;

    /**
     * Construct a MetricsHandler for the default registry.
     */
    public PmMetricsHandler()
    {
        this(List.of(CollectorRegistry.defaultRegistry));
    }

    /**
     * Construct a MetricsHandler for the given registry.
     */
    public PmMetricsHandler(List<CollectorRegistry> registries)
    {
        Objects.requireNonNull(registries);
        this.registries = registries;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        try (final var writer = new BufferWriter())
        {
            for (final var registry : registries)
            {
                TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(ctx.request())));
            }
            log.trace("Scraping PM counters: {}", writer);
            ctx.response().setStatusCode(200).putHeader("Content-Type", TextFormat.CONTENT_TYPE_004).end(writer.getBuffer());
        }
        catch (Exception e)
        {
            log.warn("Failed to scrape PM counters due to unexpected error", e);
            ctx.fail(e);
        }

    }

    private Set<String> parse(HttpServerRequest request)
    {
        return new HashSet<>(request.params().getAll("name[]"));
    }
}
