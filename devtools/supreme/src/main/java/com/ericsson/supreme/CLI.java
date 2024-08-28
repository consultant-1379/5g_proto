package com.ericsson.supreme;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.CertificateGenerator;
import com.ericsson.supreme.api.CertificateInstaller;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.config.Installation;
import com.ericsson.supreme.defaultscenario.CommonRootCa;
import com.ericsson.supreme.defaultscenario.DefaultScenarioFactory;
import com.ericsson.supreme.defaultscenario.DefaultScenarioFactory.DefaultScenarioType;
import com.ericsson.supreme.defaultscenario.Processor;
import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

import ch.qos.logback.classic.Level;

public class CLI
{
    protected static final Logger log = LoggerFactory.getLogger(CLI.class);
    public static final String PROPERTIES_OPTION_NAME = "properties";
    public static final String LOGLEVEL_OPTION_NAME = "loglevel";
    public static final String HELP_OPTION_NAME = "help";
    public static final String NAMESPACE_OPTION_NAME = "namespace";
    public static final String KUBECONFIG_OPTION_NAME = "kubeconfig";
    public static final String OUTPUT_OPTION_NAME = "output";
    public static final String DEFAULT_SCENARIO_OPTION_NAME = "default-scenario";
    public static final String ACTION_GENERATE = "generate";
    public static final String ACTION_INSTALL = "install";
    public static final List<String> ALLOWED_ARGUMENTS = List.of(ACTION_GENERATE, ACTION_INSTALL);
    protected static final List<String> ALLOWED_DEFAULT_SCENARIOS = Stream.of(DefaultScenarioType.values())
                                                                          .map(DefaultScenarioType::toString)
                                                                          .collect(Collectors.toList());
    public static final List<String> ALLOWED_LOG_LEVELS = List.of("info", "warn", "error", "debug");

    private Options options;
    private String[] input;
    private CommandLineParser parser;
    private CommandLine cmd;

    private static final String INFO = "\nInfo:\n" + " - Generate OR/AND install certificates for default targets. \n"
                                       + " - Generate OR/AND install multiple custom certificates as defined in a properties file.\n"
                                       + " - In any case if the defined CA exists, it is used to sign the new certificate. If it does not exist, it is created and then used to sign the certificate.\n"
                                       + " - The installation can be done either to yang provider or secret.\n"
                                       + " - cert-info.log: when cert/CA is created, a log file is produced under the same directory with many useful info about the certificate\n"
                                       + " - The generated certificates MUST maintain their names, otherwise the tool won't work properly.\n"
                                       + " - For a readable format of the output, you can use the /proj/sc-tools/bin/jl tool\n\n";

    private static final String FOOTER = "\nPlease report any issues to the Avengers team." + "\n\nExamples:\n"
                                         + "\t 1. Create and install certificates for default target 'scpmgr'\n"
                                         + "\t\t java -jar supreme.jar generate install -d scpmgr,k6\n"
                                         + "\t 2. Create certificates for default target 'seppsim'\n" + "\t\t java -jar supreme.jar generate -d seppsim\n"
                                         + "\t 3. Install certificates for default target 'seppsim' by using a properties file that's not in the same folder as the current jar\n"
                                         + "\t\t java -jar supreme.jar install -p ../properties.yaml -d seppsim\n"
                                         + "\t 4. Create and install custom certificates\n" + "\t\t java -jar supreme.jar generate install\n\n";

    CLI(String[] input)
    {
        this.input = input;
        this.options = setupCmdOptions();
        this.parser = new DefaultParser();
    }

    public void validateInput() throws ParseException
    {
        this.cmd = parser.parse(options, input);
        checkOptionsValidity(cmd);
        checkArgumentsValidity(cmd.getArgList());
        checkOptionsMatchArguments(cmd);
    }

    public CommandLine getCmd()
    {
        return this.cmd;
    }

