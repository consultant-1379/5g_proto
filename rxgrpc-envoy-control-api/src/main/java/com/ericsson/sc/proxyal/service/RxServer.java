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
 * Created on: Apr 10, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.proxyal.service;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.file.SipTlsCertWatch;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.reactivex.Completable;

/**
 * RX wrapper around io.grpc.Server.
 */
public class RxServer
{
    private static final Logger log = LoggerFactory.getLogger(RxServer.class);

    private io.grpc.Server server;
    private Optional<EpollEventLoopGroup> group = Optional.empty();
    private Optional<String> socketPath = Optional.empty();

    public RxServer(int port,
                    BindableService service)
    {
        this.server = ServerBuilder.forPort(port).addService(service).build();
    }

    public RxServer(int port,
                    List<BindableService> services)
    {
        final URI grpc_server_certificate_path = URI.create(EnvVars.get("MANAGER_GRPC_SERVER_CERT_PATH", "/run/secrets/grpc/certificates"));
        final URI grpc_client_ca_path = URI.create(EnvVars.get("MANAGER_GRPC_CLIENT_CA_PATH", "/run/secrets/grpc/worker/ca"));

        try
        {
            DynamicTlsCertManager dynamicTlsManager = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(grpc_server_certificate_path.getPath()),
                                                                                   SipTlsCertWatch.trustedCert(grpc_client_ca_path.getPath()));
            var dynamicTlsManagerOpt = Optional.ofNullable(dynamicTlsManager);
            Completable init = dynamicTlsManagerOpt.map(DynamicTlsCertManager::start) //
                                                   .orElse(Completable.complete());
            init.blockingAwait();

            final SslContextBuilder sslContext = SslContextBuilder.forServer(dynamicTlsManager.getKeyManager())
                                                                  .trustManager(dynamicTlsManager.getTrustManager())
                                                                  .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                                                                  .sslProvider(SslProvider.OPENSSL)
                                                                  .clientAuth(ClientAuth.REQUIRE)
                                                                  .protocols(List.of("TLSv1.2", "TLSv1.3"))
                                                                  .startTls(true)
                                                                  .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                                                                                                                           SelectorFailureBehavior.NO_ADVERTISE,
                                                                                                                           SelectedListenerFailureBehavior.ACCEPT,
                                                                                                                           ApplicationProtocolNames.HTTP_2));
            SslContext sslContextGRPC = GrpcSslContexts.configure(sslContext).build();
            var serverBuilder = NettyServerBuilder.forPort(port);
            services.forEach(serverBuilder::addService);
            this.server = serverBuilder.sslContext(sslContextGRPC).build();

        }
        catch (Exception e)
        {
            log.info("Exception in RxServer with error, {}", e);
        }
    }

    public RxServer(String socketPath,
                    BindableService service)
    {
        this.group = Optional.of(new EpollEventLoopGroup());
        this.socketPath = Optional.of(socketPath);

        boolean test = new File(socketPath).delete();
        log.debug("RxServer deleting socket returned: {}", test);

        this.server = NettyServerBuilder.forAddress(new DomainSocketAddress(socketPath))
                                        .channelType(EpollServerDomainSocketChannel.class)
                                        .workerEventLoopGroup(group.get())
                                        .bossEventLoopGroup(group.get())
                                        .withChildOption(ChannelOption.SO_KEEPALIVE, true)
                                        .withChildOption(ChannelOption.SO_REUSEADDR, true)
                                        .addService(service)
                                        .build();

    }

    public RxServer createUdsServer(String socketPath,
                                    BindableService service)
    {
        return new RxServer(socketPath, service);
    }

    public Completable start()
    {
        return Completable.fromAction(() -> this.server.start()).doOnSubscribe(__ -> log.info("Starting.")).doOnComplete(() -> log.info("Started."));
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.server.shutdown())//
                          .andThen(Completable.fromAction(() ->
                          {
                              this.server.awaitTermination();

                              if (this.group.isPresent())
                              {
                                  this.group.get().shutdownGracefully();
                                  this.group.get().awaitTermination(1, TimeUnit.MINUTES);
                              }
                              if (this.socketPath.isPresent())
                              {
                                  boolean test = new File(socketPath.get()).delete();
                                  log.debug("RxServer deleting socket returned: {}", test);
                              }
                          }))
                          .doOnSubscribe(__ -> log.info("Stopping."))
                          .doOnComplete(() -> log.info("Stopped."));
    }
}