package com.ericsson.esc.bsf.services.cm.adp.service;

import com.ericsson.esc.bsf.services.cm.adp.model.Message;
import com.ericsson.esc.bsf.services.cm.adp.model.Schema;
import com.ericsson.esc.bsf.services.cm.adp.model.SchemaGet;
import com.ericsson.esc.bsf.services.cm.adp.model.SchemaUpdate;
import com.ericsson.esc.bsf.services.cm.adp.model.SchemasList;
import com.ericsson.esc.bsf.services.cm.adp.utils.ResponseDelegate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/schemas")
public interface ConfigurationSchemas {
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  PostSchemasResponse postSchemas(Schema entity);

  @POST
  @Produces("application/json")
  @Consumes("multipart/form-data")
  PostSchemasResponse postSchemas();

  @GET
  @Produces("application/json")
  GetSchemasResponse getSchemas();

  @PUT
  @Path("/{name}")
  @Produces("application/json")
  @Consumes("application/json")
  PutSchemasByNameResponse putSchemasByName(@PathParam("name") String name, SchemaUpdate entity);

  @PUT
  @Path("/{name}")
  @Produces("application/json")
  @Consumes("multipart/form-data")
  PutSchemasByNameResponse putSchemasByName(@PathParam("name") String name);

  @DELETE
  @Path("/{name}")
  @Produces("application/json")
  DeleteSchemasByNameResponse deleteSchemasByName(@PathParam("name") String name);

  @GET
  @Path("/{name}")
  @Produces("application/json")
  GetSchemasByNameResponse getSchemasByName(@PathParam("name") String name);

  class PostSchemasResponse extends ResponseDelegate {
    private PostSchemasResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PostSchemasResponse(Response response) {
      super(response);
    }

    public static PostSchemasResponse respond201WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSchemasResponse(responseBuilder.build(), entity);
    }

    public static PostSchemasResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSchemasResponse(responseBuilder.build(), entity);
    }

    public static PostSchemasResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSchemasResponse(responseBuilder.build(), entity);
    }
  }

  class GetSchemasResponse extends ResponseDelegate {
    private GetSchemasResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetSchemasResponse(Response response) {
      super(response);
    }

    public static GetSchemasResponse respond200WithApplicationJson(SchemasList entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSchemasResponse(responseBuilder.build(), entity);
    }
  }

  class GetSchemasByNameResponse extends ResponseDelegate {
    private GetSchemasByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetSchemasByNameResponse(Response response) {
      super(response);
    }

    public static GetSchemasByNameResponse respond200WithApplicationJson(SchemaGet entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static GetSchemasByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSchemasByNameResponse(responseBuilder.build(), entity);
    }
  }

  class PutSchemasByNameResponse extends ResponseDelegate {
    private PutSchemasByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PutSchemasByNameResponse(Response response) {
      super(response);
    }

    public static PutSchemasByNameResponse respond200WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static PutSchemasByNameResponse respond201WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static PutSchemasByNameResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static PutSchemasByNameResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSchemasByNameResponse(responseBuilder.build(), entity);
    }
  }

  class DeleteSchemasByNameResponse extends ResponseDelegate {
    private DeleteSchemasByNameResponse(Response response, Object entity) {
      super(response, entity);
    }

    private DeleteSchemasByNameResponse(Response response) {
      super(response);
    }

    public static DeleteSchemasByNameResponse respond200WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static DeleteSchemasByNameResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteSchemasByNameResponse(responseBuilder.build(), entity);
    }

    public static DeleteSchemasByNameResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteSchemasByNameResponse(responseBuilder.build(), entity);
    }
  }
}
