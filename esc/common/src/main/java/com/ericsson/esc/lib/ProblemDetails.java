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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class ProblemDetails
{

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private String cause;
    @JsonInclude(Include.NON_EMPTY)
    private List<InvalidParam> invalidParams = new ArrayList<>();

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("type")
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("title")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("status")
    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("detail")
    public String getDetail()
    {
        return detail;
    }

    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("instance")
    public String getInstance()
    {
        return instance;
    }

    public void setInstance(String instance)
    {
        this.instance = instance;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("cause")
    public String getCause()
    {
        return cause;
    }

    public void setCause(String cause)
    {
        this.cause = cause;
    }

    /**
     **/

    @ApiModelProperty(value = "")
    @JsonProperty("invalidParams")
    @Size(min = 0)
    public List<InvalidParam> getInvalidParams()
    {
        return invalidParams;
    }

    public void setInvalidParams(List<InvalidParam> invalidParams)
    {
        this.invalidParams = invalidParams;
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
        ProblemDetails problemDetails = (ProblemDetails) o;
        return Objects.equals(type, problemDetails.type) && Objects.equals(title, problemDetails.title) && Objects.equals(status, problemDetails.status)
               && Objects.equals(detail, problemDetails.detail) && Objects.equals(instance, problemDetails.instance)
               && Objects.equals(cause, problemDetails.cause) && Objects.equals(invalidParams, problemDetails.invalidParams);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, title, status, detail, instance, cause, invalidParams);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProblemDetails {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
        sb.append("    instance: ").append(toIndentedString(instance)).append("\n");
        sb.append("    cause: ").append(toIndentedString(cause)).append("\n");
        sb.append("    invalidParams: ").append(toIndentedString(invalidParams)).append("\n");
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
