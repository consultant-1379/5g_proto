/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 28, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import java.util.Optional;

import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.SupportedFeatures;
import com.ericsson.esc.lib.OpenApiReqParams;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;

public class GetBindingsParams implements OpenApiReqParams
{
    private final DiscoveryQuery query;
    private final Optional<SupportedFeatures> suppFeat;

    /**
     * 
     * @param params
     * @throws IllegalArgumentException
     */
    public GetBindingsParams(RequestParameters parsedParams)
    {
        this.query = DiscoveryQuery.fromQueryParameters(parsedParams);
        this.suppFeat = Optional.ofNullable(parsedParams.queryParameter("supp-feat")).map(RequestParameter::getString).map(SupportedFeatures::new);
    }

    public DiscoveryQuery getQuery()
    {
        return query;
    }

    /**
     * @return the suppFeat
     */
    public Optional<SupportedFeatures> getSuppFeat()
    {
        return suppFeat;
    }

}
