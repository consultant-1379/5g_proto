/*
 * Unified Data Repository Service API file for subscription data
 * Unified Data Repository Service (subscription data).   The API version is defined in 3GPP TS 29.504.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29505.subscription.data;

import java.util.Arrays;
import java.util.Objects;

import org.openapitools.jackson.nullable.JsonNullable;

import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.CommunicationCharacteristics;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.EcRestriction;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.ExpectedUeBehaviour;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.LcsPrivacy;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.Model5MbsAuthorizationInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SorInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AcsInfoRm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * PpData
 */
@JsonPropertyOrder({ PpData.JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS,
                     PpData.JSON_PROPERTY_SUPPORTED_FEATURES,
                     PpData.JSON_PROPERTY_EXPECTED_UE_BEHAVIOUR_PARAMETERS,
                     PpData.JSON_PROPERTY_EC_RESTRICTION,
                     PpData.JSON_PROPERTY_ACS_INFO,
                     PpData.JSON_PROPERTY_STN_SR,
                     PpData.JSON_PROPERTY_LCS_PRIVACY,
                     PpData.JSON_PROPERTY_SOR_INFO,
                     PpData.JSON_PROPERTY_5MBS_AUTHORIZATION_INFO })
public class PpData
{
    public static final String JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS = "communicationCharacteristics";
    private JsonNullable<CommunicationCharacteristics> communicationCharacteristics = JsonNullable.<CommunicationCharacteristics>undefined();

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_EXPECTED_UE_BEHAVIOUR_PARAMETERS = "expectedUeBehaviourParameters";
    private JsonNullable<ExpectedUeBehaviour> expectedUeBehaviourParameters = JsonNullable.<ExpectedUeBehaviour>undefined();

    public static final String JSON_PROPERTY_EC_RESTRICTION = "ecRestriction";
    private JsonNullable<EcRestriction> ecRestriction = JsonNullable.<EcRestriction>undefined();

    public static final String JSON_PROPERTY_ACS_INFO = "acsInfo";
    private AcsInfoRm acsInfo;

    public static final String JSON_PROPERTY_STN_SR = "stnSr";
    private JsonNullable<String> stnSr = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_LCS_PRIVACY = "lcsPrivacy";
    private JsonNullable<LcsPrivacy> lcsPrivacy = JsonNullable.<LcsPrivacy>undefined();

    public static final String JSON_PROPERTY_SOR_INFO = "sorInfo";
    private SorInfo sorInfo;

    public static final String JSON_PROPERTY_5MBS_AUTHORIZATION_INFO = "5mbsAuthorizationInfo";
    private JsonNullable<Model5MbsAuthorizationInfo> _5mbsAuthorizationInfo = JsonNullable.<Model5MbsAuthorizationInfo>undefined();

    public PpData()
    {
    }

    public PpData communicationCharacteristics(CommunicationCharacteristics communicationCharacteristics)
    {
        this.communicationCharacteristics = JsonNullable.<CommunicationCharacteristics>of(communicationCharacteristics);

        return this;
    }

