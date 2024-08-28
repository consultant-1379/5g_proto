package com.ericsson.sc.rxkms;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

public class FileHandler
{
    /*
     * Testcases: FileHandlerTest readTokenFromFile
     */
    public Optional<String> readFile(String filePath)
    {
        File file = new File(filePath);
        if (!file.canRead())
        {
            return Optional.empty();
        }
        try
        {
            FileReader fr = new FileReader(file);
            BufferedReader bfr = new BufferedReader(fr);
            return Optional.of(bfr.readLine());
        }
        catch (IOException e)
        {
            return Optional.empty();
        }
    }
}