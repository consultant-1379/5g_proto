package com.ericsson.sc.certmcrhandlertest;

import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.sc.certmcrhandler.k8s.io.CmWatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.lang.Thread;

public class Main
{
    private static final String LOG_CONTROL_PATH = URI.create("/seppmanager/config/crcm").getPath();
    private static final String LOG_CONTROL_FILE = "crcm.json";
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String Args[])
    {
        new CmWatcher(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build());
        while (true)
        {
            try
            {
                Thread.sleep(1000 * 60);
            }
            catch (InterruptedException e)
            {
                log.error("Error: ", e);
            }
        }
    }
}
