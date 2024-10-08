/*
 * Nhss_imsUEAU
 * Nhss UE Authentication Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imsueau;

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
 * Parameters used for the Digest authentication scheme
 */
@ApiModel(description = "Parameters used for the Digest authentication scheme")
@JsonPropertyOrder({ DigestAuthentication.JSON_PROPERTY_DIGEST_REALM,
                     DigestAuthentication.JSON_PROPERTY_DIGEST_ALGORITHM,
                     DigestAuthentication.JSON_PROPERTY_DIGEST_QOP,
                     DigestAuthentication.JSON_PROPERTY_HA1 })
public class DigestAuthentication
{
    public static final String JSON_PROPERTY_DIGEST_REALM = "digestRealm";
    private String digestRealm;

    public static final String JSON_PROPERTY_DIGEST_ALGORITHM = "digestAlgorithm";
    private String digestAlgorithm;

    public static final String JSON_PROPERTY_DIGEST_QOP = "digestQop";
    private String digestQop;

    public static final String JSON_PROPERTY_HA1 = "ha1";
    private String ha1;

    public DigestAuthentication()
    {
    }

    public DigestAuthentication digestRealm(String digestRealm)
    {

        this.digestRealm = digestRealm;
        return this;
    }

    /**
     * Get digestRealm
     * 
     * @return digestRealm
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_DIGEST_REALM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDigestRealm()
    {
        return digestRealm;
    }

    @JsonProperty(JSON_PROPERTY_DIGEST_REALM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDigestRealm(String digestRealm)
    {
        this.digestRealm = digestRealm;
    }

    public DigestAuthentication digestAlgorithm(String digestAlgorithm)
    {

        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    /**
     * Algorithm used for the SIP Digest authentication scheme
     * 
     * @return digestAlgorithm
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Algorithm used for the SIP Digest authentication scheme")
    @JsonProperty(JSON_PROPERTY_DIGEST_ALGORITHM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDigestAlgorithm()
    {
        return digestAlgorithm;
    }

    @JsonProperty(JSON_PROPERTY_DIGEST_ALGORITHM)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDigestAlgorithm(String digestAlgorithm)
    {
        this.digestAlgorithm = digestAlgorithm;
    }

    public DigestAuthentication digestQop(String digestQop)
    {

        this.digestQop = digestQop;
        return this;
    }

    /**
     * Quality of Protection for the SIP Digest authentication scheme
     * 
     * @return digestQop
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Quality of Protection for the SIP Digest authentication scheme")
    @JsonProperty(JSON_PROPERTY_DIGEST_QOP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDigestQop()
    {
        return digestQop;
    }

    @JsonProperty(JSON_PROPERTY_DIGEST_QOP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDigestQop(String digestQop)
    {
        this.digestQop = digestQop;
    }

    public DigestAuthentication ha1(String ha1)
    {

        this.ha1 = ha1;
        return this;
    }

    /**
     * Get ha1
     * 
     * @return ha1
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_HA1)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getHa1()
    {
        return ha1;
    }

    @JsonProperty(JSON_PROPERTY_HA1)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setHa1(String ha1)
    {
        this.ha1 = ha1;
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
        DigestAuthentication digestAuthentication = (DigestAuthentication) o;
        return Objects.equals(this.digestRealm, digestAuthentication.digestRealm) && Objects.equals(this.digestAlgorithm, digestAuthentication.digestAlgorithm)
               && Objects.equals(this.digestQop, digestAuthentication.digestQop) && Objects.equals(this.ha1, digestAuthentication.ha1);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(digestRealm, digestAlgorithm, digestQop, ha1);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DigestAuthentication {\n");
        sb.append("    digestRealm: ").append(toIndentedString(digestRealm)).append("\n");
        sb.append("    digestAlgorithm: ").append(toIndentedString(digestAlgorithm)).append("\n");
        sb.append("    digestQop: ").append(toIndentedString(digestQop)).append("\n");
        sb.append("    ha1: ").append(toIndentedString(ha1)).append("\n");
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
