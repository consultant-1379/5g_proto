package com.ericsson.sc.sepp.manager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretCaDataList
{
    private static final Logger log = LoggerFactory.getLogger(SecretCaDataList.class);
    private String secretName;
    private Map<String, String> secrets;

    public SecretCaDataList()
    {
        this.secrets = new HashMap<>();
    }

    public void addSecret(String secretName,
                          String caValue)
    {
        secrets.put(secretName, caValue);

    }

    public void updateSecret(String secretName,
                             String caValue)
    {
        if (secrets.containsKey(secretName))
        {
            secrets.put(secretName, caValue);
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
}
