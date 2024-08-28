/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 15, 2024
 *     Author: zpalele
 */

package com.ericsson.sc.sepp.validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.adpal.cm.validator.RuleResult;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.validator.Rule;
import io.reactivex.Single;

public class Rule49
{
    private static final String RULE_NOT_APPLICABLE = "Rule is not applicable";

    private static final String PATH_TO_ASYM = "/seppmanager/certificates/asymmetricKeyCert";
    private static final String PATH_TO_CA = "/seppmanager/certificates/ca";

    private static final String OWN_CERT = "Own network certificate: ";
    private static final String RP_CERT = "Roaming Partner certificate: ";
    private static final String ASYM_PRIV_KEY = "Asymmetric Key: ";
    private static final String ASYM_KEY_CA = "Asymmetric Key's CA: ";

    private static final String INVALID_CERTIFICATE = " is not valid.\n";

    private static Rule49 obj;

    private String regex = ".*'([^']*)'.*";

    private Rule49()
    {
    }

    private List<String> readAsymKey() throws IOException
    {
        return Files.readAllLines(Path.of(PATH_TO_ASYM));
    }

    private List<String> readCAList() throws IOException
    {
        return Files.readAllLines(Path.of(PATH_TO_CA));
    }

    public Optional<String> getByName(final List<String> readasymmetrickey,
                                      final String name)
    {
        var index = readasymmetrickey.indexOf(name);
        if (index >= 0)
            return Optional.ofNullable(readasymmetrickey.get(index));
        else
            return Optional.ofNullable(null);
    }

    private void validateCertificate(List<String> path,
                                     String certificateName,
                                     AtomicBoolean isApplicable,
                                     AtomicBoolean ruleResult,
                                     StringBuilder errMsg,
                                     String whichCertificate)
    {
        if (certificateName == null)
            ruleResult.set(true);
        else
        {
            var certificate = this.getByName(path, certificateName);
            if (certificate.isPresent())
                isApplicable.set(true);
            else
            {
                ruleResult.set(false);
                errMsg.append(whichCertificate).append(certificateName).append(INVALID_CERTIFICATE);
            }
        }
    }

    private void validateOwnNetworkCertificate(NfInstance nfInstance,
                                               AtomicBoolean ruleResult,
                                               AtomicBoolean isApplicable,
                                               StringBuilder errMsg)
    {
        nfInstance.getOwnNetwork().forEach(ownNetwork ->
        {
            try
            {
                validateCertificate(readCAList(), ownNetwork.getTrustedCertificateList(), isApplicable, ruleResult, errMsg, OWN_CERT);
            }
            catch (IOException e)
            {
                ruleResult.set(false);
                errMsg.append(e.getMessage());
            }
        });
    }

    private void validateRoamingPartnerCertificate(NfInstance nfInstance,
                                                   AtomicBoolean ruleResult,
                                                   AtomicBoolean isApplicable,
                                                   StringBuilder errMsg)
    {
        nfInstance.getExternalNetwork().forEach(extNetwork -> extNetwork.getRoamingPartner().forEach(roamingPartner ->
        {
            try
            {
                validateCertificate(readCAList(), roamingPartner.getTrustedCertificateList(), isApplicable, ruleResult, errMsg, RP_CERT);
            }
            catch (IOException e)
            {
                ruleResult.set(false);
                errMsg.append(e.getMessage());
            }
        }));
    }

    private void validateAsymKeyPrivateKey(NfInstance nfInstance,
                                           AtomicBoolean ruleResult,
                                           AtomicBoolean isApplicable,
                                           StringBuilder errMsg)
    {
        nfInstance.getAsymmetricKey().forEach(asymKey ->
        {
            try
            {
                validateCertificate(readAsymKey(), asymKey.getPrivateKey(), isApplicable, ruleResult, errMsg, ASYM_PRIV_KEY);
            }
            catch (IOException e)
            {
                ruleResult.set(false);
                errMsg.append(e.getMessage());
            }
        });
    }

    private void validateAsymKeyCertificate(NfInstance nfInstance,
                                            AtomicBoolean ruleResult,
                                            AtomicBoolean isApplicable,
                                            StringBuilder errMsg)
    {
        nfInstance.getAsymmetricKey().forEach(asymKey ->
        {
            try
            {
                validateCertificate(readAsymKey(), asymKey.getCertificate(), isApplicable, ruleResult, errMsg, ASYM_KEY_CA);
            }
            catch (IOException e)
            {
                ruleResult.set(false);
                errMsg.append(e.getMessage());
            }
        });
    }

    Rule<EricssonSepp> getRule()
    {
        return config ->
        {
            var isApplicable = new AtomicBoolean(false);
            var ruleResult = new AtomicBoolean(true);
            var errMsg = new StringBuilder(100);

            if (config != null && config.getEricssonSeppSeppFunction() != null)
            {
                config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                {
                    // Check if trusted certificate list used in own network is valid
                    validateOwnNetworkCertificate(nfInstance, ruleResult, isApplicable, errMsg);

                    // Check if trusted certificate list for each RP is valid
                    validateRoamingPartnerCertificate(nfInstance, ruleResult, isApplicable, errMsg);

                    if (nfInstance.getAsymmetricKey().isEmpty())
                        ruleResult.set(true);
                    else
                    {
                        // Check if asymmetric key private key used in own and external networks are
                        // valid
                        validateAsymKeyPrivateKey(nfInstance, ruleResult, isApplicable, errMsg);

                        // Check if asymmetric key certificate used in own and external networks are
                        // valid
                        validateAsymKeyCertificate(nfInstance, ruleResult, isApplicable, errMsg);
                    }
                });
            }

            if (isApplicable.get())
                return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

            return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
        };
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(regex);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rule49 other = (Rule49) obj;
        return Objects.equals(regex, other.regex);
    }

    static Rule49 get()
    {
        if (Rule49.obj == null)
            Rule49.obj = new Rule49();
        return Rule49.obj;
    }

}
