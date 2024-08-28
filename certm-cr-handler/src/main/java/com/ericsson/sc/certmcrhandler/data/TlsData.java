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
 * Created on: Apr 2, 2024
 *     Author: zpalele
 */

package com.ericsson.sc.certmcrhandler.data;

import java.util.Objects;

/**
 * 
 */
public class TlsData
{

    private String certValue;
    private String keyValue;
    private String caCertValue;

    public TlsData(String certValue,
                   String keyValue)
    {
        this.certValue = certValue;
        this.keyValue = keyValue;
    }

    public TlsData(String ca)
    {
        this.caCertValue = ca;
    }

    public String getCertValue()
    {
        return this.certValue;
    }

    public String getKeyValue()
    {
        return this.keyValue;
    }

    public String getCaCertValue()
    {
        return this.caCertValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(caCertValue, certValue, keyValue);
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
        TlsData other = (TlsData) obj;
        return Objects.equals(caCertValue, other.caCertValue) && Objects.equals(certValue, other.certValue) && Objects.equals(keyValue, other.keyValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "TlsData [certValue=" + certValue + ", keyValue=" + keyValue + ", caCertValue=" + caCertValue + "]";
    }

}
