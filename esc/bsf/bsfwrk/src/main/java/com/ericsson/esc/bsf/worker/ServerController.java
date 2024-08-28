/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Oct 09, 2023 Author: zstoioa
 */

package com.ericsson.esc.bsf.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.worker.ServerContainer.ServerTag;
import com.ericsson.utilities.http.KubeProbe;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class ServerController
{

    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    // Represents the current global options of servers
    private final AtomicReference<BsfSrvOptions> serverGlobalOptions;

    // Current running servers
    private final AtomicReference<EnumMap<ServerTag, ServerContainer>> currentServers;

    private final KubeProbe kubeProbe;

    ServerController(final KubeProbe kubeProbe)
    {
        this.kubeProbe = kubeProbe;
        this.serverGlobalOptions = new AtomicReference<>(null);
        this.currentServers = new AtomicReference<>(new EnumMap<>(ServerTag.class));
    }

    // This contructor aims for unit test usage
    ServerController(final KubeProbe kubeProbe,
                     final BsfSrvOptions serverGlobalOptions,
                     final List<ServerContainer> srvs)
    {
        this.kubeProbe = kubeProbe;
        this.serverGlobalOptions = new AtomicReference<>(serverGlobalOptions);

        final var currentSrvs = new EnumMap<ServerTag, ServerContainer>(ServerTag.class);
        srvs.forEach(srv -> currentSrvs.put(srv.getTag(), srv));
        this.currentServers = new AtomicReference<>(currentSrvs);
    }

    Completable updateReadinessState(final boolean readiness)
    {
        log.debug("UpdateReadiness called with input: {}", readiness);

        return Completable.fromAction(() -> this.kubeProbe.setReady(readiness))
                          .doOnError(err -> log.error("Could not update readiness to {} with error: ", readiness, err))
                          .onErrorComplete();
    }

    Completable stopAllRunningServers()
    {
        final var currentSrvs = currentServers.get();

        if (currentSrvs.isEmpty() || currentSrvs.values().isEmpty())
        {
            return Completable.complete();
        }

        final var serversToStop = Flowable.fromIterable(currentSrvs.values());
        final var serverTypesToStop = new ArrayList<ServerTag>();   // Be aware, we create a copy of serverTag in order not to edit the same Set we
                                                                    // are reading
        currentSrvs.keySet().forEach(serverTypesToStop::add);

        return currentSrvs.isEmpty() ? Completable.complete() : serversToStop.flatMapCompletable(this::stopServer);
    }

    Completable stopServers(final BsfSrvOptions newGlobalOptions,
                            final List<ServerContainer> newServerContainers)
    {
        final var currentSrvs = currentServers.get();

        final var oldGlobalOptions = serverGlobalOptions.get();
        final var currentServerTypes = new ArrayList<ServerTag>();   // Be aware, we create a copy of serverTag in order not to edit the same Set we
                                                                     // are reading

        if (currentSrvs.isEmpty())
        {
            return Completable.complete();  // Nothing to stop
        }
        else
        {
            currentSrvs.keySet().forEach(currentServerTypes::add);  // Collect the tags of the current running servers
        }

        final var newServerTypes = extractServerTypes(newServerContainers);

        final var serversToStop = Objects.isNull(newGlobalOptions) || !newGlobalOptions.equals(oldGlobalOptions) ? currentServerTypes // stop all current
                                                                                                                 // servers
                                                                                                                 : currentServerTypes.stream()
                                                                                                                                     .filter(type -> !newServerTypes.contains(type)) // stop
                                                                                                                                     // servers
                                                                                                                                     // that missing
                                                                                                                                     // from new
                                                                                                                                     // servers
                                                                                                                                     .toList();

        return Objects.isNull(serversToStop) || serversToStop.isEmpty() ? Completable.complete()
                                                                        : Flowable.fromIterable(serversToStop)
                                                                                  .map(currentSrvs::get)
                                                                                  .flatMapCompletable(this::stopServer);

    }

    Completable stopServer(final ServerContainer currentServer)
    {
        final var webServerPool = currentServer.getServerPool();
        final var handlers = currentServer.getHandlers();
        final var serverTag = currentServer.getTag();
        final var serverDescription = serverTag.getDescription();

        final var stopServer = webServerPool.stopListener()
                                            .doOnError(err -> log.warn("Unexpected error while stopping listener of {} server, err: ", serverDescription, err))
                                            .onErrorComplete()
                                            .delay(30, TimeUnit.MILLISECONDS)  // Give some time to proccess in flight messages
                                            .andThen(webServerPool.shutdown()
                                                                  .doOnError(err -> log.warn("Unexpected error while shutting down {} server, err: ",
                                                                                             serverDescription,
                                                                                             err))
                                                                  .doOnComplete(() -> log.info("Shut down {} server", serverDescription))
                                                                  .onErrorComplete());

        final var stopHandlers = Completable.fromAction(() -> handlers.forEach(NBsfManagementHandler::stopExceptCleanup));

        return stopServer.andThen(stopHandlers).andThen(removeStoppedServerFromAtomic(serverTag));
    }

    Completable removeStoppedServerFromAtomic(final ServerTag server)
    {

        return currentServers.get().isEmpty() ? Completable.complete() : Completable.fromAction(() ->
        {
            final var currentServs = currentServers.get();
            currentServs.remove(server);
            currentServers.set(currentServs);
        }).doOnError(err -> log.debug("Could not update remove stopped servers from ref with error: ", err)).onErrorComplete();
    }

    Completable startServers(final BsfSrvOptions newGlobalOptions,
                             final List<ServerContainer> newServerContainers)
    {

        if (Objects.isNull(newGlobalOptions))
        {
            log.info("No Servers to start");
            return Completable.complete();
        }
        final var currentSrvs = currentServers.get();
        final var oldGlobalOptions = serverGlobalOptions.get();
        final var currentServerTypes = new ArrayList<ServerTag>();   // Be aware, we create a copy of serverTag in order not to edit the same Set we
                                                                     // are reading
        if (!currentSrvs.isEmpty())
        {
            currentSrvs.keySet().forEach(currentServerTypes::add); // Collect the tags of the current running servers
        }

        final var serversToStart = !newGlobalOptions.equals(oldGlobalOptions) ? newServerContainers
                                                                              : newServerContainers.stream()
                                                                                                   .filter(srvContainer -> !currentServerTypes.contains(srvContainer.getTag())) // Start
                                                                                                   // only
                                                                                                   // the
                                                                                                   // new
                                                                                                   // servers
                                                                                                   // that
                                                                                                   // not
                                                                                                   // already
                                                                                                   // running
                                                                                                   .toList();

        return Objects.isNull(serversToStart) || serversToStart.isEmpty() ? Completable.complete()
                                                                          : Flowable.fromIterable(serversToStart)
                                                                                    .flatMapCompletable(this::startServer)
                                                                                    .andThen(updateGlobalOptions(newGlobalOptions));

    }

    Completable startServer(final ServerContainer newServer)
    {
        final var webServerPool = newServer.getServerPool();
        final var handlers = newServer.getHandlers();
        final var serverTag = newServer.getTag();
        final var serverDescription = serverTag.getDescription();

        final var startHandlers = Flowable.fromIterable(handlers)
                                          .flatMapCompletable(NBsfManagementHandler::start)
                                          .doOnComplete(() -> log.info("Handlers for {} server started successfully", serverDescription));

        final var startServer = webServerPool.startListener()
                                             .doOnError(err -> log.warn("Unexpected error while starting listener of {} server with err: ",
                                                                        serverDescription,
                                                                        err))
                                             .doOnSubscribe(disp -> log.info("Starting {} servers", serverDescription));

        return startHandlers.andThen(startServer).andThen(addStartedServerToAtomic(newServer));
    }

    Completable addStartedServerToAtomic(final ServerContainer server)
    {
        return Completable.fromAction(() ->
        {
            final var currentServs = currentServers.get();
            currentServs.put(server.getTag(), server);
            currentServers.set(currentServs);
        }).doOnError(err -> log.debug("Could not update remove stopped servers from ref with error: ", err)).onErrorComplete();
    }

    Completable updateGlobalOptions(final BsfSrvOptions newGlobalOptions)
    {
        return Completable.fromAction(() -> serverGlobalOptions.set(newGlobalOptions));
    }

    Single<Boolean> decideToSetReadinessToFalse(final BsfSrvOptions newGlobalOptions,
                                                List<ServerContainer> newServerContainers)
    {
        if (Objects.isNull(newGlobalOptions)) // This should never happen
        {
            return Single.just(true); // Set Readiness to False
        }

        final var newServerTypes = extractServerTypes(newServerContainers);
        final var currentGlobalOptions = serverGlobalOptions.get();
        final var currentSrvs = currentServers.get();
        final Set<ServerTag> currentServerTypes = currentSrvs.isEmpty() ? Set.of() : currentSrvs.keySet();

        final var globalChange = !newGlobalOptions.equals(currentGlobalOptions); // if there is a globalChange all servers should stop to be updated

        final var oldSrvRemains = !currentServerTypes.isEmpty() && newServerTypes.stream().anyMatch(currentServerTypes::contains); // if at least one old server
                                                                                                                                   // exists in new servers then
                                                                                                                                   // we don't need to swich
                                                                                                                                   // readiness to false

        return Single.just(globalChange || !oldSrvRemains);
    }

    Single<Boolean> decideToSetReadinessToTrue()
    {
        final var currentSrvs = currentServers.get();
        log.debug("DecideToSetReadinessToTrue: {}", !currentSrvs.isEmpty());
        return Single.just(!currentSrvs.isEmpty());
    }

    List<ServerTag> extractServerTypes(final List<ServerContainer> serverContainers)
    {
        return serverContainers.stream().map(srv -> srv.getTag()).toList();
    }

    // Only for unit test usage
    BsfSrvOptions getServerGlobalOptions()
    {
        return serverGlobalOptions.get();
    }

    // Only for unit test usage
    public Collection<ServerContainer> getCurrentServers()
    {
        return currentServers.get().values();
    }

}