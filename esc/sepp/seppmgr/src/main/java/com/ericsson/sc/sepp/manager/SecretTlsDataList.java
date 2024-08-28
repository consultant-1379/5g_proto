package com.ericsson.sc.sepp.manager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretTlsDataList
{
    private static final Logger log = LoggerFactory.getLogger(SecretTlsDataList.class);
    private String secretName;
    private Map<String, TlsData> secrets;

    public SecretTlsDataList()
    {
        this.secrets = new HashMap<>();
    }

    public void addSecret(String secretName,
                          String certValue,
                          String keyValue)
    {
        TlsData tlsData = new TlsData(certValue, keyValue);
        secrets.put(secretName, tlsData);

    }

    public void updateSecret(String secretName,
                             String certValue,
                             String keyValue)
    {
        if (secrets.containsKey(secretName))
        {
            TlsData tlsData = new TlsData(certValue, keyValue);
            secrets.put(secretName, tlsData);
        }
        else
        {
            log.error("Secret with name {} does not exist on the list, so cannot be updated.", secretName);
        }
    }

    public void deleteSecret(String secretName)
    {
        if (secrets.containsKey(secretName))
        {
            secrets.remove(secretName);
        }
        else
        {
            log.error("Secret with name {} does not exist, so cannot be deleted.", secretName);
        }
    }

    public TlsData getTlsData(String secretName)
    {
        return secrets.get(secretName);
    }
}

class TlsData
{
    private String certValue;
    private String keyValue;

    public TlsData(String certValue,
                   String keyValue)
    {
        this.certValue = certValue;
        this.keyValue = keyValue;
    }

    public String getCertValue()
    {
        return certValue;
    }

    public String getKeyValue()
    {
        return keyValue;
    }
}