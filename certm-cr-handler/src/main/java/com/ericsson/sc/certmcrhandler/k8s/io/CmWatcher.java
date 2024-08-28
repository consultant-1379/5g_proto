/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 27, 2024
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.k8s.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificateSpec;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.ConfigmapWatch.ConfigmapFile;
import com.ericsson.utilities.json.Jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CmWatcher
{
    private final ConfigmapWatch configmap;
    private final Disposable cd;
    private final ConcurrentHashMap<String, ExternalCertificateSpec> externalCertificates;
    private final CrHandler handler;

    private static final Logger log = LoggerFactory.getLogger(CmWatcher.class);
    private static final ObjectMapper json = Jackson.om();

    public CmWatcher(ConfigmapWatch configmap)
    {
        this.configmap = configmap;

        this.cd = this.crsLifeCycle().retryWhen(errors -> errors.flatMap(e ->
        {
            log.warn("Could not watch configmap file, retrying.", e);
            return Flowable.timer(10, TimeUnit.SECONDS);
        }))
                      .doOnSubscribe(sub -> log.info("Starting dynamic monitoring of configmap for certm crs"))
                      .doOnComplete(() -> log.info("Stoping dynamic monitoring of configmap for certm crs"))
                      .subscribe();
        externalCertificates = new ConcurrentHashMap<>();
        handler = new CrHandler();
    }

    public Flowable<Boolean> crsLifeCycle()
    {
        return this.configmap.watch() //
                             .map(ConfigmapFile::getData)
                             .doOnNext(next -> log.debug("New configmap change identified"))
                             .doOnSubscribe(sub -> log.debug("Starting monitoring configmap file changes"))
                             .doOnTerminate(() -> log.debug("Terminating monitoring configmap file changes"))
                             .doOnComplete(() -> log.debug("Stoping monitoring configmap file changes"))
                             .doOnError(e -> log.error("Error occured while monitoring configmap file changes", e))
                             .onBackpressureBuffer()
                             .map(this::readConfigMap)
                             .map(this::updateHashMap)
                             .map(c ->
                             {
                                 c.blockingAwait(10, TimeUnit.SECONDS);
                                 return true;
                             })
                             .subscribeOn(Schedulers.io());
    }

    private ConcurrentHashMap<String, ExternalCertificateSpec> readConfigMap(String data) throws Exception
    {

        ConcurrentHashMap<String, ExternalCertificateSpec> map = new ConcurrentHashMap<>();
        log.debug("ConfigMap data: {}", data);
        json.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        try
        {
            Arrays.stream(json.readValue(data, ExternalCertificateSpec[].class)) // stream service containers log severity
                  .forEach(s ->
                  {
                      map.put(s.getGeneratedSecretName(), s);
                      log.debug("CR {} exists in CM", s.getGeneratedSecretName());
                  });
        }
        catch (JsonProcessingException e)
        {
            log.error("Error: ", e);
            throw new Exception(e);
        }
        return map;

    }

    private Completable updateHashMap(ConcurrentHashMap<String, ExternalCertificateSpec> map)
    {
        ArrayList<Completable> completables = new ArrayList<>();
        map.forEach((String name,
                     ExternalCertificateSpec e) ->
        {
            if (this.externalCertificates.containsKey(name) && !this.externalCertificates.get(name).equals(e))
            {
                completables.add(this.handler.updateCr(e, name));
                this.externalCertificates.put(name, e);
                log.debug("Updating existing CR {}", name);
            }
            else
            {
                completables.add(this.handler.createCr(e, name));
                this.externalCertificates.put(name, e);
                log.debug("Creating CR {}", name);
            }
        });
        this.externalCertificates.forEach((String name,
                                           ExternalCertificateSpec e) ->
        {
            if (!map.containsKey(name))
            {
                completables.add(this.handler.deleteCr(name));
                this.externalCertificates.remove(name);
                log.debug("Deleting CR {}", name);
            }
        });
        return Completable.merge(completables);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(cd, configmap, externalCertificates, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmWatcher other = (CmWatcher) obj;
        return Objects.equals(cd, other.cd) && Objects.equals(configmap, other.configmap) && Objects.equals(externalCertificates, other.externalCertificates)
               && Objects.equals(handler, other.handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "CmWatcher [configmap=" + configmap + ", cd=" + cd + ", externalCertificates=" + externalCertificates + ", handler=" + handler + "]";
    }

}
