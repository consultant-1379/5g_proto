/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

import java.util.Objects;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This data type is defined in the same way as the MbsMediaComp data type, but
 * with the OpenAPI nullable property set to true.
 */
@ApiModel(description = "This data type is defined in the same way as the MbsMediaComp data type, but with the OpenAPI nullable property set to true. ")
@JsonPropertyOrder({ MbsMediaCompRm.JSON_PROPERTY_MBS_MED_COMP_NUM,
                     MbsMediaCompRm.JSON_PROPERTY_MBS_FLOW_DESCS,
                     MbsMediaCompRm.JSON_PROPERTY_MBS_SDF_RES_PRIO,
                     MbsMediaCompRm.JSON_PROPERTY_MBS_MEDIA_INFO,
                     MbsMediaCompRm.JSON_PROPERTY_QOS_REF,
                     MbsMediaCompRm.JSON_PROPERTY_MBS_QO_S_REQ })
public class MbsMediaCompRm
{
    public static final String JSON_PROPERTY_MBS_MED_COMP_NUM = "mbsMedCompNum";
    private Integer mbsMedCompNum;

    public static final String JSON_PROPERTY_MBS_FLOW_DESCS = "mbsFlowDescs";
    private List<String> mbsFlowDescs = null;

    public static final String JSON_PROPERTY_MBS_SDF_RES_PRIO = "mbsSdfResPrio";
    private String mbsSdfResPrio;

    public static final String JSON_PROPERTY_MBS_MEDIA_INFO = "mbsMediaInfo";
    private MbsMediaInfo mbsMediaInfo;

    public static final String JSON_PROPERTY_QOS_REF = "qosRef";
    private String qosRef;

    public static final String JSON_PROPERTY_MBS_QO_S_REQ = "mbsQoSReq";
    private MbsQoSReq mbsQoSReq;

    public MbsMediaCompRm()
    {
    }

    public MbsMediaCompRm mbsMedCompNum(Integer mbsMedCompNum)
    {

        this.mbsMedCompNum = mbsMedCompNum;
        return this;
    }

    /**
     * Get mbsMedCompNum
     * 
     * @return mbsMedCompNum
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MBS_MED_COMP_NUM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getMbsMedCompNum()
    {
        return mbsMedCompNum;
    }

    @JsonProperty(JSON_PROPERTY_MBS_MED_COMP_NUM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMbsMedCompNum(Integer mbsMedCompNum)
    {
        this.mbsMedCompNum = mbsMedCompNum;
    }

    public MbsMediaCompRm mbsFlowDescs(List<String> mbsFlowDescs)
    {

        this.mbsFlowDescs = mbsFlowDescs;
        return this;
    }

    public MbsMediaCompRm addMbsFlowDescsItem(String mbsFlowDescsItem)
    {
        if (this.mbsFlowDescs == null)
        {
            this.mbsFlowDescs = new ArrayList<>();
        }
        this.mbsFlowDescs.add(mbsFlowDescsItem);
        return this;
    }

    /**
     * Get mbsFlowDescs
     * 
     * @return mbsFlowDescs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MBS_FLOW_DESCS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMbsFlowDescs()
    {
        return mbsFlowDescs;
    }

    @JsonProperty(JSON_PROPERTY_MBS_FLOW_DESCS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsFlowDescs(List<String> mbsFlowDescs)
    {
        this.mbsFlowDescs = mbsFlowDescs;
    }

    public MbsMediaCompRm mbsSdfResPrio(String mbsSdfResPrio)
    {

        this.mbsSdfResPrio = mbsSdfResPrio;
        return this;
    }

    /**
     * Indicates the reservation priority.
     * 
     * @return mbsSdfResPrio
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the reservation priority.")
    @JsonProperty(JSON_PROPERTY_MBS_SDF_RES_PRIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMbsSdfResPrio()
    {
        return mbsSdfResPrio;
    }

    @JsonProperty(JSON_PROPERTY_MBS_SDF_RES_PRIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsSdfResPrio(String mbsSdfResPrio)
    {
        this.mbsSdfResPrio = mbsSdfResPrio;
    }

    public MbsMediaCompRm mbsMediaInfo(MbsMediaInfo mbsMediaInfo)
    {

        this.mbsMediaInfo = mbsMediaInfo;
        return this;
    }

    /**
     * Get mbsMediaInfo
     * 
     * @return mbsMediaInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MBS_MEDIA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MbsMediaInfo getMbsMediaInfo()
    {
        return mbsMediaInfo;
    }

    @JsonProperty(JSON_PROPERTY_MBS_MEDIA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsMediaInfo(MbsMediaInfo mbsMediaInfo)
    {
        this.mbsMediaInfo = mbsMediaInfo;
    }

    public MbsMediaCompRm qosRef(String qosRef)
    {

        this.qosRef = qosRef;
        return this;
    }

    /**
     * Get qosRef
     * 
     * @return qosRef
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_QOS_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getQosRef()
    {
        return qosRef;
    }

    @JsonProperty(JSON_PROPERTY_QOS_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQosRef(String qosRef)
    {
        this.qosRef = qosRef;
    }

    public MbsMediaCompRm mbsQoSReq(MbsQoSReq mbsQoSReq)
    {

        this.mbsQoSReq = mbsQoSReq;
        return this;
    }

    /**
     * Get mbsQoSReq
     * 
     * @return mbsQoSReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MBS_QO_S_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MbsQoSReq getMbsQoSReq()
    {
        return mbsQoSReq;
    }

    @JsonProperty(JSON_PROPERTY_MBS_QO_S_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsQoSReq(MbsQoSReq mbsQoSReq)
    {
        this.mbsQoSReq = mbsQoSReq;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        MbsMediaCompRm mbsMediaCompRm = (MbsMediaCompRm) o;
        return Objects.equals(this.mbsMedCompNum, mbsMediaCompRm.mbsMedCompNum) && Objects.equals(this.mbsFlowDescs, mbsMediaCompRm.mbsFlowDescs)
               && Objects.equals(this.mbsSdfResPrio, mbsMediaCompRm.mbsSdfResPrio) && Objects.equals(this.mbsMediaInfo, mbsMediaCompRm.mbsMediaInfo)
               && Objects.equals(this.qosRef, mbsMediaCompRm.qosRef) && Objects.equals(this.mbsQoSReq, mbsMediaCompRm.mbsQoSReq);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mbsMedCompNum, mbsFlowDescs, mbsSdfResPrio, mbsMediaInfo, qosRef, mbsQoSReq);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbsMediaCompRm {\n");
        sb.append("    mbsMedCompNum: ").append(toIndentedString(mbsMedCompNum)).append("\n");
        sb.append("    mbsFlowDescs: ").append(toIndentedString(mbsFlowDescs)).append("\n");
        sb.append("    mbsSdfResPrio: ").append(toIndentedString(mbsSdfResPrio)).append("\n");
        sb.append("    mbsMediaInfo: ").append(toIndentedString(mbsMediaInfo)).append("\n");
        sb.append("    qosRef: ").append(toIndentedString(qosRef)).append("\n");
        sb.append("    mbsQoSReq: ").append(toIndentedString(mbsQoSReq)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o)
    {
        if (o == null)
        {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
