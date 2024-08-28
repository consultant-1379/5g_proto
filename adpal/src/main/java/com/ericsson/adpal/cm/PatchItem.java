/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2020
 *     Author: epaxale
 */

package com.ericsson.adpal.cm;

/**
 * 
 */

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchItem
{

    private PatchOperation op;
    private String path;
    private String from;
    private Object value = null;

    public PatchItem()
    {

    }

    public PatchItem(PatchOperation op,
                     String path,
                     String from,
                     Object value)
    {
        this.op = op;
        this.path = path;
        this.from = from;
        this.value = value;
    }

    @JsonProperty("op")
    public PatchOperation getOp()
    {
        return op;
    }

    public void setOp(PatchOperation op)
    {
        this.op = op;
    }

    @JsonProperty("path")
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @JsonProperty("from")
    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    @JsonProperty("value")
    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
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
        return Objects.equals(op, patchItem.op) && Objects.equals(path, patchItem.path) && Objects.equals(from, patchItem.from)
               && Objects.equals(value, patchItem.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(op, path, from, value);
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
