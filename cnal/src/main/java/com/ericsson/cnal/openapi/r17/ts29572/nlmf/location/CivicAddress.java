/*
 * LMF Location
 * LMF Location Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29572.nlmf.location;

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
 * Indicates a Civic address.
 */
@ApiModel(description = "Indicates a Civic address.")
@JsonPropertyOrder({ CivicAddress.JSON_PROPERTY_COUNTRY,
                     CivicAddress.JSON_PROPERTY_A1,
                     CivicAddress.JSON_PROPERTY_A2,
                     CivicAddress.JSON_PROPERTY_A3,
                     CivicAddress.JSON_PROPERTY_A4,
                     CivicAddress.JSON_PROPERTY_A5,
                     CivicAddress.JSON_PROPERTY_A6,
                     CivicAddress.JSON_PROPERTY_P_R_D,
                     CivicAddress.JSON_PROPERTY_P_O_D,
                     CivicAddress.JSON_PROPERTY_S_T_S,
                     CivicAddress.JSON_PROPERTY_H_N_O,
                     CivicAddress.JSON_PROPERTY_H_N_S,
                     CivicAddress.JSON_PROPERTY_L_M_K,
                     CivicAddress.JSON_PROPERTY_L_O_C,
                     CivicAddress.JSON_PROPERTY_N_A_M,
                     CivicAddress.JSON_PROPERTY_P_C,
                     CivicAddress.JSON_PROPERTY_B_L_D,
                     CivicAddress.JSON_PROPERTY_U_N_I_T,
                     CivicAddress.JSON_PROPERTY_F_L_R,
                     CivicAddress.JSON_PROPERTY_R_O_O_M,
                     CivicAddress.JSON_PROPERTY_P_L_C,
                     CivicAddress.JSON_PROPERTY_P_C_N,
                     CivicAddress.JSON_PROPERTY_P_O_B_O_X,
                     CivicAddress.JSON_PROPERTY_A_D_D_C_O_D_E,
                     CivicAddress.JSON_PROPERTY_S_E_A_T,
                     CivicAddress.JSON_PROPERTY_R_D,
                     CivicAddress.JSON_PROPERTY_R_D_S_E_C,
                     CivicAddress.JSON_PROPERTY_R_D_B_R,
                     CivicAddress.JSON_PROPERTY_R_D_S_U_B_B_R,
                     CivicAddress.JSON_PROPERTY_P_R_M,
                     CivicAddress.JSON_PROPERTY_P_O_M,
                     CivicAddress.JSON_PROPERTY_USAGE_RULES,
                     CivicAddress.JSON_PROPERTY_METHOD,
                     CivicAddress.JSON_PROPERTY_PROVIDED_BY })
public class CivicAddress
{
    public static final String JSON_PROPERTY_COUNTRY = "country";
    private String country;

    public static final String JSON_PROPERTY_A1 = "A1";
    private String A1;

    public static final String JSON_PROPERTY_A2 = "A2";
    private String A2;

    public static final String JSON_PROPERTY_A3 = "A3";
    private String A3;

    public static final String JSON_PROPERTY_A4 = "A4";
    private String A4;

    public static final String JSON_PROPERTY_A5 = "A5";
    private String A5;

    public static final String JSON_PROPERTY_A6 = "A6";
    private String A6;

    public static final String JSON_PROPERTY_P_R_D = "PRD";
    private String PRD;

    public static final String JSON_PROPERTY_P_O_D = "POD";
    private String POD;

    public static final String JSON_PROPERTY_S_T_S = "STS";
    private String STS;

    public static final String JSON_PROPERTY_H_N_O = "HNO";
    private String HNO;

    public static final String JSON_PROPERTY_H_N_S = "HNS";
    private String HNS;

    public static final String JSON_PROPERTY_L_M_K = "LMK";
    private String LMK;

    public static final String JSON_PROPERTY_L_O_C = "LOC";
    private String LOC;

