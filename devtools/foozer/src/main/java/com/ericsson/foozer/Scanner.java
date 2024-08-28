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
 * Created on: Sep 8, 2020
 *     Author: evouioa
 */

package com.ericsson.foozer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.util.concurrent.AtomicDouble;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;

public class Scanner
{

    String namespace;
    String release;

    ApiClient client;
    CoreV1Api api;

    List<Microservice> msList;

    public Scanner() throws IOException
    {
        this.client = Config.defaultClient();
        Configuration.setDefaultApiClient(this.client);
        this.api = new CoreV1Api();
        this.namespace = System.getProperty("namespace", "5g-bsf-evouioa");
        this.release = System.getProperty("release", "eric-sc-evouioa");
    }

    public List<V1Pod> getPodList() throws ApiException
    {
        List<V1Pod> list = api.listNamespacedPod(this.namespace, "true", null, null, null, "app.kubernetes.io/instance=" + this.release, null, null, null, null)
                              .getItems();
        return list;
    }

    public List<V1Pod> getPodListForMicroService(String ms) throws ApiException
    {
        List<V1Pod> list = api.listNamespacedPod(this.namespace, "true", null, null, null, "app=" + ms, null, null, null, null).getItems();
        return list;
    }

    public Set<String> getMicroList() throws ApiException
    {
        HashSet<String> set = new HashSet<String>();
        for (V1Pod pod : getPodList())
        {
            if (pod.getMetadata().getLabels().get("app.kubernetes.io/name") == null)
            {
                System.out.println(pod.getMetadata().getName());
            }
            set.add(pod.getMetadata().getLabels().get("app.kubernetes.io/name"));
        }
        return set;
    }

    public List<V1PersistentVolumeClaim> getPvcList() throws ApiException
    {
        List<V1PersistentVolumeClaim> pvcList = api.listNamespacedPersistentVolumeClaim(this.namespace, "true", null, null, null, null, null, null, null, null)
                                                   .getItems();
        return pvcList;
    }

    public List<V1PersistentVolumeClaim> getPvcListForMicroService(String ms) throws ApiException
    {
        List<V1PersistentVolumeClaim> pvcList = api.listNamespacedPersistentVolumeClaim(this.namespace,
                                                                                        "true",
                                                                                        null,
                                                                                        null,
                                                                                        null,
                                                                                        "app=" + ms,
                                                                                        null,
                                                                                        null,
                                                                                        null,
                                                                                        null)
                                                   .getItems();
        return pvcList;
    }

    public AtomicDouble getTotalRequestCpu() throws ApiException
    {
        AtomicDouble cpuRequest = new AtomicDouble();
        getPodList().forEach(pod -> pod.getSpec()
                                       .getContainers()
                                       .forEach(container -> cpuRequest.addAndGet(container.getResources()
                                                                                           .getRequests()
                                                                                           .get("cpu")
                                                                                           .getNumber()
                                                                                           .doubleValue())));
        return cpuRequest;

    }

    public AtomicDouble getTotalRequestMemory() throws ApiException
    {
        AtomicDouble memoryRequest = new AtomicDouble();
        getPodList().forEach(pod -> pod.getSpec()
                                       .getContainers()
                                       .forEach(container -> memoryRequest.addAndGet(container.getResources()
                                                                                              .getRequests()
                                                                                              .get("memory")
                                                                                              .getNumber()
                                                                                              .doubleValue())));
        return memoryRequest;
    }

    public AtomicDouble getTotalLimitCpu() throws ApiException
    {
        AtomicDouble cpuLimit = new AtomicDouble();
        getPodList().forEach(pod -> pod.getSpec()
                                       .getContainers()
                                       .forEach(container -> cpuLimit.addAndGet(container.getResources().getLimits().get("cpu").getNumber().doubleValue())));
        return cpuLimit;
    }

    public AtomicDouble getTotalLimitMemory() throws ApiException
    {
        AtomicDouble memoryLimit = new AtomicDouble();
        getPodList().forEach(pod -> pod.getSpec()
                                       .getContainers()
                                       .forEach(container -> memoryLimit.addAndGet(container.getResources()
                                                                                            .getLimits()
                                                                                            .get("memory")
                                                                                            .getNumber()
                                                                                            .doubleValue())));
        return memoryLimit;
    }

    public AtomicDouble getTotalPVCStorageSize() throws ApiException
    {
        AtomicDouble storageSize = new AtomicDouble();
        getPvcList().forEach(pvc -> storageSize.addAndGet(pvc.getSpec().getResources().getRequests().get("storage").getNumber().doubleValue()));
        return storageSize;
    }

    public String getPvcCsv() throws ApiException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("PVC Name").append(",").append("Storage size").append("\n");
        getPvcList().forEach(pvc -> sb.append("\n")
                                      .append(pvc.getMetadata().getName())
                                      .append(",")
                                      .append(pvc.getSpec().getResources().getRequests().get("storage").getNumber())
                                      .append(","));
        return sb.toString();
    }

    public String getPodCsv() throws ApiException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Microservice")
          .append(",")
          .append("Pod")
          .append(",")
          .append("Container")
          .append(",")
          .append("CPU Request")
          .append(",")
          .append("CPU Limit")
          .append("Memory Request")
          .append(",")
          .append("Memory Limit")
          .append("\n");
        serialize().forEach(ms -> sb.append(ms.toString()));
        return sb.toString();
    }

    public String getTotalResourceS() throws ApiException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n")
          .append("Total CPU Request:")
          .append(",")
          .append(getTotalRequestCpu())
          .append(",")
          .append("Total CPU Limit:")
          .append(",")
          .append(getTotalLimitCpu())
          .append(",")
          .append("Total Memory Request:")
          .append(",")
          .append(getTotalRequestMemory())
          .append(",")
          .append("Total Memory Limit:")
          .append(",")
          .append(getTotalLimitMemory())
          .append(",")
          .append("Total PVC Storage size:")
          .append(",")
          .append(getTotalPVCStorageSize())
          .append("\n");

        return sb.toString();
    }

    public List<Microservice> serialize() throws ApiException
    {
        List<Microservice> msList = new ArrayList<Microservice>();
        getMicroList().forEach(msName ->
        {
            try
            {
                Microservice ms = new Microservice(msName, getPodListForMicroService(msName), getPvcListForMicroService(msName));
                msList.add(ms);
            }
            catch (ApiException e)
            {
                e.printStackTrace();
            }
        });
        return msList;
    }

}
