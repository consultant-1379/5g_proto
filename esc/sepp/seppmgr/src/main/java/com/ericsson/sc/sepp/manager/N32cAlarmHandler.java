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
 * Created on: Mar 26, 2024
 *     Author: znglnck
 */

package com.ericsson.sc.sepp.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.AdditionalInformation.AdditionalInformationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.common.Triplet;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Observable;

public class N32cAlarmHandler
{
    private static final Logger log = LoggerFactory.getLogger(N32cAlarmHandler.class);
    static final String N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE_ALARM = "SeppSecurityCapabilityNegotiationFailure";
    static final String N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE = "SEPP, Security Capability Negotiation Failure";
    private static Long ALARMTTL = 3600L;

    private FmAlarmService alarmService;
    private String serviceName;

    private Map<Triple<String, String, String>, Triple<String, String, Integer>> faultyResource; // {("seppName" ,"rpName", "fqdn") :
                                                                                                 // (reason,additionalInfo,port)}

    public N32cAlarmHandler(String serviceName,
                            FmAlarmService alarmService)
    {
        this.serviceName = serviceName;
        this.alarmService = alarmService;
        this.faultyResource = new HashMap<>();
    }

    /**
     * This method checks if the list of faulty pairs contains any entry in order to
     * re-raise the alarm for that pair in order to keep it up to date. It also gets
     * the reason the alarm was raised for that faulty pair of SEPP Instance and
     * Roaming Partner from the faulty resource list.
     * 
     */
    public void checkforN32cFaultyResource()
    {
        log.debug("Checking every 30 mins for Faulty Resources to re-raise the alarm.");
        Observable.interval(0, 30, TimeUnit.MINUTES).subscribe(checking ->
        {
            if (!getFaultyResource().isEmpty())
            {
                Map<Triple<String, String, String>, Triple<String, String, Integer>> faultyResources = getFaultyResource();
                log.debug("Try to re-raise alarm for resources {}", faultyResources);
                for (Map.Entry<Triple<String, String, String>, Triple<String, String, Integer>> entry : faultyResource.entrySet())
                {
                    String seppName = entry.getKey().getLeft();
                    String rpName = entry.getKey().getMiddle();
                    String reason = entry.getValue().getLeft();
                    Triplet<String, Integer, String> alarmData = Triplet.of(entry.getKey().getRight(), // fqdn
                                                                            entry.getValue().getRight(), // port
                                                                            entry.getValue().getMiddle()); // additional-info
                    log.debug("Re-raising alarm for resource {}", entry);
                    alarmSecurityCapabilityNegotiationRaise(seppName, rpName, reason, alarmData, alarmData.getThird());
                }
            }
        });

    }

    /**
     * This method ceases the alarm "SEPP, Security Capability Negotiation Failure"
     * for every pair that exists in the faulty pair list.
     * 
     */
    public void ceaseN32cSecurityCapabilityNegotiationAlarms()
    {
        try
        {
            Map<Triple<String, String, String>, Triple<String, String, Integer>> faultyResources = getFaultyResource();
            log.debug("Try to cease alarm for resources {}", faultyResources);
            for (Map.Entry<Triple<String, String, String>, Triple<String, String, Integer>> entry : faultyResource.entrySet())
            {
                String seppName = entry.getKey().getLeft();
                String rpName = entry.getKey().getMiddle();
                Triplet<String, Integer, String> alarmData = Triplet.of(entry.getKey().getRight(), entry.getValue().getRight(), entry.getValue().getMiddle());
                alarmSecurityCapabilityNegotiationCease(seppName, rpName, alarmData);
            }
        }
        catch (JsonProcessingException e)
        {
            log.error("Caught Exception while ceasing alarm {}.", e.toString());
        }
    }

    /**
     * This method raises the "SEPP, Security Capability Negotiation Failure" alarm
     * for a given pair of SEPP instance and Roaming Partner. When raising an alarm,
     * it also updates the lists of faulty pairs and faulty resources.
     * 
     * @param seppName
     * @param rpName
     * @parame reason
     * @param alarmData
     * @param additionalInformation
     */
    public void alarmSecurityCapabilityNegotiationRaise(String seppName,
                                                        String rpName,
                                                        String reason,
                                                        Triplet<String, Integer, String> alarmData,
                                                        String additionalInformation) throws JsonProcessingException
    {
        var additionalInfo = new AdditionalInformationBuilder().withAdditionalProperty("FullRDN:", additionalInformation).build();
        log.debug("additionalinfo is: {}", additionalInformation);
        var faultIndication = new FaultIndicationBuilder().withFaultName(N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withDescription(reason)
                                                          .withFaultyResource("sepp-instance=" + seppName + "," + "roaming-partner=" + rpName + ","
                                                                              + alarmData.getFirst() + ":" + alarmData.getSecond())
                                                          .withSeverity(Severity.CRITICAL)
                                                          .withAdditionalInformation(additionalInfo)
                                                          .withExpiration((long) ALARMTTL)
                                                          .build();

        log.debug("Raise alarm with FaultIndication: {}", faultIndication);
        this.alarmService.raise(faultIndication).subscribe(() ->
        {
            log.info("Raising alarm {}", N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE_ALARM);
            faultyResource.put(Triple.of(seppName, rpName, alarmData.getFirst()), Triple.of(reason, additionalInformation, alarmData.getSecond()));
        }, t -> log.error("Error raising alarm. Cause: {}", t.toString()));
    }

    /**
     * This method ceases the "SEPP, Security Capability Negotiation Failure" alarm
     * for a given pair of SEPP instance and Roaming Partner. When ceasing an alarm,
     * it also updates the list of the faulty pairs of SEPP instance and Roaming
     * Partner.
     * 
     * @param seppName
     * @param rpName
     * @param alarmData
     */
    public void alarmSecurityCapabilityNegotiationCease(String seppName,
                                                        String rpName,
                                                        Triplet<String, Integer, String> alarmData) throws JsonProcessingException
    {
        var faultIndication = new FaultIndicationBuilder().withFaultName(N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withFaultyResource("sepp-instance=" + seppName + "," + "roaming-partner=" + rpName + ","
                                                                              + alarmData.getFirst() + ":" + alarmData.getSecond())
                                                          .build();
        log.debug("Cease alarm with FaultIndication: {}", faultIndication);
        this.alarmService.cease(faultIndication).subscribe(() ->
        {
            log.info("Ceasing alarm {}", N32C_SECURITY_NEGOTIATION_CAPABILITY_FAILURE_ALARM);
            faultyResource.remove(Triple.of(seppName, rpName, alarmData.getFirst()));
        }, t -> log.error("Error ceasing alarm. Cause: {}", t.toString()));

    }

    /**
     * This method returns the triple of faulty resource that an alarm has been
     * raised for (in fact, it is a list of, the faulty pair of SEPP instance and
     * Roaming Partner and reason).
     * 
     * @return
     */
    public Map<Triple<String, String, String>, Triple<String, String, Integer>> getFaultyResource()
    {
        return this.faultyResource;
    }

}
