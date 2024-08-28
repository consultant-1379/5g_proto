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
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * it contains information on data to be changed.
 */
@ApiModel(description = "it contains information on data to be changed.")
@JsonPropertyOrder({ PatchItem.JSON_PROPERTY_OP, PatchItem.JSON_PROPERTY_PATH, PatchItem.JSON_PROPERTY_FROM, PatchItem.JSON_PROPERTY_VALUE })
public class PatchItem
{
    public static final String JSON_PROPERTY_OP = "op";
    private String op;

    public static final String JSON_PROPERTY_PATH = "path";
    private String path;

    public static final String JSON_PROPERTY_FROM = "from";
    private String from;

    public static final String JSON_PROPERTY_VALUE = "value";
    private JsonNullable<Object> value = JsonNullable.<Object>of(null);

    public PatchItem()
    {
    }

    public PatchItem op(String op)
    {

        this.op = op;
        return this;
    }

    /**
     * Operations as defined in IETF RFC 6902.
     * 
     * @return op
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Operations as defined in IETF RFC 6902.")
    @JsonProperty(JSON_PROPERTY_OP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getOp()
    {
        return op;
    }

    @JsonProperty(JSON_PROPERTY_OP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setOp(String op)
    {
        this.op = op;
    }

    public PatchItem path(String path)
    {

        this.path = path;
        return this;
    }

    /**
     * contains a JSON pointer value (as defined in IETF RFC 6901) that references a
     * location of a resource on which the patch operation shall be performed.
     * 
     * @return path
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "contains a JSON pointer value (as defined in IETF RFC 6901) that references a location of a resource on which the patch operation shall be performed. ")
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

    public PatchItem from(String from)
    {

        this.from = from;
        return this;
    }

    /**
     * indicates the path of the source JSON element (according to JSON Pointer
     * syntax) being moved or copied to the location indicated by the
     * \&quot;path\&quot; attribute.
     * 
     * @return from
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicates the path of the source JSON element (according to JSON Pointer syntax) being moved or copied to the location indicated by the \"path\" attribute. ")
    @JsonProperty(JSON_PROPERTY_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFrom()
    {
        return from;
    }

    @JsonProperty(JSON_PROPERTY_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFrom(String from)
    {
        this.from = from;
    }

    public PatchItem value(Object value)
    {
        this.value = JsonNullable.<Object>of(value);

        return this;
    }

    /**
     * Get value
     * 
     * @return value
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public Object getValue()
    {
        return value.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Object> getValue_JsonNullable()
    {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    public void setValue_JsonNullable(JsonNullable<Object> value)
    {
        this.value = value;
    }

    public void setValue(Object value)
    {
        this.value = JsonNullable.<Object>of(value);
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
        PatchItem patchItem = (PatchItem) o;
        return Objects.equals(this.op, patchItem.op) && Objects.equals(this.path, patchItem.path) && Objects.equals(this.from, patchItem.from)
               && equalsNullable(this.value, patchItem.value);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(op, path, from, hashCodeNullable(value));
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PatchItem {\n");
        sb.append("    op: ").append(toIndentedString(op)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    from: ").append(toIndentedString(from)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
