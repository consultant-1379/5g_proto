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
 * Created on: Jan 8, 2022
 *     Author: eaoknkr
 */

package com.ericsson.supreme.defaultscenario;

import java.util.Optional;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.KubernetesClient;

/**
 * 
 */
public interface Processor
{
    void process(boolean generate,
                 Optional<KubernetesClient> kubeClient,
                 GeneratedCert commonCa) throws DefaultScenarioException;

    void setNextProcessor(Processor nextProcessor);
}
