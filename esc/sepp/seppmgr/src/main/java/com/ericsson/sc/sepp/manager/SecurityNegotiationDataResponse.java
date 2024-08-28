package com.ericsson.sc.sepp.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.state.RoutingParameter;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.http.HttpServerResponse;

/**
 * @author edimsyr The response helper class that will build the response and
 *         send it back to Yang
 */
public class SecurityNegotiationDataResponse
{
    private static final Logger log = LoggerFactory.getLogger(SecurityNegotiationDataResponse.class);

    private HttpServerResponse httpResponse;
    private Single<N32cPathParameters> n32cPathParameters;
    private N32cInterface n32c;
    private RoutingParameter routingParameter;

    public SecurityNegotiationDataResponse(HttpServerResponse httpResponse,
                                           RoutingParameter routingParameter,
                                           Single<N32cPathParameters> n32cPathParameters,
                                           N32cInterface n32c)
    {
        this.httpResponse = httpResponse;
        this.routingParameter = routingParameter;
        this.n32cPathParameters = n32cPathParameters;
        this.n32c = n32c;
    }

    public SecurityNegotiationDataResponse withRoamingPartner(Single<N32cPathParameters> n32cPathParameters)
    {
        this.n32cPathParameters = n32cPathParameters;
        return this;
    }

    public SecurityNegotiationDataResponse withN32cInteface(N32cInterface n32c)
    {
        this.n32c = n32c;
        return this;
    }

    /**
     * @return fetch the data from Etcd and handle the corresponding failures, uses
     *         the N32cInteface to call the RxEtcd returns 200 on success returns
     *         201 on empty response
     */
    private Completable fetchDataFromEtcd()
    {
        if (routingParameter == RoutingParameter.n32_c)
        {

            return n32cPathParameters.flatMap(it -> it.getNfInstanceRef().isEmpty() ? n32c.securityNegotiationDataFromRoamingPartner(it.getRoamingPartner())
                                                                                    : n32c.readDataForStateDataProvider(it))

                                     .flatMapCompletable(secList ->
                                     {
                                         if (secList.isEmpty())
                                         {
                                             return httpResponse.setStatusCode(204).rxEnd();
                                         }
                                         else
                                         {
                                             log.debug("Response from SDP: {}", secList);
                                             return httpResponse.putHeader("content-type", "application/json").rxEnd(secList.toString());
                                         }
                                     })
                                     .doOnError(e -> log.error("Unable to fetch Security Negotiation Data from Etcd", e));
        }

        return Completable.fromAction(() -> log.info("Error from Completable:routingParameter: {}", routingParameter))
                          .doOnError(e -> log.error("illegal routingParameter"));
    }

    public Completable respond()
    {
        return fetchDataFromEtcd();
    }

}