    /**
     * Verifies that all given arguments are valid; contained in ALLOWED_ARGUMENTS.
     * If there is an invalid argument a ParseException is thrown. If the
     * 
     * @param cmdArgs
     * @throws ParseException
     */
    private void checkArgumentsValidity(List<String> cmdArgs) throws ParseException
    {
        if (cmdArgs.isEmpty())
        {
            return;
        }
        if (!cmdArgs.stream()
                    .map(ALLOWED_ARGUMENTS::contains)
                    .reduce(true,
                            (a1,
                             a2) -> a1 && a2)
                    .equals(true))
        {
            throw new ParseException(String.format("Invalid positional arguments. Only %s are allowed", ALLOWED_ARGUMENTS));
        }

    }

    /**
     * Verify that at least one of the options is used. Throws ParserException
     * otherwise.
     * 
     * @param cmd
     * @throws ParseException
     */
    private void checkOptionsValidity(CommandLine cmd) throws ParseException
    {
        if (cmd.hasOption(DEFAULT_SCENARIO_OPTION_NAME))
        {
            var optionValues = Arrays.asList(cmd.getOptionValues(DEFAULT_SCENARIO_OPTION_NAME));

            for (var val : optionValues)
            {
                if (!ALLOWED_DEFAULT_SCENARIOS.contains(val))
                {
                    throw new ParseException(String.format("Invalid default scenario %s. Only %s are allowed", val, ALLOWED_DEFAULT_SCENARIOS));
                }
            }
        }

        if (cmd.hasOption(LOGLEVEL_OPTION_NAME) && !ALLOWED_LOG_LEVELS.contains(cmd.getOptionValue(LOGLEVEL_OPTION_NAME).toLowerCase()))
        {
            throw new ParseException(String.format("Invalid loglevel. Only %s are allowed", ALLOWED_LOG_LEVELS));
        }
    }

    /**
     * Verify that the arguments match with the options
     * 
     * @param cmd
     * @throws ParseException
     */
    private void checkOptionsMatchArguments(CommandLine cmd) throws ParseException
    {
        if ((cmd.hasOption(PROPERTIES_OPTION_NAME) || cmd.hasOption(DEFAULT_SCENARIO_OPTION_NAME)) && cmd.getArgList().isEmpty())
        {
            throw new ParseException(String.format("At least one of %s must be specified", ALLOWED_ARGUMENTS));
        }
    }

    private Options setupCmdOptions()
    {
        Options opt = new Options();
        Option propertiesOption = new Option("p", PROPERTIES_OPTION_NAME, true, "Propeties file for certificate generation/installation");
        propertiesOption.setRequired(false);
        opt.addOption(propertiesOption);

        Option defaultScenarioOption = new Option("d",
                                                  DEFAULT_SCENARIO_OPTION_NAME,
                                                  true,
                                                  "Comma-separated list of default scenarios for certificate generation-creation. <arg1>,<arg2>.. : "
                                                        + ALLOWED_DEFAULT_SCENARIOS.toString());
        defaultScenarioOption.setRequired(false);
        defaultScenarioOption.setArgs(Option.UNLIMITED_VALUES);
        defaultScenarioOption.setValueSeparator(',');
        opt.addOption(defaultScenarioOption);

        Option outputFolderOption = new Option("o", OUTPUT_OPTION_NAME, true, "The output folder for the default scenarios");
        outputFolderOption.setRequired(false);
        opt.addOption(outputFolderOption);

        Option kubeconfigOption = new Option("k", KUBECONFIG_OPTION_NAME, true, "The kubeconfig for the targeted cluster");
        kubeconfigOption.setRequired(false);
        opt.addOption(kubeconfigOption);

        Option logLevelOption = new Option("l", LOGLEVEL_OPTION_NAME, true, "Set log level");
        logLevelOption.setRequired(false);
        opt.addOption(logLevelOption);

        Option helpOption = new Option("h", HELP_OPTION_NAME, false, "This help file");
        logLevelOption.setRequired(false);
        opt.addOption(helpOption);

        Option namespaceOption = new Option("n", NAMESPACE_OPTION_NAME, true, "The targeted cluster namespace");
        namespaceOption.setRequired(false);
        opt.addOption(namespaceOption);

        return opt;
    }

