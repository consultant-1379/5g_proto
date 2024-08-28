/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.healtchecklogservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.utilities.dns.DnsCache;

import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.scp.api.v1.HealthCheckLogEvent;
import io.scp.api.v1.HealthCheckLogEventResponse;
import io.scp.service.outlier.v1.RxHealthCheckLogServiceGrpc.HealthCheckLogServiceImplBase;

/**
 * Implements the HealthCheckLogService which receives Health-Check-Log messages
 * from Envoy resp forwarded to its sidecar log-forwarder and then publishes
 * them on a stream that it typically subscribed to by the HealthCheck Log
 * Consolidator in the SEPP/SCP Manager.
 */
public class HealthCheckServiceSeppScp extends HealthCheckLogServiceImplBase implements HealthCheckLogService
{
    private static final Logger log = LoggerFactory.getLogger(HealthCheckServiceSeppScp.class);
    private final PublishSubject<EnvoyStatus> healthCheckEventStream;

    public HealthCheckServiceSeppScp()
    {
        healthCheckEventStream = PublishSubject.create();
    }

    public PublishSubject<EnvoyStatus> getHealthCheckEventStream()
    {
        return healthCheckEventStream;
    }

    @Override
    public Flowable<HealthCheckLogEventResponse> streamHealthCheckLogEvents(Flowable<HealthCheckLogEvent> event)
    {
        return event.map(ev ->
        {
            OperationalState status = getStatusForEvent(ev);
            if (status.equals(OperationalState.UNKNOWN))
            {
                log.warn("HealthCheck event received with Unknown status");

                return HealthCheckLogEventResponse.newBuilder().build(); // ignore received event with unknown operational state

            }
            var envoy = ev.getEnvoyId();
            var url = getUrl(ev.getEventDetails().getHost().getSocketAddress().getAddress() + ":"
                             + ev.getEventDetails().getHost().getSocketAddress().getPortValue());
            var cluster = ev.getEventDetails().getClusterName();
            log.info("HealthCheck event received: Envoy = {}, Url of the NF Service Instance = {}, Affected cluster = {}, Status = {}",
                     envoy,
                     url,
                     cluster,
                     status);
            var healthCheckEvent = new EnvoyStatus(envoy, url, cluster, status);
            healthCheckEventStream.onNext(healthCheckEvent);

            return HealthCheckLogEventResponse.newBuilder().build();
        });
    }

    /**
     * The URL of the NF Service Instance that is extracted from a Health Check
     * event in Envoy in the format {@code <IPv4>:<port>|<FQDN>}. Extract the FQDN
     * and add the port to get {@code <FQDN>:<port>} or, if the FQDN is not present,
     * return {@code <IPv4>:<port>} which is the original format from an unpatched
     * Envoy. The FQDN is added by a patch against Envoy.
     * 
     * NOTE (DND-23554) Since the introduction of the CDS/EDS split we do not get
     * the fqdn from envoy anymore and have to ignore the part right from the "|" .
     * 
     * @param upstreamUrl upstream URL received from an health check event
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
     * Given a health Check Log Event, determine the operational state and return
     * it.
     * 
     * @param ev
     * @return the status
     */
    public OperationalState getStatusForEvent(HealthCheckLogEvent ev)
    {
        OperationalState status;

        if (ev.getEventDetails().hasAddHealthyEvent())
        {
            status = OperationalState.HEALTHY_HOST_ADDITION;
        }
        else if (ev.getEventDetails().hasEjectUnhealthyEvent())
        {
            status = OperationalState.UNHEALTHY_HOST_EJECTION;
        }
        else if (ev.getEventDetails().hasDegradedHealthyHost())
        {
            status = OperationalState.DEGRADED_HEALTHY_HOST;
        }
        else if (ev.getEventDetails().hasNoLongerDegradedHost())
        {
            status = OperationalState.NO_LONGER_DEGRADED_HOST;
        }
        else if (ev.getEventDetails().hasHealthCheckFailureEvent())
        {
            status = OperationalState.FAILURE_ON_FIRST_CHECK;
        }
        else
        {
            status = OperationalState.UNKNOWN;
        }
        return status;
    }
}
