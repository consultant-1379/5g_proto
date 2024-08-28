/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 16, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

public class ProxyEncryptionProfile
{
    private String encryptionIdentifier;
    private String scramblingKey;
    private String initialVector;

    public ProxyEncryptionProfile()
    {
    }

    /**
     * Copy constructor
     *
     * @param pth
     */
    public ProxyEncryptionProfile(ProxyEncryptionProfile profile)
    {
        this.encryptionIdentifier = profile.encryptionIdentifier;
        this.scramblingKey = profile.scramblingKey;
        this.initialVector = profile.initialVector;

    }

    public String getEncryptionIdentifier()
    {
        return encryptionIdentifier;
    }

    public void setEncryptionIdentifier(String encryptionIdentifier)
    {
        this.encryptionIdentifier = encryptionIdentifier;
    }

    public String getScramblingKey()
    {
        return scramblingKey;
    }

    public void setScramblingKey(String scramblingKey)
    {
        this.scramblingKey = scramblingKey;
    }

    public String getInitialVector()
    {
        return this.initialVector;
    }

    public void setInitialVector(String initialVector)
    {
        this.initialVector = initialVector;
    }
}