
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequiredForNfType
{

    NRF("nrf"),
    UDM("udm"),
    AMF("amf"),
    SMF("smf"),
    AUSF("ausf"),
    NEF("nef"),
    PCF("pcf"),
    SMSF("smsf"),
    NSSF("nssf"),
    UDR("udr"),
    LMF("lmf"),
    GMLC("gmlc"),
    _5_G_EIR("5g-eir"),
    SEPP("sepp"),
    UPF("upf"),
    N_3_IWF("n3iwf"),
    AF("af"),
    UDSF("udsf"),
    BSF("bsf"),
    CHF("chf"),
    NWDAF("nwdaf"),
    PCSCF("pcscf"),
    CBCF("cbcf"),
    HSS("hss"),
    UCMF("ucmf"),
    SOR_AF("sor_af"),
    SPAF("spaf"),
    MME("mme"),
    SCSAS("scsas"),
    SCEF("scef"),
    SCP("scp"),
    NSSAAF("nssaaf");

    private final String value;
    private final static Map<String, RequiredForNfType> CONSTANTS = new HashMap<String, RequiredForNfType>();

    static
    {
        for (RequiredForNfType c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private RequiredForNfType(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    @JsonValue
    public String value()
    {
        return this.value;
    }

    @JsonCreator
    public static RequiredForNfType fromValue(String value)
    {
        RequiredForNfType constant = CONSTANTS.get(value);
        if (constant == null)
        {
            throw new IllegalArgumentException(value);
        }
        else
        {
            return constant;
        }
    }

}
