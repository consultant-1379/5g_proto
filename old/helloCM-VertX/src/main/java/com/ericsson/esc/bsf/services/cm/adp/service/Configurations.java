package com.ericsson.esc.bsf.services.cm.adp.service;

import com.ericsson.esc.bsf.services.cm.adp.model.Configuration;
import com.ericsson.esc.bsf.services.cm.adp.model.ConfigurationUpdate;
import com.ericsson.esc.bsf.services.cm.adp.model.ConfigurationsList;
import com.ericsson.esc.bsf.services.cm.adp.model.JsonPatch;
import com.ericsson.esc.bsf.services.cm.adp.model.Message;
import com.ericsson.esc.bsf.services.cm.adp.utils.PATCH;
import com.ericsson.esc.bsf.services.cm.adp.utils.ResponseDelegate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface Configurations {
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  PostConfigurationsResponse postConfigurations(Configuration entity);

  @GET
  @Produces("application/json")
  GetConfigurationsResponse getConfigurations();

  @PUT
  @Path("/{name}")
  @Produces("application/json")
  @Consumes("application/json")
  PutConfigurationsByNameResponse putConfigurationsByName(@PathParam("name") String name, ConfigurationUpdate entity);

  @DELETE
  @Path("/{name}")
  @Produces("application/json")
  DeleteConfigurationsByNameResponse deleteConfigurationsByName(@PathParam("name") String name);

  @PATCH
  @Path("/{name}")
  @Produces("application/json")
  @Consumes("application/json")
  PatchConfigurationsByNameResponse patchConfigurationsByName(@PathParam("name") String name, JsonPatch entity);

  @GET
  @Path("/{name}")
  @Produces("application/json")
  GetConfigurationsByNameResponse getConfigurationsByName(@PathParam("name") String name, @QueryParam("jsonpointer") String jsonpointer, @QueryParam("jsonpath") String jsonpath);

  class PostConfigurationsResponse extends ResponseDelegate {
    private PostConfigurationsResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PostConfigurationsResponse(Response response) {
      super(response);
    }

    public static HeadersFor201 headersFor201() {
      return new HeadersFor201();
    }

    public static PostConfigurationsResponse respond201WithApplicationJson(Message entity, HeadersFor201 headers) {
      Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      headers.toResponseBuilder(responseBuilder);
      return new PostConfigurationsResponse(responseBuilder.build(), entity);
    }

    public static PostConfigurationsResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostConfigurationsResponse(responseBuilder.build(), entity);
    }

    public static PostConfigurationsResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostConfigurationsResponse(responseBuilder.build(), entity);
    }

    public static PostConfigurationsResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostConfigurationsResponse(responseBuilder.build(), entity);
    }

    public static class HeadersFor201 extends HeaderBuilderBase {
      private HeadersFor201() {
      }

      public HeadersFor201 withETag(final String p) {
        headerMap.put("ETag", String.valueOf(p));;
        return this;
      }
    }
  }

  class GetConfigurationsResponse extends ResponseDelegate {
    private GetConfigurationsResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetConfigurationsResponse(Response response) {
      super(response);
    }

    public static GetConfigurationsResponse respond200WithApplicationJson(ConfigurationsList entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetConfigurationsResponse(responseBuilder.build(), entity);
    }
  }

  class PatchConfigurationsByNameResponse extends ResponseDelegate {
    private PatchConfigurationsByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PatchConfigurationsByNameResponse(Response response) {
      super(response);
    }

    public static HeadersFor200 headersFor200() {
      return new HeadersFor200();
    }

    public static PatchConfigurationsByNameResponse respond200WithApplicationJson(Message entity, HeadersFor200 headers) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      headers.toResponseBuilder(responseBuilder);
      return new PatchConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PatchConfigurationsByNameResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PatchConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PatchConfigurationsByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PatchConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PatchConfigurationsByNameResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PatchConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static class HeadersFor200 extends HeaderBuilderBase {
      private HeadersFor200() {
      }

      public HeadersFor200 withETag(final String p) {
        headerMap.put("ETag", String.valueOf(p));;
        return this;
      }
    }
  }

  class PutConfigurationsByNameResponse extends ResponseDelegate {
    private PutConfigurationsByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PutConfigurationsByNameResponse(Response response) {
      super(response);
    }

    public static HeadersFor200 headersFor200() {
      return new HeadersFor200();
    }

    public static PutConfigurationsByNameResponse respond200WithApplicationJson(Message entity, HeadersFor200 headers) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      headers.toResponseBuilder(responseBuilder);
      return new PutConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PutConfigurationsByNameResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PutConfigurationsByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static PutConfigurationsByNameResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static class HeadersFor200 extends HeaderBuilderBase {
      private HeadersFor200() {
      }

      public HeadersFor200 withETag(final String p) {
        headerMap.put("ETag", String.valueOf(p));;
        return this;
      }
    }
  }

  class DeleteConfigurationsByNameResponse extends ResponseDelegate {
    private DeleteConfigurationsByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private DeleteConfigurationsByNameResponse(Response response) {
      super(response);
    }

    public static DeleteConfigurationsByNameResponse respond200WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static DeleteConfigurationsByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteConfigurationsByNameResponse(responseBuilder.build(), entity);
    }
  }

  class GetConfigurationsByNameResponse extends ResponseDelegate {
    private GetConfigurationsByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetConfigurationsByNameResponse(Response response) {
      super(response);
    }

    public static HeadersFor200 headersFor200() {
      return new HeadersFor200();
    }

    public static GetConfigurationsByNameResponse respond200WithApplicationJson(Configuration entity, HeadersFor200 headers) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      headers.toResponseBuilder(responseBuilder);
      return new GetConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static GetConfigurationsByNameResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static GetConfigurationsByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetConfigurationsByNameResponse(responseBuilder.build(), entity);
    }

    public static class HeadersFor200 extends HeaderBuilderBase {
      private HeadersFor200() {
      }

      public HeadersFor200 withETag(final String p) {
        headerMap.put("ETag", String.valueOf(p));;
        return this;
      }
    }
  }
}
