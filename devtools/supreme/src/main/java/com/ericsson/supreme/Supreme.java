package com.ericsson.supreme;

import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.config.ConfigurationSerializer;
import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;

public class Supreme
{
    protected static final Logger log = LoggerFactory.getLogger(Supreme.class);

    public static void main(String[] args)
    {
        CLI cli = new CLI(args);
        var res = true;

        log.info("Supreme v1.0.22");

        try
        {
            cli.validateInput();

            CommandLine cmd = cli.getCmd();

            if (cmd.hasOption(CLI.HELP_OPTION_NAME))
            {
                cli.printHelp();
            }

            if (cmd.hasOption(CLI.LOGLEVEL_OPTION_NAME))
            {
                cli.handleLogLevelOption();
            }

            var config = cmd.hasOption(CLI.PROPERTIES_OPTION_NAME) ? ConfigurationSerializer.getConfiguration(cmd.getOptionValue(CLI.PROPERTIES_OPTION_NAME))
                                                                   : ConfigurationSerializer.getConfiguration("properties.yaml");

            if (cmd.hasOption(CLI.KUBECONFIG_OPTION_NAME))
            {
                config.getAdmin().setKubeconfig(cmd.getOptionValue(CLI.KUBECONFIG_OPTION_NAME));
                log.info("Kubeconfig set to {} from command line", config.getAdmin().getKubeconfig());
            }

            if (cmd.hasOption(CLI.NAMESPACE_OPTION_NAME))
            {
                config.getAdmin().setNamespace(cmd.getOptionValue(CLI.NAMESPACE_OPTION_NAME));
                log.info("Namespace set to {} from command line", config.getAdmin().getNamespace());
            }

            if (cmd.hasOption(CLI.OUTPUT_OPTION_NAME))
            {
                config.getDefaultScenarios().setOutputDir(cmd.getOptionValue(CLI.OUTPUT_OPTION_NAME));
                log.info("Output dir set to {} from command line", config.getDefaultScenarios().getOutputDir());
            }

            config.validate(cmd.hasOption(CLI.DEFAULT_SCENARIO_OPTION_NAME));

            if (cmd.hasOption(CLI.DEFAULT_SCENARIO_OPTION_NAME))
            {
                cli.handleDefaultScenarios(config);
            }
            else
            {
                // read the file and generate/install the custom certificates
                if (cmd.getArgList().contains(CLI.ACTION_GENERATE))
                {
                    cli.handleCustomGeneration(config);
                }
                if (cmd.getArgList().contains(CLI.ACTION_INSTALL))
                {
                    res = cli.handleCustomInstallation(config);
                }
            }
        }
        catch (ParseException | FileNotFoundException | CertificateInstallationException | CertificateCreationException | CertificateIOException
               | DefaultScenarioException e)
        {
            log.error("Something went wrong. Exiting.", e);
            System.exit(1);
        }

        if (!res)
        {
            log.error("At least one certificate could not be handled. Exiting");
            System.exit(1);
        }
    }
}
