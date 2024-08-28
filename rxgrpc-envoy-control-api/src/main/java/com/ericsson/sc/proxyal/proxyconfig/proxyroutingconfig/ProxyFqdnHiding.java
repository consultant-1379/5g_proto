package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterCase;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.EncryptionProfile;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHidingServiceProfile;

public class ProxyFqdnHiding implements IfProxyTopologyHiding
{

    private Map<Selector, Locator> customFqdnLocator = new HashMap<>();
    private List<ProxyScreeningRule> onFqdnUnsuccessfullAction = new ArrayList<>();
    private Optional<List<ProxyEncryptionProfile>> encryptionProfiles = Optional.empty();
    private Optional<String> activeKey = Optional.empty();
    // Before encryption identifier add a hard-coded version of our
    // scrambling algorithm. For SC1.13 the version is 'A'.
    private static final String VERSION_ENC_ALGORITHM = "A";

    public ProxyFqdnHiding()
    {

    }

    public ProxyFqdnHiding(ProxyFqdnHiding pfs)
    {
        this.customFqdnLocator = new HashMap<>(pfs.customFqdnLocator);
        this.onFqdnUnsuccessfullAction = pfs.getOnFqdnUnsuccessfullAction();
        pfs.getActiveKey().ifPresent(this::setActiveKey);
        pfs.getEncryptionProfiles().ifPresent(enProfiles ->
        {
            this.encryptionProfiles = Optional.of(new ArrayList<ProxyEncryptionProfile>());
            enProfiles.forEach(ep -> this.encryptionProfiles.get().add(ep));
        });
    }

    public Optional<List<ProxyEncryptionProfile>> getEncryptionProfiles()
    {
        return encryptionProfiles;
    }

    public void setEncryptionProfiles(Optional<List<ProxyEncryptionProfile>> encryProf)
    {
        this.encryptionProfiles = encryProf;
    }

    /**
     * @return the activeKey
     */
    public Optional<String> getActiveKey()
    {
        return activeKey;
    }

    /**
     * @param activeKey the activeKey to set
     */
    public void setActiveKey(String activeKey)
    {
        this.activeKey = Optional.ofNullable(VERSION_ENC_ALGORITHM + activeKey);
    }

    public void putToMapCustomFqdnLocator(Selector selector,
                                          Locator locator)
    {
        if (customFqdnLocator.containsKey(selector))
        {
            mergeLocators(selector, customFqdnLocator.get(selector), locator);
        }
        else
        {
            customFqdnLocator.put(selector, locator);
        }

    }

    public void addUnsuccessfullAction(ProxyScreeningRule unsuccsessfullAction)
    {
        this.onFqdnUnsuccessfullAction.add(unsuccsessfullAction);
    }

    public List<ProxyScreeningRule> getOnFqdnUnsuccessfullAction()
    {
        return this.onFqdnUnsuccessfullAction;
    }

    public Builder initBuilder()
    {
        var thBuilder = TopologyHiding.newBuilder();
        return initBuilder(thBuilder);
    }

    @Override
    public String toString()
    {
        return "ProxyFqdnHiding [unsuccessfulAction=" + onFqdnUnsuccessfullAction + ", activeKey=" + activeKey + "]";
    }

    @Override
    public IfProxyTopologyHiding clone()
    {
        return new ProxyFqdnHiding(this);
    }

    @Override
    public Builder initBuilder(Builder thBuilder)
    {
        buildEncryptionProfile(thBuilder);
        buildActiveKeyId(thBuilder);
        buildServiceProfile(thBuilder);

        return thBuilder;
    }

    private Builder buildActiveKeyId(Builder thBuilder)
    {
        this.getActiveKey().ifPresent(thBuilder::setActiveEncryptionIdentifier);

        return thBuilder;
    }

