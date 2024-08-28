/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.esc.lib;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", date = "2018-10-23T15:56:43.691083+02:00[Europe/Berlin]")
public class InvalidParam implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String param;
    private String reason;

    public InvalidParam(String param,
                        String reason)
    {
        this.param = param;
        this.reason = reason;
    }

    /**
     **/

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("param")
    @NotNull
    public String getParam()
    {
        return param;
    }

    public void setParam(String param)
    {
        this.param = param;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("reason")
    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @Override
    public boolean equals(java.lang.Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        InvalidParam invalidParam = (InvalidParam) o;
        return Objects.equals(param, invalidParam.param) && Objects.equals(reason, invalidParam.reason);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(param, reason);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class InvalidParam {\n");

        sb.append("    param: ").append(toIndentedString(param)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o)
    {
        if (o == null)
        {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
