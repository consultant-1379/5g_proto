/*
 * Nudm_UEAU
 * UDM UE Authentication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Av5GHeAka
 */
@JsonPropertyOrder({ Av5GHeAka.JSON_PROPERTY_AV_TYPE,
                     Av5GHeAka.JSON_PROPERTY_RAND,
                     Av5GHeAka.JSON_PROPERTY_XRES_STAR,
                     Av5GHeAka.JSON_PROPERTY_AUTN,
                     Av5GHeAka.JSON_PROPERTY_KAUSF })
public class Av5GHeAka
{
    public static final String JSON_PROPERTY_AV_TYPE = "avType";
    private String avType;

    public static final String JSON_PROPERTY_RAND = "rand";
    private String rand;

    public static final String JSON_PROPERTY_XRES_STAR = "xresStar";
    private String xresStar;

    public static final String JSON_PROPERTY_AUTN = "autn";
    private String autn;

    public static final String JSON_PROPERTY_KAUSF = "kausf";
    private String kausf;

    public Av5GHeAka()
    {
    }

    public Av5GHeAka avType(String avType)
    {

        this.avType = avType;
        return this;
    }

    /**
     * Get avType
     * 
     * @return avType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_AV_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAvType()
    {
        return avType;
    }

    @JsonProperty(JSON_PROPERTY_AV_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAvType(String avType)
    {
        this.avType = avType;
    }

    public Av5GHeAka rand(String rand)
    {

        this.rand = rand;
        return this;
    }

    /**
     * Get rand
     * 
     * @return rand
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_RAND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getRand()
    {
        return rand;
    }

    @JsonProperty(JSON_PROPERTY_RAND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRand(String rand)
    {
        this.rand = rand;
    }

    public Av5GHeAka xresStar(String xresStar)
    {

        this.xresStar = xresStar;
        return this;
    }

    /**
     * Get xresStar
     * 
     * @return xresStar
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_XRES_STAR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getXresStar()
    {
        return xresStar;
    }

    @JsonProperty(JSON_PROPERTY_XRES_STAR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setXresStar(String xresStar)
    {
        this.xresStar = xresStar;
    }

    public Av5GHeAka autn(String autn)
    {

        this.autn = autn;
        return this;
    }

    /**
     * Get autn
     * 
     * @return autn
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_AUTN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAutn()
    {
        return autn;
    }

    @JsonProperty(JSON_PROPERTY_AUTN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAutn(String autn)
    {
        this.autn = autn;
    }

    public Av5GHeAka kausf(String kausf)
    {

        this.kausf = kausf;
        return this;
    }

    /**
     * Get kausf
     * 
     * @return kausf
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_KAUSF)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getKausf()
    {
        return kausf;
    }

    @JsonProperty(JSON_PROPERTY_KAUSF)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setKausf(String kausf)
    {
        this.kausf = kausf;
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
        Av5GHeAka av5GHeAka = (Av5GHeAka) o;
        return Objects.equals(this.avType, av5GHeAka.avType) && Objects.equals(this.rand, av5GHeAka.rand) && Objects.equals(this.xresStar, av5GHeAka.xresStar)
               && Objects.equals(this.autn, av5GHeAka.autn) && Objects.equals(this.kausf, av5GHeAka.kausf);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(avType, rand, xresStar, autn, kausf);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Av5GHeAka {\n");
        sb.append("    avType: ").append(toIndentedString(avType)).append("\n");
        sb.append("    rand: ").append(toIndentedString(rand)).append("\n");
        sb.append("    xresStar: ").append(toIndentedString(xresStar)).append("\n");
        sb.append("    autn: ").append(toIndentedString(autn)).append("\n");
        sb.append("    kausf: ").append(toIndentedString(kausf)).append("\n");
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
