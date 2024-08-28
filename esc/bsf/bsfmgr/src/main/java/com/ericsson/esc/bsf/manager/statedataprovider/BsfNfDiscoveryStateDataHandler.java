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
 * Created on: Apr 2, 2024
 *     Author: znpvaap
 */

package com.ericsson.esc.bsf.manager.statedataprovider;

import com.ericsson.adpal.cm.state.StateDataInput;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.ericsson.sc.rxetcd.RxEtcd;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * The implementation of the BsfStateDataHandler that provides the requests
 * handling from yang-provider to state data provider and the responses to
 * yang-provider
 */
public class BsfNfDiscoveryStateDataHandler implements BsfStateDataHandler
{

    private final RxEtcd etcd;
    private final JsonValueSerializer<String, String> etcdSerializer;

    public BsfNfDiscoveryStateDataHandler(RxEtcd etcd,
                                          JsonValueSerializer<String, String> etcdSerializer)
    {
        this.etcd = etcd;
        this.etcdSerializer = etcdSerializer;
    }

    @Override
    public String handlerName()
    {
        return "Bsf Nf Discovery Data handler";
    }

    @Override
    public Completable handleRequest(Single<StateDataInput> input)
    {
        return input.flatMapCompletable(stateDataInput -> new BsfModelLastUpdateDataResponse(this.etcd,
                                                                                             stateDataInput.getCtx().response(),
                                                                                             this.etcdSerializer).respond());
    }

}
