/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy;

public class ProxyConstants
{
    private ProxyConstants()
    {
        // Private constructor
    }

    public static final class METADATA
    {
        private METADATA()
        {
        }

        public static final String HOST = "host";
        public static final String SEPP_SUPPORT = "support";
        public static final String SUPPORT_TFQDN = "TFQDN";
        public static final String POOL = "pool";
        public static final String SUPPORT_NF = "NF";
        public static final String INDIRECT = "Indirect";
        public static final String NF_TYPE_SCP = "scp";
        public static final String NF_TYPE_SEPP = "sepp";
        public static final String NF_TYPE_SEPP_INDIRECT = "sepp_indirect";
        public static final String NF_TYPE_NF = "NF";
        public static final String SUPPORT = "support";
        public static final String NF_INSTANCE_ID = "nfInstanceId";
        public static final String NF_TYPE = "nf_type";
        public static final String PER_NF_COUNTER = "pernfcounter";
        public static final String INTER_PLMN_FQDN = "interplmn_fqdn";
        public static final String VTAP_ENABLED = "vtap_enabled";
        public static final String MATCH_TLS = "matchTLS";
        public static final String ENDPOINT_POLICY = "endpoint_policy";
        public static final String INELIGIBLE_SANS = "ineligible_sans";
        public static final String INELIGIBLE_SANS_VERSION = "ineligible_sans_version";
        public static final String PREFIX = "prefix";

    }

    public static final class HEADERS
    {
        private HEADERS()
        {
        }

        public static final String FAILOVER_PROFILE = "x-eric-fop";
        public static final String CLUSTER = "x-cluster";
        public static final String HOST = "x-host";
        public static final String X_NOTIFY_URI = "x-notify-uri";
        public static final String PATH = "path";
        public static final String TARGET_API_ROOT = "3gpp-Sbi-Target-apiRoot";

    }
}