    /**
     * Get communicationCharacteristics
     * 
     * @return communicationCharacteristics
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public CommunicationCharacteristics getCommunicationCharacteristics()
    {
        return communicationCharacteristics.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<CommunicationCharacteristics> getCommunicationCharacteristics_JsonNullable()
    {
        return communicationCharacteristics;
    }

    @JsonProperty(JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS)
    public void setCommunicationCharacteristics_JsonNullable(JsonNullable<CommunicationCharacteristics> communicationCharacteristics)
    {
        this.communicationCharacteristics = communicationCharacteristics;
    }

    public void setCommunicationCharacteristics(CommunicationCharacteristics communicationCharacteristics)
    {
        this.communicationCharacteristics = JsonNullable.<CommunicationCharacteristics>of(communicationCharacteristics);
    }

    public PpData supportedFeatures(String supportedFeatures)
    {

        this.supportedFeatures = supportedFeatures;
        return this;
    }

    /**
     * A string used to indicate the features supported by an API that is used as
     * defined in clause 6.6 in 3GPP TS 29.500. The string shall contain a bitmask
     * indicating supported features in hexadecimal representation Each character in
     * the string shall take a value of \&quot;0\&quot; to \&quot;9\&quot;,
     * \&quot;a\&quot; to \&quot;f\&quot; or \&quot;A\&quot; to \&quot;F\&quot; and
     * shall represent the support of 4 features as described in table 5.2.2-3. The
     * most significant character representing the highest-numbered features shall
     * appear first in the string, and the character representing features 1 to 4
     * shall appear last in the string. The list of features and their numbering
     * (starting with 1) are defined separately for each API. If the string contains
     * a lower number of characters than there are defined features for an API, all
     * features that would be represented by characters that are not present in the
     * string are not supported.
     * 
     * @return supportedFeatures
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupportedFeatures()
    {
        return supportedFeatures;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
    }

    public PpData expectedUeBehaviourParameters(ExpectedUeBehaviour expectedUeBehaviourParameters)
    {
        this.expectedUeBehaviourParameters = JsonNullable.<ExpectedUeBehaviour>of(expectedUeBehaviourParameters);

        return this;
    }

    /**
     * Get expectedUeBehaviourParameters
     * 
     * @return expectedUeBehaviourParameters
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public ExpectedUeBehaviour getExpectedUeBehaviourParameters()
    {
        return expectedUeBehaviourParameters.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_EXPECTED_UE_BEHAVIOUR_PARAMETERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<ExpectedUeBehaviour> getExpectedUeBehaviourParameters_JsonNullable()
    {
        return expectedUeBehaviourParameters;
    }

    @JsonProperty(JSON_PROPERTY_EXPECTED_UE_BEHAVIOUR_PARAMETERS)
    public void setExpectedUeBehaviourParameters_JsonNullable(JsonNullable<ExpectedUeBehaviour> expectedUeBehaviourParameters)
    {
        this.expectedUeBehaviourParameters = expectedUeBehaviourParameters;
    }

    public void setExpectedUeBehaviourParameters(ExpectedUeBehaviour expectedUeBehaviourParameters)
    {
        this.expectedUeBehaviourParameters = JsonNullable.<ExpectedUeBehaviour>of(expectedUeBehaviourParameters);
    }

    public PpData ecRestriction(EcRestriction ecRestriction)
    {
        this.ecRestriction = JsonNullable.<EcRestriction>of(ecRestriction);

        return this;
    }

    /**
     * Get ecRestriction
     * 
     * @return ecRestriction
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public EcRestriction getEcRestriction()
    {
        return ecRestriction.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_EC_RESTRICTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<EcRestriction> getEcRestriction_JsonNullable()
    {
        return ecRestriction;
    }

    @JsonProperty(JSON_PROPERTY_EC_RESTRICTION)
    public void setEcRestriction_JsonNullable(JsonNullable<EcRestriction> ecRestriction)
    {
        this.ecRestriction = ecRestriction;
    }

    public void setEcRestriction(EcRestriction ecRestriction)
    {
        this.ecRestriction = JsonNullable.<EcRestriction>of(ecRestriction);
    }

    public PpData acsInfo(AcsInfoRm acsInfo)
    {

        this.acsInfo = acsInfo;
        return this;
    }

    /**
     * Get acsInfo
     * 
     * @return acsInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACS_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AcsInfoRm getAcsInfo()
    {
        return acsInfo;
    }

    @JsonProperty(JSON_PROPERTY_ACS_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAcsInfo(AcsInfoRm acsInfo)
    {
        this.acsInfo = acsInfo;
    }

    public PpData stnSr(String stnSr)
    {
        this.stnSr = JsonNullable.<String>of(stnSr);

        return this;
    }

    /**
     * String representing the STN-SR as defined in clause 18.6 of 3GPP TS 23.003
     * with the OpenAPI &#39;nullable: true&#39; property.
     * 
     * @return stnSr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing the STN-SR as defined in clause 18.6 of 3GPP TS 23.003 with the OpenAPI 'nullable: true' property.  ")
    @JsonIgnore

    public String getStnSr()
    {
        return stnSr.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_STN_SR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getStnSr_JsonNullable()
    {
        return stnSr;
    }

    @JsonProperty(JSON_PROPERTY_STN_SR)
    public void setStnSr_JsonNullable(JsonNullable<String> stnSr)
    {
        this.stnSr = stnSr;
    }

    public void setStnSr(String stnSr)
    {
        this.stnSr = JsonNullable.<String>of(stnSr);
    }

    public PpData lcsPrivacy(LcsPrivacy lcsPrivacy)
    {
        this.lcsPrivacy = JsonNullable.<LcsPrivacy>of(lcsPrivacy);

        return this;
    }

    /**
     * Get lcsPrivacy
     * 
     * @return lcsPrivacy
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public LcsPrivacy getLcsPrivacy()
    {
        return lcsPrivacy.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_LCS_PRIVACY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<LcsPrivacy> getLcsPrivacy_JsonNullable()
    {
        return lcsPrivacy;
    }

    @JsonProperty(JSON_PROPERTY_LCS_PRIVACY)
    public void setLcsPrivacy_JsonNullable(JsonNullable<LcsPrivacy> lcsPrivacy)
    {
        this.lcsPrivacy = lcsPrivacy;
    }

    public void setLcsPrivacy(LcsPrivacy lcsPrivacy)
    {
        this.lcsPrivacy = JsonNullable.<LcsPrivacy>of(lcsPrivacy);
    }

    public PpData sorInfo(SorInfo sorInfo)
    {

        this.sorInfo = sorInfo;
        return this;
    }

    /**
     * Get sorInfo
     * 
     * @return sorInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SOR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SorInfo getSorInfo()
    {
        return sorInfo;
    }

    @JsonProperty(JSON_PROPERTY_SOR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSorInfo(SorInfo sorInfo)
    {
        this.sorInfo = sorInfo;
    }

    public PpData _5mbsAuthorizationInfo(Model5MbsAuthorizationInfo _5mbsAuthorizationInfo)
    {
        this._5mbsAuthorizationInfo = JsonNullable.<Model5MbsAuthorizationInfo>of(_5mbsAuthorizationInfo);

        return this;
    }

    /**
     * Get _5mbsAuthorizationInfo
     * 
     * @return _5mbsAuthorizationInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public Model5MbsAuthorizationInfo get5mbsAuthorizationInfo()
    {
        return _5mbsAuthorizationInfo.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_5MBS_AUTHORIZATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Model5MbsAuthorizationInfo> get5mbsAuthorizationInfo_JsonNullable()
    {
        return _5mbsAuthorizationInfo;
    }

    @JsonProperty(JSON_PROPERTY_5MBS_AUTHORIZATION_INFO)
    public void set5mbsAuthorizationInfo_JsonNullable(JsonNullable<Model5MbsAuthorizationInfo> _5mbsAuthorizationInfo)
    {
        this._5mbsAuthorizationInfo = _5mbsAuthorizationInfo;
    }

    public void set5mbsAuthorizationInfo(Model5MbsAuthorizationInfo _5mbsAuthorizationInfo)
    {
        this._5mbsAuthorizationInfo = JsonNullable.<Model5MbsAuthorizationInfo>of(_5mbsAuthorizationInfo);
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
        PpData ppData = (PpData) o;
        return equalsNullable(this.communicationCharacteristics, ppData.communicationCharacteristics)
               && Objects.equals(this.supportedFeatures, ppData.supportedFeatures)
               && equalsNullable(this.expectedUeBehaviourParameters, ppData.expectedUeBehaviourParameters)
               && equalsNullable(this.ecRestriction, ppData.ecRestriction) && Objects.equals(this.acsInfo, ppData.acsInfo)
               && equalsNullable(this.stnSr, ppData.stnSr) && equalsNullable(this.lcsPrivacy, ppData.lcsPrivacy) && Objects.equals(this.sorInfo, ppData.sorInfo)
               && equalsNullable(this._5mbsAuthorizationInfo, ppData._5mbsAuthorizationInfo);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hashCodeNullable(communicationCharacteristics),
                            supportedFeatures,
                            hashCodeNullable(expectedUeBehaviourParameters),
                            hashCodeNullable(ecRestriction),
                            acsInfo,
                            hashCodeNullable(stnSr),
                            hashCodeNullable(lcsPrivacy),
                            sorInfo,
                            hashCodeNullable(_5mbsAuthorizationInfo));
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
        sb.append("class PpData {\n");
        sb.append("    communicationCharacteristics: ").append(toIndentedString(communicationCharacteristics)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    expectedUeBehaviourParameters: ").append(toIndentedString(expectedUeBehaviourParameters)).append("\n");
        sb.append("    ecRestriction: ").append(toIndentedString(ecRestriction)).append("\n");
        sb.append("    acsInfo: ").append(toIndentedString(acsInfo)).append("\n");
        sb.append("    stnSr: ").append(toIndentedString(stnSr)).append("\n");
        sb.append("    lcsPrivacy: ").append(toIndentedString(lcsPrivacy)).append("\n");
        sb.append("    sorInfo: ").append(toIndentedString(sorInfo)).append("\n");
        sb.append("    _5mbsAuthorizationInfo: ").append(toIndentedString(_5mbsAuthorizationInfo)).append("\n");
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
