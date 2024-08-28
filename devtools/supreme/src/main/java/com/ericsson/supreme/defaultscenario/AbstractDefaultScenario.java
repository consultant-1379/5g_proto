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

import java.nio.file.Path;
import java.util.List;

import com.ericsson.supreme.api.CertificateGenerator;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.supreme.kernel.Utils;

/**
 * 
 */
abstract class AbstractDefaultScenario
{
    protected final Configuration config;

    AbstractDefaultScenario(Configuration config)
    {
        this.config = config;
    }

    protected GeneratedCert defaultCreateCerts(String name,
                                               String commonName,
                                               List<String> sans,
                                               GeneratedCert ca) throws DefaultScenarioException
    {
        try
        {
            var cert = CertificateGenerator.createCertificateSignedByRoot(name, this.config.getDefaultScenarios().getExpirationDays(), commonName, sans, ca);
            var outPath = Utils.createDirs(Path.of(this.config.getDefaultScenarios().getOutputDir(), name));

            CertificateIO.exportCertificate(cert, outPath);
            CertificateTool.createLogFile(cert, outPath);

            return cert;
        }
        catch (CertificateCreationException | CertificateIOException e)
        {
            throw new DefaultScenarioException("Certificate creation for " + name + " has failed", e);
        }

    }

    protected GeneratedCert defaultCreateCa(String name,
                                            String commonName) throws DefaultScenarioException
    {
        try
        {
            var ca = CertificateGenerator.createCertificateAuthority(name, this.config.getDefaultScenarios().getExpirationDays(), commonName);
            var outPath = Utils.createDirs(Path.of(this.config.getDefaultScenarios().getOutputDir(), name));

            CertificateIO.exportCertificate(ca, outPath);
            CertificateTool.createLogFile(ca, outPath);

            return ca;
        }
        catch (CertificateCreationException | CertificateIOException e)
        {
            throw new DefaultScenarioException("Certificate authority creation for " + name + " has failed", e);
        }
    }

    protected GeneratedCert defaultReadCerts(String name) throws DefaultScenarioException
    {
        try
        {
            return CertificateIO.readGeneratedCert(name, Path.of(this.config.getDefaultScenarios().getOutputDir(), name))
                                .orElseThrow(() -> new DefaultScenarioException("The certificate files of '" + name + "' were not found in directory "
                                                                                + config.getDefaultScenarios().getOutputDir()));

        }
        catch (CertificateIOException e)
        {
            throw new DefaultScenarioException("Unable to read certificate " + name, e);
        }
    }

}
