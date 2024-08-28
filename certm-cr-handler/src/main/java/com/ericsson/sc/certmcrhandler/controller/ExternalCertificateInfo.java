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
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.controller;

import java.util.Objects;

public class ExternalCertificateInfo
{
    private SecretEventType eventType;
    private String secretName;
    private String tlsCertValue;
    private String tlsKeyValue;
    private String caCrtValue;

    /**
     * @return the eventType
     */
    public SecretEventType getEventType()
    {
        return eventType;
    }

    /**
     * @param eventType
     * @param secretName
     * @param tlsCertValue
     * @param tlsKeyValue
     * @param caCrtValue
     */
    public ExternalCertificateInfo(SecretEventType eventType,
                                   String secretName,
                                   String tlsCertValue,
                                   String tlsKeyValue,
                                   String caCrtValue)
    {
        super();
        this.eventType = eventType;
        this.secretName = secretName;
        this.tlsCertValue = tlsCertValue;
        this.tlsKeyValue = tlsKeyValue;
        this.caCrtValue = caCrtValue;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(SecretEventType eventType)
    {
        this.eventType = eventType;
    }

    /**
     * @return the secretName
     */
    public String getSecretName()
    {
        return secretName;
    }

    /**
     * @param secretName the secretName to set
     */
    public void setSecretName(String secretName)
    {
        this.secretName = secretName;
    }

    /**
     * @return the tlsCertValue
     */
    public String getTlsCertValue()
    {
        return tlsCertValue;
    }

    /**
     * @param tlsCertValue the tlsCertValue to set
     */
    public void setTlsCertValue(String tlsCertValue)
    {
        this.tlsCertValue = tlsCertValue;
    }

    /**
     * @return the tlsKeyValue
     */
    public String getTlsKeyValue()
    {
        return tlsKeyValue;
    }

    /**
     * @param tlsKeyValue the tlsKeyValue to set
     */
    public void setTlsKeyValue(String tlsKeyValue)
    {
        this.tlsKeyValue = tlsKeyValue;
    }

    /**
     * @return the caCrtValue
     */
    public String getCaCrtValue()
    {
        return caCrtValue;
    }

    /**
     * @param caCrtValue the caCrtValue to set
     */
    public void setCaCrtValue(String caCrtValue)
    {
        this.caCrtValue = caCrtValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(caCrtValue, eventType, secretName, tlsCertValue, tlsKeyValue);
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
        ExternalCertificateInfo other = (ExternalCertificateInfo) obj;
        return Objects.equals(caCrtValue, other.caCrtValue) && eventType == other.eventType && Objects.equals(secretName, other.secretName)
               && Objects.equals(tlsCertValue, other.tlsCertValue) && Objects.equals(tlsKeyValue, other.tlsKeyValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ExternalCertificateInfo [eventType=" + eventType + ", secretName=" + secretName + ", tlsCertValue=" + tlsCertValue + ", tlsKeyValue="
               + tlsKeyValue + ", caCrtValue=" + caCrtValue + "]";
    }
}