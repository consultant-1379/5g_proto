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
 * Created on: Mar 1, 2021
 *     Author: echaias
 */

package com.ericsson.sc.scp.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.CommonConfigUtils.NfType;
import com.ericsson.sc.configutil.CommonConfigUtils.RdnKeys;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.AdditionalInformation.AdditionalInformationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.proxyal.outlierlogservice.EnvoyStatus;
import com.ericsson.sc.proxyal.outlierlogservice.OperationalState;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Completable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 */
public class NfInstanceAlarmHandler
{

    static final String NF_SERVICE_INSTANCE_UNHEALTHY_ALARM = "ScpNfServiceInstanceActiveHealthCheckFailed";
    static final String NF_SERVICE_INSTANCE_UNHEALTHY_ALARM_DESC = "SCP, The NF Service Instance active health check failed";

    static final String NF_NODE_UNREACHABLE_ALARM = "ScpNfServiceInstanceNotReachable";
    static final String NF_NODE_UNREACHABLE_DESC = "SCP, The NF Service Instance is not reachable for service requests";

    static final String NO_SCP_CONFIG = "No SCP configuration defined.";

    /**
     * The max. percent of blocked nfs in a pool before an alarm is raised
     */
    static final int NF_POOL_ALARM_THRESHOLD = 60;

    // Alarms are raised with a time-to-live (TTL) and have to be re-raised before
    // they expire. We have to add a safety margin to the TTL compared to the timer
    // that triggers the periodic update in case the periodic update job is delayed.
    // If that would happen, then the AlarmHandlerImpl would automatically cease the
    // alarm and we would very shortly after that raise the alarm again, causing
    // flapping alarms.
    private Long alarmTtl = 60000L;
    private static double ALARM_SAFETY_MARGIN = 1.5;

    private static final String FULL_RDN = "fullRdn";

    private static final Logger log = LoggerFactory.getLogger(NfInstanceAlarmHandler.class);

    private FmAlarmService alarmService;
    private PublishSubject<EnvoyStatus> hostStatusUpdates;
    private PublishSubject<com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus> hostStatusUpdatesHealthCheck;
    private PublishSubject<String> workerDisconnections;
    private BehaviorSubject<Optional<EricssonScp>> configFlow;
    private String serviceName;

    private Map<String, FaultyNfInstance> faultyNfInstances = new ConcurrentHashMap<>();
    private Map<String, FaultyNfInstance> faultyNfInstancesAfterHealthCheck = new ConcurrentHashMap<>();

    private Optional<EricssonScp> scpCfg;

    private static class FaultyNfInstance
    {
        private String alarmRdn;
        private Map<String, String> additionalInfo = new HashMap<>();

        private Map<String, Integer> reportedByEnvoy = new HashMap<>();

        private long lastFaultReportTime;
        private int blockingTime;

        /**
         * Construct a NF Instance (static or discovered) for that an alarm "SCP, NF
         * Instance Not Reachable" is to be raised or ceased
         * 
         * @param nfLabelRdn String containing NF Instance name and label, or
         *                   "UNKNOWN_NFINSTANCE"
         */
        public FaultyNfInstance(final Map<String, String> additionalInfo)
        {
            super();
            this.additionalInfo = additionalInfo;
            this.alarmRdn = constructRdnFromMap(additionalInfo);
            this.lastFaultReportTime = System.currentTimeMillis();
        }

        /**
         * Construct a NF Instance (static or discovered) for that an alarm "SCP, NF
         * Instance Not Reachable" is to be raised or ceased
         * 
         * @param nfLabelRdn   String containing NF Instance name and label, or
         *                     "UNKNOWN_NFINSTANCE"
         * @param blockingTime
         */
        public FaultyNfInstance(final Map<String, String> additionalInfo,
                                final int blockingTime)
        {
            super();
            this.additionalInfo = additionalInfo;
            this.alarmRdn = constructRdnFromMap(additionalInfo);
            this.lastFaultReportTime = System.currentTimeMillis();
            this.blockingTime = blockingTime;
        }

