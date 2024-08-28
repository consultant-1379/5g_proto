package com.ericsson.sc.sepp.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.adpal.cm.validator.RuleResult;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.OnNfProfileAbsence;
import com.ericsson.sc.validator.Rule;
import com.google.re2j.Pattern;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import io.reactivex.Single;

class Rule33
{
    private static final String RULE_NOT_APPLICABLE = "Rule is not applicable";

    private static Rule33 obj;

    private String regex = ".*'([^']*)'.*";

    private Rule33()
    {
    }

    Rule<EricssonSepp> getRule()
    {
        return config ->
        {
            var isApplicable = new AtomicBoolean(false);
            var ruleResult = new AtomicBoolean(true);
            var errMsg = new StringBuilder(100);
            HashMap<String, List<String>> subnetsIPv4 = new HashMap<>();
            HashMap<String, List<String>> subnetsIPv6 = new HashMap<>();
            if (config != null && config.getEricssonSeppSeppFunction() != null)
            {
                config.getEricssonSeppSeppFunction()
                      .getNfInstance()
                      .stream()
                      .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                      .flatMap(nf -> nf.getTopologyHiding().stream())
                      .filter(tph -> tph.getIpAddressHiding() != null && tph.getIpAddressHiding().getOnNfProfileAbsence() != null)
                      .forEach(tph ->
                      {
                          var tphExpr = tph.getCondition();
                          var onNfProfileAbsense = tph.getIpAddressHiding().getOnNfProfileAbsence();
                          String profileName = tph.getName();
                          // predicate expression is mandatory in yang

                          isApplicable.set(true);

                          Optional<String> nf = this.exportNf(tphExpr);
                          this.checkProfiles(nf, onNfProfileAbsense, profileName, ruleResult, errMsg, subnetsIPv4, subnetsIPv6);
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
        Rule33 other = (Rule33) obj;
        return Objects.equals(regex, other.regex);
    }

    private void checkProfiles(Optional<String> nf,
                               OnNfProfileAbsence onNfProfileAbsense,
                               String profileName,
                               AtomicBoolean ruleResult,
                               StringBuilder errMsg,
                               HashMap<String, List<String>> subnetsIPv4,
                               HashMap<String, List<String>> subnetsIPv6)
    {
        nf.ifPresentOrElse(nfName ->
        {
            onNfProfileAbsense.getRemoveIpv4AddressRange().forEach(ip ->
            {
                subnetsIPv4.forEach((key,
                                     value) ->
                {
                    if (!key.equals(nfName))
                    {
                        this.checkConflict(subnetsIPv4.get(key), ip, nfName, key, profileName, ruleResult, errMsg);
                    }
                });
                this.updateMap(subnetsIPv4, ip, nfName);
            });

            onNfProfileAbsense.getRemoveIpv6AddressRange().forEach(ip ->
            {
                subnetsIPv6.forEach((key,
                                     value) ->
                {
                    if (!key.equals(nfName))
                    {
                        this.checkConflict(subnetsIPv6.get(key), ip, nfName, key, profileName, ruleResult, errMsg);
                    }
                });
                this.updateMap(subnetsIPv6, ip, nfName);
            });
        }, () ->
        {
            ruleResult.set(false);
            errMsg.append("The value of the target-nf-type within the condition should be enclosed by single quotes.");
        });

    }

    private void updateMap(HashMap<String, List<String>> subnets,
                           String ip,
                           String nfName)
    {
        if (subnets.containsKey(nfName))
            subnets.get(nfName).add(ip);
        else
        {
            List<String> tmp = new ArrayList<>();
            tmp.add(ip);
            subnets.put(nfName, tmp);
        }
    }

    private void checkConflict(List<String> list,
                               String ip,
                               String nfName,
                               String nfName2,
                               String profileName,
                               AtomicBoolean ruleResult,
                               StringBuilder errMsg)
    {
        list.stream().forEach(subnet ->
        {
            IPAddress addressa = new IPAddressString(subnet).getAddress();
            IPAddress addressb = new IPAddressString(ip).getAddress();
            if (addressb.prefixContains(addressa) || addressa.prefixContains(addressb))
            {
                ruleResult.set(false);
                errMsg.append("\nThe ip " + ip + " of the topology hiding profile " + profileName + " that belongs to nf " + nfName + " overlaps with the ip "
                              + subnet + " that belong to nf " + nfName2);
            }
        });

    }

    static Rule33 get()
    {
        if (Rule33.obj == null)
            Rule33.obj = new Rule33();
        return Rule33.obj;
    }

    private Optional<String> exportNf(String expression)
    {
        var pattern = Pattern.compile(this.regex);
        var matcher = pattern.matcher(expression);
        if (matcher.find())
        {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }
}