    void handleDefaultScenarios(Configuration config) throws DefaultScenarioException
    {
        var generate = cmd.getArgList().contains(CLI.ACTION_GENERATE);
        var install = cmd.getArgList().contains(CLI.ACTION_INSTALL);

        var factory = new DefaultScenarioFactory(config);

        var scenarios = cmd.getOptionValues(CLI.DEFAULT_SCENARIO_OPTION_NAME);
        var iterator = new ArrayIterator<String>(scenarios);

        // first default value
        var headProcessor = (CommonRootCa) factory.getDefaultScenario(DefaultScenarioType.ROOTCA);

        if (Stream.of(scenarios).map(DefaultScenarioType::fromValue).noneMatch(d -> d == DefaultScenarioType.ROOTCA))
        {
            headProcessor.setInstallation(false);
        }

        Processor previousProcessor = headProcessor;

        while (iterator.hasNext())
        {
            var value = iterator.next();
            var type = DefaultScenarioType.fromValue(value);
            if (type != DefaultScenarioType.ROOTCA)
            {
                // if not the head processor
                var currentProcessor = factory.getDefaultScenario(type);
                previousProcessor.setNextProcessor(currentProcessor);
                previousProcessor = currentProcessor;
            }
        }

        try
        {
            Optional<KubernetesClient> kubeClient = install ? Optional.of(Utils.getKubernetesClient(config.getAdmin().getNamespace(),
                                                                                                    config.getAdmin().getKubeconfig()))
                                                            : Optional.empty();
            headProcessor.process(generate, kubeClient, null);
        }
        catch (KubernetesClientException e)
        {
            throw new DefaultScenarioException("The Kubernetes Client could not be initialized", e);
        }

    }

