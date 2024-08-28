package com.ericsson.esc.bsf.services.cm.adp.service;

import com.ericsson.esc.bsf.services.cm.adp.model.Configuration;
import com.ericsson.esc.bsf.services.cm.adp.model.ConfigurationUpdate;
import com.ericsson.esc.bsf.services.cm.adp.model.ConfigurationsList;
import com.ericsson.esc.bsf.services.cm.adp.model.JsonPatch;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/configurations")
public class ConfigurationsImpl implements Configurations{

  @Override
  public PostConfigurationsResponse postConfigurations(Configuration entity){
    return null;
  }

  @Override
  public GetConfigurationsResponse getConfigurations(){
    return GetConfigurationsResponse.respond200WithApplicationJson(new ConfigurationsList());
  }

  @Override
  public PutConfigurationsByNameResponse putConfigurationsByName(@PathParam("name") String name, ConfigurationUpdate entity){
    return null;
  }

  @Override
  public DeleteConfigurationsByNameResponse deleteConfigurationsByName(@PathParam("name") String name){
    return null;
  }

  @Override
  public PatchConfigurationsByNameResponse patchConfigurationsByName(@PathParam("name") String name, JsonPatch entity){
    return null;
  }

  @Override
  public GetConfigurationsByNameResponse getConfigurationsByName(@PathParam("name") String name, @QueryParam("jsonpointer") String jsonpointer, @QueryParam("jsonpath") String jsonpath){
    return null;
  }
}