        /**
         * @param additionalInfo2
         * @return
         */
        private String constructRdnFromMap(Map<String, String> info)
        {
            var type = info.get(RdnKeys.NF_TYPE.toString());

            var alarmRdnBuilder = new StringBuilder();

            alarmRdnBuilder.append("nf-pool=").append(info.get(RdnKeys.POOL.toString()));

            if (type.equals(NfType.STATIC.toString()) || type.equals(NfType.DISCOVERED.toString()))
            {
                alarmRdnBuilder.append(",nf-instance=")
                               .append(info.get(RdnKeys.NF_INSTANCE.toString()))
                               .append(",nf-service=")
                               .append(info.get(RdnKeys.NF_SERVICE.toString()));

            }
            else if (type.equals(NfType.STATIC_SEPP.toString()))
            {
                alarmRdnBuilder.append(",sepp-instance=").append(info.get(RdnKeys.NF_INSTANCE.toString()));

            }
            else // static or discovered scp
            {
                alarmRdnBuilder.append(",scp-instance=").append(info.get(RdnKeys.NF_INSTANCE.toString()));

                if (type.equals(NfType.STATIC_SCP_WITHOUT_DOMAIN.toString()))
                    alarmRdnBuilder.append(",nf-service=").append(info.get(RdnKeys.NF_SERVICE.toString()));
                else
                    alarmRdnBuilder.append(",scp-domain=").append(info.get(RdnKeys.NF_SERVICE.toString()));

            }

            if (!info.get(RdnKeys.IP_ADDRESS.toString()).equals(""))
                alarmRdnBuilder.append(",ipAddress=").append(info.get(RdnKeys.IP_ADDRESS.toString()));

            return alarmRdnBuilder.toString();

        }

        /**
         * @return the additionalInfo
         */
        public Map<String, String> getAdditionalInfo()
        {
            return additionalInfo;
        }

        public Map<String, Integer> getReportedByEnvoy()
        {
            return reportedByEnvoy;
        }

        public String getAlarmRdn()
        {
            return this.alarmRdn;
        }

        public boolean isAlarmed()
        {
            return !this.reportedByEnvoy.isEmpty();
        }

        public boolean isOutDated()
        {
            return (System.currentTimeMillis() - this.lastFaultReportTime >= (blockingTime * 1000 + 10 * 1000));
        }

        public void addReportedByEnvoy(String envoyId)
        {
            if (!this.reportedByEnvoy.containsKey(envoyId))
                this.reportedByEnvoy.put(envoyId, 1);
            else
                this.reportedByEnvoy.put(envoyId, this.reportedByEnvoy.get(envoyId) + 1);

            this.lastFaultReportTime = System.currentTimeMillis();
        }

        public void removeReportedByEnvoy(String envoyId)
        {
            if (this.reportedByEnvoy.containsKey(envoyId))
            {
                var value = this.reportedByEnvoy.get(envoyId);

                if (value <= 1)
                {
                    this.reportedByEnvoy.remove(envoyId);
                }
                else
                {
                    this.reportedByEnvoy.put(envoyId, value - 1);
                }
            }
        }

        public void removeFailedHealthCheckEnvoyReporter(String envoyId)
        {
            if (this.reportedByEnvoy.containsKey(envoyId))
            {

                this.reportedByEnvoy.remove(envoyId);

            }
        }

        public String addInfoToJson()
        {
            var type = additionalInfo.get(RdnKeys.NF_TYPE.toString());

            StringBuilder fullRdn = new StringBuilder();

            fullRdn.append("nf=scp-function,nf-instance=")
                   .append(additionalInfo.get(RdnKeys.SEPP_SCP_NF_INSTANCE.toString()))
                   .append(",nf-pool=")
                   .append(additionalInfo.get(RdnKeys.POOL.toString()))
                   .append(",");

            if (type.equals(NfType.STATIC.toString()) || type.equals(NfType.DISCOVERED.toString()))
                fullRdn.append(type)
                       .append("-nf-instance=")
                       .append(additionalInfo.get(RdnKeys.NF_INSTANCE.toString()))
                       .append(",")
                       .append(type)
                       .append("-nf-service=")
                       .append(additionalInfo.get(RdnKeys.NF_SERVICE.toString()));
            else if (type.equals(NfType.STATIC_SCP_WITHOUT_DOMAIN.toString()))
            {
                fullRdn.append("static-scp-instance=")
                       .append(additionalInfo.get(RdnKeys.NF_INSTANCE.toString()))
                       .append(",static-scp-service=")
                       .append(additionalInfo.get(RdnKeys.NF_SERVICE.toString()));

            }
            else
            {
                fullRdn.append(type).append("-instance=").append(additionalInfo.get(RdnKeys.NF_INSTANCE.toString()));

                if (!type.equals(NfType.STATIC_SEPP.toString()))
                    fullRdn.append(",").append(type).append("-domain-info=").append(additionalInfo.get(RdnKeys.NF_SERVICE.toString()));
            }

            if (!additionalInfo.get(RdnKeys.IP_ADDRESS.toString()).equals(""))
                fullRdn.append(",ipAddress=").append(additionalInfo.get(RdnKeys.IP_ADDRESS.toString()));

            return fullRdn.toString();
        }
    }

