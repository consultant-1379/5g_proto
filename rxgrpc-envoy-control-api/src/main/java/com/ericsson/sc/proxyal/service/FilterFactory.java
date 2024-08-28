package com.ericsson.sc.proxyal.service;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterFactory
{

    private static final Logger log = LoggerFactory.getLogger(FilterFactory.class);

    private static final String LUA_TEMPLATE_FILE = "lua_filter.vm";

    /**
     * Return the LUA filter for CSA
     * 
     * @param ownPort
     * @param ownIp
     * @return the text for a LUA filter processing requests and replies
     */
    public static String getCsaLuaFilter(Map<String, Map<String, String>> contextData)
    {
        var context = new VelocityContext();
        var ve = new VelocityEngine();

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        ve.setProperty("runtime.strict_mode.enable", true);
        ve.init();

        context.put("config", contextData);

        var template = ve.getTemplate(LUA_TEMPLATE_FILE, "UTF-8");
        var writer = new StringWriter();
        template.merge(context, writer);

        log.debug("generated LUA filter:\n {}\n", writer.toString());
        return writer.toString();
    }

    /**
     * Return the LUA filter for CSA
     * 
     * @param ownPort
     * @param ownIp
     * @return the text for a LUA filter processing requests and replies
     */
    public static String getCsaLuaFilter(final String ownIp,
                                         final String ownPort,
                                         final String ownTlsPort,
                                         final String convergedChargingApiRoot,
                                         final String convergedChargingApiName,
                                         final String convergedChargingApiVersion,
                                         final String convergedChargingApiSRUP,
                                         final String spendingLimitApiRoot,
                                         final String spendingLimitApiName,
                                         final String spendingLimitApiVersion,
                                         final String spendingLimitApiSRUP)
    {
        var context = new VelocityContext();
        var ve = new VelocityEngine();

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        ve.setProperty("runtime.strict_mode.enable", true);
        ve.init();

        addDataToContext(context, "ownIp", ownIp);
        addDataToContext(context, "ownPort", ownPort);
        addDataToContext(context, "ownTlsPort", ownTlsPort);
        addDataToContext(context, "ccapiroot", convergedChargingApiRoot);
        addDataToContext(context, "ccapiname", convergedChargingApiName);
        addDataToContext(context, "ccapiversion", convergedChargingApiVersion);
        addDataToContext(context, "ccapisrup", convergedChargingApiSRUP);
        addDataToContext(context, "slapiroot", spendingLimitApiRoot);
        addDataToContext(context, "slapiname", spendingLimitApiName);
        addDataToContext(context, "slapiversion", spendingLimitApiVersion);
        addDataToContext(context, "slapisrup", spendingLimitApiSRUP);
        addDataToContext(context, "nl", "\n");

        var template = ve.getTemplate(LUA_TEMPLATE_FILE, "UTF-8");
        var writer = new StringWriter();
        template.merge(context, writer);

        log.debug("generated LUA filter:\n {}\n", writer);
        return writer.toString();
    }

    /**
     * Add a key/value pair to the context. In the template, you can use $key to get
     * the value.
     * 
     * @param context
     * @param key
     * @param value
     */
    static void addDataToContext(VelocityContext context,
                                 final String key,
                                 final String value)
    {
        context.put(key, value);
    }

    /**
     * @param routingContext
     * @return
     */
    public static String getCsaLuaFilterString(Map<String, Object> routingContext)
    {
        var context = new VelocityContext();
        var ve = new VelocityEngine();

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        ve.setProperty("runtime.strict_mode.enable", true);
        ve.init();

        for (var configObject : routingContext.entrySet())
        {
            context.put(configObject.getKey(), configObject.getValue());
        }

        var template = ve.getTemplate(LUA_TEMPLATE_FILE, "UTF-8");
        var writer = new StringWriter();
        template.merge(context, writer);

        log.debug("generated LUA filter:\n {}\n", writer.toString());
        return writer.toString();
    }

}
