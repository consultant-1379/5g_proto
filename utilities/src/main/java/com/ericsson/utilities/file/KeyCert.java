/** 
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 21, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.file;

public interface KeyCert
{
    String getPrivateKey();

    String getCertificate();

    static KeyCert create(final String privateKey,
                          final String certificate)
    {
        return new KeyCert()
        {
            @Override
            public String getPrivateKey()
            {
                return privateKey;
            }

            @Override
            public String getCertificate()
            {
                return certificate;
            }
        };
    }
}
