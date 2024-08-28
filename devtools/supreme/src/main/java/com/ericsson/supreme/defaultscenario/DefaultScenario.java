/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 5, 2022
 *     Author: esamioa
 */

package com.ericsson.supreme.defaultscenario;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.KubernetesClient;

/**
 * The interface that must be implemented by all default scenarios
 */
public interface DefaultScenario
{
    void createCerts(GeneratedCert ca) throws DefaultScenarioException;

    void readCerts() throws DefaultScenarioException;

    void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException;

    /**
     * This is for self-signed certs, or for certs that don't use the common root ca
     * 
     * @throws DefaultScenarioException
     */
    void createCerts() throws DefaultScenarioException;
}