    public static final String JSON_PROPERTY_N_A_M = "NAM";
    private String NAM;

    public static final String JSON_PROPERTY_P_C = "PC";
    private String PC;

    public static final String JSON_PROPERTY_B_L_D = "BLD";
    private String BLD;

    public static final String JSON_PROPERTY_U_N_I_T = "UNIT";
    private String UNIT;

    public static final String JSON_PROPERTY_F_L_R = "FLR";
    private String FLR;

    public static final String JSON_PROPERTY_R_O_O_M = "ROOM";
    private String ROOM;

    public static final String JSON_PROPERTY_P_L_C = "PLC";
    private String PLC;

    public static final String JSON_PROPERTY_P_C_N = "PCN";
    private String PCN;

    public static final String JSON_PROPERTY_P_O_B_O_X = "POBOX";
    private String POBOX;

    public static final String JSON_PROPERTY_A_D_D_C_O_D_E = "ADDCODE";
    private String ADDCODE;

    public static final String JSON_PROPERTY_S_E_A_T = "SEAT";
    private String SEAT;

    public static final String JSON_PROPERTY_R_D = "RD";
    private String RD;

    public static final String JSON_PROPERTY_R_D_S_E_C = "RDSEC";
    private String RDSEC;

    public static final String JSON_PROPERTY_R_D_B_R = "RDBR";
    private String RDBR;

    public static final String JSON_PROPERTY_R_D_S_U_B_B_R = "RDSUBBR";
    private String RDSUBBR;

    public static final String JSON_PROPERTY_P_R_M = "PRM";
    private String PRM;

    public static final String JSON_PROPERTY_P_O_M = "POM";
    private String POM;

    public static final String JSON_PROPERTY_USAGE_RULES = "usageRules";
    private String usageRules;

    public static final String JSON_PROPERTY_METHOD = "method";
    private String method;

    public static final String JSON_PROPERTY_PROVIDED_BY = "providedBy";
    private String providedBy;

    public CivicAddress()
    {
    }

    public CivicAddress country(String country)
    {

        this.country = country;
        return this;
    }