    void handleLogLevelOption()
    {
        var logLevel = cmd.getOptionValue(CLI.LOGLEVEL_OPTION_NAME);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(logLevel));
    }

    void handleCustomGeneration(Configuration config) throws CertificateCreationException, CertificateIOException
    {
        log.info("Starting custom generation");
        var caStore = generateRootCertificates(config);
        generateCertificates(config, caStore);
    }

    Map<String, GeneratedCert> generateRootCertificates(Configuration config) throws CertificateCreationException, CertificateIOException
    {
        var caStore = new HashedMap<String, GeneratedCert>();

        // 1. Generate Certificate authorities and save them as file.
        if (config.getGeneration().getCertificateAuthorities() != null)
        {
            var cas = config.getGeneration().getCertificateAuthorities();

            for (var ca : cas)
            {
                var outPath = Path.of(ca.getOutputDir());

                var certOptional = CertificateIO.readGeneratedCert(ca.getName(), outPath);
                if (certOptional.isEmpty())
                {
                    // 1.b Generate CA
                    var newCert = CertificateGenerator.createCertificateAuthority(ca.getName(),
                                                                                  ca.getAlgorithm().getBits(),
                                                                                  ca.getExpirationDays(),
                                                                                  ca.getCommonName());

                    // 1.c Store CA to file
                    CertificateIO.exportCertificate(newCert, outPath);
                    CertificateTool.createLogFile(newCert, outPath);
                    caStore.put(ca.getName(), newCert);
                }
                else
                {
                    caStore.put(ca.getName(), certOptional.get());
                }
            }
        }

        return caStore;
    }

    private void generateCertificates(Configuration config,
                                      Map<String, GeneratedCert> caStore) throws CertificateCreationException, CertificateIOException
    {
        // 2. Generate Certificates and save them as file
        if (config.getGeneration().getCertificates() != null)
        {
            var certs = config.getGeneration().getCertificates();

            for (var certificate : certs)
            {
                var path = Path.of(certificate.getOutputDir());

                // 2.b Generate Certificate
                GeneratedCert cert = null;

                if (certificate.getSign().isSelfSigned())
                {
                    cert = CertificateGenerator.createSelfSignedCertificate(certificate.getName(), //
                                                                            certificate.getAlgorithm().getBits(),
                                                                            certificate.getExpirationDays(),
                                                                            certificate.getCommonName(),
                                                                            certificate.getSans());
                }
                else
                {
                    if (caStore.containsKey(certificate.getSign().getCertificateAuthority()))
                    {
                        cert = CertificateGenerator.createCertificateSignedByRoot(certificate.getName(),
                                                                                  certificate.getAlgorithm().getBits(),
                                                                                  certificate.getExpirationDays(),
                                                                                  certificate.getCommonName(),
                                                                                  certificate.getSans(),
                                                                                  caStore.get(certificate.getSign().getCertificateAuthority()));
                    }
                    else
                    {
                        throw new CertificateCreationException("The certificate authority provided for certificate " + certificate.getName() + " is not valid");
                    }
                }

                // 2.c Store Certificate to file
                CertificateIO.exportCertificate(cert, path);
                CertificateTool.createLogFile(cert, path);
            }
        }
    }

    boolean handleCustomInstallation(Configuration config) throws CertificateInstallationException
    {
        log.info("Starting custom installation");
        CertificateInstaller certificateInstaller;
        try
        {
            var kubeClient = Utils.getKubernetesClient(config.getAdmin().getNamespace(), config.getAdmin().getKubeconfig());
            certificateInstaller = Utils.getGenericInstaller(config.getAdmin(), kubeClient);
        }
        catch (NetconfClientException | KubernetesClientException e)
        {
            throw new CertificateInstallationException("Could not initialize the installer", e);
        }
        return this.install(config, certificateInstaller);
    }

    private boolean install(Configuration config,
                            CertificateInstaller certificateInstaller)
    {
        var res = true;
        for (var installation : config.getInstallations())
        {
            var path = Path.of(installation.getDir());
            var name = installation.getName();

            try
            {
                var certOptional = CertificateIO.readGeneratedCert(name, path);

                if (certOptional.isPresent())
                {
                    if (installation.getTarget().getNetconf() != null)
                    {
                        res &= this.installAtNetconf(installation, certificateInstaller, certOptional.get());
                    }
                    else
                    {
                        res &= this.installAtSecret(installation, certificateInstaller, certOptional.get());
                    }
                }
            }
            catch (CertificateInstallationException | CertificateIOException e)
            {
                log.warn("Installation for {} will be skipped. {}", name, e);
                res = false;
            }
        }

        return res;

    }

    private boolean installAtNetconf(Installation installation,
                                     CertificateInstaller certificateInstaller,
                                     GeneratedCert cert) throws CertificateInstallationException
    {
        var netconf = installation.getTarget().getNetconf();
        if (netconf.getListName() != null)
        {
            return certificateInstaller.installCertificateAuthorityAtNetconf(netconf.getListName(), netconf.getCertificateName(), cert);
        }
        else
        {
            return certificateInstaller.installCertificateAtNetconf(netconf.getKeyName(), netconf.getCertificateName(), cert);
        }
    }

    private boolean installAtSecret(Installation installation,
                                    CertificateInstaller certificateInstaller,
                                    GeneratedCert cert) throws CertificateInstallationException
    {
        if (Utils.isCa(cert))
        {
            return certificateInstaller.installCertificateAuthorityAtSecret(installation.getTarget().getSecretName(), cert);
        }
        else
        {
            return certificateInstaller.installCertificateAtTlsSecret(installation.getTarget().getSecretName(), cert);
        }
    }

    /**
     * 
     */
    void printHelp()
    {
        var formatter = new HelpFormatter();
        formatter.printHelp("supreme", INFO, this.options, FOOTER, true);
        System.exit(0);
    }
}
