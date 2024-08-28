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

import java.util.List;

import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Pod;

/**
 * 
 */
public class Microservice
{

    String name;
    List<V1Pod> podList;
    List<V1PersistentVolumeClaim> pvcList;

    /**
     * @param ms
     * @param podListForMicroService
     * @param pvcListForMicroService
     */
    public Microservice(String msName,
                        List<V1Pod> podList,
                        List<V1PersistentVolumeClaim> pvcList)
    {
        this.name = msName;
        this.podList = podList;
        this.pvcList = pvcList;
    }

    public List<V1PersistentVolumeClaim> getPvcList()
    {
        return this.pvcList;
    }

    public List<V1Pod> getPodList()
    {
        return podList;
    }

    public String getName()
    {
        return this.getName();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(",");
        podList.forEach(pod ->
        {
            sb.append(pod.getMetadata().getName()).append(",");
            pod.getSpec().getContainers().forEach(container ->
            {
                sb.append(container.getName())
                  .append(",")
                  .append(container.getResources().getRequests().get("cpu").getNumber())
                  .append(",")
                  .append(container.getResources().getLimits().get("cpu").getNumber())
                  .append(",")
                  .append(container.getResources().getRequests().get("memory").getNumber())
                  .append(",")
                  .append(container.getResources().getLimits().get("memory").getNumber())
                  .append("\n")
                  .append(",,");
            });
            sb.append("\n").append(",");
        });
        sb.setLength(sb.length() - 1); // delete latest comma
        return sb.toString();
    }

}
