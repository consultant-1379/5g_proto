package com.ericsson.sc.proxyal.service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.Flowable;
import io.reactivex.BackpressureStrategy;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScramblingKeysRotationService
{
    private BehaviorSubject<Date> configFlow;
    private Scheduler scheduler;
    private ConcurrentHashMap<String, String> activeKeys;

    private static ScramblingKeysRotationService service;

    private static final Logger log = LoggerFactory.getLogger(ScramblingKeysRotationService.class);

    private ScramblingKeysRotationService()
    {
        this.configFlow = BehaviorSubject.create();
        activeKeys = new ConcurrentHashMap<>();
        this.configFlow.onNext(new Date());
    }

    public static ScramblingKeysRotationService getInstance()
    {
        if (service == null)
        {
            service = new ScramblingKeysRotationService();
        }
        return service;
    }

    public void startRotation(Date date,
                              ConcurrentMap<String, String> activeKeys)
    {
        activeKeys.forEach((rp,
                            id) -> this.activeKeys.put(rp, id));// There must be a deep copy
        this.createScheduler(date);
    }

    /**
     * @return the activeKeys
     */
    public ConcurrentMap<String, String> getActiveKeys()
    {
        return activeKeys;
    }

    public Flowable<Date> getFlowable()
    {
        return this.configFlow.toFlowable(BackpressureStrategy.LATEST);
    }

    private void createScheduler(Date date)
    {
        log.debug("Create a Scheduler for the Rotation of Scrambling Keys");
        try
        {
            if (this.scheduler != null && !this.scheduler.isShutdown())
            {
                log.debug("Scheduler is shuting down");
                this.scheduler.shutdown();
            }

            log.debug("Initialize Scheduler for keys rotation");
            DirectSchedulerFactory.getInstance().createVolatileScheduler(1);
            this.scheduler = DirectSchedulerFactory.getInstance().getScheduler();
            final var job = JobBuilder.newJob(CustomJob.class).build();
            final var trigger = TriggerBuilder.newTrigger().startAt(date).build();

            final var jobFactory = new JobFactory()
            {

                @Override
                public Job newJob(TriggerFiredBundle bundle,
                                  Scheduler scheduler) throws SchedulerException
                {
                    return ctx ->
                    {

                        if (!configFlow.hasThrowable() && !configFlow.hasComplete())
                        {
                            try
                            {
                                log.debug("Scheduled job has been triggered");
                                configFlow.onNext(ctx.getScheduledFireTime());
                            }
                            catch (Exception e)
                            {
                                log.debug("Execution of job has been failed. Cause {}", e.getMessage());
                                configFlow.onError(e);
                            }
                        }
                    };
                }

            };

            this.scheduler.setJobFactory(jobFactory);
            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
            log.debug("Rotation scheduler has been started for date: {}", date);

        }
        catch (SchedulerException e)
        {
            log.error("Unable to start scheduler for scrambling keys rotation. Cause: {}", e.getMessage());
        }
    }

    public class CustomJob implements Job
    {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            // This is a Dummy job that does nothing. Still it is needed to create the
            // scheduler.
        }

    }

}
