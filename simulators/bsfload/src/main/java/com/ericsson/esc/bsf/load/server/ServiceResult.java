/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 24, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.util.List;

import com.ericsson.esc.bsf.load.server.BsfLoadService.ResultStatus;
import com.ericsson.esc.bsf.load.server.BsfLoadService.WorkLoadEntry;

/**
 * The result of a BsfLoadService operation.
 */
public class ServiceResult
{
    private final List<InvalidParameter> invalidParams;
    private final List<WorkLoadEntry> workLoadEntries;
    private final ResultStatus status;

    private ServiceResult(List<InvalidParameter> invalidParams,
                          List<WorkLoadEntry> workLoadEntries,
                          ResultStatus status)
    {
        this.invalidParams = invalidParams;
        this.workLoadEntries = workLoadEntries;
        this.status = status;
    }

    public static ServiceResult withStatus(ResultStatus status)
    {
        return new ServiceResult(List.of(), List.of(), status);
    }

    public static ServiceResult withWorkLoadEntries(ResultStatus status,
                                                    List<WorkLoadEntry> workloadEntries)
    {
        return new ServiceResult(List.of(), workloadEntries, status);
    }

    public static ServiceResult withInvalidParams(ResultStatus status,
                                                  List<InvalidParameter> invalidParams)
    {
        return new ServiceResult(invalidParams, List.of(), status);
    }

    /**
     * @return the invalidParams
     */
    public List<InvalidParameter> getInvalidParams()
    {
        return invalidParams;
    }

    /**
     * @return the workLoadEntries
     */
    public List<WorkLoadEntry> getWorkLoadEntries()
    {
        return workLoadEntries;
    }

    /**
     * @return the status
     */
    public ResultStatus getStatus()
    {
        return status;
    }

    @Override
    public String toString()
    {
        return "ServiceResult [invalidParams=" + invalidParams + ", workLoadEntries=" + workLoadEntries + ", status=" + status + "]";
    }
}
