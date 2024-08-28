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
 * Created on: Jul 14, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.net.InetAddresses;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of HTTP requests for BSF. This class is responsible to
 * generate the requests according to the provided configuration options, such
 * as the traffic rate, the number of requests, the IP ranges of the produced
 * requests and the request type.
 */
public class TrafficSet implements Comparable<TrafficSet>
{
    private static final Logger log = LoggerFactory.getLogger(TrafficSet.class);
    private static final JsonMapper jm = JsonMapper.builder().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true).build();

    private static final String API_PATH = "/nbsf-management/v1/pcfBindings";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer %s";
    private static final String SBI_NF_PEER_INFO_HEADER_NAME = "3gpp-Sbi-NF-Peer-Info";
    private static final String SBI_NF_PEER_INFO_HEADER_VALUE = "srcinst=DummySrcInst; srcservinst=DummySrcServInst; srcscp=DummySrcScp";

    private final BsfLoadConfiguration bsfLoadConfig;
    private final TrafficSetConfiguration setConfig;
    private final long timeout;

    private final List<WebClient> webClients;
    private final AtomicInteger clientIndex = new AtomicInteger(0);

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        ERROR_RESPONSE
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    public TrafficSet(BsfLoadConfiguration bsfLoadConfig,
                      TrafficSetConfiguration setConfig,
                      List<WebClient> webClients)
    {
        this.bsfLoadConfig = bsfLoadConfig;
        this.setConfig = setConfig;
        this.timeout = (setConfig.getTimeout() != null) ? setConfig.getTimeout() : bsfLoadConfig.getTimeout();
        this.webClients = webClients;
    }

    public Flowable<Response> execute(Flowable<String> bindingIds)
    {
        Flowable<Response> execution;
        Flowable<RequestInput> requestInput;

        switch (setConfig.getType())
        {
            case DEREGISTER:
                // If no bindingIds are provided, then generate random bindingIds.
                var input = (bindingIds != null) ? bindingIds : generateBindingIds();
                requestInput = input.map(bindingId -> new RequestInput(API_PATH + "/" + bindingId, null));
                execution = execute(requestInput, deregister());
                break;

            case DISCOVERY:
                requestInput = generateIpv4Addresses().map(inet4Addr -> new RequestInput(API_PATH + "?ipv4Addr=" + inet4Addr, null));
                execution = execute(requestInput, discovery());
                break;

            case REGISTER:
                requestInput = generateBindings().map(binding -> new RequestInput(API_PATH + "/", binding));
                execution = execute(requestInput, register());
                break;

            default:
                execution = Flowable.error(new RuntimeException("Wrong request type provided."));
        }
        return execution;
    }

    private Flowable<Response> execute(Flowable<RequestInput> requestInput,
                                       Function<RequestInput, Single<Response>> executor)
    {
        return rateController().zipWith(requestInput,
                                        (tick,
                                         inputSourceValue) -> inputSourceValue)
                               .flatMapSingle(executor::apply, false, bsfLoadConfig.getMaxParallelTransactions())
                               .doOnNext(resp ->
                               {
                                   if (!resp.success)
                                       safeLog.log(Lbl.ERROR_RESPONSE, l -> log.info(resp.getErrorMsg()));
                               });
    }

    public Function<RequestInput, Single<Response>> deregister()
    {
        return input -> Single.defer(() ->
        {
            try
            {
                final var token = this.setConfig.getAuthAccessToken();
                final String targetHost;
                if (setConfig.getTargetHost().contains(":")) // check if the IP needs sq brackets or if they already exist
                {
                    targetHost = setConfig.getTargetHost().contains("[") ? setConfig.getTargetHost() : "[" + setConfig.getTargetHost() + "]";
                }
                else
                {
                    targetHost = setConfig.getTargetHost();
                }

                var request = this.getNextClient().delete(bsfLoadConfig.getTargetPort(), targetHost, input.getTargetResource());

                // Add oAuth header if the feature is enabled
                request = token != null ? request.putHeader(AUTH_HEADER, String.format(BEARER, token)) : request;
                // Add sbi-nf-peer-info header if the feature is enabled
                request = this.setConfig.getSbiNfPeerInfoEnabled() ? request.putHeader(SBI_NF_PEER_INFO_HEADER_NAME, SBI_NF_PEER_INFO_HEADER_VALUE) : request;

                return request.timeout(this.timeout).rxSend().map(Response::new).onErrorReturn(Response::new);

            }
            catch (Exception e)
            {
                return Single.just(new Response(e));
            }
        });

    }

    public Function<RequestInput, Single<Response>> discovery()
    {
        return input -> Single.defer(() ->
        {
            try
            {
                final var token = this.setConfig.getAuthAccessToken();
                final String targetHost;
                if (setConfig.getTargetHost().contains(":")) // check if the IP needs sq brackets or if they already exist
                {
                    targetHost = setConfig.getTargetHost().contains("[") ? setConfig.getTargetHost() : "[" + setConfig.getTargetHost() + "]";
                }
                else
                {
                    targetHost = setConfig.getTargetHost();
                }
                var request = this.getNextClient().get(bsfLoadConfig.getTargetPort(), targetHost, input.getTargetResource());

                // Add oAuth header if the feature is enabled
                request = token != null ? request.putHeader(AUTH_HEADER, String.format(BEARER, token)) : request;
                // Add sbi-nf-peer-info header if the feature is enabled
                request = this.setConfig.getSbiNfPeerInfoEnabled() ? request.putHeader(SBI_NF_PEER_INFO_HEADER_NAME, SBI_NF_PEER_INFO_HEADER_VALUE) : request;

                return request.timeout(this.timeout).rxSend().map(Response::new).onErrorReturn(Response::new);
            }
            catch (Exception e)
            {
                return Single.just(new Response(e));
            }
        });

    }

    public Function<RequestInput, Single<Response>> register()
    {
        return input -> Single.defer(() ->
        {
            try
            {
                final var token = this.setConfig.getAuthAccessToken();
                final String targetHost;
                if (setConfig.getTargetHost().contains(":")) // check if the IP needs sq brackets or if they already exist
                {
                    targetHost = setConfig.getTargetHost().contains("[") ? setConfig.getTargetHost() : "[" + setConfig.getTargetHost() + "]";
                }
                else
                {
                    targetHost = setConfig.getTargetHost();
                }
                var request = this.getNextClient().post(bsfLoadConfig.getTargetPort(), targetHost, input.getTargetResource());

                // Add oAuth header if the feature is enabled
                request = token != null ? request.putHeader(AUTH_HEADER, String.format(BEARER, token)) : request;
                // Add sbi-nf-peer-info header if the feature is enabled
                request = this.setConfig.getSbiNfPeerInfoEnabled() ? request.putHeader(SBI_NF_PEER_INFO_HEADER_NAME, SBI_NF_PEER_INFO_HEADER_VALUE) : request;

                return request.timeout(this.timeout)
                              .putHeader("Content-Type", "application/json")
                              .rxSendBuffer(Buffer.buffer(jm.writeValueAsBytes(input.getBinding())))
                              .map(Response::new)
                              .onErrorReturn(Response::new);
            }
            catch (Exception e)
            {
                return Single.just(new Response(e));
            }
        });
    }

    private WebClient getNextClient()
    {
        return this.webClients.get(clientIndex.getAndIncrement() % webClients.size());
    }

    /**
     * Generates ipv4 addresses according to the given ip-range.
     * 
     * @return A Flowable of ipv4 addresses in String format
     */
    public Flowable<String> generateIpv4Addresses()
    {
        final var inetAddr = InetAddresses.toBigInteger(InetAddresses.forString(setConfig.getIpRange().getStartIP()));
        return Flowable.rangeLong(0L, setConfig.getIpRange().getRange())
                       .map(i -> InetAddresses.fromIPv4BigInteger(inetAddr.add(BigInteger.valueOf(i))))
                       .map(Inet4Address::getHostAddress)
                       .repeat()
                       .take(setConfig.getNumRequests());
    }

    /**
     * Generates PcfBindings according to the given ip-range.
     * 
     * @return A Flowable of PcfBindings
     */
    public Flowable<PcfBinding> generateBindings()
    {
        return generateIpv4Addresses().map(this::createBinding);
    }

    /**
     * Generates random binding-ids.
     * 
     * @return A Flowable of binding-ids in String format
     */
    public Flowable<String> generateBindingIds()
    {
        return Flowable.rangeLong(0L, setConfig.getNumRequests()).map(i -> UUID.randomUUID().toString());
    }

    private PcfBinding createBinding(String ipv4Addr)
    {
        String supi = "imsi-310150123456789";
        String gpsi = "msisdn-918369110173";
        String dnn = "valid.ericsson.se";
        String pcfFqdn = "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org";
        String ipDomain = "dom" + ipv4Addr;
        Snssai snssai = Snssai.create(64, "E8F44A");
        final var pcfId = setConfig.getPcfId();
        final var optionalRecoveryTime = setConfig.getRecoveryTime();
        final var recoveryTime = optionalRecoveryTime.isPresent() ? optionalRecoveryTime.get() : null;

        return PcfBinding.createJson(supi,
                                     gpsi,
                                     ipv4Addr,
                                     null,
                                     ipDomain,
                                     null,
                                     dnn,
                                     pcfFqdn,
                                     null,
                                     null,
                                     null,
                                     snssai,
                                     pcfId,
                                     recoveryTime,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null);
    }

    /**
     * Controls the rate at which the requests are produced.
     * 
     * @return A Flowable of long numbers
     */
    public Flowable<Long> rateController()
    {
        return Flowable.intervalRange(0L, //
                                      setConfig.getNumRequests(),
                                      0L,
                                      1000000L / setConfig.getTps(),
                                      TimeUnit.MICROSECONDS)
                       .onBackpressureBuffer();
    }

    /**
     * @return the setConfig
     */
    public TrafficSetConfiguration getSetConfig()
    {
        return setConfig;
    }

    @Override
    public int compareTo(TrafficSet o)
    {
        return Integer.compare(this.getSetConfig().getOrder(), o.getSetConfig().getOrder());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bsfLoadConfig == null) ? 0 : bsfLoadConfig.hashCode());
        result = prime * result + ((setConfig == null) ? 0 : setConfig.hashCode());
        result = prime * result + (int) (timeout ^ (timeout >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrafficSet other = (TrafficSet) obj;
        if (bsfLoadConfig == null)
        {
            if (other.bsfLoadConfig != null)
                return false;
        }
        else if (!bsfLoadConfig.equals(other.bsfLoadConfig))
            return false;
        if (setConfig == null)
        {
            if (other.setConfig != null)
                return false;
        }
        else if (!setConfig.equals(other.setConfig))
            return false;
        if (timeout != other.timeout)
            return false;
        return true;
    }

    static final class RequestInput
    {
        final PcfBinding binding;
        final String targetResource;

        RequestInput(String targetUrlResource,
                     PcfBinding binding)
        {
            this.binding = binding;
            this.targetResource = targetUrlResource;
        }

        PcfBinding getBinding()
        {
            return binding;
        }

        String getTargetResource()
        {
            return targetResource;
        }
    }
}
