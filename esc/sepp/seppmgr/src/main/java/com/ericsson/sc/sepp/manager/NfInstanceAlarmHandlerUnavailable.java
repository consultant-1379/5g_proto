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
 * Created on: Jan 2, 2024
 *     Author: zarisar
 */

package com.ericsson.sc.sepp.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data.Result;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.AdditionalInformation.AdditionalInformationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.exceptions.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Completable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.reactivex.core.Vertx;

/**
 * 
 */
public class NfInstanceAlarmHandlerUnavailable
{
    private static final Logger log = LoggerFactory.getLogger(NfInstanceAlarmHandlerUnavailable.class);
    static final String NF_INSTANCE_UNAVAILABLE_ALARM = "SeppNfInstanceUnavailable";
    static final String NF_INSTANCE_UNAVAILABLE_DESC = "SEPP, NF Instance Unavailable";
    static final String ERIC_PM_SERVER_SERVICE_PORT = "ERIC_PM_SERVER_SERVICE_PORT";
    static final String ERIC_PM_SERVER_SERVICE_HOST = "ERIC_PM_SERVER_SERVICE_HOST";
    static final String NO_SEPP_CONFIG = "No SEPP configuration defined.";
    public static final int POLLING_INTERVAL_MILLIS = 15000;
    private Long ALARMTTL = 3600L;
    private final PmAdapter.Inquisitor inquisitor2xx;
    private final PmAdapter.Inquisitor inquisitor5xx;
    private final PmAdapter.Inquisitor inquisitorTm;
    private final PmAdapter.Inquisitor inquisitorPfe;

    private FmAlarmService alarmService;
    private String serviceName;

    private Map<String, String> faultyNfInstances = new ConcurrentHashMap<>();
    private BehaviorSubject<AlarmHandler> alarmHandler = BehaviorSubject.create();
    private List<DataOfNfInstance> dataPerNfList = new ArrayList<>();
    private BehaviorSubject<Optional<EricssonSepp>> configFlow;
    private Map<String, List<String>> nfData = new HashMap<>();
    private boolean code2xxChecked;
    private boolean code5xxChecked;
    private boolean timeoutChecked;
    private boolean pfeChecked;
    private BehaviorSubject<Boolean> dChecked = BehaviorSubject.createDefault(false);
    private Optional<EricssonSepp> seppCfg;

    private static class FaultyNfInstance
    {
        private String additionalInformation;
        private String alarmSource;
        private String instanceId;

        public FaultyNfInstance(DataOfNfInstance dataPerNf)
        {
            super();
            this.alarmSource = constructSource(dataPerNf);
            this.additionalInformation = constructAdditionalInfo(dataPerNf);
            this.instanceId = dataPerNf.getNfInstanceId();
        }

        private String constructSource(DataOfNfInstance dataPerNf)
        {
            if (dataPerNf.getNfType().equals("DISCOVERED_NF") || dataPerNf.getNfType().equals("STATIC_NF"))
            {
                return "nf-pool=" + dataPerNf.getPoolName() + ",nf-instance-id=" + dataPerNf.getNfInstanceId();
            }
            else if (dataPerNf.getNfType().equals("DISCOVERED_SCP") || dataPerNf.getNfType().equals("STATIC_SCP"))
            {
                return "scp-pool=" + dataPerNf.getPoolName() + ",scp-instance-id=" + dataPerNf.getNfInstanceId();
            }
            else
            {
                return "sepp-pool=" + dataPerNf.getPoolName() + ",sepp-instance-id=" + dataPerNf.getNfInstanceId();
            }
        }