    public NfInstanceAlarmHandler(String serviceName,
                                  FmAlarmService alarmService,
                                  PublishSubject<EnvoyStatus> hostStatusUpdates,
                                  PublishSubject<com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus> hostStatusUpdatesHealthCheck,
                                  PublishSubject<String> workerDisconnections,
                                  BehaviorSubject<Optional<EricssonScp>> configFlow)
    {
        super();
        this.serviceName = serviceName;
        this.alarmService = alarmService;
        this.hostStatusUpdates = hostStatusUpdates;
        this.hostStatusUpdatesHealthCheck = hostStatusUpdatesHealthCheck;
        this.workerDisconnections = workerDisconnections;
        this.configFlow = configFlow;
    }

    void setAlarmTtl(Long alarmTtl)
    {
        this.alarmTtl = alarmTtl;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            log.info("Start.");
            startEventHandler();
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> log.info("Stop."));
    }

    private void startEventHandler()
    {
        log.info("Starting EventHandler.");

        hostStatusUpdatesHealthCheck.retry().subscribe(hostStatus ->
        {
            try
            {
                if (hostStatus.getOperationalState() == com.ericsson.sc.proxyal.healtchecklogservice.OperationalState.UNHEALTHY_HOST_EJECTION
                    || hostStatus.getOperationalState() == com.ericsson.sc.proxyal.healtchecklogservice.OperationalState.FAILURE_ON_FIRST_CHECK)
                {
                    handleUnhealthyStatus(hostStatus);
                }
                else if (hostStatus.getOperationalState() == com.ericsson.sc.proxyal.healtchecklogservice.OperationalState.HEALTHY_HOST_ADDITION)
                {
                    handleNoLongerUnhealthyStatus(hostStatus);
                }
            }
            catch (Exception e)
            {
                log.error("Caught Exception while handling status updates for health check events:{}.", e.toString());
            }
        }, err -> log.error("Error:{}.", err.toString()));

        hostStatusUpdates.retry().subscribe(hostStatus ->
        {
            try
            {
                if (hostStatus.getOperationalState() == OperationalState.BLOCKED || hostStatus.getOperationalState() == OperationalState.UNREACHABLE)
                {
                    handleUnreachableStatus(hostStatus);
                }
                else
                {
                    handleReachableStatus(hostStatus);
                }
            }
            catch (Exception e)
            {
                log.error("Caught Exception while handling status updates:{}.", e.toString());
            }
        }, err -> log.error("Error:{}.", err.toString()));

        workerDisconnections.retry().subscribe(envoyId ->
        {
            log.info("Envoy disconnection from:{}.", envoyId);

            for (FaultyNfInstance faultyNfInst : faultyNfInstances.values())
            {
                faultyNfInst.removeReportedByEnvoy(envoyId);

                if (!faultyNfInst.isAlarmed())
                {
                    alarmNfNodeNotReachableCease(faultyNfInst);
                }
            }

            for (FaultyNfInstance faultyNfInst : faultyNfInstancesAfterHealthCheck.values())
            {
                faultyNfInst.removeReportedByEnvoy(envoyId);

                if (!faultyNfInst.isAlarmed())
                {
                    alarmNfServiceInstanceUnhealthyCease(faultyNfInst);
                }
            }
        }, err -> log.error("Error:{}.", err.toString()));

        configFlow.retry().subscribe(cfg ->
        {
            log.info("Configuration update received.");
            this.scpCfg = cfg;

            if (this.scpCfg.isPresent())
            {
                if (scpCfg.get().getEricssonScpScpFunction().getNfInstance().size() == 0)
                {
                    log.debug("Configuration is Empty/Deleted");
                    for (FaultyNfInstance faultyNfInst : faultyNfInstances.values())
                    {

                        alarmNfNodeNotReachableCease(faultyNfInst);
                        faultyNfInstances.remove(faultyNfInst.getAlarmRdn());

                    }
                    for (FaultyNfInstance faultyNfInst : faultyNfInstancesAfterHealthCheck.values())
                    {

                        alarmNfServiceInstanceUnhealthyCease(faultyNfInst);
                        faultyNfInstancesAfterHealthCheck.remove(faultyNfInst.getAlarmRdn());

                    }
                }
                else
                {
                    log.debug("Configuration is presented");

                    for (FaultyNfInstance faultyNfInst : faultyNfInstances.values())
                    {
                        if (!CommonConfigUtils.isNfServiceNameValid(this.scpCfg.get().getEricssonScpScpFunction(), faultyNfInst.getAdditionalInfo()))
                        {
                            alarmNfNodeNotReachableCease(faultyNfInst);
                            faultyNfInstances.remove(faultyNfInst.getAlarmRdn());
                        }
                    }
                    for (FaultyNfInstance faultyNfInst : faultyNfInstancesAfterHealthCheck.values())
                    {
                        log.debug("Faulty Nf Instances for active health check exist");

                        if (!CommonConfigUtils.isNfServiceNameValid(this.scpCfg.get().getEricssonScpScpFunction(), faultyNfInst.getAdditionalInfo()))
                        {
                            log.debug("Faulty Nf Instances does not exist any more in the updated config - Try to delete them from the faulty list");

                            alarmNfServiceInstanceUnhealthyCease(faultyNfInst);
                            faultyNfInstancesAfterHealthCheck.remove(faultyNfInst.getAlarmRdn());
                        }
                    }
                }
            }
            else
            {

                log.debug(NO_SCP_CONFIG);
            }
        }, err -> log.error("Error:{}.", err.toString()));

        VertxInstance.get().setPeriodic(alarmTtl, id ->
        {
            try
            {
                updateAlarms();
            }
            catch (JsonProcessingException e)
            {
                log.error("Error during alarm update every {} ms.", alarmTtl, e);
            }
        });

        log.info("EventHandler started.");
    }

    private void updateAlarms() throws JsonProcessingException
    {
        log.debug("Periodic alarm update.");

        for (FaultyNfInstance faultyNfInst : faultyNfInstances.values())
        {
            if (faultyNfInst.isAlarmed())
            {
                if (!faultyNfInst.isOutDated())
                {
                    alarmNfNodeNotReachableRaise(faultyNfInst);
                }
                else
                {
                    log.info("Outdated blocking status detected for NF Service Instance.");
                    faultyNfInstances.remove(faultyNfInst.getAlarmRdn());
                }
            }
        }
        for (FaultyNfInstance faultyNfInst : faultyNfInstancesAfterHealthCheck.values())
        {
            if (faultyNfInst.isAlarmed())
            {
                alarmNfServiceInstanceUnhealthyRaise(faultyNfInst);
            }
        }
    }

    private void handleUnhealthyStatus(com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus hostStatus) throws JsonProcessingException
    {
        log.debug("handleUnhealthyStatus:{}.", hostStatus);
        if (this.scpCfg.isPresent())
        {
            List<Map<String, String>> nfInstanceRdns = CommonConfigUtils.getNfInstanceRdn(this.scpCfg.get().getEricssonScpScpFunction(),
                                                                                          hostStatus.getUrl(),
                                                                                          hostStatus.getCluster());
            log.debug("Rdns found in the configuration for unhealthy status:\n.");
            nfInstanceRdns.stream().forEach(rdn -> log.info(rdn.toString()));

            if (nfInstanceRdns.isEmpty())
            {
                log.debug("Received log event related to Active Health Check for unknown NF Service Instance.");
                return;
            }

            for (var nfRdn : nfInstanceRdns)
            {
                FaultyNfInstance faultyNfInstance = new FaultyNfInstance(nfRdn);

                if (!faultyNfInstancesAfterHealthCheck.containsKey(faultyNfInstance.getAlarmRdn()))
                {
                    log.debug("New NF Instance Fault Indication related to Active Health Check received .");
                    alarmNfServiceInstanceUnhealthyRaise(faultyNfInstance);
                    faultyNfInstance.addReportedByEnvoy(hostStatus.getEnvoyId());
                    faultyNfInstancesAfterHealthCheck.put(faultyNfInstance.getAlarmRdn(), faultyNfInstance);
                }
                else
                {
                    faultyNfInstancesAfterHealthCheck.get(faultyNfInstance.getAlarmRdn()).addReportedByEnvoy(hostStatus.getEnvoyId());
                    log.debug("Reported by envoy:");
                    log.debug(faultyNfInstancesAfterHealthCheck.get(faultyNfInstance.getAlarmRdn()).getReportedByEnvoy().toString());
                }
            }
        }
        else
        {
            log.debug(NO_SCP_CONFIG);
        }

    }

    private void handleNoLongerUnhealthyStatus(com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus hostStatus) throws JsonProcessingException
    {
        log.debug("handleNoLongerUnhealthyStatus:{}.", hostStatus);

        if (this.scpCfg.isPresent())
        {
            List<Map<String, String>> nfInstanceRdns = CommonConfigUtils.getNfInstanceRdn(this.scpCfg.get().getEricssonScpScpFunction(),
                                                                                          hostStatus.getUrl(),
                                                                                          hostStatus.getCluster());
            log.debug("Rdns found in the configuration for no longer unhealthy status handling:\n.");
            nfInstanceRdns.stream().forEach(rdn -> log.debug(rdn.toString()));

            if (nfInstanceRdns.isEmpty())
            {
                log.debug("Received log event related to Active Health Check for unknown NF Service Instance.");
                return;
            }

            for (var nfRdn : nfInstanceRdns)
            {
                FaultyNfInstance nfInstance = new FaultyNfInstance(nfRdn);

                if (faultyNfInstancesAfterHealthCheck.containsKey(nfInstance.getAlarmRdn()))
                {
                    nfInstance = faultyNfInstancesAfterHealthCheck.get(nfInstance.getAlarmRdn());
                    nfInstance.removeFailedHealthCheckEnvoyReporter(hostStatus.getEnvoyId());
                    log.debug("Envoy sources still considering it as faulty:");
                    log.debug(faultyNfInstancesAfterHealthCheck.get(nfInstance.getAlarmRdn()).getReportedByEnvoy().toString());
                    if (!nfInstance.isAlarmed())
                    {
                        alarmNfServiceInstanceUnhealthyCease(nfInstance);
                        faultyNfInstancesAfterHealthCheck.remove(nfInstance.getAlarmRdn());
                    }
                }
            }
        }
        else
        {
            log.debug(NO_SCP_CONFIG);
        }
    }

    private void handleUnreachableStatus(EnvoyStatus hostStatus) throws JsonProcessingException
    {
        log.debug("handleUnreachableStatus:{}.", hostStatus);

        if (this.scpCfg.isPresent())
        {
            List<Map<String, String>> nfInstanceRdns = CommonConfigUtils.getNfInstanceRdn(this.scpCfg.get().getEricssonScpScpFunction(),
                                                                                          hostStatus.getUrl(),
                                                                                          hostStatus.getCluster());

            if (nfInstanceRdns.isEmpty())
            {
                log.info("Received lock event for unknown NF Service Instance.");
                return;
            }

            // blockingTime of the cluster
            int blockingTime = CommonConfigUtils.getTempBlockingTime(this.scpCfg.get().getEricssonScpScpFunction(), hostStatus.getCluster());
            log.debug("Blocking time for cluster {} : {}", hostStatus.getCluster(), blockingTime);

            for (var nfRdn : nfInstanceRdns)
            {
                FaultyNfInstance faultyNfInstance = new FaultyNfInstance(nfRdn, blockingTime);

                if (!faultyNfInstances.containsKey(faultyNfInstance.getAlarmRdn()))
                {
                    log.debug("New NF Instance Fault Indication received.");
                    alarmNfNodeNotReachableRaise(faultyNfInstance);

                    faultyNfInstance.addReportedByEnvoy(hostStatus.getEnvoyId());
                    faultyNfInstances.put(faultyNfInstance.getAlarmRdn(), faultyNfInstance);
                }
                else
                {
                    faultyNfInstances.get(faultyNfInstance.getAlarmRdn()).addReportedByEnvoy(hostStatus.getEnvoyId());
                }
            }
        }
        else
        {
            log.debug(NO_SCP_CONFIG);
        }
    }

    private void handleReachableStatus(EnvoyStatus hostStatus) throws JsonProcessingException
    {
        log.debug("handleReachableStatus:{}.", hostStatus);

        if (this.scpCfg.isPresent())
        {
            List<Map<String, String>> nfInstanceRdns = CommonConfigUtils.getNfInstanceRdn(this.scpCfg.get().getEricssonScpScpFunction(),
                                                                                          hostStatus.getUrl(),
                                                                                          hostStatus.getCluster());

            if (nfInstanceRdns.isEmpty())
            {
                log.info("Received unlock event for unknown NF Service Instance.");
                return;
            }

            for (var nfRdn : nfInstanceRdns)
            {
                FaultyNfInstance nfInstance = new FaultyNfInstance(nfRdn, 0);

                if (faultyNfInstances.containsKey(nfInstance.getAlarmRdn()))
                {
                    nfInstance = faultyNfInstances.get(nfInstance.getAlarmRdn());
                    nfInstance.removeReportedByEnvoy(hostStatus.getEnvoyId());

                    if (!nfInstance.isAlarmed())
                    {
                        alarmNfNodeNotReachableCease(nfInstance);
                        faultyNfInstances.remove(nfInstance.getAlarmRdn());
                    }
                }
            }
        }
        else
        {
            log.debug(NO_SCP_CONFIG);
        }
    }

    private void alarmNfServiceInstanceUnhealthyRaise(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var additionalInformation = new AdditionalInformationBuilder().withAdditionalProperty(FULL_RDN, faultyNfInstance.addInfoToJson()).build();
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_SERVICE_INSTANCE_UNHEALTHY_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withDescription(NF_SERVICE_INSTANCE_UNHEALTHY_ALARM_DESC)
                                                          .withAdditionalInformation(additionalInformation)
                                                          .withFaultyResource(faultyNfInstance.getAlarmRdn())
                                                          .withSeverity(Severity.MAJOR)
                                                          .withExpiration((long) (ALARM_SAFETY_MARGIN * alarmTtl / 1000))
                                                          .build();

        log.debug("Raise alarm after failed Active Health Check with FaultIndication: {}", faultIndication);
        this.alarmService.raise(faultIndication)
                         .subscribe(() -> log.info("Raising alarm {}", NF_SERVICE_INSTANCE_UNHEALTHY_ALARM),
                                    t -> log.error("Error raising alarm after failed Active Health Check. Cause: {}", t.toString()));
    }

    private void alarmNfServiceInstanceUnhealthyCease(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_SERVICE_INSTANCE_UNHEALTHY_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withFaultyResource(faultyNfInstance.getAlarmRdn())
                                                          .withDescription(NF_SERVICE_INSTANCE_UNHEALTHY_ALARM_DESC)
                                                          .build();
        this.alarmService.cease(faultIndication)
                         .subscribe(() -> log.info("ceased alarm {}", NF_SERVICE_INSTANCE_UNHEALTHY_ALARM),
                                    t -> log.error("Error ceasing alarm after successful Active Health Check. Cause: {}", t.toString()));
    }

    private void alarmNfNodeNotReachableRaise(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var additionalInformation = new AdditionalInformationBuilder().withAdditionalProperty(FULL_RDN, faultyNfInstance.addInfoToJson()).build();
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_NODE_UNREACHABLE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withDescription(NF_NODE_UNREACHABLE_DESC)
                                                          .withAdditionalInformation(additionalInformation)
                                                          .withFaultyResource(faultyNfInstance.getAlarmRdn())
                                                          .withSeverity(Severity.MAJOR)
                                                          .withExpiration((long) (ALARM_SAFETY_MARGIN * alarmTtl / 1000))
                                                          .build();

        log.debug("Raise alarm with FaultIndication: {}", faultIndication);
        this.alarmService.raise(faultIndication)
                         .subscribe(() -> log.info("Raising alarm {}", NF_NODE_UNREACHABLE_ALARM),
                                    t -> log.error("Error raising alarm. Cause: {}", t.toString()));
    }

    private void alarmNfNodeNotReachableCease(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_NODE_UNREACHABLE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withFaultyResource(faultyNfInstance.getAlarmRdn())
                                                          .withDescription(NF_NODE_UNREACHABLE_DESC)
                                                          .build();
        this.alarmService.cease(faultIndication)
                         .subscribe(() -> log.info("Ceasing alarm {}", NF_NODE_UNREACHABLE_ALARM),
                                    t -> log.error("Error ceasing alarm. Cause: {}", t.toString()));
    }
}
