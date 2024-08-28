
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Tls12LocalCipher
{

    ECDHE_RSA_WITH_AES_128_GCM_SHA_256("ecdhe-rsa-with-aes-128-gcm-sha256"),
    ECDHE_RSA_WITH_AES_256_GCM_SHA_384("ecdhe-rsa-with-aes-256-gcm-sha384"),
    ECDHE_ECDSA_WITH_AES_128_GCM_SHA_256("ecdhe-ecdsa-with-aes-128-gcm-sha256"),
    ECDHE_ECDSA_WITH_AES_256_GCM_SHA_384("ecdhe-ecdsa-with-aes-256-gcm-sha384"),
    ECDHE_RSA_WITH_CHACHA_20_POLY_1305_SHA_256("ecdhe-rsa-with-chacha20-poly1305-sha256"),
    ECDHE_ECDSA_WITH_CHACHA_20_POLY_1305_SHA_256("ecdhe-ecdsa-with-chacha20-poly1305-sha256"),
    RSA_WITH_AES_128_CBC_SHA("rsa-with-aes-128-cbc-sha"),
    RSA_WITH_AES_256_CBC_SHA("rsa-with-aes-256-cbc-sha"),
    RSA_WITH_AES_128_CBC_SHA_256("rsa-with-aes-128-cbc-sha256"),
    RSA_WITH_AES_256_CBC_SHA_256("rsa-with-aes-256-cbc-sha256"),
    DHE_RSA_WITH_AES_128_CBC_SHA("dhe-rsa-with-aes-128-cbc-sha"),
    DHE_RSA_WITH_AES_256_CBC_SHA("dhe-rsa-with-aes-256-cbc-sha"),
    DHE_RSA_WITH_AES_128_CBC_SHA_256("dhe-rsa-with-aes-128-cbc-sha256"),
    DHE_RSA_WITH_AES_256_CBC_SHA_256("dhe-rsa-with-aes-256-cbc-sha256"),
    ECDHE_ECDSA_WITH_AES_128_CBC_SHA_256("ecdhe-ecdsa-with-aes-128-cbc-sha256"),
    ECDHE_ECDSA_WITH_AES_256_CBC_SHA_384("ecdhe-ecdsa-with-aes-256-cbc-sha384"),
    ECDHE_RSA_WITH_AES_128_CBC_SHA_256("ecdhe-rsa-with-aes-128-cbc-sha256"),
    ECDHE_RSA_WITH_AES_256_CBC_SHA_384("ecdhe-rsa-with-aes-256-cbc-sha384"),
    ECDHE_RSA_WITH_AES_128_CBC_SHA("ecdhe-rsa-with-aes-128-cbc-sha"),
    ECDHE_RSA_WITH_AES_256_CBC_SHA("ecdhe-rsa-with-aes-256-cbc-sha"),
    RSA_WITH_3_DES_EDE_CBC_SHA("rsa-with-3des-ede-cbc-sha"),
    ECDHE_RSA_WITH_3_DES_EDE_CBC_SHA("ecdhe-rsa-with-3des-ede-cbc-sha");

    private final String value;
    private final static Map<String, Tls12LocalCipher> CONSTANTS = new HashMap<String, Tls12LocalCipher>();

    static
    {
        for (Tls12LocalCipher c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Tls12LocalCipher(String value)
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
    public static Tls12LocalCipher fromValue(String value)
    {
        Tls12LocalCipher constant = CONSTANTS.get(value);
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
