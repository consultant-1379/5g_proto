/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 27, 2024
 *     Author: zpitgio
 */

package com.ericsson.sc.certmcrhandler.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretTlsDataList
{
    private static final Logger log = LoggerFactory.getLogger(SecretTlsDataList.class);
    private Map<String, TlsData> secrets;

    public SecretTlsDataList()
    {
        this.secrets = new HashMap<>();
    }

    /**
     * @return the secrets
     */
    public Map<String, TlsData> getSecrets()
    {
        return secrets;
    }

    public void addSecret(String secretName,
                          String certValue,
                          String keyValue)
    {
        TlsData tlsData = new TlsData(certValue, keyValue);
        secrets.put(secretName, tlsData);

    }

    public void addSecret(String secretName,
                          String caValue)
    {
        TlsData tlsData = new TlsData(caValue);
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

    public void updateSecret(String secretName,
                             String caValue)
    {
        if (secrets.containsKey(secretName))
        {
            TlsData tlsData = new TlsData(caValue);
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(secrets);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecretTlsDataList other = (SecretTlsDataList) obj;
        return Objects.equals(secrets, other.secrets);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SecretTlsDataList [secrets=" + secrets + "]";
    }

}
