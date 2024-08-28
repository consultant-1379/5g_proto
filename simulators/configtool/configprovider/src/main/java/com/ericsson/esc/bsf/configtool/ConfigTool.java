package com.ericsson.esc.bsf.configtool;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.sc.bsf.model.BsfConfig;
import com.ericsson.sc.bsf.model.NrfIpEndpoint;
import com.ericsson.sc.bsf.model.NrfProfile;
import com.ericsson.sc.bsf.model.PcfIpEndpoint;
import com.ericsson.sc.bsf.model.PcfProfile;
import com.ericsson.sc.bsf.model.ServingIpv4AddressRange;
import com.ericsson.utilities.reactivex.VertxInstance;

public class ConfigTool
{
    private static final Logger log = LoggerFactory.getLogger(ConfigTool.class);

    public static long getPID()
    {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    public static void main(String[] args) throws Exception
    {
        int exitStatus = 0;

        log.info(getPID() + ": Started...");

        CmAdapter<BsfConfig> cmProvider = new CmAdapter<BsfConfig>(BsfConfig.class,
                                                                   "bsf" + "-" + new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace"))),
                                                                   VertxInstance.get());

        BsfConfig bsf = new BsfConfig();

        {
            List<NrfIpEndpoint> nrfIpEndpoints = new ArrayList<>();
            nrfIpEndpoints.add(new NrfIpEndpoint("127.0.0.1", "IPv6Prefix1", "TCP", 8080));
            nrfIpEndpoints.add(new NrfIpEndpoint("127.0.0.2", "IPv6Prefix2", "TCP", 8081));
            NrfProfile nrfProfile = new NrfProfile(new URI("http://nrfsim/server"), nrfIpEndpoints);

            bsf.setNrfProfile(nrfProfile);
        }

        {
            List<PcfIpEndpoint> pcfIpEndpoints = new ArrayList<>();
            pcfIpEndpoints.add(new PcfIpEndpoint("10.47.11.1", null, "TCP", 8180));
            pcfIpEndpoints.add(new PcfIpEndpoint("10.47.11.2", null, "TCP", 8181));

            List<ServingIpv4AddressRange> servingIpv4AddressRanges = new ArrayList<>();
            servingIpv4AddressRanges.add(new ServingIpv4AddressRange("10.47.11.1", "10.47.11.2"));

            List<PcfProfile> pcfProfiles = new ArrayList<>();
            pcfProfiles.add(new PcfProfile("pcfNfId1", pcfIpEndpoints, "pcf001.slice-v2x.opx.3gpp", servingIpv4AddressRanges, null, null, null));

            bsf.setPcfProfiles(pcfProfiles);
        }

        Integer result;
        int rounds = 0;

        do
        {
            result = cmProvider.getConfiguration().update(bsf).blockingGet();

            if (result < 200 || result > 299)
            {
                log.info("Update configuration failed with status code {}. Retrying...", result);
                Thread.sleep(1000);
            }
        }
        while (rounds++ < 3 && (result < 200 || result > 299));

        Thread.sleep(12000);

        bsf.getNrfProfile().setUri(new URI("http://nrfsim/server2"));
        cmProvider.getConfiguration().update(bsf).blockingGet();

        /* commented out to have a stable config for the demo
        Thread.sleep(12000);

        cmProvider.getConfiguration().delete().blockingGet();

        Thread.sleep(60000000);
        */

        // wait forever
        while (true)
        {
            Thread.sleep(Long.MAX_VALUE);
        }

        // System.exit(exitStatus);
    }

}
