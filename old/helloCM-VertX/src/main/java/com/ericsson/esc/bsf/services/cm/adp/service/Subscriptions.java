package com.ericsson.esc.bsf.services.cm.adp.service;

import com.ericsson.esc.bsf.services.cm.adp.model.Message;
import com.ericsson.esc.bsf.services.cm.adp.model.Subscription;
import com.ericsson.esc.bsf.services.cm.adp.model.SubscriptionGet;
import com.ericsson.esc.bsf.services.cm.adp.model.SubscriptionUpdate;
import com.ericsson.esc.bsf.services.cm.adp.model.SubscriptionsList;
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

@Path("/subscriptions")
public interface Subscriptions {
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  PostSubscriptionsResponse postSubscriptions(Subscription entity);

  @GET
  @Produces("application/json")
  GetSubscriptionsResponse getSubscriptions();

  @PUT
  @Path("/{id}")
  @Produces("application/json")
  @Consumes("application/json")
  PutSubscriptionsByIdResponse putSubscriptionsById(@PathParam("id") String id, SubscriptionUpdate entity);

  @DELETE
  @Path("/{id}")
  @Produces("application/json")
  DeleteSubscriptionsByIdResponse deleteSubscriptionsById(@PathParam("id") String id);

  @GET
  @Path("/{id}")
  @Produces("application/json")
  GetSubscriptionsByIdResponse getSubscriptionsById(@PathParam("id") String id);

  class GetSubscriptionsResponse extends ResponseDelegate {
    private GetSubscriptionsResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetSubscriptionsResponse(Response response) {
      super(response);
    }

    public static GetSubscriptionsResponse respond200WithApplicationJson(SubscriptionsList entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSubscriptionsResponse(responseBuilder.build(), entity);
    }
  }

  class PostSubscriptionsResponse extends ResponseDelegate {
    private PostSubscriptionsResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PostSubscriptionsResponse(Response response) {
      super(response);
    }

    public static PostSubscriptionsResponse respond201WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSubscriptionsResponse(responseBuilder.build(), entity);
    }

    public static PostSubscriptionsResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSubscriptionsResponse(responseBuilder.build(), entity);
    }

    public static PostSubscriptionsResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSubscriptionsResponse(responseBuilder.build(), entity);
    }

    public static PostSubscriptionsResponse respond409WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PostSubscriptionsResponse(responseBuilder.build(), entity);
    }
  }

  class PutSubscriptionsByIdResponse extends ResponseDelegate {
    private PutSubscriptionsByIdResponse(Response response, Object entity) {
      super(response, entity);
    }

    private PutSubscriptionsByIdResponse(Response response) {
      super(response);
    }

    public static PutSubscriptionsByIdResponse respond200WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }

    public static PutSubscriptionsByIdResponse respond400WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }

    public static PutSubscriptionsByIdResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new PutSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }
  }

  class DeleteSubscriptionsByIdResponse extends ResponseDelegate {
    private DeleteSubscriptionsByIdResponse(Response response, Object entity) {
      super(response, entity);
    }

    private DeleteSubscriptionsByIdResponse(Response response) {
      super(response);
    }

    public static DeleteSubscriptionsByIdResponse respond200WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }

    public static DeleteSubscriptionsByIdResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new DeleteSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }
  }

  class GetSubscriptionsByIdResponse extends ResponseDelegate {
    private GetSubscriptionsByIdResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetSubscriptionsByIdResponse(Response response) {
      super(response);
    }

    public static GetSubscriptionsByIdResponse respond200WithApplicationJson(SubscriptionGet entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }

    public static GetSubscriptionsByIdResponse respond404WithApplicationJson(Message entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new GetSubscriptionsByIdResponse(responseBuilder.build(), entity);
    }
  }
}
