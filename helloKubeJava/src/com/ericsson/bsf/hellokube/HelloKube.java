/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 8, 2018
 *     Author: eedbjhe
 */

package com.ericsson.bsf.hellokube;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class HelloKube
{

    private static void wait(int seconds)
    {
        try
        {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void typeWriter(String message,
                                 long millisPerChar)
    {
        for (int i = 0; i < message.length(); i++)
        {
            System.out.print(message.charAt(i));

            try
            {
                Thread.sleep(millisPerChar);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println();
    }

    private static void sing()
    {
        InputStream stream = HelloKube.class.getResourceAsStream("/resources/lyrics.txt");
        Scanner input = null;
        try
        {
            input = new Scanner(stream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while (input.hasNextLine())
        {
            typeWriter(input.nextLine(), 30);
            wait(1);
        }
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        input.close();
    }

    public static void main(String[] args)
    {

        typeWriter("Hello! I am a tiny kube!", 30);
        wait(1);
        typeWriter("And I like to sing!\n", 30);
        wait(1);

        while (true)
        {
            sing();
            System.out.println("\nLet's go again!");
            wait(3);
        }
    }
}