        public String constructAdditionalInfo(DataOfNfInstance dataPerNf)
        {
            var type = dataPerNf.getNfType();

            StringBuilder additionalInfo = new StringBuilder();

            additionalInfo.append("nf=sepp-function,nf-instance=")
                          .append(dataPerNf.getNfInst())
                          .append(",nf-pool=")
                          .append(dataPerNf.getPoolName())
                          .append(",");

            if (type.equals("STATIC_NF"))
                additionalInfo.append("static-nf-instance=")
                              .append(dataPerNf.getNfName())
                              .append(",static-nf-instance-id=")
                              .append(dataPerNf.getNfInstanceId());
            else if (type.equals("DISCOVERED_NF"))
                additionalInfo.append("discovered-nf-instance=").append(dataPerNf.getNfInstanceId());
            else if (type.equals("STATIC_SCP"))
                additionalInfo.append("static-scp-instance=")
                              .append(dataPerNf.getNfName())
                              .append(",static-scp-instance-id=")
                              .append(dataPerNf.getNfInstanceId());
            else if (type.equals("DISCOVERED_SCP"))
                additionalInfo.append("discovered-scp-instance=").append(dataPerNf.getNfInstanceId());
            else // SEPP
                additionalInfo.append("static-sepp-instance=")
                              .append(dataPerNf.getNfName())
                              .append(",static-sepp-instance-id=")
                              .append(dataPerNf.getNfInstanceId());

            return additionalInfo.toString();
        }

        public String getAlarmSource()
        {
            return this.alarmSource;
        }

        /**
         * @return the additionalInfo
         */
        public String getAdditionalInfo()
        {
            return this.additionalInformation;
        }

        public String getInstanceId()
        {
            return this.instanceId;
        }

    }

    /**
     * This class is used to create an object for each NF in order to store all the
     * data that are required for the raising and ceasing of
     * ScpNfInstanceUnavailable alarm
     */
    private static class DataOfNfInstance
    {

        private String poolName;
        private String nfInstanceId;
        private String nfName;
        private String nfInst;
        private String nfType;
        private Double valueOf2xx; // number of 2xx failures
        private Double valueOf5xx; // number of 5xx failures
        private Double valueOfTm; // number of timeouts
        private Double valueOfPfe; // number of pending failure eject
        private Double threshold;
        private Double lastAlarm; // the sum of valueOf5xx, valueOfTm and valueOfPfe when the alarm is ceased
        private Boolean raised;
        private Boolean ceased;

        public DataOfNfInstance(String nfInstanceId,
                                String poolName,
                                String nfType,
                                Double valueOf2xx,
                                Double valueOf5xx,
                                Double valueOfTm,
                                Double valueOfPfe,
                                Double threshold,
                                String nfName,
                                String nfInst)
        {
            this.poolName = poolName;
            this.nfInstanceId = nfInstanceId;
            this.nfType = nfType;
            this.nfName = nfName;
            this.nfInst = nfInst;
            this.valueOf2xx = valueOf2xx;
            this.valueOf5xx = valueOf5xx;
            this.valueOfTm = valueOfTm;
            this.valueOfPfe = valueOfPfe;
            this.threshold = threshold;
            this.lastAlarm = -1.0;
            this.ceased = false;
            this.raised = false;
        }

        /**
         * @return The poolName value
         */
        public String getPoolName()
        {
            return this.poolName;
        }

        /**
         * @return The nfInstanceId value
         */
        public String getNfInstanceId()
        {
            return this.nfInstanceId;
        }

        /**
         * @return The nfName value
         */
        public String getNfName()
        {
            return this.nfName;
        }

        /**
         * @return The nfName value
         */
        public String getNfInst()
        {
            return this.nfInst;
        }

        /**
         * @return The nfType value
         */
        public String getNfType()
        {
            return this.nfType;
        }

        /**
         * @return The threshold value
         */
        public Double getThreshold()
        {
            return this.threshold;
        }

        /**
         * Set the threshold value
         */
        public void setThreshold(double threshold)
        {
            this.threshold = threshold;
        }

        /**
         * @return The lastAlarm value
         */
        public Double getLastAlarm()
        {
            return this.lastAlarm;
        }

        /**
         * Set the lastAlarm value
         * 
         * @param lastAlarm
         */
        public void setLastAlarm(double lastAlarm)
        {
            this.lastAlarm = lastAlarm;
        }

        /**
         * @return The raised value
         */
        public Boolean getRaised()
        {
            return this.raised;
        }

        /**
         * Set the raised value
         * 
         * @param raised
         */
        public void setRaised(Boolean raised)
        {
            this.raised = raised;
        }

        /**
         * @return The ceased value
         */
        public Boolean getCeased()
        {
            return this.ceased;
        }

