/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 23, 2021
 *     Author: echaias
 */

package com.ericsson.sc.proxyal.outlierlogservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.utilities.dns.DnsCache;

import io.envoyproxy.envoy.data.cluster.v3.Action;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.scp.api.v1.OutlierLogEvent;
import io.scp.api.v1.OutlierLogEventResponse;
import io.scp.service.outlier.v1.RxOutlierLogServiceGrpc.OutlierLogServiceImplBase;

/**
 * Implements the OutlierLogService which receives Outlier-Log messages from
 * Envoy resp. its sidecar and then publishes them on a stream that it typically
 * subscribed to by the TBL Log Consolidator in the SEPP/SCP Manager.
 */
public class OutlierLogServiceSeppScp extends OutlierLogServiceImplBase implements OutlierLogService
{
    private static final Logger log = LoggerFactory.getLogger(OutlierLogServiceSeppScp.class);

    private final PublishSubject<EnvoyStatus> outlierEventStream;

    public OutlierLogServiceSeppScp()
    {
        outlierEventStream = PublishSubject.create();
    }

    public PublishSubject<EnvoyStatus> getOutlierEventStream()
    {
        return outlierEventStream;
    }

    @Override
    public Flowable<OutlierLogEventResponse> streamOutlierLogEvents(Flowable<OutlierLogEvent> event)
    {
        return event.map(ev ->
        {
            // this if, prevents the event from triggering the alarm 2 times
            // when we have a gateway_failure (502,503,504) DND-26961
            if (ev.getEventDetails().getAction().equals(Action.EJECT) && !ev.getEventDetails().getEnforced())
            {
                return OutlierLogEventResponse.newBuilder().build();
            }
            var envoy = ev.getEnvoyId();
            var url = getUrl(ev.getEventDetails().getUpstreamUrl());
            var cluster = ev.getEventDetails().getClusterName();
            OperationalState status = getStatusForEvent(ev);
            log.info("TBL event: Envoy = {}, Url of the NF Service Instance = {}, Affected cluster = {}, Status = {}", envoy, url, cluster, status);
            var outlierEvent = new EnvoyStatus(envoy, url, cluster, status);
            outlierEventStream.onNext(outlierEvent);
            return OutlierLogEventResponse.newBuilder().build();
        });
    }

    /**
     * The URL of the NF Service Instance that is extracted from an Outlier
     * detection event in Envoy in the format {@code <IPv4>:<port>|<FQDN>}. Extract
     * the FQDN and add the port to get {@code <FQDN>:<port>} or, if the FQDN is not
     * present, return {@code <IPv4>:<port>} which is the original format from an
     * unpatched Envoy. The FQDN is added by a patch against Envoy.
     * 
     * NOTE (DND-23554) Since the introduction of the CDS/EDS split we do not get
     * the fqdn from envoy anymore and have to ignore the part right from the "|" .
     * 
     * @param upstreamUrl upstream URL received from an outlier detection event
     * @return String in the format {@code <IPv4>:<port>|<FQDN>:<port>}. Port, fqdn
     *         are optional
     */
    public String getUrl(String upstreamUrl)
    {
        log.debug("upstreamUrl:{}.", upstreamUrl);
        var ipPortFqdn = upstreamUrl.split("\\|");
        // Since the introduction of the CDS/EDS split we do not get the fqdn from
        // envoy anymore and have to ignore the part right from the "|" .
        // TODO: remove the envoy patch for SC 1.3
        var ipAddr = getIpFromUrl(ipPortFqdn[0]);
        var port = getPortFromUrl(ipPortFqdn[0]);
        var fqdn = getFqdn(ipAddr);
        if (port.isBlank())
        {
            if (fqdn.isBlank())
                return ipAddr;
            else
                return ipAddr + "|" + fqdn;
        }
        else
        {
            if (fqdn.isBlank())
                return ipAddr + ":" + port;
            else
                return ipAddr + ":" + port + "|" + fqdn + ":" + port;
        }
    }

    /***
     * Return the Ip Address from a given url.
     * 
     * NOTE: In case of an Ipv6 address it removes the brackets
     * 
     * @param url
     * @return
     */
    private String getIpFromUrl(final String url)
    {
        // find out if we have a port and not just an ipV6 address
        String rValue;
        if (url.contains(":") && !(url.startsWith("[") && url.endsWith("]")))
            rValue = url.substring(0, url.lastIndexOf(":")).replace("[", "").replace("]", "");
        else
            rValue = url.substring(url.lastIndexOf("[") + 1, url.lastIndexOf("]")); // url = ipV6address

        return rValue;

    }

    private String getPortFromUrl(final String url)
    {
        // find out if we have a port and not just an ipV6 address
        if (url.contains(":") && !(url.startsWith("[") && url.endsWith("]")))
        {
            return url.substring(url.lastIndexOf(":") + 1, url.length());
        }
        else // no port found
            return "";

    }

    /*
     * returns a FQDN string for the given hostId if possible otherwise ""
     */
    private String getFqdn(String hostId)
    {
        if (DnsCache.isNumericalIpAddress(hostId))
        {
            log.info("trying to get fqdn for host:{}.", hostId);
            var fqdn = DnsCache.getInstance().toHost(hostId);
            if (!fqdn.isEmpty())
            {
                return fqdn.get();
            }
        }
        return "";
    }

    /**
     * Given an Outlier Log Event, determine the operational state and return it.
     * 
     * @param ev
     * @return the status
     */
    public OperationalState getStatusForEvent(OutlierLogEvent ev)
    {
        OperationalState status;
        var action = ev.getEventDetails().getAction();

        if (action.equals(Action.UNEJECT))
        {
            status = OperationalState.REACHABLE;
        }
        else if (ev.getEventDetails().getEnforced())
        {
            status = OperationalState.BLOCKED;
        }
        else
        {
            status = OperationalState.UNREACHABLE;
        }
        return status;
    }
}