    /**
     * Get country
     * 
     * @return country
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_COUNTRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCountry()
    {
        return country;
    }

    @JsonProperty(JSON_PROPERTY_COUNTRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCountry(String country)
    {
        this.country = country;
    }

    public CivicAddress A1(String A1)
    {

        this.A1 = A1;
        return this;
    }

    /**
     * Get A1
     * 
     * @return A1
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A1)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA1()
    {
        return A1;
    }

    @JsonProperty(JSON_PROPERTY_A1)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA1(String A1)
    {
        this.A1 = A1;
    }

    public CivicAddress A2(String A2)
    {

        this.A2 = A2;
        return this;
    }

    /**
     * Get A2
     * 
     * @return A2
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A2)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA2()
    {
        return A2;
    }

    @JsonProperty(JSON_PROPERTY_A2)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA2(String A2)
    {
        this.A2 = A2;
    }

    public CivicAddress A3(String A3)
    {

        this.A3 = A3;
        return this;
    }

    /**
     * Get A3
     * 
     * @return A3
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A3)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA3()
    {
        return A3;
    }

    @JsonProperty(JSON_PROPERTY_A3)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA3(String A3)
    {
        this.A3 = A3;
    }

    public CivicAddress A4(String A4)
    {

        this.A4 = A4;
        return this;
    }

    /**
     * Get A4
     * 
     * @return A4
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A4)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA4()
    {
        return A4;
    }

    @JsonProperty(JSON_PROPERTY_A4)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA4(String A4)
    {
        this.A4 = A4;
    }

    public CivicAddress A5(String A5)
    {

        this.A5 = A5;
        return this;
    }

    /**
     * Get A5
     * 
     * @return A5
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A5)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA5()
    {
        return A5;
    }

    @JsonProperty(JSON_PROPERTY_A5)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA5(String A5)
    {
        this.A5 = A5;
    }

    public CivicAddress A6(String A6)
    {

        this.A6 = A6;
        return this;
    }

    /**
     * Get A6
     * 
     * @return A6
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A6)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getA6()
    {
        return A6;
    }

    @JsonProperty(JSON_PROPERTY_A6)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setA6(String A6)
    {
        this.A6 = A6;
    }

    public CivicAddress PRD(String PRD)
    {

        this.PRD = PRD;
        return this;
    }

    /**
     * Get PRD
     * 
     * @return PRD
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_R_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPRD()
    {
        return PRD;
    }

    @JsonProperty(JSON_PROPERTY_P_R_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPRD(String PRD)
    {
        this.PRD = PRD;
    }

    public CivicAddress POD(String POD)
    {

        this.POD = POD;
        return this;
    }

    /**
     * Get POD
     * 
     * @return POD
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_O_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPOD()
    {
        return POD;
    }

    @JsonProperty(JSON_PROPERTY_P_O_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPOD(String POD)
    {
        this.POD = POD;
    }

    public CivicAddress STS(String STS)
    {

        this.STS = STS;
        return this;
    }

    /**
     * Get STS
     * 
     * @return STS
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_T_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSTS()
    {
        return STS;
    }

    @JsonProperty(JSON_PROPERTY_S_T_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSTS(String STS)
    {
        this.STS = STS;
    }

    public CivicAddress HNO(String HNO)
    {

        this.HNO = HNO;
        return this;
    }

    /**
     * Get HNO
     * 
     * @return HNO
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_H_N_O)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHNO()
    {
        return HNO;
    }

    @JsonProperty(JSON_PROPERTY_H_N_O)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHNO(String HNO)
    {
        this.HNO = HNO;
    }

    public CivicAddress HNS(String HNS)
    {

        this.HNS = HNS;
        return this;
    }

    /**
     * Get HNS
     * 
     * @return HNS
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_H_N_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHNS()
    {
        return HNS;
    }

    @JsonProperty(JSON_PROPERTY_H_N_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHNS(String HNS)
    {
        this.HNS = HNS;
    }

    public CivicAddress LMK(String LMK)
    {

        this.LMK = LMK;
        return this;
    }

    /**
     * Get LMK
     * 
     * @return LMK
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_L_M_K)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLMK()
    {
        return LMK;
    }

    @JsonProperty(JSON_PROPERTY_L_M_K)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLMK(String LMK)
    {
        this.LMK = LMK;
    }

    public CivicAddress LOC(String LOC)
    {

        this.LOC = LOC;
        return this;
    }

    /**
     * Get LOC
     * 
     * @return LOC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_L_O_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLOC()
    {
        return LOC;
    }

    @JsonProperty(JSON_PROPERTY_L_O_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLOC(String LOC)
    {
        this.LOC = LOC;
    }

    public CivicAddress NAM(String NAM)
    {

        this.NAM = NAM;
        return this;
    }

    /**
     * Get NAM
     * 
     * @return NAM
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N_A_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNAM()
    {
        return NAM;
    }

    @JsonProperty(JSON_PROPERTY_N_A_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNAM(String NAM)
    {
        this.NAM = NAM;
    }

    public CivicAddress PC(String PC)
    {

        this.PC = PC;
        return this;
    }

    /**
     * Get PC
     * 
     * @return PC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPC()
    {
        return PC;
    }

    @JsonProperty(JSON_PROPERTY_P_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPC(String PC)
    {
        this.PC = PC;
    }

    public CivicAddress BLD(String BLD)
    {

        this.BLD = BLD;
        return this;
    }

    /**
     * Get BLD
     * 
     * @return BLD
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_B_L_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBLD()
    {
        return BLD;
    }

    @JsonProperty(JSON_PROPERTY_B_L_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBLD(String BLD)
    {
        this.BLD = BLD;
    }

    public CivicAddress UNIT(String UNIT)
    {

        this.UNIT = UNIT;
        return this;
    }

    /**
     * Get UNIT
     * 
     * @return UNIT
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_U_N_I_T)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUNIT()
    {
        return UNIT;
    }

    @JsonProperty(JSON_PROPERTY_U_N_I_T)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUNIT(String UNIT)
    {
        this.UNIT = UNIT;
    }

    public CivicAddress FLR(String FLR)
    {

        this.FLR = FLR;
        return this;
    }

    /**
     * Get FLR
     * 
     * @return FLR
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_F_L_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFLR()
    {
        return FLR;
    }

    @JsonProperty(JSON_PROPERTY_F_L_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFLR(String FLR)
    {
        this.FLR = FLR;
    }

    public CivicAddress ROOM(String ROOM)
    {

        this.ROOM = ROOM;
        return this;
    }

    /**
     * Get ROOM
     * 
     * @return ROOM
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_O_O_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getROOM()
    {
        return ROOM;
    }

    @JsonProperty(JSON_PROPERTY_R_O_O_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setROOM(String ROOM)
    {
        this.ROOM = ROOM;
    }

    public CivicAddress PLC(String PLC)
    {

        this.PLC = PLC;
        return this;
    }

    /**
     * Get PLC
     * 
     * @return PLC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_L_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPLC()
    {
        return PLC;
    }

    @JsonProperty(JSON_PROPERTY_P_L_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPLC(String PLC)
    {
        this.PLC = PLC;
    }

    public CivicAddress PCN(String PCN)
    {

        this.PCN = PCN;
        return this;
    }

    /**
     * Get PCN
     * 
     * @return PCN
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_C_N)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPCN()
    {
        return PCN;
    }

    @JsonProperty(JSON_PROPERTY_P_C_N)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPCN(String PCN)
    {
        this.PCN = PCN;
    }

    public CivicAddress POBOX(String POBOX)
    {

        this.POBOX = POBOX;
        return this;
    }

    /**
     * Get POBOX
     * 
     * @return POBOX
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_O_B_O_X)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPOBOX()
    {
        return POBOX;
    }

    @JsonProperty(JSON_PROPERTY_P_O_B_O_X)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPOBOX(String POBOX)
    {
        this.POBOX = POBOX;
    }

    public CivicAddress ADDCODE(String ADDCODE)
    {

        this.ADDCODE = ADDCODE;
        return this;
    }

    /**
     * Get ADDCODE
     * 
     * @return ADDCODE
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_A_D_D_C_O_D_E)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getADDCODE()
    {
        return ADDCODE;
    }

    @JsonProperty(JSON_PROPERTY_A_D_D_C_O_D_E)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setADDCODE(String ADDCODE)
    {
        this.ADDCODE = ADDCODE;
    }

    public CivicAddress SEAT(String SEAT)
    {

        this.SEAT = SEAT;
        return this;
    }

    /**
     * Get SEAT
     * 
     * @return SEAT
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_E_A_T)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSEAT()
    {
        return SEAT;
    }

    @JsonProperty(JSON_PROPERTY_S_E_A_T)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSEAT(String SEAT)
    {
        this.SEAT = SEAT;
    }

    public CivicAddress RD(String RD)
    {

        this.RD = RD;
        return this;
    }

    /**
     * Get RD
     * 
     * @return RD
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRD()
    {
        return RD;
    }

    @JsonProperty(JSON_PROPERTY_R_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRD(String RD)
    {
        this.RD = RD;
    }

    public CivicAddress RDSEC(String RDSEC)
    {

        this.RDSEC = RDSEC;
        return this;
    }

    /**
     * Get RDSEC
     * 
     * @return RDSEC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_D_S_E_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRDSEC()
    {
        return RDSEC;
    }

    @JsonProperty(JSON_PROPERTY_R_D_S_E_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRDSEC(String RDSEC)
    {
        this.RDSEC = RDSEC;
    }

    public CivicAddress RDBR(String RDBR)
    {

        this.RDBR = RDBR;
        return this;
    }

    /**
     * Get RDBR
     * 
     * @return RDBR
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_D_B_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRDBR()
    {
        return RDBR;
    }

    @JsonProperty(JSON_PROPERTY_R_D_B_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRDBR(String RDBR)
    {
        this.RDBR = RDBR;
    }

    public CivicAddress RDSUBBR(String RDSUBBR)
    {

        this.RDSUBBR = RDSUBBR;
        return this;
    }

    /**
     * Get RDSUBBR
     * 
     * @return RDSUBBR
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_D_S_U_B_B_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRDSUBBR()
    {
        return RDSUBBR;
    }

    @JsonProperty(JSON_PROPERTY_R_D_S_U_B_B_R)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRDSUBBR(String RDSUBBR)
    {
        this.RDSUBBR = RDSUBBR;
    }

    public CivicAddress PRM(String PRM)
    {

        this.PRM = PRM;
        return this;
    }

    /**
     * Get PRM
     * 
     * @return PRM
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_R_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPRM()
    {
        return PRM;
    }

    @JsonProperty(JSON_PROPERTY_P_R_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPRM(String PRM)
    {
        this.PRM = PRM;
    }

    public CivicAddress POM(String POM)
    {

        this.POM = POM;
        return this;
    }

    /**
     * Get POM
     * 
     * @return POM
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_P_O_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPOM()
    {
        return POM;
    }

    @JsonProperty(JSON_PROPERTY_P_O_M)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPOM(String POM)
    {
        this.POM = POM;
    }

    public CivicAddress usageRules(String usageRules)
    {

        this.usageRules = usageRules;
        return this;
    }

    /**
     * Get usageRules
     * 
     * @return usageRules
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USAGE_RULES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUsageRules()
    {
        return usageRules;
    }

    @JsonProperty(JSON_PROPERTY_USAGE_RULES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsageRules(String usageRules)
    {
        this.usageRules = usageRules;
    }

    public CivicAddress method(String method)
    {

        this.method = method;
        return this;
    }

    /**
     * Get method
     * 
     * @return method
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMethod()
    {
        return method;
    }

    @JsonProperty(JSON_PROPERTY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMethod(String method)
    {
        this.method = method;
    }

    public CivicAddress providedBy(String providedBy)
    {

        this.providedBy = providedBy;
        return this;
    }

    /**
     * Get providedBy
     * 
     * @return providedBy
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PROVIDED_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProvidedBy()
    {
        return providedBy;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDED_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProvidedBy(String providedBy)
    {
        this.providedBy = providedBy;
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
        CivicAddress civicAddress = (CivicAddress) o;
        return Objects.equals(this.country, civicAddress.country) && Objects.equals(this.A1, civicAddress.A1) && Objects.equals(this.A2, civicAddress.A2)
               && Objects.equals(this.A3, civicAddress.A3) && Objects.equals(this.A4, civicAddress.A4) && Objects.equals(this.A5, civicAddress.A5)
               && Objects.equals(this.A6, civicAddress.A6) && Objects.equals(this.PRD, civicAddress.PRD) && Objects.equals(this.POD, civicAddress.POD)
               && Objects.equals(this.STS, civicAddress.STS) && Objects.equals(this.HNO, civicAddress.HNO) && Objects.equals(this.HNS, civicAddress.HNS)
               && Objects.equals(this.LMK, civicAddress.LMK) && Objects.equals(this.LOC, civicAddress.LOC) && Objects.equals(this.NAM, civicAddress.NAM)
               && Objects.equals(this.PC, civicAddress.PC) && Objects.equals(this.BLD, civicAddress.BLD) && Objects.equals(this.UNIT, civicAddress.UNIT)
               && Objects.equals(this.FLR, civicAddress.FLR) && Objects.equals(this.ROOM, civicAddress.ROOM) && Objects.equals(this.PLC, civicAddress.PLC)
               && Objects.equals(this.PCN, civicAddress.PCN) && Objects.equals(this.POBOX, civicAddress.POBOX)
               && Objects.equals(this.ADDCODE, civicAddress.ADDCODE) && Objects.equals(this.SEAT, civicAddress.SEAT) && Objects.equals(this.RD, civicAddress.RD)
               && Objects.equals(this.RDSEC, civicAddress.RDSEC) && Objects.equals(this.RDBR, civicAddress.RDBR)
               && Objects.equals(this.RDSUBBR, civicAddress.RDSUBBR) && Objects.equals(this.PRM, civicAddress.PRM) && Objects.equals(this.POM, civicAddress.POM)
               && Objects.equals(this.usageRules, civicAddress.usageRules) && Objects.equals(this.method, civicAddress.method)
               && Objects.equals(this.providedBy, civicAddress.providedBy);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(country,
                            A1,
                            A2,
                            A3,
                            A4,
                            A5,
                            A6,
                            PRD,
                            POD,
                            STS,
                            HNO,
                            HNS,
                            LMK,
                            LOC,
                            NAM,
                            PC,
                            BLD,
                            UNIT,
                            FLR,
                            ROOM,
                            PLC,
                            PCN,
                            POBOX,
                            ADDCODE,
                            SEAT,
                            RD,
                            RDSEC,
                            RDBR,
                            RDSUBBR,
                            PRM,
                            POM,
                            usageRules,
                            method,
                            providedBy);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CivicAddress {\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    A1: ").append(toIndentedString(A1)).append("\n");
        sb.append("    A2: ").append(toIndentedString(A2)).append("\n");
        sb.append("    A3: ").append(toIndentedString(A3)).append("\n");
        sb.append("    A4: ").append(toIndentedString(A4)).append("\n");
        sb.append("    A5: ").append(toIndentedString(A5)).append("\n");
        sb.append("    A6: ").append(toIndentedString(A6)).append("\n");
        sb.append("    PRD: ").append(toIndentedString(PRD)).append("\n");
        sb.append("    POD: ").append(toIndentedString(POD)).append("\n");
        sb.append("    STS: ").append(toIndentedString(STS)).append("\n");
        sb.append("    HNO: ").append(toIndentedString(HNO)).append("\n");
        sb.append("    HNS: ").append(toIndentedString(HNS)).append("\n");
        sb.append("    LMK: ").append(toIndentedString(LMK)).append("\n");
        sb.append("    LOC: ").append(toIndentedString(LOC)).append("\n");
        sb.append("    NAM: ").append(toIndentedString(NAM)).append("\n");
        sb.append("    PC: ").append(toIndentedString(PC)).append("\n");
        sb.append("    BLD: ").append(toIndentedString(BLD)).append("\n");
        sb.append("    UNIT: ").append(toIndentedString(UNIT)).append("\n");
        sb.append("    FLR: ").append(toIndentedString(FLR)).append("\n");
        sb.append("    ROOM: ").append(toIndentedString(ROOM)).append("\n");
        sb.append("    PLC: ").append(toIndentedString(PLC)).append("\n");
        sb.append("    PCN: ").append(toIndentedString(PCN)).append("\n");
        sb.append("    POBOX: ").append(toIndentedString(POBOX)).append("\n");
        sb.append("    ADDCODE: ").append(toIndentedString(ADDCODE)).append("\n");
        sb.append("    SEAT: ").append(toIndentedString(SEAT)).append("\n");
        sb.append("    RD: ").append(toIndentedString(RD)).append("\n");
        sb.append("    RDSEC: ").append(toIndentedString(RDSEC)).append("\n");
        sb.append("    RDBR: ").append(toIndentedString(RDBR)).append("\n");
        sb.append("    RDSUBBR: ").append(toIndentedString(RDSUBBR)).append("\n");
        sb.append("    PRM: ").append(toIndentedString(PRM)).append("\n");
        sb.append("    POM: ").append(toIndentedString(POM)).append("\n");
        sb.append("    usageRules: ").append(toIndentedString(usageRules)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    providedBy: ").append(toIndentedString(providedBy)).append("\n");
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
