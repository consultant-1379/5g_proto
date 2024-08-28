package com.ericsson.sc.keyexporter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class ConfigTest
{

    @Test
    void fromJsonSftpConfigTest()
    {
        final var json = "[\n" //
                         + "  {\n" //
                         + "     \"host\": \"10.200.118.139\",\n" //
                         + "     \"sftpPort\": 24,\n" //
                         + "     \"username\": \"admin\",\n" //
                         + "     \"password\": \"topSecret\",\n" //
                         + "     \"uploadDir\": \"data\"\n" //
                         + "  }\n" //
                         + "]";

        final var parsed = SftpConfig.fromString(json);
        assertEquals(parsed.getHost(), "10.200.118.139");
        assertEquals(parsed.getPort(), 24);
        assertEquals(parsed.getUser(), "admin");
        assertEquals(parsed.getPassword(), "topSecret");
        assertEquals(parsed.getRemotePath(), "data");
    }

    @Test
    void fromJsonSftpConfigFailTest()
    {
        assertThrows(IllegalArgumentException.class, () -> SftpConfig.fromString("[]"));
        assertThrows(IllegalArgumentException.class, () -> SftpConfig.fromString("[\"sftpPort\" : 24]"));
    }

    @Test
    void fromJsonTapConfigTest()
    {
        final var input = "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-bsf-worker\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n" //
                          + "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-scp-worker\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n"; //
        final var fetchedServiceName = "eric-bsf-worker";

        final var parsed = TapConfig.fromString(input, fetchedServiceName).get();
        assertEquals(parsed.getServiceName(), fetchedServiceName);
        assertEquals(parsed.isTapEnabled(), false);
    }

    @Test
    void fromJsonWrongServiceNameTest()
    {
        final var input = "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-bsf-workerr\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n" //
                          + "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-scp-worker\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n"; //
        final var fetchedServiceName = "eric-bsf-worker";

        final var parsed = TapConfig.fromString(input, fetchedServiceName);
        assertTrue(parsed.isEmpty(), "Failed to ignore wrong service name");
    }

    @Test
    void fromJsonDuplicateServiceNameTest()
    {
        final var input = "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-bsf-worker\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n" //
                          + "[ \n" //
                          + "       {\n" //
                          + "            \"ServiceName\": \"eric-bsf-worker\",\n" //
                          + "            \"TAP_ENABLED\": false,\n" //
                          + "            \"TracingMode\": \"sftp\",\n" //
                          + "            \"TAP_INT\": \"eth0\",\n" //
                          + "            \"NF_TYPE\": \"bsf\", \n" //
                          + "            \"TAP_FILTER\": \"port 80\"\n" //
                          + "       }\n" //
                          + "]\n"; //
        final var fetchedServiceName = "eric-bsf-worker";

        final var parsed = TapConfig.fromString(input, fetchedServiceName).get();
        assertEquals(parsed.getServiceName(), fetchedServiceName);
        assertEquals(parsed.isTapEnabled(), false);
    }

}
