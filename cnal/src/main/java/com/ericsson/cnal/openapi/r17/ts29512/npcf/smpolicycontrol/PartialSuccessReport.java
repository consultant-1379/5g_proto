/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
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
 * Includes the information reported by the SMF when some of the PCC rules
 * and/or session rules are not successfully installed/activated.
 */
@ApiModel(description = "Includes the information reported by the SMF when some of the PCC rules and/or session rules are not successfully installed/activated.")
@JsonPropertyOrder({ PartialSuccessReport.JSON_PROPERTY_FAILURE_CAUSE,
                     PartialSuccessReport.JSON_PROPERTY_RULE_REPORTS,
                     PartialSuccessReport.JSON_PROPERTY_SESS_RULE_REPORTS,
                     PartialSuccessReport.JSON_PROPERTY_UE_CAMPING_REP,
                     PartialSuccessReport.JSON_PROPERTY_POLICY_DEC_FAILURE_REPORTS,
                     PartialSuccessReport.JSON_PROPERTY_INVALID_POLICY_DECS })
public class PartialSuccessReport
{
    public static final String JSON_PROPERTY_FAILURE_CAUSE = "failureCause";
    private String failureCause;

    public static final String JSON_PROPERTY_RULE_REPORTS = "ruleReports";
    private List<RuleReport> ruleReports = null;

    public static final String JSON_PROPERTY_SESS_RULE_REPORTS = "sessRuleReports";
    private List<SessionRuleReport> sessRuleReports = null;

    public static final String JSON_PROPERTY_UE_CAMPING_REP = "ueCampingRep";
    private UeCampingRep ueCampingRep;

    public static final String JSON_PROPERTY_POLICY_DEC_FAILURE_REPORTS = "policyDecFailureReports";
    private List<String> policyDecFailureReports = null;

    public static final String JSON_PROPERTY_INVALID_POLICY_DECS = "invalidPolicyDecs";
    private List<InvalidParam> invalidPolicyDecs = null;

    public PartialSuccessReport()
    {
    }

    public PartialSuccessReport failureCause(String failureCause)
    {

        this.failureCause = failureCause;
        return this;
    }