        /**
         * Set the raised value
         * 
         * @param raised
         */
        public void setCeased(Boolean ceased)
        {
            this.ceased = ceased;
        }

        /**
         * @return The valueOf2xx value
         */
        public Double getValueOf2xx()
        {
            return this.valueOf2xx;
        }

        /**
         * Set the valueOf2xx value
         *
         * @param raised
         */
        public void setValueOf2xx(Double valueOf2xx)
        {
            this.valueOf2xx = valueOf2xx;
        }

        /**
         * @return The valueOf5xx value
         */
        public Double getValueOf5xx()
        {
            return this.valueOf5xx;
        }

        /**
         * Set the valueOf5xx value
         * 
         * @param raised
         */
        public void setValueOf5xx(Double valueOf5xx)
        {
            this.valueOf5xx = valueOf5xx;
        }

        /**
         * @return The valueOfTm value
         */
        public Double getValueOfTm()
        {
            return this.valueOfTm;
        }

        /**
         * Set the setValueOfTm value
         * 
         * @param raised
         */
        public void setValueOfTm(Double valueOfTm)
        {
            this.valueOfTm = valueOfTm;
        }

        /**
         * @return The valueOfPfe value
         */
        public Double getValueOfPfe()
        {
            return this.valueOfPfe;
        }

        /**
         * Set the setValueOfPfe value
         * 
         * @param raised
         */
        public void setValueOfPfe(Double valueOfPfe)
        {
            this.valueOfPfe = valueOfPfe;
        }
    }

    /**
     * This class is used to handle all the 'DataOfNfInstance' objects that have
     * been created for each NF
     */
    private static class AlarmHandler
    {
        private List<DataOfNfInstance> dataPerNfList;

        public AlarmHandler(List<DataOfNfInstance> dataPerNfList)
        {
            this.dataPerNfList = dataPerNfList;
        }

        public List<DataOfNfInstance> getDataPerNfList()
        {
            return this.dataPerNfList;
        }

    }

