package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple container class. Holds data for matching an incoming request. The data
 * is used to select a traffic route.
 */
public class ProxyRouteMatch
{
    private Optional<String> prefix = Optional.empty();
    private Optional<String> path = Optional.empty();
    private Optional<String> regexp = Optional.empty();
    private Optional<Map<String, Boolean>> presentValueHeaders = Optional.empty();
    private Optional<Map<String, String>> exactMatchValueHeaders = Optional.empty();
    private Optional<Map<String, String>> regexValueHeaders = Optional.empty();

    public static final Logger log = LoggerFactory.getLogger(ProxyRouteMatch.class);

    /**
     * Add all headers that need to be matched against their existence or not.
     * 
     * @param presentValueHeaders
     * @return
     */
    public ProxyRouteMatch addAllPresentValueHeaders(Map<String, Boolean> presentValueHeaders)
    {
        if (this.presentValueHeaders.isEmpty())
        {
            this.presentValueHeaders = Optional.of(new HashMap<>());
        }

        this.presentValueHeaders.get().putAll(presentValueHeaders);
        return this;
    }

    /**
     * Add all headers that need to have an exact value
     * 
     * @param exactMatchValueHeaders
     * @return
     */
    public ProxyRouteMatch addAllExactMatchValueHeaders(Map<String, String> exactMatchValueHeaders)
    {
        if (this.exactMatchValueHeaders.isEmpty())
        {
            this.exactMatchValueHeaders = Optional.of(new HashMap<>());
        }

        this.exactMatchValueHeaders.get().putAll(exactMatchValueHeaders);
        return this;
    }

    /**
     * Add all headers that need to be checked against a regex
     * 
     * @param regexValueHeaders
     * @return
     */
    public ProxyRouteMatch addAllRegexValueHeaders(Map<String, String> regexValueHeaders)
    {
        if (this.regexValueHeaders.isEmpty())
        {
            this.regexValueHeaders = Optional.of(new HashMap<>());
        }

        this.regexValueHeaders.get().putAll(regexValueHeaders);
        return this;
    }

    public ProxyRouteMatch addPresentValueHeader(String name,
                                                 Boolean value)
    {
        if (this.presentValueHeaders.isEmpty())
        {
            this.presentValueHeaders = Optional.of(new HashMap<>());
        }

        this.presentValueHeaders.get().put(name, value);
        return this;
    }

    public ProxyRouteMatch addExactMatchValueHeader(String name,
                                                    String value)
    {
        if (this.exactMatchValueHeaders.isEmpty())
        {
            this.exactMatchValueHeaders = Optional.of(new HashMap<>());
        }

        this.exactMatchValueHeaders.get().put(name, value);
        return this;
    }

    public ProxyRouteMatch addRegexValueHeader(String name,
                                               String value)
    {
        if (this.regexValueHeaders.isEmpty())
        {
            this.regexValueHeaders = Optional.of(new HashMap<>());
        }

        this.regexValueHeaders.get().put(name, value);
        return this;
    }

    /**
     * @param path the path to set. path and regexp will take empty values
     * @return
     */
    public ProxyRouteMatch setPrefix(String prefix)
    {
        this.prefix = Optional.of(prefix);
        this.path = Optional.empty(); // make sure that there's only one of the three
        this.regexp = Optional.empty();
        return this;
    }

    /**
     * @param path the path to set. Prefix and regexp will take empty values
     * @return
     */
    public ProxyRouteMatch setPath(String path)
    {
        this.path = Optional.of(path);
        this.prefix = Optional.empty(); // make sure that there's only one of the three
        this.regexp = Optional.empty();
        return this;
    }

    /**
     * @param regexp the regexp to set. Prefix and Path will take empty values
     * @return
     */
    public ProxyRouteMatch setRegexp(String regexp)
    {
        this.regexp = Optional.of(regexp);
        this.prefix = Optional.empty(); // make sure that there's only one of the three
        this.path = Optional.empty();
        return this;
    }

    /**
     * Copy-constructor (shallow copy, but since all members are strings which are
     * immutable in Java it's ok.
     * 
     * @param a The object to copy from.
     */
    public ProxyRouteMatch(ProxyRouteMatch a)
    {
        this.prefix = a.prefix;
        this.path = a.path;
        this.regexp = a.regexp;
        a.exactMatchValueHeaders.ifPresent(item ->
        {
            var tempMap = new HashMap<String, String>();
            item.forEach(tempMap::put);
            this.exactMatchValueHeaders = Optional.of(tempMap);
        });
        a.presentValueHeaders.ifPresent(item ->
        {
            var tempMap = new HashMap<String, Boolean>();
            item.forEach(tempMap::put);
            this.presentValueHeaders = Optional.of(tempMap);
        });
        a.regexValueHeaders.ifPresent(item ->
        {
            var tempMap = new HashMap<String, String>();
            item.forEach(tempMap::put);
            this.regexValueHeaders = Optional.of(tempMap);
        });
    }

    /**
     * 
     */
    public ProxyRouteMatch()
    {
    }

    public Optional<String> getPrefix()
    {
        return prefix;
    }

    public Optional<String> getPath()
    {
        return path;
    }

    public Optional<String> getRegexp()
    {
        return regexp;
    }

    /**
     * @return the presentValueHeaders
     */
    public Optional<Map<String, Boolean>> getPresentValueHeaders()
    {
        return presentValueHeaders;
    }

    /**
     * @return the exactMatchValueHeaders
     */
    public Optional<Map<String, String>> getExactMatchValueHeaders()
    {
        return exactMatchValueHeaders;
    }

    /**
     * @return the regexValueHeaders
     */
    public Optional<Map<String, String>> getRegexValueHeaders()
    {
        return regexValueHeaders;
    }

    @Override
    public String toString()
    {
        var result = new StringBuilder();

        // One of (prefix, path, regex):
        this.prefix.ifPresent(p -> result.append("prefix=" + p));
        this.path.ifPresent(p -> result.append("exact-path=" + p));
        this.regexp.ifPresent(p -> result.append("regexp=" + p));

        this.exactMatchValueHeaders.ifPresent(h -> result.append(" exact-match-headers: " + h));
        this.presentValueHeaders.ifPresent(h -> result.append(" present-value-headers: " + h));
        this.regexValueHeaders.ifPresent(h -> result.append(" regex-value-headers: " + h));

        var str = result.toString();
        // Error if nothing is defined:
        if (str.isEmpty())
        {
            str = "ERROR[ProxyRouteMatch:toString()]";
        }

        return str;
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((this.exactMatchValueHeaders.isEmpty()) ? 0 : this.exactMatchValueHeaders.hashCode());
        result = prime * result + ((this.presentValueHeaders.isEmpty()) ? 0 : this.presentValueHeaders.hashCode());
        result = prime * result + ((this.regexValueHeaders.isEmpty()) ? 0 : this.regexValueHeaders.hashCode());
        result = prime * result + ((this.path.isEmpty()) ? 0 : path.hashCode());
        result = prime * result + ((this.prefix.isEmpty()) ? 0 : prefix.hashCode());
        result = prime * result + ((this.regexp.isEmpty()) ? 0 : regexp.hashCode());
        return result;
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
        ProxyRouteMatch other = (ProxyRouteMatch) obj;

        return this.exactMatchValueHeaders.equals(other.exactMatchValueHeaders) //
               && this.presentValueHeaders.equals(other.presentValueHeaders) //
               && this.regexValueHeaders.equals(other.regexValueHeaders) && this.path.equals(other.path) //
               && this.prefix.equals(other.prefix) //
               && this.regexp.equals(other.regexp);
    }
}
