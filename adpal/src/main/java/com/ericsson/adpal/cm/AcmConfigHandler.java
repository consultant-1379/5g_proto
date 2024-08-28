package com.ericsson.adpal.cm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.model.IetfNetconfAcm;
import com.ericsson.adpal.cm.model.IetfNetconfAcmNacm;
import com.ericsson.adpal.cm.model.Rule;
import com.ericsson.adpal.cm.model.Rule__1;
import com.ericsson.adpal.cm.model.Rule__1.Action;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AcmConfigHandler
{
    private static final Logger log = LoggerFactory.getLogger(AcmConfigHandler.class);
    private static final String ERICSSON_BSF = "ericsson-bsf";
//    private static final String ERICSSON_CSA = "ericsson-csa";
    private static final String ERICSSON_SCP = "ericsson-scp";
    private static final String ERICSSON_SEPP = "ericsson-sepp";
    private static final String ERICSSON_BRM = "ericsson-brm";
    private static final String ERICSSON_STM = "ericsson-diameter-adp";
    private static final String IETF_KEYSTORE = "ietf-keystore";
    private static final String IETF_TRUSTSTORE = "ietf-truststore";
    private static final String IETF_SYSTEM = "ietf-system";
    private static final String ERICSSON_KEYSTORE_EXT = "ericsson-keystore-ext";
    private static final String ERICSSON_TRUSTSTORE_EXT = "ericsson-truststore-ext";
    private static final String ERICSSON_SYSTEM_EXT = "ericsson-system-ext";
    private static final String IETF_NETCONF_ACM = "ietf-netconf-acm";
    private static final String IETF_NETCONF_MONITORING = "ietf-netconf-monitoring";

    private static final String READ_WRITE = "*";
    private static final String READ = "read";
    private static final String PERMIT = "permit";
    private static final String CLI = "cli";
    private static final String BSF_COMMENT = "Rule updated by BSF-Manager";
//    private static final String CSA_COMMENT = "Rule updated by CSA-Manager";
    private static final String SCP_COMMENT = "Rule updated by SCP-Manager";
    private static final String SEPP_COMMENT = "Rule updated by SEPP-Manager";
    private static final String BSF_ADMIN = "bsf-admin";
//    private static final String CSA_ADMIN = "csa-admin";
    private static final String SCP_ADMIN = "scp-admin";
    private static final String SEPP_ADMIN = "sepp-admin";
    private static final String BSF_SECURITY_ADMIN = "bsf-security-admin";
//    private static final String CSA_SECURITY_ADMIN = "csa-security-admin";
    private static final String SCP_SECURITY_ADMIN = "scp-security-admin";
    private static final String SEPP_SECURITY_ADMIN = "sepp-security-admin";
    private static final String BSF_READ_ONLY = "bsf-read-only";
//    private static final String CSA_READ_ONLY = "csa-read-only";
    private static final String SCP_READ_ONLY = "scp-read-only";
    private static final String SEPP_READ_ONLY = "sepp-read-only";

    private static final String TAG_MODULE = "module-name";
    private static final String TAG_ACTION = "action";
    private static final String TAG_NAME = "name";
    private static final String TAG_TAILF_ACM_CONTEXT = "tailf-acm:context";
    private static final String TAG_TAILF_ACM_CMDRULE = "tailf-acm:cmdrule";
    private static final String TAG_ACCESS_OPERTATIONS = "access-operations";
    private static final String TAG_COMMENT = "comment";
    private static final String TAG_GROUP = "group";
    private static final String TAG_RULE = "rule";
    private static final String TAG_PATH = "path";
    private static final String TAG_COMMAND = "command";
    private static final String TAG_CONTEXT = "context";

    private static final String CMD_METRICS_QUERY = "metrics-query";
    private static final String CMD_SHOW_ALARM = "show alarm";
    private static final String CMD_SHOW_ALARM_HISTORY = "show alarm-history";

    private static final String SYSTEM_STATE = "system-state";
    private static final String SYSTEM = "system";
    private static final String AUTHENTICATION = "authentication";
    private static final String ADMIN_USER = "admin-user";
    private static final String NAME = "name";

    JsonObject acmConfig = new JsonObject();
    List<String> groups;
    List<Rule__1> rules;
    Rule acmRuleList;
    ObjectMapper objectMapper = Jackson.om();

    public AcmConfigHandler()
    {
        // Do nothing because thats what I want!
    }

    public static IetfNetconfAcm addScpAcmDefaultConfig(IetfNetconfAcm config)
    {
        try
        {
            IetfNetconfAcm newConfig = CmAdapter.copyConfig(config, IetfNetconfAcm.class);
            Optional<IetfNetconfAcmNacm> nacmConfig = newConfig.getIetfNetconfAcmNacm();
            IetfNetconfAcmNacm newNacmConfig;
            if (nacmConfig.isPresent())
            {
                newNacmConfig = nacmConfig.get();
            }
            else
            {
                newNacmConfig = new IetfNetconfAcmNacm();
                newNacmConfig.setEnableNacm(true);
            }

            newNacmConfig.setDeniedDataWrites(0);
            newNacmConfig.setDeniedNotifications(0);
            newNacmConfig.setDeniedOperations(0);

            Optional<List<Rule>> rules = newNacmConfig.getRuleList();
            List<Rule> newRules;
            if (rules.isPresent())
            {
                newRules = rules.get();
            }
            else
            {
                newRules = new ArrayList<>();
            }

// CSA does not exists anymore            
//            Rule csaAdmin = new Rule().withName("ericsson-csa-manager-1-csa-admin");
//            List<String> csaAdminGroup = new ArrayList<>();
//            csaAdminGroup.add(CSA_ADMIN);
//            csaAdmin.setGroup(csaAdminGroup);
//            Rule__1 csaAdminRule = new Rule__1().withName("ericsson-csa-1-csa-admin")
//                                                .withModuleName(ERICSSON_CSA)
//                                                .withAccessOperations(READ_WRITE)
//                                                .withAction(Action.PERMIT)
//                                                .withComment(CSA_COMMENT);
//            Rule__1 certmCsaAdminKeystoreRule = new Rule__1().withName("ericsson-sec-certm-1-csa-admin")
//                                                             .withModuleName(IETF_KEYSTORE)
//                                                             .withAccessOperations(READ)
//                                                             .withAction(Action.PERMIT)
//                                                             .withComment(CSA_COMMENT);
//            Rule__1 certmCsaAdminTrustorRule = new Rule__1().withName("ericsson-sec-certm-2-csa-admin")
//                                                            .withModuleName(IETF_TRUSTSTORE)
//                                                            .withAccessOperations(READ)
//                                                            .withAction(Action.PERMIT)
//                                                            .withComment(CSA_COMMENT);
//            Rule__1 certmCsaAdminKeystoreExtRule = new Rule__1().withName("ericsson-sec-certm-3-csa-admin")
//                                                                .withModuleName(ERICSSON_KEYSTORE_EXT)
//                                                                .withAccessOperations(READ)
//                                                                .withAction(Action.PERMIT)
//                                                                .withComment(CSA_COMMENT);
//            Rule__1 certmCsaAdminTrustorExtRule = new Rule__1().withName("ericsson-sec-certm-4-csa-admin")
//                                                               .withModuleName(ERICSSON_TRUSTSTORE_EXT)
//                                                               .withAccessOperations(READ)
//                                                               .withAction(Action.PERMIT)
//                                                               .withComment(CSA_COMMENT);
//            Rule__1 brmCsaAdminRule = new Rule__1().withName("ericsson-brm-1-csa-admin")
//                                                   .withModuleName(ERICSSON_BRM)
//                                                   .withAccessOperations(READ)
//                                                   .withAction(Action.PERMIT)
//                                                   .withComment(CSA_COMMENT);
//            List<Rule__1> csaAdminRuleLists = new ArrayList<>();
//            csaAdminRuleLists.add(csaAdminRule);
//            csaAdminRuleLists.add(certmCsaAdminKeystoreRule);
//            csaAdminRuleLists.add(certmCsaAdminTrustorRule);
//            csaAdminRuleLists.add(certmCsaAdminKeystoreExtRule);
//            csaAdminRuleLists.add(certmCsaAdminTrustorExtRule);
//            csaAdminRuleLists.add(brmCsaAdminRule);
//            csaAdmin.setRule(csaAdminRuleLists);
//            newRules.add(csaAdmin);
//
//            Rule csaSecAdmin = new Rule().withName("ericsson-csa-manager-1-csa-security-admin");
//            List<String> csaSecurityAdminGroup = new ArrayList<>();
//            csaSecurityAdminGroup.add(CSA_SECURITY_ADMIN);
//            csaSecAdmin.setGroup(csaSecurityAdminGroup);
//            Rule__1 certmCsaSecAdminKeystoreRule = new Rule__1().withName("ietf-keystore-1-csa-security-admin")
//                                                                .withModuleName(IETF_KEYSTORE)
//                                                                .withAccessOperations(READ)
//                                                                .withAction(Action.PERMIT)
//                                                                .withComment(CSA_COMMENT);
//            Rule__1 certmCsaSecAdminTrustorRule = new Rule__1().withName("ietf-truststore-1-csa-security-admin")
//                                                               .withModuleName(IETF_TRUSTSTORE)
//                                                               .withAccessOperations(READ)
//                                                               .withAction(Action.PERMIT)
//                                                               .withComment(CSA_COMMENT);
//            Rule__1 certmCsaSecAdminKeystoreExtRule = new Rule__1().withName("ericsson-keystore-ext-1-csa-security-admin")
//                                                                   .withModuleName(ERICSSON_KEYSTORE_EXT)
//                                                                   .withAccessOperations(READ)
//                                                                   .withAction(Action.PERMIT)
//                                                                   .withComment(CSA_COMMENT);
//            Rule__1 certmCsaSecAdminTrustorExtRule = new Rule__1().withName("ericsson-truststore-ext-1-csa-security-admin")
//                                                                  .withModuleName(ERICSSON_TRUSTSTORE_EXT)
//                                                                  .withAccessOperations(READ)
//                                                                  .withAction(Action.PERMIT)
//                                                                  .withComment(CSA_COMMENT);
//            Rule__1 csaSecAdminIetfNetconfAcmRule = new Rule__1().withName("ietf-netconf-acm-1-csa-security-admin")
//                                                                 .withModuleName(IETF_NETCONF_ACM)
//                                                                 .withAccessOperations(READ)
//                                                                 .withAction(Action.PERMIT)
//                                                                 .withComment(CSA_COMMENT);
//            Rule__1 csaSecAdminIetfSystemStateRule = new Rule__1().withName("ietf-system-1-csa-security-admin")
//                                                                  .withModuleName(IETF_SYSTEM)
//                                                                  .withAccessOperations(READ)
//                                                                  .withAction(Action.PERMIT)
//                                                                  .withPath("/" + SYSTEM_STATE + "/")
//                                                                  .withComment(CSA_COMMENT);
//            Rule__1 csaSecAdminIetfSystemAuthRule = new Rule__1().withName("ietf-system-2-csa-security-admin")
//                                                                 .withModuleName(IETF_SYSTEM)
//                                                                 .withAccessOperations(READ)
//                                                                 .withAction(Action.PERMIT)
//                                                                 .withPath("/" + SYSTEM + "/" + AUTHENTICATION + "/*")
//                                                                 .withComment(CSA_COMMENT);
//            Rule__1 csaSecAdminIetfSystemStateExtRule = new Rule__1().withName("ericsson-system-ext-1-csa-security-admin")
//                                                                     .withModuleName(ERICSSON_SYSTEM_EXT)
//                                                                     .withAccessOperations(READ)
//                                                                     .withAction(Action.PERMIT)
//                                                                     .withPath("/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME)
//                                                                     .withComment(CSA_COMMENT);
//            Rule__1 csaSecAdminIetfSystemStateExtAuthRule = new Rule__1().withName("ericsson-system-ext-2-csa-security-admin")
//                                                                         .withModuleName(ERICSSON_SYSTEM_EXT)
//                                                                         .withAccessOperations(READ)
//                                                                         .withAction(Action.PERMIT)
//                                                                         .withPath("/" + SYSTEM + "/" + AUTHENTICATION + "/*")
//                                                                         .withComment(CSA_COMMENT);
//
//            List<Rule__1> csaSecurityAdminRuleLists = new ArrayList<>();
//            csaSecurityAdminRuleLists.add(certmCsaSecAdminKeystoreRule);
//            csaSecurityAdminRuleLists.add(certmCsaSecAdminTrustorRule);
//            csaSecurityAdminRuleLists.add(certmCsaSecAdminKeystoreExtRule);
//            csaSecurityAdminRuleLists.add(certmCsaSecAdminTrustorExtRule);
//            csaSecurityAdminRuleLists.add(csaSecAdminIetfNetconfAcmRule);
//            csaSecurityAdminRuleLists.add(csaSecAdminIetfSystemStateRule);
//            csaSecurityAdminRuleLists.add(csaSecAdminIetfSystemAuthRule);
//            csaSecurityAdminRuleLists.add(csaSecAdminIetfSystemStateExtRule);
//            csaSecurityAdminRuleLists.add(csaSecAdminIetfSystemStateExtAuthRule);
//            csaSecAdmin.setRule(csaSecurityAdminRuleLists);
//            newRules.add(csaSecAdmin);
//
//            Rule csaReadOnly = new Rule().withName("ericsson-csa-manager-1-csa-read-only");
//            List<String> csaReadOnlyGroup = new ArrayList<>();
//            csaReadOnlyGroup.add(CSA_READ_ONLY);
//            csaReadOnly.setGroup(csaReadOnlyGroup);
//            Rule__1 csaReadOnlyRule = new Rule__1().withName("ericsson-csa-1-csa-read-only")
//                                                   .withModuleName(ERICSSON_CSA)
//                                                   .withAccessOperations(READ)
//                                                   .withAction(Action.PERMIT)
//                                                   .withComment(CSA_COMMENT);
//            Rule__1 certmCsaReadOnlyKeystoreRule = new Rule__1().withName("ietf-keystore-1-csa-read-only")
//                                                                .withModuleName(IETF_KEYSTORE)
//                                                                .withAccessOperations(READ)
//                                                                .withAction(Action.PERMIT)
//                                                                .withComment(CSA_COMMENT);
//            Rule__1 certmCsaReadOnlyTrustorRule = new Rule__1().withName("ietf-truststore-1-csa-read-only")
//                                                               .withModuleName(IETF_TRUSTSTORE)
//                                                               .withAccessOperations(READ)
//                                                               .withAction(Action.PERMIT)
//                                                               .withComment(CSA_COMMENT);
//            Rule__1 certmCsaReadOnlyKeystoreExtRule = new Rule__1().withName("ericsson-keystore-ext-1-csa-read-only")
//                                                                   .withModuleName(ERICSSON_KEYSTORE_EXT)
//                                                                   .withAccessOperations(READ)
//                                                                   .withAction(Action.PERMIT)
//                                                                   .withComment(CSA_COMMENT);
//            Rule__1 certmCsaReadOnlyTrustorExtRule = new Rule__1().withName("ericsson-truststore-ext-1-csa-read-only")
//                                                                  .withModuleName(ERICSSON_TRUSTSTORE_EXT)
//                                                                  .withAccessOperations(READ)
//                                                                  .withAction(Action.PERMIT)
//                                                                  .withComment(CSA_COMMENT);
//            List<Rule__1> csaReadOnlyRuleLists = new ArrayList<>();
//            csaReadOnlyRuleLists.add(csaReadOnlyRule);
//            csaReadOnlyRuleLists.add(certmCsaReadOnlyKeystoreRule);
//            csaReadOnlyRuleLists.add(certmCsaReadOnlyTrustorRule);
//            csaReadOnlyRuleLists.add(certmCsaReadOnlyKeystoreExtRule);
//            csaReadOnlyRuleLists.add(certmCsaReadOnlyTrustorExtRule);
//            csaReadOnly.setRule(csaReadOnlyRuleLists);
//            newRules.add(csaReadOnly);

            Rule scpAdmin = new Rule().withName("ericsson-scp-manager-1-scp-admin");
            List<String> scpAdminGroup = new ArrayList<>();
            scpAdminGroup.add(SCP_ADMIN);
            scpAdmin.setGroup(scpAdminGroup);
            Rule__1 scpAdminRule = new Rule__1().withName("ericsson-scp-1-scp-admin")
                                                .withModuleName(ERICSSON_SCP)
                                                .withAccessOperations(READ_WRITE)
                                                .withAction(Action.PERMIT)
                                                .withComment(SCP_COMMENT);
            Rule__1 certmScpAdminKeystoreRule = new Rule__1().withName("ericsson-sec-certm-1-scp-admin")
                                                             .withModuleName(IETF_KEYSTORE)
                                                             .withAccessOperations(READ)
                                                             .withAction(Action.PERMIT)
                                                             .withComment(SCP_COMMENT);
            Rule__1 certmScpAdminTrustorRule = new Rule__1().withName("ericsson-sec-certm-2-scp-admin")
                                                            .withModuleName(IETF_TRUSTSTORE)
                                                            .withAccessOperations(READ)
                                                            .withAction(Action.PERMIT)
                                                            .withComment(SCP_COMMENT);
            Rule__1 certmScpAdminKeystoreExtRule = new Rule__1().withName("ericsson-sec-certm-3-scp-admin")
                                                                .withModuleName(ERICSSON_KEYSTORE_EXT)
                                                                .withAccessOperations(READ)
                                                                .withAction(Action.PERMIT)
                                                                .withComment(SCP_COMMENT);
            Rule__1 certmScpAdminTrustorExtRule = new Rule__1().withName("ericsson-sec-certm-4-scp-admin")
                                                               .withModuleName(ERICSSON_TRUSTSTORE_EXT)
                                                               .withAccessOperations(READ)
                                                               .withAction(Action.PERMIT)
                                                               .withComment(SCP_COMMENT);
            Rule__1 brmScpAdminRule = new Rule__1().withName("ericsson-brm-1-scp-admin")
                                                   .withModuleName(ERICSSON_BRM)
                                                   .withAccessOperations(READ)
                                                   .withAction(Action.PERMIT)
                                                   .withComment(SCP_COMMENT);
            List<Rule__1> scpAdminRuleLists = new ArrayList<>();
            scpAdminRuleLists.add(scpAdminRule);
            scpAdminRuleLists.add(certmScpAdminKeystoreRule);
            scpAdminRuleLists.add(certmScpAdminTrustorRule);
            scpAdminRuleLists.add(certmScpAdminKeystoreExtRule);
            scpAdminRuleLists.add(certmScpAdminTrustorExtRule);
            scpAdminRuleLists.add(brmScpAdminRule);
            scpAdmin.setRule(scpAdminRuleLists);
            newRules.add(scpAdmin);

            Rule scpSecAdmin = new Rule().withName("ericsson-scp-manager-2-scp-security-admin");
            List<String> scpSecurityAdminGroup = new ArrayList<>();
            scpSecurityAdminGroup.add(SCP_SECURITY_ADMIN);
            scpSecAdmin.setGroup(scpSecurityAdminGroup);
            Rule__1 certmScpSecAdminKeystoreRule = new Rule__1().withName("ietf-keystore-1-scp-security-admin")
                                                                .withModuleName(IETF_KEYSTORE)
                                                                .withAccessOperations(READ)
                                                                .withAction(Action.PERMIT)
                                                                .withComment(SCP_COMMENT);
            Rule__1 certmScpSecAdminTrustorRule = new Rule__1().withName("ietf-truststore-1-scp-security-admin")
                                                               .withModuleName(IETF_TRUSTSTORE)
                                                               .withAccessOperations(READ)
                                                               .withAction(Action.PERMIT)
                                                               .withComment(SCP_COMMENT);
            Rule__1 certmScpSecAdminKeystoreExtRule = new Rule__1().withName("ericsson-keystore-ext-1-scp-security-admin")
                                                                   .withModuleName(ERICSSON_KEYSTORE_EXT)
                                                                   .withAccessOperations(READ)
                                                                   .withAction(Action.PERMIT)
                                                                   .withComment(SCP_COMMENT);
            Rule__1 certmScpSecAdminTrustorExtRule = new Rule__1().withName("ericsson-truststore-ext-1-scp-security-admin")
                                                                  .withModuleName(ERICSSON_TRUSTSTORE_EXT)
                                                                  .withAccessOperations(READ)
                                                                  .withAction(Action.PERMIT)
                                                                  .withComment(SCP_COMMENT);
            Rule__1 scpSecAdminIetfNetconfAcmRule = new Rule__1().withName("ietf-netconf-acm-1-scp-security-admin")
                                                                 .withModuleName(IETF_NETCONF_ACM)
                                                                 .withAccessOperations(READ)
                                                                 .withAction(Action.PERMIT)
                                                                 .withComment(SCP_COMMENT);
            Rule__1 scpSecAdminIetfSystemStateRule = new Rule__1().withName("ietf-system-1-scp-security-admin")
                                                                  .withModuleName(IETF_SYSTEM)
                                                                  .withAccessOperations(READ)
                                                                  .withAction(Action.PERMIT)
                                                                  .withPath("/" + SYSTEM_STATE + "/")
                                                                  .withComment(SCP_COMMENT);
            Rule__1 scpSecAdminIetfSystemAuthRule = new Rule__1().withName("ietf-system-2-scp-security-admin")
                                                                 .withModuleName(IETF_SYSTEM)
                                                                 .withAccessOperations(READ)
                                                                 .withAction(Action.PERMIT)
                                                                 .withPath("/" + SYSTEM + "/" + AUTHENTICATION + "/*")
                                                                 .withComment(SCP_COMMENT);
            Rule__1 scpSecAdminIetfSystemStateExtRule = new Rule__1().withName("ericsson-system-ext-1-scp-security-admin")
                                                                     .withModuleName(ERICSSON_SYSTEM_EXT)
                                                                     .withAccessOperations(READ)
                                                                     .withAction(Action.PERMIT)
                                                                     .withPath("/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME)
                                                                     .withComment(SCP_COMMENT);
            Rule__1 scpSecAdminIetfSystemStateExtAuthRule = new Rule__1().withName("ericsson-system-ext-2-scp-security-admin")
                                                                         .withModuleName(ERICSSON_SYSTEM_EXT)
                                                                         .withAccessOperations(READ)
                                                                         .withAction(Action.PERMIT)
                                                                         .withPath("/" + SYSTEM + "/" + AUTHENTICATION + "/*")
                                                                         .withComment(SCP_COMMENT);

            List<Rule__1> scpSecurityAdminRuleLists = new ArrayList<>();
            scpSecurityAdminRuleLists.add(certmScpSecAdminKeystoreRule);
            scpSecurityAdminRuleLists.add(certmScpSecAdminTrustorRule);
            scpSecurityAdminRuleLists.add(certmScpSecAdminKeystoreExtRule);
            scpSecurityAdminRuleLists.add(certmScpSecAdminTrustorExtRule);
            scpSecurityAdminRuleLists.add(scpSecAdminIetfNetconfAcmRule);
            scpSecurityAdminRuleLists.add(scpSecAdminIetfSystemStateRule);
            scpSecurityAdminRuleLists.add(scpSecAdminIetfSystemAuthRule);
            scpSecurityAdminRuleLists.add(scpSecAdminIetfSystemStateExtRule);
            scpSecurityAdminRuleLists.add(scpSecAdminIetfSystemStateExtAuthRule);
            scpSecAdmin.setRule(scpSecurityAdminRuleLists);
            newRules.add(scpSecAdmin);

            Rule scpReadOnly = new Rule().withName("ericsson-scp-manager-3-scp-read-only");
            List<String> scpReadOnlyGroup = new ArrayList<>();
            scpReadOnlyGroup.add(SCP_READ_ONLY);
            scpReadOnly.setGroup(scpReadOnlyGroup);
            Rule__1 scpReadOnlyRule = new Rule__1().withName("ericsson-scp-1-scp-read-only")
                                                   .withModuleName(ERICSSON_SCP)
                                                   .withAccessOperations(READ)
                                                   .withAction(Action.PERMIT)
                                                   .withComment(SCP_COMMENT);
            Rule__1 certmScpReadOnlyKeystoreRule = new Rule__1().withName("ietf-keystore-1-scp-read-only")
                                                                .withModuleName(IETF_KEYSTORE)
                                                                .withAccessOperations(READ)
                                                                .withAction(Action.PERMIT)
                                                                .withComment(SCP_COMMENT);
            Rule__1 certmScpReadOnlyTrustorRule = new Rule__1().withName("ietf-truststore-1-scp-read-only")
                                                               .withModuleName(IETF_TRUSTSTORE)
                                                               .withAccessOperations(READ)
                                                               .withAction(Action.PERMIT)
                                                               .withComment(SCP_COMMENT);
            Rule__1 certmScpReadOnlyKeystoreExtRule = new Rule__1().withName("ericsson-keystore-ext-1-scp-read-only")
                                                                   .withModuleName(ERICSSON_KEYSTORE_EXT)
                                                                   .withAccessOperations(READ)
                                                                   .withAction(Action.PERMIT)
                                                                   .withComment(SCP_COMMENT);
            Rule__1 certmScpReadOnlyTrustorExtRule = new Rule__1().withName("ericsson-truststore-ext-1-scp-read-only")
                                                                  .withModuleName(ERICSSON_TRUSTSTORE_EXT)
                                                                  .withAccessOperations(READ)
                                                                  .withAction(Action.PERMIT)
                                                                  .withComment(SCP_COMMENT);
            List<Rule__1> scpReadOnlyRuleLists = new ArrayList<>();
            scpReadOnlyRuleLists.add(scpReadOnlyRule);
            scpReadOnlyRuleLists.add(certmScpReadOnlyKeystoreRule);
            scpReadOnlyRuleLists.add(certmScpReadOnlyTrustorRule);
            scpReadOnlyRuleLists.add(certmScpReadOnlyKeystoreExtRule);
            scpReadOnlyRuleLists.add(certmScpReadOnlyTrustorExtRule);
            scpReadOnly.setRule(scpReadOnlyRuleLists);
            newRules.add(scpReadOnly);

            newNacmConfig.setRuleList(newRules);
            newConfig.setIetfNetconfAcmNacm(newNacmConfig);
            return newConfig;
        }
        catch (Exception e)
        {
            log.error("Error during the ACM config update with default SC user groups\n{}", e);
            return config;
        }
    }

//    public static JsonObject generateCsaAdminAcmDefaultConfig()
//    {
//        // create values for csa-admin
//        var csaAdminValues = new JsonObject();
//        // create list of groups for csa-admin
//        var csaAdminGroups = new JsonArray();
//        // add supported groups
//        csaAdminGroups.add(CSA_ADMIN);
//        csaAdminValues.put(TAG_GROUP, csaAdminGroups);
//        // add rule-list name
//        csaAdminValues.put(TAG_NAME, "ericsson-csa-manager-1-csa-admin");
//        // Create Rules
//        var csaAdminRules = new JsonArray();
//        // add rule parameters for module ericsson-csa
//        var csaAdminEricCsa = new JsonObject();
//        csaAdminEricCsa.put(TAG_MODULE, ERICSSON_CSA);
//        csaAdminEricCsa.put(TAG_ACTION, PERMIT);
//        csaAdminEricCsa.put(TAG_NAME, "ericsson-csa-1-csa-admin");
//        csaAdminEricCsa.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminEricCsa.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaAdminEricCsa.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminEricCsa);
//
//        // add rule parameters for module ietf-keystore
//        var csaAdminKeyStore = new JsonObject();
//        csaAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
//        csaAdminKeyStore.put(TAG_ACTION, PERMIT);
//        csaAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-csa-admin");
//        csaAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminKeyStore.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminKeyStore);
//
//        // add rule parameters for module ietf-truststore
//        var csaAdminTruststore = new JsonObject();
//        csaAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
//        csaAdminTruststore.put(TAG_ACTION, PERMIT);
//        csaAdminTruststore.put(TAG_NAME, "ietf-truststore-1-csa-admin");
//        csaAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminTruststore.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminTruststore);
//
//        // add rule parameters for module ericsson-keystore-ext
//        var csaAdminKeyStoreExt = new JsonObject();
//        csaAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
//        csaAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
//        csaAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-csa-admin");
//        csaAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminKeyStoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminKeyStoreExt);
//
//        // add rule parameters for module ericsson-truststore-ext
//        var csaAdminTruststoreExt = new JsonObject();
//        csaAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
//        csaAdminTruststoreExt.put(TAG_ACTION, PERMIT);
//        csaAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-csa-admin");
//        csaAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminTruststoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminTruststoreExt);
//
//        // add rule parameters for module ericsson-brm
//        JsonObject csaAdminBrm = new JsonObject();
//        csaAdminBrm.put(TAG_MODULE, ERICSSON_BRM);
//        csaAdminBrm.put(TAG_ACTION, PERMIT);
//        csaAdminBrm.put(TAG_NAME, "ericsson-brm-1-csa-admin");
//        csaAdminBrm.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminBrm.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminBrm.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminBrm);
//
//        // add rule parameters for system modules
//        var csaAdminDefault1 = new JsonObject();
//        csaAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
//        csaAdminDefault1.put(TAG_ACTION, PERMIT);
//        csaAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-csa-admin");
//        csaAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaAdminDefault1.put(TAG_COMMENT, CSA_COMMENT);
//        csaAdminRules.add(csaAdminDefault1);
//
//        // add rules
//        csaAdminValues.put(TAG_RULE, csaAdminRules);
//
//        // Create cmd rules
//        var csaAdminCmdRules = new JsonArray();
//
//        // add rule parameters for module ericsson-csa
//        var csaAdminMetrics = new JsonObject();
//        csaAdminMetrics.put(TAG_NAME, "cli-metrics-query-cmd");
//        csaAdminMetrics.put(TAG_ACTION, PERMIT);
//        csaAdminMetrics.put(TAG_COMMAND, CMD_METRICS_QUERY);
//        csaAdminMetrics.put(TAG_CONTEXT, CLI);
//        csaAdminMetrics.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaAdminCmdRules.add(csaAdminMetrics);
//
//        // add rule parameters for module ericsson-csa
//        var csaAdminShowAlarm = new JsonObject();
//        csaAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
//        csaAdminShowAlarm.put(TAG_ACTION, PERMIT);
//        csaAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
//        csaAdminShowAlarm.put(TAG_CONTEXT, CLI);
//        csaAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaAdminCmdRules.add(csaAdminShowAlarm);
//
//        // add rule parameters for module ericsson-csa
//        var csaAdminShowAlarmHistory = new JsonObject();
//        csaAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
//        csaAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
//        csaAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
//        csaAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
//        csaAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaAdminCmdRules.add(csaAdminShowAlarmHistory);
//
//        // add cmd rules
//        csaAdminValues.put(TAG_TAILF_ACM_CMDRULE, csaAdminCmdRules);
//
//        return csaAdminValues;
//    }
//
//    public static JsonObject generateCsaSecAdminAcmDefaultConfig()
//    {
//        // create values for csa-security-admin
//        var csaSecAdminValues = new JsonObject();
//        // create list of groups for csa-security-admin
//        var csaSecAdminGroups = new JsonArray();
//        // add supported groups
//        csaSecAdminGroups.add(CSA_SECURITY_ADMIN);
//        csaSecAdminValues.put(TAG_GROUP, csaSecAdminGroups);
//        // add rule-list name
//        csaSecAdminValues.put(TAG_NAME, "ericsson-csa-manager-1-csa-security-admin");
//        // Create Rules
//        var csaSecAdminRules = new JsonArray();
//
//        // add rule parameters for module ietf-system
//        var csaSecAdminSystem = new JsonObject();
//        csaSecAdminSystem.put(TAG_MODULE, IETF_SYSTEM);
//        csaSecAdminSystem.put(TAG_PATH, "/" + SYSTEM_STATE + "/");
//        csaSecAdminSystem.put(TAG_ACTION, PERMIT);
//        csaSecAdminSystem.put(TAG_NAME, "ietf-system-1-csa-security-admin");
//        csaSecAdminSystem.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminSystem.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminSystem.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminSystem);
//
//        var csaSecAdminSystem2 = new JsonObject();
//        csaSecAdminSystem2.put(TAG_MODULE, IETF_SYSTEM);
//        csaSecAdminSystem2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
//        csaSecAdminSystem2.put(TAG_ACTION, PERMIT);
//        csaSecAdminSystem2.put(TAG_NAME, "ietf-system-2-csa-security-admin");
//        csaSecAdminSystem2.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminSystem2.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminSystem2.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminSystem2);
//
//        // add rule parameters for module ericsson-system-ext
//        var csaSecAdminSystemExt = new JsonObject();
//        csaSecAdminSystemExt.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
//        csaSecAdminSystemExt.put(TAG_PATH, "/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME);
//        csaSecAdminSystemExt.put(TAG_ACTION, PERMIT);
//        csaSecAdminSystemExt.put(TAG_NAME, "ericsson-system-ext-1-csa-security-admin");
//        csaSecAdminSystemExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminSystemExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminSystemExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminSystemExt);
//
//        var csaSecAdminSystemExt2 = new JsonObject();
//        csaSecAdminSystemExt2.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
//        csaSecAdminSystemExt2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
//        csaSecAdminSystemExt2.put(TAG_ACTION, PERMIT);
//        csaSecAdminSystemExt2.put(TAG_NAME, "ericsson-system-ext-2-csa-security-admin");
//        csaSecAdminSystemExt2.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminSystemExt2.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminSystemExt2.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminSystemExt2);
//
//        // add rule parameters for module ietf-keystore
//        var csaSecAdminKeyStore = new JsonObject();
//        csaSecAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
//        csaSecAdminKeyStore.put(TAG_ACTION, PERMIT);
//        csaSecAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-csa-security-admin");
//        csaSecAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminKeyStore.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminKeyStore);
//
//        // add rule parameters for module ietf-truststore
//        var csaSecAdminTruststore = new JsonObject();
//        csaSecAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
//        csaSecAdminTruststore.put(TAG_ACTION, PERMIT);
//        csaSecAdminTruststore.put(TAG_NAME, "ietf-truststore-1-csa-security-admin");
//        csaSecAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminTruststore.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminTruststore);
//
//        // add rule parameters for module ericsson-keystore-ext
//        var csaSecAdminKeyStoreExt = new JsonObject();
//        csaSecAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
//        csaSecAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
//        csaSecAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-csa-security-admin");
//        csaSecAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminKeyStoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminKeyStoreExt);
//
//        // add rule parameters for module ericsson-truststore-ext
//        var csaSecAdminTruststoreExt = new JsonObject();
//        csaSecAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
//        csaSecAdminTruststoreExt.put(TAG_ACTION, PERMIT);
//        csaSecAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-csa-security-admin");
//        csaSecAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminTruststoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminTruststoreExt);
//
//        // add rule parameters for module ietf-netconf-acm
//        var csaSecAdminNetconfAcmExt = new JsonObject();
//        csaSecAdminNetconfAcmExt.put(TAG_MODULE, IETF_NETCONF_ACM);
//        csaSecAdminNetconfAcmExt.put(TAG_ACTION, PERMIT);
//        csaSecAdminNetconfAcmExt.put(TAG_NAME, "ietf-netconf-acm-1-csa-security-admin");
//        csaSecAdminNetconfAcmExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminNetconfAcmExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminNetconfAcmExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminNetconfAcmExt);
//
//        // add rule parameters for system modules
//        var csaSecAdminDefault1 = new JsonObject();
//        csaSecAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
//        csaSecAdminDefault1.put(TAG_ACTION, PERMIT);
//        csaSecAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-csa-security-admin");
//        csaSecAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaSecAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaSecAdminDefault1.put(TAG_COMMENT, CSA_COMMENT);
//        csaSecAdminRules.add(csaSecAdminDefault1);
//
//        // add rules
//        csaSecAdminValues.put(TAG_RULE, csaSecAdminRules);
//
//        // Create cmd rules
//        var csaSecAdminCmdRules = new JsonArray();
//
//        // add rule parameters for module ericsson-csa
//        var csaSecAdminShowAlarm = new JsonObject();
//        csaSecAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
//        csaSecAdminShowAlarm.put(TAG_ACTION, PERMIT);
//        csaSecAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
//        csaSecAdminShowAlarm.put(TAG_CONTEXT, CLI);
//        csaSecAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaSecAdminCmdRules.add(csaSecAdminShowAlarm);
//
//        // add rule parameters for module ericsson-csa
//        var csaSecAdminShowAlarmHistory = new JsonObject();
//        csaSecAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
//        csaSecAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
//        csaSecAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
//        csaSecAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
//        csaSecAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaSecAdminCmdRules.add(csaSecAdminShowAlarmHistory);
//
//        // add cmd rules
//        csaSecAdminValues.put(TAG_TAILF_ACM_CMDRULE, csaSecAdminCmdRules);
//
//        return csaSecAdminValues;
//    }
//
//    public static JsonObject generateCsaReadOnlyAcmDefaultConfig()
//    {
//        // create values for csa-read-only
//        var csaReadOnlyValues = new JsonObject();
//        // create list of groups for csa-read-only
//        var csaReadOnlyGroups = new JsonArray();
//        // add supported groups
//        csaReadOnlyGroups.add(CSA_READ_ONLY);
//        csaReadOnlyValues.put(TAG_GROUP, csaReadOnlyGroups);
//        // add rule-list name
//        csaReadOnlyValues.put(TAG_NAME, "ericsson-csa-manager-1-csa-read-only");
//        // Create Rules
//        var csaReadOnlyRules = new JsonArray();
//        // add rule parameters for module ericsson-csa
//        var csaReadOnlyEricCsa = new JsonObject();
//        csaReadOnlyEricCsa.put(TAG_MODULE, ERICSSON_CSA);
//        csaReadOnlyEricCsa.put(TAG_ACTION, PERMIT);
//        csaReadOnlyEricCsa.put(TAG_NAME, "ericsson-csa-1-csa-read-only");
//        csaReadOnlyEricCsa.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyEricCsa.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyEricCsa.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyEricCsa);
//
//        // add rule parameters for module ietf-keystore
//        var csaReadOnlyKeyStore = new JsonObject();
//        csaReadOnlyKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
//        csaReadOnlyKeyStore.put(TAG_ACTION, PERMIT);
//        csaReadOnlyKeyStore.put(TAG_NAME, "ietf-keystore-1-csa-read-only");
//        csaReadOnlyKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyKeyStore.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyKeyStore);
//
//        // add rule parameters for module ietf-truststore
//        var csaReadOnlyTruststore = new JsonObject();
//        csaReadOnlyTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
//        csaReadOnlyTruststore.put(TAG_ACTION, PERMIT);
//        csaReadOnlyTruststore.put(TAG_NAME, "ietf-truststore-1-csa-read-only");
//        csaReadOnlyTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyTruststore.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyTruststore);
//
//        // add rule parameters for module ietf-keystore-ext
//        var csaReadOnlyKeyStoreExt = new JsonObject();
//        csaReadOnlyKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
//        csaReadOnlyKeyStoreExt.put(TAG_ACTION, PERMIT);
//        csaReadOnlyKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-csa-read-only");
//        csaReadOnlyKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyKeyStoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyKeyStoreExt);
//
//        // add rule parameters for module ietf-truststore-ext
//        var csaReadOnlyTruststoreExt = new JsonObject();
//        csaReadOnlyTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
//        csaReadOnlyTruststoreExt.put(TAG_ACTION, PERMIT);
//        csaReadOnlyTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-csa-read-only");
//        csaReadOnlyTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyTruststoreExt.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyTruststoreExt);
//
//        // add rule parameters for system modules
//        var csaReadOnlyDefault1 = new JsonObject();
//        csaReadOnlyDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
//        csaReadOnlyDefault1.put(TAG_ACTION, PERMIT);
//        csaReadOnlyDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-csa-read-only");
//        csaReadOnlyDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
//        csaReadOnlyDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
//        csaReadOnlyDefault1.put(TAG_COMMENT, CSA_COMMENT);
//        csaReadOnlyRules.add(csaReadOnlyDefault1);
//
//        // add rules
//        csaReadOnlyValues.put(TAG_RULE, csaReadOnlyRules);
//
//        // Create cmd rules
//        var csaReadOnlyCmdRules = new JsonArray();
//
//        // add rule parameters for module ericsson-csa
//        var csaReadOnlyShowAlarm = new JsonObject();
//        csaReadOnlyShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
//        csaReadOnlyShowAlarm.put(TAG_ACTION, PERMIT);
//        csaReadOnlyShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
//        csaReadOnlyShowAlarm.put(TAG_CONTEXT, CLI);
//        csaReadOnlyShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaReadOnlyCmdRules.add(csaReadOnlyShowAlarm);
//
//        // add rule parameters for module ericsson-csa
//        var csaReadOnlyShowAlarmHistory = new JsonObject();
//        csaReadOnlyShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
//        csaReadOnlyShowAlarmHistory.put(TAG_ACTION, PERMIT);
//        csaReadOnlyShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
//        csaReadOnlyShowAlarmHistory.put(TAG_CONTEXT, CLI);
//        csaReadOnlyShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
//        csaReadOnlyCmdRules.add(csaReadOnlyShowAlarmHistory);
//
//        // add cmd rules
//        csaReadOnlyValues.put(TAG_TAILF_ACM_CMDRULE, csaReadOnlyCmdRules);
//
//        return csaReadOnlyValues;
//    }

    public static JsonObject generateScpAdminAcmDefaultConfig()
    {
        // create values for scp-admin
        var scpAdminValues = new JsonObject();
        // create list of groups for scp-admin
        var scpAdminGroups = new JsonArray();
        // add supported groups
        scpAdminGroups.add(SCP_ADMIN);
        scpAdminValues.put(TAG_GROUP, scpAdminGroups);
        // add rule-list name
        scpAdminValues.put(TAG_NAME, "ericsson-scp-manager-1-scp-admin");
        // Create Rules
        var scpAdminRules = new JsonArray();
        // add rule parameters for module ericsson-scp
        var scpAdminEricScp = new JsonObject();
        scpAdminEricScp.put(TAG_MODULE, ERICSSON_SCP);
        scpAdminEricScp.put(TAG_ACTION, PERMIT);
        scpAdminEricScp.put(TAG_NAME, "ericsson-scp-1-scp-admin");
        scpAdminEricScp.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminEricScp.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpAdminEricScp.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminEricScp);

        // add rule parameters for module ietf-keystore
        var scpAdminKeyStore = new JsonObject();
        scpAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        scpAdminKeyStore.put(TAG_ACTION, PERMIT);
        scpAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-scp-admin");
        scpAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminKeyStore.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var scpAdminTruststore = new JsonObject();
        scpAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        scpAdminTruststore.put(TAG_ACTION, PERMIT);
        scpAdminTruststore.put(TAG_NAME, "ietf-truststore-1-scp-admin");
        scpAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminTruststore.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var scpAdminKeyStoreExt = new JsonObject();
        scpAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        scpAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        scpAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-scp-admin");
        scpAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminKeyStoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var scpAdminTruststoreExt = new JsonObject();
        scpAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        scpAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        scpAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-scp-admin");
        scpAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminTruststoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminTruststoreExt);

        // add rule parameters for module ericsson-brm
        var scpAdminBrm = new JsonObject();
        scpAdminBrm.put(TAG_MODULE, ERICSSON_BRM);
        scpAdminBrm.put(TAG_ACTION, PERMIT);
        scpAdminBrm.put(TAG_NAME, "ericsson-brm-1-scp-admin");
        scpAdminBrm.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminBrm.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminBrm.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminBrm);

        // add rule parameters for system modules
        var scpAdminDefault1 = new JsonObject();
        scpAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        scpAdminDefault1.put(TAG_ACTION, PERMIT);
        scpAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-scp-admin");
        scpAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        scpAdminDefault1.put(TAG_COMMENT, SCP_COMMENT);
        scpAdminRules.add(scpAdminDefault1);

        // add rules
        scpAdminValues.put(TAG_RULE, scpAdminRules);

        // Create cmd rules
        var scpAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-scp
        var scpAdminMetrics = new JsonObject();
        scpAdminMetrics.put(TAG_NAME, "cli-metrics-query-cmd");
        scpAdminMetrics.put(TAG_ACTION, PERMIT);
        scpAdminMetrics.put(TAG_COMMAND, CMD_METRICS_QUERY);
        scpAdminMetrics.put(TAG_CONTEXT, CLI);
        scpAdminMetrics.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpAdminCmdRules.add(scpAdminMetrics);

        // add rule parameters for module ericsson-scp
        var scpAdminShowAlarm = new JsonObject();
        scpAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        scpAdminShowAlarm.put(TAG_ACTION, PERMIT);
        scpAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        scpAdminShowAlarm.put(TAG_CONTEXT, CLI);
        scpAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpAdminCmdRules.add(scpAdminShowAlarm);

        // add rule parameters for module ericsson-scp
        var scpAdminShowAlarmHistory = new JsonObject();
        scpAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        scpAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        scpAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        scpAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        scpAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpAdminCmdRules.add(scpAdminShowAlarmHistory);

        // add cmd rules
        scpAdminValues.put(TAG_TAILF_ACM_CMDRULE, scpAdminCmdRules);

        return scpAdminValues;
    }

    public static JsonObject generateScpSecAdminAcmDefaultConfig()
    {
        // create values for scp-security-admin
        var scpSecAdminValues = new JsonObject();
        // create list of groups for scp-security-admin
        var scpSecAdminGroups = new JsonArray();
        // add supported groups
        scpSecAdminGroups.add(SCP_SECURITY_ADMIN);
        scpSecAdminValues.put(TAG_GROUP, scpSecAdminGroups);
        // add rule-list name
        scpSecAdminValues.put(TAG_NAME, "ericsson-scp-manager-2-scp-security-admin");
        // Create Rules
        var scpSecAdminRules = new JsonArray();

        // add rule parameters for module ietf-system
        var scpSecAdminSystem = new JsonObject();
        scpSecAdminSystem.put(TAG_MODULE, IETF_SYSTEM);
        scpSecAdminSystem.put(TAG_PATH, "/" + SYSTEM_STATE + "/");
        scpSecAdminSystem.put(TAG_ACTION, PERMIT);
        scpSecAdminSystem.put(TAG_NAME, "ietf-system-1-scp-security-admin");
        scpSecAdminSystem.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminSystem.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminSystem.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminSystem);

        var scpSecAdminSystem2 = new JsonObject();
        scpSecAdminSystem2.put(TAG_MODULE, IETF_SYSTEM);
        scpSecAdminSystem2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        scpSecAdminSystem2.put(TAG_ACTION, PERMIT);
        scpSecAdminSystem2.put(TAG_NAME, "ietf-system-2-scp-security-admin");
        scpSecAdminSystem2.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminSystem2.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminSystem2.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminSystem2);

        // add rule parameters for module ericsson-system-ext
        var scpSecAdminSystemExt = new JsonObject();
        scpSecAdminSystemExt.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        scpSecAdminSystemExt.put(TAG_PATH, "/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME);
        scpSecAdminSystemExt.put(TAG_ACTION, PERMIT);
        scpSecAdminSystemExt.put(TAG_NAME, "ericsson-system-ext-1-scp-security-admin");
        scpSecAdminSystemExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminSystemExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminSystemExt.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminSystemExt);

        var scpSecAdminSystemExt2 = new JsonObject();
        scpSecAdminSystemExt2.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        scpSecAdminSystemExt2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        scpSecAdminSystemExt2.put(TAG_ACTION, PERMIT);
        scpSecAdminSystemExt2.put(TAG_NAME, "ericsson-system-ext-2-scp-security-admin");
        scpSecAdminSystemExt2.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminSystemExt2.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminSystemExt2.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminSystemExt2);

        // add rule parameters for module ietf-keystore
        var scpSecAdminKeyStore = new JsonObject();
        scpSecAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        scpSecAdminKeyStore.put(TAG_ACTION, PERMIT);
        scpSecAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-scp-security-admin");
        scpSecAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminKeyStore.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var scpSecAdminTruststore = new JsonObject();
        scpSecAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        scpSecAdminTruststore.put(TAG_ACTION, PERMIT);
        scpSecAdminTruststore.put(TAG_NAME, "ietf-truststore-1-scp-security-admin");
        scpSecAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminTruststore.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var scpSecAdminKeyStoreExt = new JsonObject();
        scpSecAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        scpSecAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        scpSecAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-scp-security-admin");
        scpSecAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminKeyStoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var scpSecAdminTruststoreExt = new JsonObject();
        scpSecAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        scpSecAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        scpSecAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-scp-security-admin");
        scpSecAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminTruststoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminTruststoreExt);

        // add rule parameters for module ietf-truststore-ext
        var scpSecAdminNetconfAcmExt = new JsonObject();
        scpSecAdminNetconfAcmExt.put(TAG_MODULE, IETF_NETCONF_ACM);
        scpSecAdminNetconfAcmExt.put(TAG_ACTION, PERMIT);
        scpSecAdminNetconfAcmExt.put(TAG_NAME, "ietf-netconf-acm-1-scp-security-admin");
        scpSecAdminNetconfAcmExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminNetconfAcmExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminNetconfAcmExt.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminNetconfAcmExt);

        // add rule parameters for system modules
        var scpSecAdminDefault1 = new JsonObject();
        scpSecAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        scpSecAdminDefault1.put(TAG_ACTION, PERMIT);
        scpSecAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-scp-security-admin");
        scpSecAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpSecAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        scpSecAdminDefault1.put(TAG_COMMENT, SCP_COMMENT);
        scpSecAdminRules.add(scpSecAdminDefault1);

        // add rules
        scpSecAdminValues.put(TAG_RULE, scpSecAdminRules);

        // Create cmd rules
        var scpSecAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-scp
        var scpSecAdminShowAlarm = new JsonObject();
        scpSecAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        scpSecAdminShowAlarm.put(TAG_ACTION, PERMIT);
        scpSecAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        scpSecAdminShowAlarm.put(TAG_CONTEXT, CLI);
        scpSecAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpSecAdminCmdRules.add(scpSecAdminShowAlarm);

        // add rule parameters for module ericsson-scp
        var scpSecAdminShowAlarmHistory = new JsonObject();
        scpSecAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        scpSecAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        scpSecAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        scpSecAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        scpSecAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpSecAdminCmdRules.add(scpSecAdminShowAlarmHistory);

        // add cmd rules
        scpSecAdminValues.put(TAG_TAILF_ACM_CMDRULE, scpSecAdminCmdRules);

        return scpSecAdminValues;
    }

    public static JsonObject generateScpReadOnlyAcmDefaultConfig()
    {
        // create values for scp-read-only
        var scpReadOnlyValues = new JsonObject();
        // create list of groups for scp-read-only
        var scpReadOnlyGroups = new JsonArray();
        // add supported groups
        scpReadOnlyGroups.add(SCP_READ_ONLY);
        scpReadOnlyValues.put(TAG_GROUP, scpReadOnlyGroups);
        // add rule-list name
        scpReadOnlyValues.put(TAG_NAME, "ericsson-scp-manager-3-scp-read-only");
        // Create Rules
        var scpReadOnlyRules = new JsonArray();
        // add rule parameters for module ericsson-scp
        var scpReadOnlyEricScp = new JsonObject();
        scpReadOnlyEricScp.put(TAG_MODULE, ERICSSON_SCP);
        scpReadOnlyEricScp.put(TAG_ACTION, PERMIT);
        scpReadOnlyEricScp.put(TAG_NAME, "ericsson-scp-1-scp-read-only");
        scpReadOnlyEricScp.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyEricScp.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyEricScp.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyEricScp);

        // add rule parameters for module ietf-keystore
        var scpReadOnlyKeyStore = new JsonObject();
        scpReadOnlyKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        scpReadOnlyKeyStore.put(TAG_ACTION, PERMIT);
        scpReadOnlyKeyStore.put(TAG_NAME, "ietf-keystore-1-scp-read-only");
        scpReadOnlyKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyKeyStore.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyKeyStore);

        // add rule parameters for module ietf-truststore
        var scpReadOnlyTruststore = new JsonObject();
        scpReadOnlyTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        scpReadOnlyTruststore.put(TAG_ACTION, PERMIT);
        scpReadOnlyTruststore.put(TAG_NAME, "ietf-truststore-1-scp-read-only");
        scpReadOnlyTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyTruststore.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyTruststore);

        // add rule parameters for module ietf-keystore-ext
        var scpReadOnlyKeyStoreExt = new JsonObject();
        scpReadOnlyKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        scpReadOnlyKeyStoreExt.put(TAG_ACTION, PERMIT);
        scpReadOnlyKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-scp-read-only");
        scpReadOnlyKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyKeyStoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyKeyStoreExt);

        // add rule parameters for module ietf-truststore-ext
        var scpReadOnlyTruststoreExt = new JsonObject();
        scpReadOnlyTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        scpReadOnlyTruststoreExt.put(TAG_ACTION, PERMIT);
        scpReadOnlyTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-scp-read-only");
        scpReadOnlyTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyTruststoreExt.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyTruststoreExt);

        // add rule parameters for system modules
        var scpReadOnlyDefault1 = new JsonObject();
        scpReadOnlyDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        scpReadOnlyDefault1.put(TAG_ACTION, PERMIT);
        scpReadOnlyDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-scp-read-only");
        scpReadOnlyDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        scpReadOnlyDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        scpReadOnlyDefault1.put(TAG_COMMENT, SCP_COMMENT);
        scpReadOnlyRules.add(scpReadOnlyDefault1);

        // add rules
        scpReadOnlyValues.put(TAG_RULE, scpReadOnlyRules);

        // Create cmd rules
        var scpReadOnlyCmdRules = new JsonArray();

        // add rule parameters for module ericsson-scp
        var scpReadOnlyShowAlarm = new JsonObject();
        scpReadOnlyShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        scpReadOnlyShowAlarm.put(TAG_ACTION, PERMIT);
        scpReadOnlyShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        scpReadOnlyShowAlarm.put(TAG_CONTEXT, CLI);
        scpReadOnlyShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpReadOnlyCmdRules.add(scpReadOnlyShowAlarm);

        // add rule parameters for module ericsson-scp
        var scpReadOnlyShowAlarmHistory = new JsonObject();
        scpReadOnlyShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        scpReadOnlyShowAlarmHistory.put(TAG_ACTION, PERMIT);
        scpReadOnlyShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        scpReadOnlyShowAlarmHistory.put(TAG_CONTEXT, CLI);
        scpReadOnlyShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        scpReadOnlyCmdRules.add(scpReadOnlyShowAlarmHistory);

        // add cmd rules
        scpReadOnlyValues.put(TAG_TAILF_ACM_CMDRULE, scpReadOnlyCmdRules);

        return scpReadOnlyValues;
    }

    public static JsonObject generateSeppAdminAcmDefaultConfig()
    {
        // create values for sepp-admin
        var seppAdminValues = new JsonObject();
        // create list of groups for sepp-admin
        var seppAdminGroups = new JsonArray();
        // add supported groups
        seppAdminGroups.add(SEPP_ADMIN);
        seppAdminValues.put(TAG_GROUP, seppAdminGroups);
        // add rule-list name
        seppAdminValues.put(TAG_NAME, "ericsson-sepp-manager-1-sepp-admin");
        // Create Rules
        var seppAdminRules = new JsonArray();
        // add rule parameters for module ericsson-sepp
        var seppAdminEricSepp = new JsonObject();
        seppAdminEricSepp.put(TAG_MODULE, ERICSSON_SEPP);
        seppAdminEricSepp.put(TAG_ACTION, PERMIT);
        seppAdminEricSepp.put(TAG_NAME, "ericsson-sepp-1-sepp-admin");
        seppAdminEricSepp.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminEricSepp.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppAdminEricSepp.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminEricSepp);

        // add rule parameters for module ietf-keystore
        var seppAdminKeyStore = new JsonObject();
        seppAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        seppAdminKeyStore.put(TAG_ACTION, PERMIT);
        seppAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-sepp-admin");
        seppAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminKeyStore.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var seppAdminTruststore = new JsonObject();
        seppAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        seppAdminTruststore.put(TAG_ACTION, PERMIT);
        seppAdminTruststore.put(TAG_NAME, "ietf-truststore-1-sepp-admin");
        seppAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminTruststore.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var seppAdminKeyStoreExt = new JsonObject();
        seppAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        seppAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        seppAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-sepp-admin");
        seppAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminKeyStoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var seppAdminTruststoreExt = new JsonObject();
        seppAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        seppAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        seppAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-sepp-admin");
        seppAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminTruststoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminTruststoreExt);

        // add rule parameters for module ericsson-brm
        var seppAdminBrm = new JsonObject();
        seppAdminBrm.put(TAG_MODULE, ERICSSON_BRM);
        seppAdminBrm.put(TAG_ACTION, PERMIT);
        seppAdminBrm.put(TAG_NAME, "ericsson-brm-1-sepp-admin");
        seppAdminBrm.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminBrm.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminBrm.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminBrm);

        // add rule parameters for system modules
        var seppAdminDefault1 = new JsonObject();
        seppAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        seppAdminDefault1.put(TAG_ACTION, PERMIT);
        seppAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-sepp-admin");
        seppAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        seppAdminDefault1.put(TAG_COMMENT, SEPP_COMMENT);
        seppAdminRules.add(seppAdminDefault1);

        // add rules
        seppAdminValues.put(TAG_RULE, seppAdminRules);

        // Create cmd rules
        var seppAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-sepp
        var seppAdminMetrics = new JsonObject();
        seppAdminMetrics.put(TAG_NAME, "cli-metrics-query-cmd");
        seppAdminMetrics.put(TAG_ACTION, PERMIT);
        seppAdminMetrics.put(TAG_COMMAND, CMD_METRICS_QUERY);
        seppAdminMetrics.put(TAG_CONTEXT, CLI);
        seppAdminMetrics.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppAdminCmdRules.add(seppAdminMetrics);

        // add rule parameters for module ericsson-sepp
        var seppAdminShowAlarm = new JsonObject();
        seppAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        seppAdminShowAlarm.put(TAG_ACTION, PERMIT);
        seppAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        seppAdminShowAlarm.put(TAG_CONTEXT, CLI);
        seppAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppAdminCmdRules.add(seppAdminShowAlarm);

        // add rule parameters for module ericsson-sepp
        var seppAdminShowAlarmHistory = new JsonObject();
        seppAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        seppAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        seppAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        seppAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        seppAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppAdminCmdRules.add(seppAdminShowAlarmHistory);

        // add cmd rules
        seppAdminValues.put(TAG_TAILF_ACM_CMDRULE, seppAdminCmdRules);

        return seppAdminValues;
    }

    public static JsonObject generateSeppSecAdminAcmDefaultConfig()
    {
        // create values for sepp-security-admin
        var seppSecAdminValues = new JsonObject();
        // create list of groups for sepp-security-admin
        var seppSecAdminGroups = new JsonArray();
        // add supported groups
        seppSecAdminGroups.add(SEPP_SECURITY_ADMIN);
        seppSecAdminValues.put(TAG_GROUP, seppSecAdminGroups);
        // add rule-list name
        seppSecAdminValues.put(TAG_NAME, "ericsson-sepp-manager-2-sepp-security-admin");
        // Create Rules
        var seppSecAdminRules = new JsonArray();

        // add rule parameters for module ietf-system
        var seppSecAdminSystem = new JsonObject();
        seppSecAdminSystem.put(TAG_MODULE, IETF_SYSTEM);
        seppSecAdminSystem.put(TAG_PATH, "/" + SYSTEM_STATE + "/");
        seppSecAdminSystem.put(TAG_ACTION, PERMIT);
        seppSecAdminSystem.put(TAG_NAME, "ietf-system-1-sepp-security-admin");
        seppSecAdminSystem.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminSystem.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminSystem.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminSystem);

        var seppSecAdminSystem2 = new JsonObject();
        seppSecAdminSystem2.put(TAG_MODULE, IETF_SYSTEM);
        seppSecAdminSystem2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        seppSecAdminSystem2.put(TAG_ACTION, PERMIT);
        seppSecAdminSystem2.put(TAG_NAME, "ietf-system-2-sepp-security-admin");
        seppSecAdminSystem2.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminSystem2.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminSystem2.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminSystem2);

        // add rule parameters for module ericsson-system-ext
        var seppSecAdminSystemExt = new JsonObject();
        seppSecAdminSystemExt.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        seppSecAdminSystemExt.put(TAG_PATH, "/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME);
        seppSecAdminSystemExt.put(TAG_ACTION, PERMIT);
        seppSecAdminSystemExt.put(TAG_NAME, "ericsson-system-ext-1-sepp-security-admin");
        seppSecAdminSystemExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminSystemExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminSystemExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminSystemExt);

        var seppSecAdminSystemExt2 = new JsonObject();
        seppSecAdminSystemExt2.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        seppSecAdminSystemExt2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        seppSecAdminSystemExt2.put(TAG_ACTION, PERMIT);
        seppSecAdminSystemExt2.put(TAG_NAME, "ericsson-system-ext-2-sepp-security-admin");
        seppSecAdminSystemExt2.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminSystemExt2.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminSystemExt2.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminSystemExt2);

        // add rule parameters for module ietf-keystore
        var seppSecAdminKeyStore = new JsonObject();
        seppSecAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        seppSecAdminKeyStore.put(TAG_ACTION, PERMIT);
        seppSecAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-sepp-security-admin");
        seppSecAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminKeyStore.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var seppSecAdminTruststore = new JsonObject();
        seppSecAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        seppSecAdminTruststore.put(TAG_ACTION, PERMIT);
        seppSecAdminTruststore.put(TAG_NAME, "ietf-truststore-1-sepp-security-admin");
        seppSecAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminTruststore.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var seppSecAdminKeyStoreExt = new JsonObject();
        seppSecAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        seppSecAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        seppSecAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-sepp-security-admin");
        seppSecAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminKeyStoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var seppSecAdminTruststoreExt = new JsonObject();
        seppSecAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        seppSecAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        seppSecAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-sepp-security-admin");
        seppSecAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminTruststoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminTruststoreExt);

        // add rule parameters for module ietf-truststore-ext
        var seppSecAdminNetconfAcmExt = new JsonObject();
        seppSecAdminNetconfAcmExt.put(TAG_MODULE, IETF_NETCONF_ACM);
        seppSecAdminNetconfAcmExt.put(TAG_ACTION, PERMIT);
        seppSecAdminNetconfAcmExt.put(TAG_NAME, "ietf-netconf-acm-1-sepp-security-admin");
        seppSecAdminNetconfAcmExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminNetconfAcmExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminNetconfAcmExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminNetconfAcmExt);

        // add rule parameters for system modules
        var seppSecAdminDefault1 = new JsonObject();
        seppSecAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        seppSecAdminDefault1.put(TAG_ACTION, PERMIT);
        seppSecAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-sepp-security-admin");
        seppSecAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppSecAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        seppSecAdminDefault1.put(TAG_COMMENT, SEPP_COMMENT);
        seppSecAdminRules.add(seppSecAdminDefault1);

        // add rules
        seppSecAdminValues.put(TAG_RULE, seppSecAdminRules);

        // Create cmd rules
        var seppSecAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-sepp
        var seppSecAdminShowAlarm = new JsonObject();
        seppSecAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        seppSecAdminShowAlarm.put(TAG_ACTION, PERMIT);
        seppSecAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        seppSecAdminShowAlarm.put(TAG_CONTEXT, CLI);
        seppSecAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppSecAdminCmdRules.add(seppSecAdminShowAlarm);

        // add rule parameters for module ericsson-sepp
        var seppSecAdminShowAlarmHistory = new JsonObject();
        seppSecAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        seppSecAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        seppSecAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        seppSecAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        seppSecAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppSecAdminCmdRules.add(seppSecAdminShowAlarmHistory);

        // add cmd rules
        seppSecAdminValues.put(TAG_TAILF_ACM_CMDRULE, seppSecAdminCmdRules);

        return seppSecAdminValues;
    }

    public static JsonObject generateSeppReadOnlyAcmDefaultConfig()
    {
        // create values for sepp-read-only
        var seppReadOnlyValues = new JsonObject();
        // create list of groups for sepp-read-only
        var seppReadOnlyGroups = new JsonArray();
        // add supported groups
        seppReadOnlyGroups.add(SEPP_READ_ONLY);
        seppReadOnlyValues.put(TAG_GROUP, seppReadOnlyGroups);
        // add rule-list name
        seppReadOnlyValues.put(TAG_NAME, "ericsson-sepp-manager-3-sepp-read-only");
        // Create Rules
        var seppReadOnlyRules = new JsonArray();
        // add rule parameters for module ericsson-sepp
        var seppReadOnlyEricSepp = new JsonObject();
        seppReadOnlyEricSepp.put(TAG_MODULE, ERICSSON_SEPP);
        seppReadOnlyEricSepp.put(TAG_ACTION, PERMIT);
        seppReadOnlyEricSepp.put(TAG_NAME, "ericsson-sepp-1-sepp-read-only");
        seppReadOnlyEricSepp.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyEricSepp.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyEricSepp.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyEricSepp);

        // add rule parameters for module ietf-keystore
        var seppReadOnlyKeyStore = new JsonObject();
        seppReadOnlyKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        seppReadOnlyKeyStore.put(TAG_ACTION, PERMIT);
        seppReadOnlyKeyStore.put(TAG_NAME, "ietf-keystore-1-sepp-read-only");
        seppReadOnlyKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyKeyStore.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyKeyStore);

        // add rule parameters for module ietf-truststore
        var seppReadOnlyTruststore = new JsonObject();
        seppReadOnlyTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        seppReadOnlyTruststore.put(TAG_ACTION, PERMIT);
        seppReadOnlyTruststore.put(TAG_NAME, "ietf-truststore-1-sepp-read-only");
        seppReadOnlyTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyTruststore.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyTruststore);

        // add rule parameters for module ietf-keystore-ext
        var seppReadOnlyKeyStoreExt = new JsonObject();
        seppReadOnlyKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        seppReadOnlyKeyStoreExt.put(TAG_ACTION, PERMIT);
        seppReadOnlyKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-sepp-read-only");
        seppReadOnlyKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyKeyStoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyKeyStoreExt);

        // add rule parameters for module ietf-truststore-ext
        var seppReadOnlyTruststoreExt = new JsonObject();
        seppReadOnlyTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        seppReadOnlyTruststoreExt.put(TAG_ACTION, PERMIT);
        seppReadOnlyTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-sepp-read-only");
        seppReadOnlyTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyTruststoreExt.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyTruststoreExt);

        // add rule parameters for system modules
        var seppReadOnlyDefault1 = new JsonObject();
        seppReadOnlyDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        seppReadOnlyDefault1.put(TAG_ACTION, PERMIT);
        seppReadOnlyDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-sepp-read-only");
        seppReadOnlyDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        seppReadOnlyDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        seppReadOnlyDefault1.put(TAG_COMMENT, SEPP_COMMENT);
        seppReadOnlyRules.add(seppReadOnlyDefault1);

        // add rules
        seppReadOnlyValues.put(TAG_RULE, seppReadOnlyRules);

        // Create cmd rules
        var seppReadOnlyCmdRules = new JsonArray();

        // add rule parameters for module ericsson-sepp
        var seppReadOnlyShowAlarm = new JsonObject();
        seppReadOnlyShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        seppReadOnlyShowAlarm.put(TAG_ACTION, PERMIT);
        seppReadOnlyShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        seppReadOnlyShowAlarm.put(TAG_CONTEXT, CLI);
        seppReadOnlyShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppReadOnlyCmdRules.add(seppReadOnlyShowAlarm);

        // add rule parameters for module ericsson-sepp
        var seppReadOnlyShowAlarmHistory = new JsonObject();
        seppReadOnlyShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        seppReadOnlyShowAlarmHistory.put(TAG_ACTION, PERMIT);
        seppReadOnlyShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        seppReadOnlyShowAlarmHistory.put(TAG_CONTEXT, CLI);
        seppReadOnlyShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        seppReadOnlyCmdRules.add(seppReadOnlyShowAlarmHistory);

        // add cmd rules
        seppReadOnlyValues.put(TAG_TAILF_ACM_CMDRULE, seppReadOnlyCmdRules);

        return seppReadOnlyValues;
    }

    public static JsonObject generateBsfAdminAcmDefaultConfig(final boolean bsfDiameterEnabled)
    {
        // create values for bsf-admin
        var bsfAdminValues = new JsonObject();

        // create list of groups for bsf-admin
        var bsfAdminGroups = new JsonArray();

        // add supported groups
        bsfAdminGroups.add(BSF_ADMIN);
        bsfAdminValues.put(TAG_GROUP, bsfAdminGroups);

        // add rule-list name
        bsfAdminValues.put(TAG_NAME, "ericsson-bsf-manager-1-bsf-admin");

        // Create Rules
        var bsfAdminRules = new JsonArray();

        // add rule parameters for module ericsson-bsf
        var bsfAdminEricBsf = new JsonObject();
        bsfAdminEricBsf.put(TAG_MODULE, ERICSSON_BSF);
        bsfAdminEricBsf.put(TAG_ACTION, PERMIT);
        bsfAdminEricBsf.put(TAG_NAME, "ericsson-bsf-1-bsf-admin");
        bsfAdminEricBsf.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminEricBsf.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfAdminEricBsf.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminEricBsf);

        // add rule parameters for module ietf-keystore
        var bsfAdminKeyStore = new JsonObject();
        bsfAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        bsfAdminKeyStore.put(TAG_ACTION, PERMIT);
        bsfAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-bsf-admin");
        bsfAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminKeyStore.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var bsfAdminTruststore = new JsonObject();
        bsfAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        bsfAdminTruststore.put(TAG_ACTION, PERMIT);
        bsfAdminTruststore.put(TAG_NAME, "ietf-truststore-1-bsf-admin");
        bsfAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminTruststore.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var bsfAdminKeyStoreExt = new JsonObject();
        bsfAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        bsfAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        bsfAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-bsf-admin");
        bsfAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminKeyStoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var bsfAdminTruststoreExt = new JsonObject();
        bsfAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        bsfAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        bsfAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-bsf-admin");
        bsfAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminTruststoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminTruststoreExt);

        // add rule parameters for module ericsson-brm
        var bsfAdminBrm = new JsonObject();
        bsfAdminBrm.put(TAG_MODULE, ERICSSON_BRM);
        bsfAdminBrm.put(TAG_ACTION, PERMIT);
        bsfAdminBrm.put(TAG_NAME, "ericsson-brm-1-bsf-admin");
        bsfAdminBrm.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminBrm.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminBrm.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminBrm);

        // add rule parameters for system modules
        var bsfAdminDefault1 = new JsonObject();
        bsfAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        bsfAdminDefault1.put(TAG_ACTION, PERMIT);
        bsfAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-bsf-admin");
        bsfAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfAdminDefault1.put(TAG_COMMENT, BSF_COMMENT);
        bsfAdminRules.add(bsfAdminDefault1);

        if (bsfDiameterEnabled)
        {
            bsfAdminRules.add(generateBsfDiameterRules(BSF_ADMIN));
        }

        // add rules
        bsfAdminValues.put(TAG_RULE, bsfAdminRules);

        // Create cmd rules
        var bsfAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-bsf
        var bsfAdminMetrics = new JsonObject();
        bsfAdminMetrics.put(TAG_NAME, "cli-metrics-query-cmd");
        bsfAdminMetrics.put(TAG_ACTION, PERMIT);
        bsfAdminMetrics.put(TAG_COMMAND, CMD_METRICS_QUERY);
        bsfAdminMetrics.put(TAG_CONTEXT, CLI);
        bsfAdminMetrics.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfAdminCmdRules.add(bsfAdminMetrics);

        // add rule parameters for module ericsson-bsf
        var bsfAdminShowAlarm = new JsonObject();
        bsfAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        bsfAdminShowAlarm.put(TAG_ACTION, PERMIT);
        bsfAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        bsfAdminShowAlarm.put(TAG_CONTEXT, CLI);
        bsfAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfAdminCmdRules.add(bsfAdminShowAlarm);

        // add rule parameters for module ericsson-bsf
        var bsfAdminShowAlarmHistory = new JsonObject();
        bsfAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        bsfAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        bsfAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        bsfAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        bsfAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfAdminCmdRules.add(bsfAdminShowAlarmHistory);

        // add cmd rules
        bsfAdminValues.put(TAG_TAILF_ACM_CMDRULE, bsfAdminCmdRules);

        return bsfAdminValues;
    }

    public static JsonObject generateBsfSecAdminAcmDefaultConfig()
    {

        // create values for bsf-security-admin
        var bsfSecAdminValues = new JsonObject();
        // create list of groups for bsf-security-admin
        var bsfSecAdminGroups = new JsonArray();
        // add supported groups
        bsfSecAdminGroups.add(BSF_SECURITY_ADMIN);
        bsfSecAdminValues.put(TAG_GROUP, bsfSecAdminGroups);
        // add rule-list name
        bsfSecAdminValues.put(TAG_NAME, "ericsson-bsf-manager-2-bsf-security-admin");
        // Create Rules
        var bsfSecAdminRules = new JsonArray();

        // add rule parameters for module ietf-system
        var bsfSecAdminSystem = new JsonObject();
        bsfSecAdminSystem.put(TAG_MODULE, IETF_SYSTEM);
        bsfSecAdminSystem.put(TAG_PATH, "/" + SYSTEM_STATE + "/");
        bsfSecAdminSystem.put(TAG_ACTION, PERMIT);
        bsfSecAdminSystem.put(TAG_NAME, "ietf-system-1-bsf-security-admin");
        bsfSecAdminSystem.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminSystem.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminSystem.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminSystem);

        var bsfSecAdminSystem2 = new JsonObject();
        bsfSecAdminSystem2.put(TAG_MODULE, IETF_SYSTEM);
        bsfSecAdminSystem2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        bsfSecAdminSystem2.put(TAG_ACTION, PERMIT);
        bsfSecAdminSystem2.put(TAG_NAME, "ietf-system-2-bsf-security-admin");
        bsfSecAdminSystem2.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminSystem2.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminSystem2.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminSystem2);

        // add rule parameters for module ericsson-system-ext
        var bsfSecAdminSystemExt = new JsonObject();
        bsfSecAdminSystemExt.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        bsfSecAdminSystemExt.put(TAG_PATH, "/" + SYSTEM_STATE + "/" + AUTHENTICATION + "/" + ADMIN_USER + "/" + NAME);
        bsfSecAdminSystemExt.put(TAG_ACTION, PERMIT);
        bsfSecAdminSystemExt.put(TAG_NAME, "ericsson-system-ext-1-bsf-security-admin");
        bsfSecAdminSystemExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminSystemExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminSystemExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminSystemExt);

        var bsfSecAdminSystemExt2 = new JsonObject();
        bsfSecAdminSystemExt2.put(TAG_MODULE, ERICSSON_SYSTEM_EXT);
        bsfSecAdminSystemExt2.put(TAG_PATH, "/" + SYSTEM + "/" + AUTHENTICATION + "/*");
        bsfSecAdminSystemExt2.put(TAG_ACTION, PERMIT);
        bsfSecAdminSystemExt2.put(TAG_NAME, "ericsson-system-ext-2-bsf-security-admin");
        bsfSecAdminSystemExt2.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminSystemExt2.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminSystemExt2.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminSystemExt2);

        // add rule parameters for module ietf-keystore
        var bsfSecAdminKeyStore = new JsonObject();
        bsfSecAdminKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        bsfSecAdminKeyStore.put(TAG_ACTION, PERMIT);
        bsfSecAdminKeyStore.put(TAG_NAME, "ietf-keystore-1-bsf-security-admin");
        bsfSecAdminKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminKeyStore.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminKeyStore);

        // add rule parameters for module ietf-truststore
        var bsfSecAdminTruststore = new JsonObject();
        bsfSecAdminTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        bsfSecAdminTruststore.put(TAG_ACTION, PERMIT);
        bsfSecAdminTruststore.put(TAG_NAME, "ietf-truststore-1-bsf-security-admin");
        bsfSecAdminTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminTruststore.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminTruststore);

        // add rule parameters for module ericsson-keystore-ext
        var bsfSecAdminKeyStoreExt = new JsonObject();
        bsfSecAdminKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        bsfSecAdminKeyStoreExt.put(TAG_ACTION, PERMIT);
        bsfSecAdminKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-bsf-security-admin");
        bsfSecAdminKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminKeyStoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminKeyStoreExt);

        // add rule parameters for module ericsson-truststore-ext
        var bsfSecAdminTruststoreExt = new JsonObject();
        bsfSecAdminTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        bsfSecAdminTruststoreExt.put(TAG_ACTION, PERMIT);
        bsfSecAdminTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-bsf-security-admin");
        bsfSecAdminTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminTruststoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminTruststoreExt);

        // add rule parameters for module ietf-netconf-acm
        var bsfSecAdminNetconfAcmExt = new JsonObject();
        bsfSecAdminNetconfAcmExt.put(TAG_MODULE, IETF_NETCONF_ACM);
        bsfSecAdminNetconfAcmExt.put(TAG_ACTION, PERMIT);
        bsfSecAdminNetconfAcmExt.put(TAG_NAME, "ietf-netconf-acm-1-bsf-security-admin");
        bsfSecAdminNetconfAcmExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminNetconfAcmExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminNetconfAcmExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminNetconfAcmExt);

        // add rule parameters for system modules
        var bsfSecAdminDefault1 = new JsonObject();
        bsfSecAdminDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        bsfSecAdminDefault1.put(TAG_ACTION, PERMIT);
        bsfSecAdminDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-bsf-security-admin");
        bsfSecAdminDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfSecAdminDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfSecAdminDefault1.put(TAG_COMMENT, BSF_COMMENT);
        bsfSecAdminRules.add(bsfSecAdminDefault1);

        // add rules
        bsfSecAdminValues.put(TAG_RULE, bsfSecAdminRules);

        // Create cmd rules
        var bsfSecAdminCmdRules = new JsonArray();

        // add rule parameters for module ericsson-bsf
        var bsfSecAdminShowAlarm = new JsonObject();
        bsfSecAdminShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        bsfSecAdminShowAlarm.put(TAG_ACTION, PERMIT);
        bsfSecAdminShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        bsfSecAdminShowAlarm.put(TAG_CONTEXT, CLI);
        bsfSecAdminShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfSecAdminCmdRules.add(bsfSecAdminShowAlarm);

        // add rule parameters for module ericsson-bsf
        var bsfSecAdminShowAlarmHistory = new JsonObject();
        bsfSecAdminShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        bsfSecAdminShowAlarmHistory.put(TAG_ACTION, PERMIT);
        bsfSecAdminShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        bsfSecAdminShowAlarmHistory.put(TAG_CONTEXT, CLI);
        bsfSecAdminShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfSecAdminCmdRules.add(bsfSecAdminShowAlarmHistory);

        // add cmd rules
        bsfSecAdminValues.put(TAG_TAILF_ACM_CMDRULE, bsfSecAdminCmdRules);

        return bsfSecAdminValues;
    }

    public static JsonObject generateBsfReadOnlyAcmDefaultConfig(final boolean bsfDiameterEnabled)
    {

        // create values for bsf-read-only
        var bsfReadOnlyValues = new JsonObject();
        // create list of groups for bsf-read-only
        var bsfReadOnlyGroups = new JsonArray();
        // add supported groups
        bsfReadOnlyGroups.add(BSF_READ_ONLY);
        bsfReadOnlyValues.put(TAG_GROUP, bsfReadOnlyGroups);
        // add rule-list name
        bsfReadOnlyValues.put(TAG_NAME, "ericsson-bsf-manager-3-bsf-read-only");
        // Create Rules
        var bsfReadOnlyRules = new JsonArray();
        // add rule parameters for module ericsson-bsf
        var bsfReadOnlyEricBsf = new JsonObject();
        bsfReadOnlyEricBsf.put(TAG_MODULE, ERICSSON_BSF);
        bsfReadOnlyEricBsf.put(TAG_ACTION, PERMIT);
        bsfReadOnlyEricBsf.put(TAG_NAME, "ericsson-bsf-1-bsf-read-only");
        bsfReadOnlyEricBsf.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyEricBsf.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyEricBsf.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyEricBsf);

        // add rule parameters for module ietf-keystore
        var bsfReadOnlyKeyStore = new JsonObject();
        bsfReadOnlyKeyStore.put(TAG_MODULE, IETF_KEYSTORE);
        bsfReadOnlyKeyStore.put(TAG_ACTION, PERMIT);
        bsfReadOnlyKeyStore.put(TAG_NAME, "ietf-keystore-1-bsf-read-only");
        bsfReadOnlyKeyStore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyKeyStore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyKeyStore.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyKeyStore);

        // add rule parameters for module ietf-truststore
        var bsfReadOnlyTruststore = new JsonObject();
        bsfReadOnlyTruststore.put(TAG_MODULE, IETF_TRUSTSTORE);
        bsfReadOnlyTruststore.put(TAG_ACTION, PERMIT);
        bsfReadOnlyTruststore.put(TAG_NAME, "ietf-truststore-1-bsf-read-only");
        bsfReadOnlyTruststore.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyTruststore.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyTruststore.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyTruststore);

        // add rule parameters for module ietf-keystore-ext
        var bsfReadOnlyKeyStoreExt = new JsonObject();
        bsfReadOnlyKeyStoreExt.put(TAG_MODULE, ERICSSON_KEYSTORE_EXT);
        bsfReadOnlyKeyStoreExt.put(TAG_ACTION, PERMIT);
        bsfReadOnlyKeyStoreExt.put(TAG_NAME, "ericsson-keystore-ext-1-bsf-read-only");
        bsfReadOnlyKeyStoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyKeyStoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyKeyStoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyKeyStoreExt);

        // add rule parameters for module ietf-truststore-ext
        var bsfReadOnlyTruststoreExt = new JsonObject();
        bsfReadOnlyTruststoreExt.put(TAG_MODULE, ERICSSON_TRUSTSTORE_EXT);
        bsfReadOnlyTruststoreExt.put(TAG_ACTION, PERMIT);
        bsfReadOnlyTruststoreExt.put(TAG_NAME, "ericsson-truststore-ext-1-bsf-read-only");
        bsfReadOnlyTruststoreExt.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyTruststoreExt.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyTruststoreExt.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyTruststoreExt);

        // add rule parameters for ietf-netconf-monitoring
        var bsfReadOnlyDefault1 = new JsonObject();
        bsfReadOnlyDefault1.put(TAG_MODULE, IETF_NETCONF_MONITORING);
        bsfReadOnlyDefault1.put(TAG_ACTION, PERMIT);
        bsfReadOnlyDefault1.put(TAG_NAME, "ietf-netconf-monitoring-1-bsf-read-only");
        bsfReadOnlyDefault1.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyDefault1.put(TAG_ACCESS_OPERTATIONS, READ);
        bsfReadOnlyDefault1.put(TAG_COMMENT, BSF_COMMENT);
        bsfReadOnlyRules.add(bsfReadOnlyDefault1);

        if (bsfDiameterEnabled)
        {
            // add rule parameters for module ericsson-diameter-adp
            bsfReadOnlyRules.add(generateBsfDiameterRules(BSF_READ_ONLY));
        }

        // add rules
        bsfReadOnlyValues.put(TAG_RULE, bsfReadOnlyRules);

        // Create cmd rules
        var bsfReadOnlyCmdRules = new JsonArray();

        // add rule parameters for module ericsson-bsf
        var bsfReadOnlyShowAlarm = new JsonObject();
        bsfReadOnlyShowAlarm.put(TAG_NAME, "cli-show-alarm-cmd");
        bsfReadOnlyShowAlarm.put(TAG_ACTION, PERMIT);
        bsfReadOnlyShowAlarm.put(TAG_COMMAND, CMD_SHOW_ALARM);
        bsfReadOnlyShowAlarm.put(TAG_CONTEXT, CLI);
        bsfReadOnlyShowAlarm.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfReadOnlyCmdRules.add(bsfReadOnlyShowAlarm);

        // add rule parameters for module ericsson-bsf
        var bsfReadOnlyShowAlarmHistory = new JsonObject();
        bsfReadOnlyShowAlarmHistory.put(TAG_NAME, "cli-show-alarm-history-cmd");
        bsfReadOnlyShowAlarmHistory.put(TAG_ACTION, PERMIT);
        bsfReadOnlyShowAlarmHistory.put(TAG_COMMAND, CMD_SHOW_ALARM_HISTORY);
        bsfReadOnlyShowAlarmHistory.put(TAG_CONTEXT, CLI);
        bsfReadOnlyShowAlarmHistory.put(TAG_ACCESS_OPERTATIONS, READ_WRITE);
        bsfReadOnlyCmdRules.add(bsfReadOnlyShowAlarmHistory);

        // add cmd rules
        bsfReadOnlyValues.put(TAG_TAILF_ACM_CMDRULE, bsfReadOnlyCmdRules);

        return bsfReadOnlyValues;
    }

    public static JsonObject generateBsfDiameterRules(final String user)
    {
        final var bsfReadOnlyEricStm = new JsonObject();
        bsfReadOnlyEricStm.put(TAG_MODULE, ERICSSON_STM);
        bsfReadOnlyEricStm.put(TAG_ACTION, PERMIT);
        bsfReadOnlyEricStm.put(TAG_NAME, String.format("ericsson-diameter-adp-1-%s", user));
        bsfReadOnlyEricStm.put(TAG_TAILF_ACM_CONTEXT, "*");
        bsfReadOnlyEricStm.put(TAG_ACCESS_OPERTATIONS, user.contains("read") ? READ : READ_WRITE);
        bsfReadOnlyEricStm.put(TAG_COMMENT, BSF_COMMENT);

        return bsfReadOnlyEricStm;
    }

    public static JsonArray generateDefaultAdminGroupsConfig()
    {

        // create values for bsf-security-admin
        var sysAdminGroup = new JsonObject();
        sysAdminGroup.put("name", "system-admin");
        var sysSecAdminGroup = new JsonObject();
        sysSecAdminGroup.put("name", "system-security-admin");
        var sysRoGroup = new JsonObject();
        sysRoGroup.put("name", "system-read-only");

        // create list of groups for bsf-security-admin
        JsonArray groups = new JsonArray();
        groups.add(sysAdminGroup);
        groups.add(sysSecAdminGroup);
        groups.add(sysRoGroup);

        return groups;
    }

    public static CmConfig<IetfNetconfAcm> newCmConfiguration(CmConfig<IetfNetconfAcm> cmAcmCfg)
    {
        final var newCfg = addScpAcmDefaultConfig(cmAcmCfg.get());
        log.debug("New ietf-netconf-acm: {}", newCfg);
        return new CmConfig<>(newCfg, cmAcmCfg.getETag());
    }

    public static void main(String[] args)
    {
        log.info("Challengers!!!");
    }

}