    public NfInstanceAlarmHandlerUnavailable(final Vertx vertx,
                                             String serviceName,
                                             FmAlarmService alarmService,
                                             BehaviorSubject<Optional<EricssonSepp>> configFlow)
    {
        super();
        this.serviceName = serviceName;
        this.alarmService = alarmService;
        this.configFlow = configFlow;

        var class2xxChanges = Query.metric("sepp_egress_pool_nf_class2_sum");
        var class5xxChanges = Query.metric("sepp_egress_pool_nf_class5_sum");
        var classTimeoutChanges = Query.metric("sepp_egress_pool_nf_timeout_sum");
        var classPendingFailureEjectChanges = Query.metric("sepp_egress_pool_nf_pending_failure_eject_sum");

        this.inquisitor2xx = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                    Integer.parseInt(EnvVars.get(ERIC_PM_SERVER_SERVICE_PORT)),
                                                                    EnvVars.get(ERIC_PM_SERVER_SERVICE_HOST)),
                                                      POLLING_INTERVAL_MILLIS,
                                                      class2xxChanges);
        this.inquisitor5xx = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                    Integer.parseInt(EnvVars.get(ERIC_PM_SERVER_SERVICE_PORT)),
                                                                    EnvVars.get(ERIC_PM_SERVER_SERVICE_HOST)),
                                                      POLLING_INTERVAL_MILLIS,
                                                      class5xxChanges);
        this.inquisitorTm = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                   Integer.parseInt(EnvVars.get(ERIC_PM_SERVER_SERVICE_PORT)),
                                                                   EnvVars.get(ERIC_PM_SERVER_SERVICE_HOST)),
                                                     POLLING_INTERVAL_MILLIS,
                                                     classTimeoutChanges);
        this.inquisitorPfe = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                    Integer.parseInt(EnvVars.get(ERIC_PM_SERVER_SERVICE_PORT)),
                                                                    EnvVars.get(ERIC_PM_SERVER_SERVICE_HOST)),
                                                      POLLING_INTERVAL_MILLIS,
                                                      classPendingFailureEjectChanges);

    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            log.info("EventHandler for NF Instance Unavailable Alarm started.");

            configFlow.retry().subscribe(this::configChanges, err -> log.error("Error:{}.", err.toString()));

            this.inquisitor2xx.getData().doOnNext(this::check2xxChanges).subscribe(d ->
            {
            }, t -> log.error("Error with cause: {}", Utils.toString(t, log.isDebugEnabled())));
            this.inquisitor5xx.getData().doOnNext(this::check5xxChanges).subscribe(d ->
            {
            }, t -> log.error("Error with cause: {}", Utils.toString(t, log.isDebugEnabled())));
            this.inquisitorTm.getData().doOnNext(this::checkTimeoutChanges).subscribe(d ->
            {
            }, t -> log.error("Error with cause: {}", Utils.toString(t, log.isDebugEnabled())));
            this.inquisitorPfe.getData().doOnNext(this::checkPendingFailureEjectChanges).subscribe(d ->
            {
            }, t -> log.error("Error with cause: {}", Utils.toString(t, log.isDebugEnabled())));

            this.dChecked.retry().doOnNext(this::dataChecked).subscribe(d ->
            {
            }, t -> log.error("Error with cause: {}", Utils.toString(t, log.isDebugEnabled())));

            this.alarmHandler.retry().doOnNext(this::alarmController).subscribe(d ->
            {
            }, t -> log.error("Error retrieving alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));

        })
                          .mergeWith(this.inquisitor2xx.start())
                          .mergeWith(this.inquisitor5xx.start())
                          .mergeWith(this.inquisitorTm.start())
                          .mergeWith(this.inquisitorPfe.start());
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            // Cease all the alarms
            this.dataPerNfList.stream().filter(Objects::nonNull).forEach(nf ->
            {
                try
                {
                    alarmNfInstanceUnavailableCease(new FaultyNfInstance(nf));
                }
                catch (JsonProcessingException e)
                {
                    log.error("Caught Exception while ceasing the alarm {}.", e.toString());
                }
            });
            // Stop the inquisitors from fetching data from the PM
            this.inquisitor2xx.stop().blockingAwait();
            this.inquisitor5xx.stop().blockingAwait();
            this.inquisitorTm.stop().blockingAwait();
            this.inquisitorPfe.stop().blockingAwait();

            log.info("Stop.");
        });
    }

    /**
     * Create a method which verifies that the data from the
     * envoy_upstream_rq_xx_per_nf counter with 2xx,5xx envoy status code, also from
     * envoy_upstream_rq_timeout_per_nf counter and from
     * envoy_upstream_rq_pending_failure_eject_per_nf have been fetched
     * 
     * @param dataUpdated
     * 
     * 
     */
    private void dataChecked(Boolean dataUpdated)
    {
        if (Boolean.TRUE.equals(dataUpdated))
        {
            this.code2xxChecked = false;
            this.code5xxChecked = false;
            this.timeoutChecked = false;
            this.pfeChecked = false;
            this.alarmHandler.onNext(new AlarmHandler(this.dataPerNfList));
        }
    }

    /**
     * Create a method which updates the data for each NF according to the
     * envoy_upstream_rq_xx_per_nf counter with 2xx envoy status code. If a 2xx is
     * received, then for this NF the value that ceases the alarm is set to true.
     * 
     * @param data
     */
    private synchronized void check2xxChanges(List<PmAdapter.Query.Response.Data> data)
    {
        this.code2xxChecked = false;
        if (data != null && !this.nfData.isEmpty())
        {
            data.get(0).getResult().parallelStream().filter(Objects::nonNull).forEach(dataPerNf ->
            {

                var nfId = dataPerNf.getMetric().get("nf_instance_id");
                var value = dataPerNf.getValue().get(1);

                this.dataPerNfList.stream().filter(nf -> nf.getNfInstanceId().equals(nfId)).findFirst().ifPresentOrElse(nf ->
                {
                    if (nf.getValueOf2xx() < value)
                        nf.setCeased(true);
                    nf.setValueOf2xx(value);
                }, () ->
                {
                    if (nfData.containsKey(nfId))
                    {
                        var dataOfNfInstance = colletionOfDataPerNf(dataPerNf, "2xx");
                        this.dataPerNfList.add(dataOfNfInstance);
                    }
                });

            });
        }
        this.code2xxChecked = true;
        var dataUpdated = this.code5xxChecked && this.timeoutChecked && this.pfeChecked;
        this.dChecked.onNext(dataUpdated);
    }

    /**
     * Create a method which updates the data for each NF according to the
     * envoy_upstream_rq_xx_per_nf counter with 5xx envoy status code.If a 5xx is
     * received, then for this NF the value which counts these failures is updated.
     * 
     * @param data
     */
    private synchronized void check5xxChanges(List<PmAdapter.Query.Response.Data> data)
    {

        this.code5xxChecked = false;
        if (data != null && !this.nfData.isEmpty())
        {
            data.get(0).getResult().parallelStream().filter(Objects::nonNull).forEach(dataPerNf ->
            {
                var nfId = dataPerNf.getMetric().get("nf_instance_id");
                var value = dataPerNf.getValue().get(1);

                this.dataPerNfList.stream().filter(nf -> nf.getNfInstanceId().equals(nfId)).findFirst().ifPresentOrElse(nf -> nf.setValueOf5xx(value), () ->
                {
                    if (nfData.containsKey(nfId))
                    {
                        var dataOfNfInstance = colletionOfDataPerNf(dataPerNf, "5xx");
                        this.dataPerNfList.add(dataOfNfInstance);
                    }
                });
            });
        }
        this.code5xxChecked = true;

        var dataUpdated = this.code2xxChecked && this.timeoutChecked && this.pfeChecked;
        this.dChecked.onNext(dataUpdated);
    }

    /**
     * Create a method which updates the data for each NF according to the
     * envoy_upstream_rq_timeout_per_nf counter.If a timeout is received, then for
     * this NF the value which counts these failures is updated.
     * 
     * @param data
     */
    private synchronized void checkTimeoutChanges(List<PmAdapter.Query.Response.Data> data)
    {

        this.timeoutChecked = false;
        if (data != null && !this.nfData.isEmpty())
        {
            data.get(0).getResult().parallelStream().filter(Objects::nonNull).forEach(dataPerNf ->
            {
                var nfId = dataPerNf.getMetric().get("nf_instance_id");
                var value = dataPerNf.getValue().get(1);
                this.dataPerNfList.stream().filter(nf -> nf.getNfInstanceId().equals(nfId)).findFirst().ifPresentOrElse(nf -> nf.setValueOfTm(value), () ->
                {
                    if (nfData.containsKey(nfId))
                    {
                        var dataOfNfInstance = colletionOfDataPerNf(dataPerNf, "Timeout");
                        this.dataPerNfList.add(dataOfNfInstance);
                    }
                });
            });
        }

        this.timeoutChecked = true;
        var dataUpdated = this.code2xxChecked && this.code5xxChecked && this.pfeChecked;
        this.dChecked.onNext(dataUpdated);
    }

    /**
     * Create a method which updates the data for each NF according to the
     * envoy_upstream_rq_pending_failure_eject_per_nf counter.If this counter is
     * stepped, then for this NF the value which counts these failures is updated.
     * 
     * @param data
     */
    private synchronized void checkPendingFailureEjectChanges(List<PmAdapter.Query.Response.Data> data)
    {

        this.pfeChecked = false;
        if (data != null && !this.nfData.isEmpty())
        {
            data.get(0).getResult().parallelStream().filter(Objects::nonNull).forEach(dataPerNf ->
            {
                var nfId = dataPerNf.getMetric().get("nf_instance_id");
                var value = dataPerNf.getValue().get(1);
                this.dataPerNfList.stream().filter(nf -> nf.getNfInstanceId().equals(nfId)).findFirst().ifPresentOrElse(nf -> nf.setValueOfPfe(value), () ->
                {
                    if (nfData.containsKey(nfId))
                    {
                        var dataOfNfInstance = colletionOfDataPerNf(dataPerNf, "Pfe");
                        this.dataPerNfList.add(dataOfNfInstance);
                    }
                });
            });
        }

        this.pfeChecked = true;
        var dataUpdated = this.code2xxChecked && this.code5xxChecked && this.timeoutChecked;
        this.dChecked.onNext(dataUpdated);
    }

    /**
     * In this method the alarm is set to raised or ceased based on the latest
     * changes in the counters. If changes exist on the 5xx or timeout counters and
     * these failures exceed the threshold value then the alarm should be raised. If
     * changes exist on the 2xx counter in the last 15s, without any failure, then
     * the alarm should be ceased. When the alarm ceases the value of lastAlarm is
     * updated with the sum of 5xx and timeout failures.
     * 
     * @param alarmHandler
     */
    private void alarmController(AlarmHandler alarmHandler)
    {
        log.debug("Alarm controller per NF ");
        alarmHandler.getDataPerNfList().stream().filter(Objects::nonNull).forEach(nf ->
        {
            if (nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() < nf.getLastAlarm())
            {
                nf.setLastAlarm(nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe());
            }
            var checkedFailures = nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() - nf.getLastAlarm() < nf.getThreshold();
            if (!checkedFailures)
            {
                nf.setRaised(true);
            }

            if (Boolean.TRUE.equals(nf.getRaised()) && (nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() != nf.getLastAlarm()))
            {
                nf.setLastAlarm(nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe());
                nf.setCeased(false);
                try
                {
                    handleUnavailableStatus(nf);
                }
                catch (JsonProcessingException e)
                {
                    log.error("Caught Exception while handling unavailable status {}.", e.toString());
                }
            }
            if (Boolean.TRUE.equals(nf.getCeased()) && (nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() == nf.getLastAlarm()))
            {
                try
                {
                    handleAvailableStatus(nf);
                }
                catch (JsonProcessingException e)
                {
                    log.error("Caught Exception while handling available status {}.", e.toString());
                }
                nf.setRaised(false);
                nf.setCeased(false);
            }

        });
    }

    /**
     * This method is used in order to fetch the latest data for each NF from
     * configuration. The fetched data are : Threshold per pool , ip and port for
     * each NF.
     * 
     * @param seppCfg
     */
    private synchronized void configChanges(Optional<EricssonSepp> seppCfg)
    {
        log.debug("Configuration update received.");
        this.seppCfg = seppCfg;
        if (this.seppCfg.isPresent())
        {
            this.nfData = CommonConfigUtils.getDataPerNf(this.seppCfg.get().getEricssonSeppSeppFunction());
            if (this.nfData == null || this.nfData.isEmpty())
            {
                log.debug("No pool has configured the thershold value");
                this.dataPerNfList.stream().filter(Objects::nonNull).forEach(nf ->
                {
                    try
                    {
                        handleAvailableStatus(nf);
                        nf.setRaised(false);
                    }
                    catch (JsonProcessingException e)
                    {
                        log.error("Caught Exception while handling available status during config changes {}.", e.toString());
                    }
                });
                this.dataPerNfList.clear();
            }
            else
            {
                this.dataPerNfList.stream().filter(Objects::nonNull).forEach(nf ->
                {
                    if (!this.nfData.containsKey(nf.getNfInstanceId()))
                    {
                        log.debug("The NF with id {}, has not enabled the SeppNfInstanceUnavailable alarm.", nf.getNfInstanceId());
                        try
                        {
                            handleAvailableStatus(nf);
                            nf.setRaised(false);
                        }
                        catch (JsonProcessingException e)
                        {
                            log.error("Caught Exception while handling available status during config changes {}.", e.toString());
                        }
                    }
                    else
                    {
                        var threshold = Double.valueOf(this.nfData.get(nf.getNfInstanceId()).get(0));
                        nf.setThreshold(threshold);
                        if (nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() > nf.getThreshold())
                        {
                            nf.setLastAlarm(nf.getValueOf5xx() + nf.getValueOfTm() + nf.getValueOfPfe() - 1.0);
                        }
                    }
                });
            }
        }
        else
        {
            this.nfData.clear();
            this.dataPerNfList.clear();
        }
    }

    /**
     * This method is used to create a new DataOfNfInstance object for each NF. The
     * param 'dataPerNf' contains all the information that fetched from PM. The
     * param 'count' is used to distinguish the value that received from the three
     * counters
     * 
     * @param dataPerNf
     * @param counter
     * @return
     */
    private DataOfNfInstance colletionOfDataPerNf(Result dataPerNf,
                                                  String counter)
    {

        var nfId = dataPerNf.getMetric().get("nf_instance_id");
        var poolName = dataPerNf.getMetric().get("pool_name");
        var value = dataPerNf.getValue().get(1);
        var nfName = this.nfData.get(nfId).get(2);
        var nfInst = this.nfData.get(nfId).get(3);
        // nfData contains the address,nfType,nfName,nfInst and the threshold per NF
        var threshold = Double.valueOf(this.nfData.get(nfId).get(0));
        var nfType = this.nfData.get(nfId).get(1);
        var valueOf2xx = 0.0;
        var valueOf5xx = 0.0;
        var valueOfTm = 0.0;
        var valueOfPfe = 0.0;

        if (counter.equals("5xx"))
            valueOf5xx = value;
        else if (counter.equals("Timeout"))
            valueOfTm = value;
        else if (counter.equals("Pfe"))
            valueOfPfe = value;
        else
            valueOf2xx = value;
        return new DataOfNfInstance(nfId, poolName, nfType, valueOf2xx, valueOf5xx, valueOfTm, valueOfPfe, threshold, nfName, nfInst);

    }

    private void handleUnavailableStatus(DataOfNfInstance nf) throws JsonProcessingException
    {
        log.debug("handleUnavailableStatus, nfInstanceId: {}", nf.getNfInstanceId());

        FaultyNfInstance faultyNfInstance = new FaultyNfInstance(nf);
        alarmNfInstanceUnavailableRaise(faultyNfInstance);

        log.debug("New NF Instance Fault Indication received.");
        faultyNfInstances.put(faultyNfInstance.getInstanceId(), faultyNfInstance.getInstanceId());

    }

    private void handleAvailableStatus(DataOfNfInstance nf) throws JsonProcessingException
    {
        log.debug("handleAvailableStatus, nfInstanceId: {}", nf.getNfInstanceId());

        FaultyNfInstance nfInstance = new FaultyNfInstance(nf);
        if (faultyNfInstances.containsKey(nfInstance.getInstanceId()))
        {
            alarmNfInstanceUnavailableCease(nfInstance);
            faultyNfInstances.remove(nfInstance.getInstanceId());

        }

    }

    private void alarmNfInstanceUnavailableRaise(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var additionalInformation = new AdditionalInformationBuilder().withAdditionalProperty("FULL_RDN", faultyNfInstance.getAdditionalInfo()).build();
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_INSTANCE_UNAVAILABLE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withDescription(NF_INSTANCE_UNAVAILABLE_DESC)
                                                          .withFaultyResource(faultyNfInstance.getAlarmSource())
                                                          .withSeverity(Severity.MAJOR)
                                                          .withAdditionalInformation(additionalInformation)
                                                          .withExpiration((long) ALARMTTL)
                                                          .build();

        log.debug("Raise alarm with FaultIndication: {}", faultIndication);
        this.alarmService.raise(faultIndication)
                         .subscribe(() -> log.info("Raising alarm {}", NF_INSTANCE_UNAVAILABLE_ALARM),
                                    t -> log.error("Error raising alarm. Cause: {}", t.toString()));
    }

    private void alarmNfInstanceUnavailableCease(FaultyNfInstance faultyNfInstance) throws JsonProcessingException
    {
        var faultIndication = new FaultIndicationBuilder().withFaultName(NF_INSTANCE_UNAVAILABLE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withFaultyResource(faultyNfInstance.getAlarmSource())
                                                          .withDescription(NF_INSTANCE_UNAVAILABLE_DESC)
                                                          .build();
        log.debug("Cease alarm with FaultIndication: {}", faultIndication);
        this.alarmService.cease(faultIndication)
                         .subscribe(() -> log.info("Ceasing alarm {}", NF_INSTANCE_UNAVAILABLE_ALARM),
                                    t -> log.error("Error ceasing alarm. Cause: {}", t.toString()));
    }

}
