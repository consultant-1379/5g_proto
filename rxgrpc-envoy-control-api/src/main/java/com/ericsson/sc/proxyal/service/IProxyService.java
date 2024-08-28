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
 * Created on: Jun 23, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import com.ericsson.sc.proxyal.outlierlogservice.EnvoyStatus;

import io.reactivex.Completable;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 */
public interface IProxyService
{

    Completable start();

    Completable stop();

    /*
     * publishes the ID of disconnecting proxy worker PODs
     */
    PublishSubject<String> getEnvoyDisconnections();

    /*
     * publishes the status event objects triggered by proxy outlier detection
     */
    PublishSubject<EnvoyStatus> getOutlierEventStream();

    PublishSubject<com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus> getHealthCheckEventStream();

}