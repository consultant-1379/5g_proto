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
 * Created on: Sep 8, 2020
 *     Author: evouioa
 */

package com.ericsson.foozer;

import java.io.IOException;

import io.kubernetes.client.openapi.ApiException;

/**
 * === The FOOtprint analyzER ===
 */
public class Ignition
{

    public static void main(String[] args) throws ApiException, IOException
    {
        Scanner scanner = new Scanner();
        System.out.println(scanner.getPodCsv());
        System.out.println(scanner.getPvcCsv());
        System.out.println(scanner.getTotalResourceS());
    }
}
