package com.ericsson.esc.bsf.main;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

import com.ericsson.esc.bsf.services.cm.adp.service.ConfigurationsImpl;

@ApplicationPath("/cm")
public class RestEasyApp extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new LinkedHashSet<Class<?>>();
        resources.add(ConfigurationsImpl.class);
        return resources;
    }

}
