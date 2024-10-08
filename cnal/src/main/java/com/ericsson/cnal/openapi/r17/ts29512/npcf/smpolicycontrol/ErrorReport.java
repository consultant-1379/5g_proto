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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
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
 * Contains the rule error reports.
 */
@ApiModel(description = "Contains the rule error reports.")
@JsonPropertyOrder({ ErrorReport.JSON_PROPERTY_ERROR,
                     ErrorReport.JSON_PROPERTY_RULE_REPORTS,
                     ErrorReport.JSON_PROPERTY_SESS_RULE_REPORTS,
                     ErrorReport.JSON_PROPERTY_POL_DEC_FAILURE_REPORTS,
                     ErrorReport.JSON_PROPERTY_INVALID_POLICY_DECS })
public class ErrorReport
{
    public static final String JSON_PROPERTY_ERROR = "error";
    private ProblemDetails error;

    public static final String JSON_PROPERTY_RULE_REPORTS = "ruleReports";
    private List<RuleReport> ruleReports = null;

    public static final String JSON_PROPERTY_SESS_RULE_REPORTS = "sessRuleReports";
    private List<SessionRuleReport> sessRuleReports = null;

    public static final String JSON_PROPERTY_POL_DEC_FAILURE_REPORTS = "polDecFailureReports";
    private List<String> polDecFailureReports = null;

    public static final String JSON_PROPERTY_INVALID_POLICY_DECS = "invalidPolicyDecs";
    private List<InvalidParam> invalidPolicyDecs = null;

    public ErrorReport()
    {
    }

    public ErrorReport error(ProblemDetails error)
    {

        this.error = error;
        return this;
    }

    /**
     * Get error
     * 
     * @return error
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ERROR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ProblemDetails getError()
    {
        return error;
    }

    @JsonProperty(JSON_PROPERTY_ERROR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setError(ProblemDetails error)
    {
        this.error = error;
    }

    public ErrorReport ruleReports(List<RuleReport> ruleReports)
    {

        this.ruleReports = ruleReports;
        return this;
    }

    public ErrorReport addRuleReportsItem(RuleReport ruleReportsItem)
    {
        if (this.ruleReports == null)
        {
            this.ruleReports = new ArrayList<>();
        }
        this.ruleReports.add(ruleReportsItem);
        return this;
    }

    /**
     * Used to report the PCC rule failure.
     * 
     * @return ruleReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Used to report the PCC rule failure.")
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

    public ErrorReport sessRuleReports(List<SessionRuleReport> sessRuleReports)
    {

        this.sessRuleReports = sessRuleReports;
        return this;
    }

    public ErrorReport addSessRuleReportsItem(SessionRuleReport sessRuleReportsItem)
    {
        if (this.sessRuleReports == null)
        {
            this.sessRuleReports = new ArrayList<>();
        }
        this.sessRuleReports.add(sessRuleReportsItem);
        return this;
    }

    /**
     * Used to report the session rule failure.
     * 
     * @return sessRuleReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Used to report the session rule failure.")
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

    public ErrorReport polDecFailureReports(List<String> polDecFailureReports)
    {

        this.polDecFailureReports = polDecFailureReports;
        return this;
    }

    public ErrorReport addPolDecFailureReportsItem(String polDecFailureReportsItem)
    {
        if (this.polDecFailureReports == null)
        {
            this.polDecFailureReports = new ArrayList<>();
        }
        this.polDecFailureReports.add(polDecFailureReportsItem);
        return this;
    }

    /**
     * Used to report failure of the policy decision and/or condition data.
     * 
     * @return polDecFailureReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Used to report failure of the policy decision and/or condition data.")
    @JsonProperty(JSON_PROPERTY_POL_DEC_FAILURE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getPolDecFailureReports()
    {
        return polDecFailureReports;
    }

    @JsonProperty(JSON_PROPERTY_POL_DEC_FAILURE_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPolDecFailureReports(List<String> polDecFailureReports)
    {
        this.polDecFailureReports = polDecFailureReports;
    }

    public ErrorReport invalidPolicyDecs(List<InvalidParam> invalidPolicyDecs)
    {

        this.invalidPolicyDecs = invalidPolicyDecs;
        return this;
    }

    public ErrorReport addInvalidPolicyDecsItem(InvalidParam invalidPolicyDecsItem)
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
        ErrorReport errorReport = (ErrorReport) o;
        return Objects.equals(this.error, errorReport.error) && Objects.equals(this.ruleReports, errorReport.ruleReports)
               && Objects.equals(this.sessRuleReports, errorReport.sessRuleReports)
               && Objects.equals(this.polDecFailureReports, errorReport.polDecFailureReports)
               && Objects.equals(this.invalidPolicyDecs, errorReport.invalidPolicyDecs);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(error, ruleReports, sessRuleReports, polDecFailureReports, invalidPolicyDecs);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorReport {\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    ruleReports: ").append(toIndentedString(ruleReports)).append("\n");
        sb.append("    sessRuleReports: ").append(toIndentedString(sessRuleReports)).append("\n");
        sb.append("    polDecFailureReports: ").append(toIndentedString(polDecFailureReports)).append("\n");
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