    private Builder buildEncryptionProfile(Builder thBuilder)
    {

        this.getEncryptionProfiles()
            .ifPresent(eps -> eps.forEach(ep -> thBuilder.addEncryptionProfiles(EncryptionProfile.newBuilder()
                                                                                                 .setEncryptionIdentifier(VERSION_ENC_ALGORITHM
                                                                                                                          + ep.getEncryptionIdentifier())
                                                                                                 .setInitialVector(ep.getInitialVector())
                                                                                                 .setScramblingKey(ep.getScramblingKey()))));
        return thBuilder;
    }

    private Builder buildServiceProfile(Builder thBuilder)
    {
        var fqdnHidingProfile = TopologyHidingServiceProfile.newBuilder();

        if (onFqdnUnsuccessfullAction != null && !onFqdnUnsuccessfullAction.isEmpty())
        {
            onFqdnUnsuccessfullAction.stream().forEach(unSuccAction ->
            {
                var unsuccessfullFilterCase = FilterCase.newBuilder();

                unsuccessfullFilterCase.addFilterRules(unSuccAction.initBuilder()).setName(unSuccAction.getName());

                fqdnHidingProfile.addUnsuccessfulOperationFilterCases(unsuccessfullFilterCase);
            });
        }

        this.customFqdnLocator.forEach((selector,
                                        locator) ->
        {
            var thFqdnHide = TopologyHidingServiceProfile.ServiceCase.newBuilder();
            var thFqdnUnHide = TopologyHidingServiceProfile.ServiceCase.newBuilder();

            var scBuilder = TopologyHidingServiceProfile.ServiceContext.newBuilder();
            // key.getIsResponse is used because we can have the same selector for response
            // and request
            // The name would be SelectorName+Scrambling/DescramblingList+0/1
            var filterCaseHide = FilterCase.newBuilder().setName(selector.getName() + "FqdnHidingList" + selector.getIsResponse().toString());
            var filterCaseUnHide = FilterCase.newBuilder().setName(selector.getName() + "FqdnUnHidingList" + selector.getIsResponse().toString());

            locator.getHidingProxyScreeningRules().forEach(hidingRule -> filterCaseHide.addFilterRules(hidingRule.initBuilder()));
            locator.getUnHidingProxyScreeningRules().forEach(unHidingRule -> filterCaseUnHide.addFilterRules(unHidingRule.initBuilder()));

            locator.getMessageData().forEach(data ->
            {
                filterCaseHide.addFilterData(data.initBuilder());
                filterCaseUnHide.addFilterData(data.initBuilder());
            });

            selector.getServiceVersion().ifPresent(scBuilder::setApiVersion);
            selector.getResource().ifPresent(scBuilder::setResourceMatcher);
            selector.getServiceName().ifPresent(scBuilder::setApiName);
            selector.getHttpMethod().ifPresent(scBuilder::setHttpMethod);
            scBuilder.setDirectionValue(selector.getIsResponse()).setIsNotification(selector.getNotificationMessage());

            if (!filterCaseHide.getFilterRulesList().isEmpty())
            {
                thFqdnHide.setFilterCase(filterCaseHide.build());
                thFqdnHide.setServiceType(scBuilder.build());
                thFqdnHide.setServiceCaseName(selector.getName());
                fqdnHidingProfile.addTopologyHidingServiceCases(thFqdnHide.build());
            }

            if (!filterCaseUnHide.getFilterRulesList().isEmpty())
            {
                thFqdnUnHide.setFilterCase(filterCaseUnHide.build());
                thFqdnUnHide.setServiceType(scBuilder.build());
                thFqdnUnHide.setServiceCaseName(selector.getName());
                fqdnHidingProfile.addTopologyUnhidingServiceCases(thFqdnUnHide.build());
            }
        });

        thBuilder.mergeServiceProfile(fqdnHidingProfile.build());

        return thBuilder;
    }

    private void mergeLocators(Selector s,
                               Locator loc1,
                               Locator loc2)
    {
        loc1.getHidingProxyScreeningRules().addAll(loc2.getHidingProxyScreeningRules());
        loc1.getUnHidingProxyScreeningRules().addAll(loc2.getUnHidingProxyScreeningRules());
        customFqdnLocator.put(s, loc1);
    }
}
