package com.ericsson.sc.rxkms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.apache.commons.io.FileUtils;

public class FileHandlerTest
{
    private static final String EXAMPLE_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImN6MVZYYkwwWjltVFpVUkRaREgyOFJjeTJOdlVpVDZmUHJPVmhZbkNjYW8ifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiLCJrM3MiXSwiZXhwIjoxNzIxNzM3Mjc4LCJpYXQiOjE2OTAyMDEyNzgsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiI1Zy1ic2YtemtvdW5payIsInBvZCI6eyJuYW1lIjoic2xlZXAtN2Y4Njk2Njc2Zi1jYnNkcSIsInVpZCI6IjMxYjQ0NmQzLTZiZjktNDJmZS05MWYyLTUyZDkwMDY0YTY4YyJ9LCJzZXJ2aWNlYWNjb3VudCI6eyJuYW1lIjoidGVzdGluZy1hY2NvdW50IiwidWlkIjoiMmExMzE0ODMtZWQwNS00OWQ4LWFkNWQtZTk1MDZlZDZkYWZjIn0sIndhcm5hZnRlciI6MTY5MDIwNDg4NX0sIm5iZiI6MTY5MDIwMTI3OCwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OjVnLWJzZi16a291bmlrOnRlc3RpbmctYWNjb3VudCJ9.qo8s4cniMhX_IXLPa9jkLxDHcUHsec6GVBYZJV_2whGnPzg7VRjScznLUErIfgFUcJOMiCTt0aBvgiZjXGrkv67oOHXXYLtOq2sqUFl4fo0Tk-uhSkhDvKP7lw634-fB-QHa7rZlYmixkWQsfi_GtFaBLQMdWY7Jzg6QewyKay2dK4_SJNCNvadpeM5E_fifaA13M64Gazw_6V4ItzCkl9X6pTmnr5ACahKp-bte1mSeDhJ2PZ3ZIZOiSrZ7nDcN4vN3O0rsFBx4b7aGS5aUi80OeNSyeciRTRlYlqo2JVnZl5EaXUfybKoaZIBHuFI3Qcc-XFum1zmXeN6EXA93ig";

    public File createFile(String prefix,
                           String sufix)
    {
        try
        {
            return File.createTempFile(prefix, sufix);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public File copyFile(File src,
                         File dest)
    {
        if (!dest.canExecute())
        {
            // TODO handle exception
        }
        try
        {
            FileUtils.copyFile(src, dest);
            return dest;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
            // TODO handle error
        }
    }

    public void writeToFile(File file,
                            String text)
    {
        // if (!file.canWrite())
        // TODO throw exception
        try (FileWriter fw = new FileWriter(file))
        {
            fw.write(text, 0, text.length());
            fw.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // TODO handle exception
        }

    }

    public void createFile(File file,
                           String text)
    {
        // if (!file.canWrite() || !file.canExecute())
        // TODO throw exception
        try
        {
            if (!file.createNewFile())
            {
                System.out.println("Failed to create file " + file.getAbsolutePath());
            }
            try (FileWriter fw = new FileWriter(file))
            {
                fw.write(text, 0, text.length());
                fw.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // TODO handle exception
        }

    }

    public void createFolder(File folder)
    {
        if (!folder.mkdirs())

        {
            System.out.println("Failed to create folder " + folder.getAbsolutePath());
        }
    }

    public void deleteFile(File file)
    {
        if (!file.delete())
        {
            System.out.println("Failed to delete file " + file.getAbsolutePath());
        }
    }

    @Test
    public void readTokenFromFile()
    {
        FileHandler fh = new FileHandler();
        File file = this.createFile("prefix", "suffix");
        this.writeToFile(file, EXAMPLE_TOKEN);
        Optional<String> token = fh.readFile(file.getAbsolutePath());
        // Checks
        Assert.assertEquals(token.isPresent(), true);
        Assert.assertEquals(token.get(), EXAMPLE_TOKEN);
        // Clean
        Assert.assertEquals(file.delete(), true);
        Assert.assertEquals(file.exists(), false);
    }
}