    /**
     * Indicates the cause of the failure in a Partial Success Report.
     * 
     * @return failureCause
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the cause of the failure in a Partial Success Report.")
    @JsonProperty(JSON_PROPERTY_FAILURE_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getFailureCause()
    {
        return failureCause;
    }

    @JsonProperty(JSON_PROPERTY_FAILURE_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFailureCause(String failureCause)
    {
        this.failureCause = failureCause;
    }

    public PartialSuccessReport ruleReports(List<RuleReport> ruleReports)
    {

        this.ruleReports = ruleReports;
        return this;
    }

    public PartialSuccessReport addRuleReportsItem(RuleReport ruleReportsItem)
    {
        if (this.ruleReports == null)
        {
            this.ruleReports = new ArrayList<>();
        }
        this.ruleReports.add(ruleReportsItem);
        return this;
    }

    /**
     * Information about the PCC rules provisioned by the PCF not successfully
     * installed/activated.
     * 
     * @return ruleReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Information about the PCC rules provisioned by the PCF not successfully installed/activated.")
    @JsonProperty(JSON_PROPERTY_RULE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RuleReport> getRuleReports()
    {
        return ruleReports;
    }

    @JsonProperty(JSON_PROPERTY_RULE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRuleReports(List<RuleReport> ruleReports)
    {
        this.ruleReports = ruleReports;
    }

    public PartialSuccessReport sessRuleReports(List<SessionRuleReport> sessRuleReports)
    {

        this.sessRuleReports = sessRuleReports;
        return this;
    }

    public PartialSuccessReport addSessRuleReportsItem(SessionRuleReport sessRuleReportsItem)
    {
        if (this.sessRuleReports == null)
        {
            this.sessRuleReports = new ArrayList<>();
        }
        this.sessRuleReports.add(sessRuleReportsItem);
        return this;
    }

    /**
     * Information about the session rules provisioned by the PCF not successfully
     * installed.
     * 
     * @return sessRuleReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Information about the session rules provisioned by the PCF not successfully installed.")
    @JsonProperty(JSON_PROPERTY_SESS_RULE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SessionRuleReport> getSessRuleReports()
    {
        return sessRuleReports;
    }

    @JsonProperty(JSON_PROPERTY_SESS_RULE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessRuleReports(List<SessionRuleReport> sessRuleReports)
    {
        this.sessRuleReports = sessRuleReports;
    }

    public PartialSuccessReport ueCampingRep(UeCampingRep ueCampingRep)
    {

        this.ueCampingRep = ueCampingRep;
        return this;
    }

    /**
     * Get ueCampingRep
     * 
     * @return ueCampingRep
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_CAMPING_REP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UeCampingRep getUeCampingRep()
    {
        return ueCampingRep;
    }

    @JsonProperty(JSON_PROPERTY_UE_CAMPING_REP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeCampingRep(UeCampingRep ueCampingRep)
    {
        this.ueCampingRep = ueCampingRep;
    }

    public PartialSuccessReport policyDecFailureReports(List<String> policyDecFailureReports)
    {

        this.policyDecFailureReports = policyDecFailureReports;
        return this;
    }

    public PartialSuccessReport addPolicyDecFailureReportsItem(String policyDecFailureReportsItem)
    {
        if (this.policyDecFailureReports == null)
        {
            this.policyDecFailureReports = new ArrayList<>();
        }
        this.policyDecFailureReports.add(policyDecFailureReportsItem);
        return this;
    }

    /**
     * Contains the type(s) of failed policy decision and/or condition data.
     * 
     * @return policyDecFailureReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the type(s) of failed policy decision and/or condition data.")
    @JsonProperty(JSON_PROPERTY_POLICY_DEC_FAILURE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getPolicyDecFailureReports()
    {
        return policyDecFailureReports;
    }

    @JsonProperty(JSON_PROPERTY_POLICY_DEC_FAILURE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPolicyDecFailureReports(List<String> policyDecFailureReports)
    {
        this.policyDecFailureReports = policyDecFailureReports;
    }

    public PartialSuccessReport invalidPolicyDecs(List<InvalidParam> invalidPolicyDecs)
    {

        this.invalidPolicyDecs = invalidPolicyDecs;
        return this;
    }

    public PartialSuccessReport addInvalidPolicyDecsItem(InvalidParam invalidPolicyDecsItem)
    {
        if (this.invalidPolicyDecs == null)
        {
            this.invalidPolicyDecs = new ArrayList<>();
        }
        this.invalidPolicyDecs.add(invalidPolicyDecsItem);
        return this;
    }

    /**
     * Indicates the invalid parameters for the reported type(s) of the failed
     * policy decision and/or condition data.
     * 
     * @return invalidPolicyDecs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the invalid parameters for the reported type(s) of the failed policy decision and/or condition data.")
    @JsonProperty(JSON_PROPERTY_INVALID_POLICY_DECS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<InvalidParam> getInvalidPolicyDecs()
    {
        return invalidPolicyDecs;
    }

    @JsonProperty(JSON_PROPERTY_INVALID_POLICY_DECS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInvalidPolicyDecs(List<InvalidParam> invalidPolicyDecs)
    {
        this.invalidPolicyDecs = invalidPolicyDecs;
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
        PartialSuccessReport partialSuccessReport = (PartialSuccessReport) o;
        return Objects.equals(this.failureCause, partialSuccessReport.failureCause) && Objects.equals(this.ruleReports, partialSuccessReport.ruleReports)
               && Objects.equals(this.sessRuleReports, partialSuccessReport.sessRuleReports)
               && Objects.equals(this.ueCampingRep, partialSuccessReport.ueCampingRep)
               && Objects.equals(this.policyDecFailureReports, partialSuccessReport.policyDecFailureReports)
               && Objects.equals(this.invalidPolicyDecs, partialSuccessReport.invalidPolicyDecs);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(failureCause, ruleReports, sessRuleReports, ueCampingRep, policyDecFailureReports, invalidPolicyDecs);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PartialSuccessReport {\n");
        sb.append("    failureCause: ").append(toIndentedString(failureCause)).append("\n");
        sb.append("    ruleReports: ").append(toIndentedString(ruleReports)).append("\n");
        sb.append("    sessRuleReports: ").append(toIndentedString(sessRuleReports)).append("\n");
        sb.append("    ueCampingRep: ").append(toIndentedString(ueCampingRep)).append("\n");
        sb.append("    policyDecFailureReports: ").append(toIndentedString(policyDecFailureReports)).append("\n");
        sb.append("    invalidPolicyDecs: ").append(toIndentedString(invalidPolicyDecs)).append("\n");
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
