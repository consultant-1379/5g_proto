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
 * Created on: Jan 30, 2011
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.lib.BadRequestException;
import com.ericsson.esc.lib.MalformedMessageException;
import com.ericsson.esc.lib.OpenApiReqParams;
import com.ericsson.esc.lib.ValidationException;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.ext.web.api.RequestParameters;

public class PostBindingsParams implements OpenApiReqParams
{
    private static final ObjectMapper mapper = Jackson.om();

    private final PcfBinding pcfBinding;

    public PostBindingsParams(RequestParameters parsedParams)
    {
        try
        {
            this.pcfBinding = mapper.convertValue(parsedParams.body().getJsonObject().getMap(), PcfBinding.class);
        }
        catch (IllegalArgumentException e)
        {
            if (e.getCause() instanceof JsonProcessingException && e.getCause().getCause() instanceof BadRequestException)
            {
                throw (BadRequestException) e.getCause().getCause();
            }
            throw new MalformedMessageException("Could not parse PcfBinding data structure", e);
        }
        catch (Exception e)
        {
            throw new MalformedMessageException("Could not parse PcfBinding data structure", e);
        }

        // Ensure that ipv6 prefix length is strictly 64. This is a deviation from the
        // 3gpp spec

        if (this.pcfBinding.getIpv6Prefix() != null && this.pcfBinding.getIpv6Prefix().getPrefixLength() != 64)
        {
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                          "ipv6Prefix",
                                          "Invalid ipv6 prefix length " + this.pcfBinding.getIpv6Prefix().getPrefixLength() + ". Only 64 is supported");
        }
        if (this.pcfBinding.getAddIpv6Prefixes() != null)
        {
            this.pcfBinding.getAddIpv6Prefixes().forEach(addPrefix ->
            {
                if (addPrefix.getPrefixLength() != 64)
                {
                    throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                                  "addIpv6Prefixes",
                                                  "Invalid ipv6 prefix length " + addPrefix.getPrefixLength() + ". Only 64 is supported");
                }
            });
        }
    }

    public PcfBinding getPcfBinding()
    {
        return this.pcfBinding;
    }

}
