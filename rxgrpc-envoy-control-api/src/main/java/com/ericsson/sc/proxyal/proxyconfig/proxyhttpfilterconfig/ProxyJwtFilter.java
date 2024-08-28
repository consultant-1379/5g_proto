/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 3, 2020
 *     Author: epaxale
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.core.v3.DataSource;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtRequirement;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.RequirementRule;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;

/**
 * 
 */
public class ProxyJwtFilter extends ProxyFilter implements IfHttpFilter
{
    private static final String FILTER_NAME = "envoy.filters.http.jwt_authn";

    private Set<ProxyJwtProvider> providers = new HashSet<>();
    private Set<ProxyJwtRule> rules = new HashSet<>();

    public ProxyJwtFilter()
    {
        super(FILTER_NAME);
    }

    public ProxyJwtFilter(ProxyJwtFilter jwtFilter)
    {
        super(FILTER_NAME);
        jwtFilter.getProviders().forEach(provider -> this.providers.add(provider));
        jwtFilter.getRules().forEach(rule -> this.rules.add(rule));
    }

    /**
     * @return the providers
     */
    public Set<ProxyJwtProvider> getProviders()
    {
        return providers;
    }

    /**
     * @param providers the providers to set
     */
    public void setProviders(Set<ProxyJwtProvider> providers)
    {
        this.providers = providers;
    }

    /**
     * @return the rules
     */
    public Set<ProxyJwtRule> getRules()
    {
        return rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(Set<ProxyJwtRule> rules)
    {
        this.rules = rules;
    }

    public void addProvider(ProxyJwtProvider provider)
    {
        providers.add(provider);
    }

    public void addRule(ProxyJwtRule rule)
    {
        rules.add(rule);
    }

    @Override
    public String toString()
    {
        return "\nName: " + this.getName() + "\nProviders: " + providers.toString() + "\nRules: " + rules.toString();
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((providers == null) ? 0 : providers.hashCode());
        result = prime * result + ((rules == null) ? 0 : rules.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof ProxyJwtFilter))
        {
            return false;
        }

        var other = (ProxyJwtFilter) o;

        var providersEquals = (this.providers == null && other.providers == null) || (this.providers != null && this.providers.equals(other.providers));
        var rulesEquals = (this.rules == null && other.rules == null) || (this.rules != null && this.rules.equals(other.rules));

        return rulesEquals && providersEquals;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#appendConfig(
     * com.ericsson.sc.proxyal.proxyconfig.ProxyListener,
     * io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.
     * HttpConnectionManager.Builder)
     */
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        var jwtBuilder = JwtAuthentication.newBuilder();

        this.getRules()
            .forEach(match -> jwtBuilder.addRules(RequirementRule.newBuilder()
                                                                 .setMatch(RouteMatch.newBuilder()
                                                                                     .setSafeRegex(RegexMatcher.newBuilder().setRegex(match.getRegexp()))
                                                                                     .build())
                                                                 .setRequires(JwtRequirement.newBuilder().setProviderName(match.getProviderName()).build())
                                                                 .build()));

        this.getProviders()
            .forEach(provider -> jwtBuilder.putProviders(provider.getName(),
                                                         JwtProvider.newBuilder()
                                                                    .addAllAudiences(provider.getAudiences())
                                                                    .setLocalJwks(DataSource.newBuilder().setInlineString(provider.getPublicKeys()).build())
                                                                    .setPayloadInMetadata(provider.getPayloadInMetadataName())
                                                                    .build()));

        // JWT filter should be always first
        builder.addHttpFilters(0, HttpFilter.newBuilder().setName(this.getName()).setTypedConfig(Any.pack(jwtBuilder.build())));

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#getPriority()
     */
    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.JWT_AUTH;
    }

}
