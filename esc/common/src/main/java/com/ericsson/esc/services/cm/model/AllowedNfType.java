
package com.ericsson.esc.services.cm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AllowedNfType
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
    NWDAF("nwdaf");

    private final String value;
    private final static Map<String, AllowedNfType> CONSTANTS = new HashMap<String, AllowedNfType>();

    static
    {
        for (AllowedNfType c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private AllowedNfType(String value)
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
    public static AllowedNfType fromValue(String value)
    {
        AllowedNfType constant = CONSTANTS.get(value);
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
