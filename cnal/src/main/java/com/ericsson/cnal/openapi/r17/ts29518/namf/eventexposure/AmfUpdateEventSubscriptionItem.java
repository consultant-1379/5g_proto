/*
 * Namf_EventExposure
 * AMF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PresenceInfo;
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
 * Document describing the modification(s) to an AMF Event Subscription
 */
@ApiModel(description = "Document describing the modification(s) to an AMF Event Subscription")
@JsonPropertyOrder({ AmfUpdateEventSubscriptionItem.JSON_PROPERTY_OP,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_PATH,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_VALUE,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_PRESENCE_INFO,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_EXCLUDE_SUPI_LIST,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_EXCLUDE_GPSI_LIST,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_INCLUDE_SUPI_LIST,
                     AmfUpdateEventSubscriptionItem.JSON_PROPERTY_INCLUDE_GPSI_LIST })
public class AmfUpdateEventSubscriptionItem
{
    /**
     * Gets or Sets op
     */
    public enum OpEnum
    {
        ADD("add"),

        REMOVE("remove"),

        REPLACE("replace");

        private String value;

        OpEnum(String value)
        {
            this.value = value;
        }

        @JsonValue
        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return String.valueOf(value);
        }

        @JsonCreator
        public static OpEnum fromValue(String value)
        {
            for (OpEnum b : OpEnum.values())
            {
                if (b.value.equals(value))
                {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    public static final String JSON_PROPERTY_OP = "op";
    private OpEnum op;

    public static final String JSON_PROPERTY_PATH = "path";
    private String path;

    public static final String JSON_PROPERTY_VALUE = "value";
    private AmfEvent value;

    public static final String JSON_PROPERTY_PRESENCE_INFO = "presenceInfo";
    private PresenceInfo presenceInfo;

    public static final String JSON_PROPERTY_EXCLUDE_SUPI_LIST = "excludeSupiList";
    private List<String> excludeSupiList = null;

    public static final String JSON_PROPERTY_EXCLUDE_GPSI_LIST = "excludeGpsiList";
    private List<String> excludeGpsiList = null;

    public static final String JSON_PROPERTY_INCLUDE_SUPI_LIST = "includeSupiList";
    private List<String> includeSupiList = null;

    public static final String JSON_PROPERTY_INCLUDE_GPSI_LIST = "includeGpsiList";
    private List<String> includeGpsiList = null;

    public AmfUpdateEventSubscriptionItem()
    {
    }

    public AmfUpdateEventSubscriptionItem op(OpEnum op)
    {

        this.op = op;
        return this;
    }

    /**
     * Get op
     * 
     * @return op
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_OP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public OpEnum getOp()
    {
        return op;
    }

    @JsonProperty(JSON_PROPERTY_OP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setOp(OpEnum op)
    {
        this.op = op;
    }

    public AmfUpdateEventSubscriptionItem path(String path)
    {

        this.path = path;
        return this;
    }

    /**
     * Get path
     * 
     * @return path
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getPath()
    {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPath(String path)
    {
        this.path = path;
    }

    public AmfUpdateEventSubscriptionItem value(AmfEvent value)
    {

        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmfEvent getValue()
    {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(AmfEvent value)
    {
        this.value = value;
    }

    public AmfUpdateEventSubscriptionItem presenceInfo(PresenceInfo presenceInfo)
    {

        this.presenceInfo = presenceInfo;
        return this;
    }

    /**
     * Get presenceInfo
     * 
     * @return presenceInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRESENCE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PresenceInfo getPresenceInfo()
    {
        return presenceInfo;
    }

    @JsonProperty(JSON_PROPERTY_PRESENCE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPresenceInfo(PresenceInfo presenceInfo)
    {
        this.presenceInfo = presenceInfo;
    }

    public AmfUpdateEventSubscriptionItem excludeSupiList(List<String> excludeSupiList)
    {

        this.excludeSupiList = excludeSupiList;
        return this;
    }

    public AmfUpdateEventSubscriptionItem addExcludeSupiListItem(String excludeSupiListItem)
    {
        if (this.excludeSupiList == null)
        {
            this.excludeSupiList = new ArrayList<>();
        }
        this.excludeSupiList.add(excludeSupiListItem);
        return this;
    }

    /**
     * Get excludeSupiList
     * 
     * @return excludeSupiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getExcludeSupiList()
    {
        return excludeSupiList;
    }

    @JsonProperty(JSON_PROPERTY_EXCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExcludeSupiList(List<String> excludeSupiList)
    {
        this.excludeSupiList = excludeSupiList;
    }

    public AmfUpdateEventSubscriptionItem excludeGpsiList(List<String> excludeGpsiList)
    {

        this.excludeGpsiList = excludeGpsiList;
        return this;
    }

    public AmfUpdateEventSubscriptionItem addExcludeGpsiListItem(String excludeGpsiListItem)
    {
        if (this.excludeGpsiList == null)
        {
            this.excludeGpsiList = new ArrayList<>();
        }
        this.excludeGpsiList.add(excludeGpsiListItem);
        return this;
    }

    /**
     * Get excludeGpsiList
     * 
     * @return excludeGpsiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getExcludeGpsiList()
    {
        return excludeGpsiList;
    }

    @JsonProperty(JSON_PROPERTY_EXCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExcludeGpsiList(List<String> excludeGpsiList)
    {
        this.excludeGpsiList = excludeGpsiList;
    }

    public AmfUpdateEventSubscriptionItem includeSupiList(List<String> includeSupiList)
    {

        this.includeSupiList = includeSupiList;
        return this;
    }

    public AmfUpdateEventSubscriptionItem addIncludeSupiListItem(String includeSupiListItem)
    {
        if (this.includeSupiList == null)
        {
            this.includeSupiList = new ArrayList<>();
        }
        this.includeSupiList.add(includeSupiListItem);
        return this;
    }

    /**
     * Get includeSupiList
     * 
     * @return includeSupiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIncludeSupiList()
    {
        return includeSupiList;
    }

    @JsonProperty(JSON_PROPERTY_INCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeSupiList(List<String> includeSupiList)
    {
        this.includeSupiList = includeSupiList;
    }

    public AmfUpdateEventSubscriptionItem includeGpsiList(List<String> includeGpsiList)
    {

        this.includeGpsiList = includeGpsiList;
        return this;
    }

    public AmfUpdateEventSubscriptionItem addIncludeGpsiListItem(String includeGpsiListItem)
    {
        if (this.includeGpsiList == null)
        {
            this.includeGpsiList = new ArrayList<>();
        }
        this.includeGpsiList.add(includeGpsiListItem);
        return this;
    }

    /**
     * Get includeGpsiList
     * 
     * @return includeGpsiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIncludeGpsiList()
    {
        return includeGpsiList;
    }

    @JsonProperty(JSON_PROPERTY_INCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeGpsiList(List<String> includeGpsiList)
    {
        this.includeGpsiList = includeGpsiList;
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
        AmfUpdateEventSubscriptionItem amfUpdateEventSubscriptionItem = (AmfUpdateEventSubscriptionItem) o;
        return Objects.equals(this.op, amfUpdateEventSubscriptionItem.op) && Objects.equals(this.path, amfUpdateEventSubscriptionItem.path)
               && Objects.equals(this.value, amfUpdateEventSubscriptionItem.value)
               && Objects.equals(this.presenceInfo, amfUpdateEventSubscriptionItem.presenceInfo)
               && Objects.equals(this.excludeSupiList, amfUpdateEventSubscriptionItem.excludeSupiList)
               && Objects.equals(this.excludeGpsiList, amfUpdateEventSubscriptionItem.excludeGpsiList)
               && Objects.equals(this.includeSupiList, amfUpdateEventSubscriptionItem.includeSupiList)
               && Objects.equals(this.includeGpsiList, amfUpdateEventSubscriptionItem.includeGpsiList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(op, path, value, presenceInfo, excludeSupiList, excludeGpsiList, includeSupiList, includeGpsiList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmfUpdateEventSubscriptionItem {\n");
        sb.append("    op: ").append(toIndentedString(op)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    presenceInfo: ").append(toIndentedString(presenceInfo)).append("\n");
        sb.append("    excludeSupiList: ").append(toIndentedString(excludeSupiList)).append("\n");
        sb.append("    excludeGpsiList: ").append(toIndentedString(excludeGpsiList)).append("\n");
        sb.append("    includeSupiList: ").append(toIndentedString(includeSupiList)).append("\n");
        sb.append("    includeGpsiList: ").append(toIndentedString(includeGpsiList)).append("\n");
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
