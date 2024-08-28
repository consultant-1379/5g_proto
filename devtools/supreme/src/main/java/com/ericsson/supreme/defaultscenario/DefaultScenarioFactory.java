/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 5, 2022
 *     Author: esamioa
 */

package com.ericsson.supreme.defaultscenario;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.DefaultScenarioException;

/**
 * Factory that creates the appropriate Default Scenario
 */
public final class DefaultScenarioFactory
{
    private final Configuration config;

    public DefaultScenarioFactory(Configuration config)
    {
        this.config = config;
    }

    public Processor getDefaultScenario(DefaultScenarioType defaultScenarioType) throws DefaultScenarioException
    {
        switch (defaultScenarioType)
        {
            case ROOTCA:
                return new CommonRootCa(config);
            case SCPMGR:
                return new ScpManager(config);
            case SCPWRK:
                return new ScpWorker(config);
            case SEPPMGR:
                return new SeppManager(config);
            case SEPPWRK:
                return new SeppWorker(config);
            case BSFMGR:
                return new BsfManager(config);
            case BSFWRK:
                return new BsfWorker(config);
            case K6:
                return new K6(config);
            case DIAMETER:
                return new Diameter(config);
            case DSCLOAD:
                return new DscLoad(config);
            case INFLUXDB:
                return new InfluxDb(config);
            case TELEGRAF:
                return new Telegraf(config);
            case INTERNAL_LDAP:
                return new InternalLdap(config);
            case REFERRAL_LDAP:
                return new ReferralLdap(config);
            case NLF:
                return new Nlf(config);
            case SLF:
                return new Slf(config);
            case SYSLOG:
                return new Syslog(config);
            case TRANSFORMER:
                return new Transformer(config);
            case NBI:
                return new Nbi(config);
            case CHFSIM:
                return new Chfsim(config);
            case CHFSIM_SEPP:
                return new ChfsimSepp(config);
            case NRFSIM:
                return new Nrfsim(config);
            case SEPPSIM:
                return new Seppsim(config);
            case SEPPSIM_SCP:
                return new SeppsimScp(config);
            case SEPPSIM_N32C:
                return new SeppsimN32c(config);
            case BSFLOAD:
                return new BsfLoad(config);
            case PMRW:
                return new Pmrw(config);
            case LUMBERJACK:
                return new TransformerExternal(config);
            case LUMBERJACKX:
                return new TransformerExternalMultiple(config);
            case YANG_PROVIDER:
                return new YangProvider(config);
            case CQL_CLIENT_EXTERNAL:
                return new CqlClientExternal(config);
            case CQL_SERVER_EXTERNAL:
                return new CqlServerExternal(config);
            case CASSANDRA_INTERNODE_EXTERNAL:
                return new CassandraInternodeExternal(config);
            case NETCONF_TLS_CLIENT:
                return new NetconfTlsClient(config);
            case PVTB:
                return new ProbeVirtualTapBroker(config);
            default:
                throw new DefaultScenarioException("The default scenario is not recognized");
        }
    }

    public enum DefaultScenarioType
    {
        ROOTCA("rootca"),
        SCPMGR("scpmgr"),
        SCPWRK("scpwrk"),
        SEPPMGR("seppmgr"),
        SEPPWRK("seppwrk"),
        BSFMGR("bsfmgr"),
        BSFWRK("bsfwrk"),
        K6("k6"),
        DIAMETER("diameter"),
        DSCLOAD("dscload"),
        INFLUXDB("influxdb"),
        TELEGRAF("telegraf"),
        INTERNAL_LDAP("internal-ldap"),
        REFERRAL_LDAP("referral-ldap"),
        NLF("nlf"),
        SLF("slf"),
        SYSLOG("syslog"),
        TRANSFORMER("transformer"),
        NBI("nbi"),
        CHFSIM("chfsim"),
        NRFSIM("nrfsim"),
        CHFSIM_SEPP("chfsim-sepp"),
        SEPPSIM("seppsim"),
        SEPPSIM_SCP("seppsim-scp"),
        SEPPSIM_N32C("seppsim-n32c"),
        BSFLOAD("bsfload"),
        PMRW("pmrw"),
        LUMBERJACK("ext-lj"),
        LUMBERJACKX("ext-lj-x"),
        YANG_PROVIDER("yang-provider"),
        CQL_CLIENT_EXTERNAL("cql-client-external"),
        CQL_SERVER_EXTERNAL("cql-server-external"),
        CASSANDRA_INTERNODE_EXTERNAL("cassandra-internode-external"),
        NETCONF_TLS_CLIENT("netconf-tls-client"),
        PVTB("pvtb");

        private static final Map<String, DefaultScenarioType> CONSTANTS = new HashMap<>();

        static
        {
            for (DefaultScenarioType c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private String value;

        DefaultScenarioType(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        public static DefaultScenarioType fromValue(String value)
        {
            DefaultScenarioType constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }
}
