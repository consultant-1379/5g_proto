
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Tls13LocalCipherSuite
{

    TLS_AES_256_GCM_SHA_384("tls-aes-256-gcm-sha384"),
    TLS_CHACHA_20_POLY_1305_SHA_256("tls-chacha20-poly1305-sha256"),
    TLS_AES_128_GCM_SHA_256("tls-aes-128-gcm-sha256"),
    TLS_AES_128_CCM_SHA_256("tls-aes-128-ccm-sha256"),
    TLS_AES_128_CCM_8_SHA_256("tls-aes-128-ccm-8-sha256");

    private final String value;
    private final static Map<String, Tls13LocalCipherSuite> CONSTANTS = new HashMap<String, Tls13LocalCipherSuite>();

    static
    {
        for (Tls13LocalCipherSuite c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Tls13LocalCipherSuite(String value)
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
    public static Tls13LocalCipherSuite fromValue(String value)
    {
        Tls13LocalCipherSuite constant = CONSTANTS.get(value);
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
