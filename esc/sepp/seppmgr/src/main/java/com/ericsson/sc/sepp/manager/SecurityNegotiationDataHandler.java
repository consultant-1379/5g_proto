package com.ericsson.sc.sepp.manager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.state.StateDataInput;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author edimsyr The custom implementation of a SeppStateDataHandler
 *         specifically for SecurityNegotiation Procedure of N32-c
 *
 */
public class SecurityNegotiationDataHandler implements SeppStateDataHandler
{
    private static final Logger log = LoggerFactory.getLogger(SecurityNegotiationDataHandler.class);

    private N32cInterface n32c;
    private ConcurrentHashMap<String, String> pathParams;
    private static final String RP_TAG = "roaming-partner";
    private static final String SEC_NEG_DATA_TAG = "security-negotiation-data";
    private static final String RECEIVED_PLMN_ID_TAG = "received-plmn-id";
    private static final String SEPP_TAG = "sepp-name=";

    public SecurityNegotiationDataHandler(N32cInterface n32c)
    {
        this.n32c = n32c;
    }

    @Override
    public Completable handleRequest(Single<StateDataInput> input)
    {
        return input.flatMapCompletable(in -> new SecurityNegotiationDataResponse(in.getCtx().response(),
                                                                                  in.getRoutingParameter(),
                                                                                  extractParamsFromRequest(extractPathParameters(in.getCtx().normalizedPath())),
                                                                                  n32c).respond());
    }

    /**
     * @param path The Path from which the parameters are going to be extracted
     *             example: path: /foo/bar/ti the parameters are param1=foo,
     *             param2=bar,param3=ti
     * @return Single<ConcurrentHashMap<String, String>> a map with the parameters
     *         extracted example: from path /foo/bar/ as key: param1 and as value:
     *         foo
     */
    private Single<ConcurrentHashMap<String, String>> extractPathParameters(String path)
    {
        return Single.create(emitter ->
        {
            pathParams = new ConcurrentHashMap<>();
            List<String> pathParameters = Arrays.asList(path.split("/"));
            log.debug("path for sdp: {}", path);
            for (int count = 0; count < pathParameters.size(); count++)
            {
                if (count == pathParameters.size() - 1)
                {
                    pathParams.put("lastParam", pathParameters.get(count));
                }
                pathParams.put("param" + (count + 1), pathParameters.get(count));

            }
            if (pathParams.isEmpty())
            {
                emitter.onError(new Exception("Path parameters not extracted properly"));
            }
            else
            {
                emitter.onSuccess(pathParams);
            }
        });
    }

    /**
     * @param parameters The parameters to extract the roaming partner from
     * @return Single<String> the Roaming partner and Sepp nf instance ref extracted
     */
    private Single<N32cPathParameters> extractParamsFromRequest(Single<ConcurrentHashMap<String, String>> parameters)
    {
        return parameters.map(params ->
        {
            N32cPathParameters p = new N32cPathParameters();
            p.setNfInstanceRef("");
            params.entrySet()
                  .stream()
                  .filter(t -> t.getValue().equals(RP_TAG) || t.getValue().contains(SEPP_TAG) || t.getValue().equals(RECEIVED_PLMN_ID_TAG)
                               || t.getKey().equals("lastParam"))
                  .forEach(param ->
                  {

                      if (param.getValue().contains(SEPP_TAG))
                      {
                          p.setNfInstanceRef(param.getValue().substring(SEPP_TAG.length()));
                      }
                      else if (param.getValue().equals(RP_TAG))
                      {
                          p.setRoamingPartner(params.get("param" + String.valueOf((Integer.valueOf(param.getKey().substring(5)) + 1))).substring(5));
                      }
                      if (param.getKey().equals("lastParam"))
                      {
                          p.setLastValue(param.getValue());
                      }
                  });

            log.debug("N32cPathParameters: {}", p);
            return p;

        });
    }

    /**
     * return the name of the Handler for debugging purposes
     */
    @Override
    public String handlerName()
    {
        return "Security Negotiation Data handler";
    }

}
