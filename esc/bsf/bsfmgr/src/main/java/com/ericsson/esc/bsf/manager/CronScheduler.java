package com.ericsson.esc.bsf.manager;

import java.util.Date;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class CronScheduler
{

    private static final Logger log = LoggerFactory.getLogger(CronScheduler.class);

    private CronScheduler()
    {
        throw new IllegalStateException("CronScheduler class");
    }

    /**
     * 
     * @param cronSpec
     * @return A flowable of ticks (scheduled Date) with the period described in
     *         given cron expression.
     */
    public static Flowable<Date> create(CronExpression cronSpec)
    {

        return Flowable.create(emitter ->
        {

            final var job = JobBuilder.newJob(TheJob.class).build();
            DirectSchedulerFactory.getInstance().createVolatileScheduler(1);
            final var scheduler = DirectSchedulerFactory.getInstance().getScheduler();
            final var trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cronSpec)).build();
            final var jobFactory = new JobFactory()
            {

                @Override
                public Job newJob(TriggerFiredBundle bundle,
                                  Scheduler scheduler) throws SchedulerException
                {
                    return ctx ->
                    {
                        if (!emitter.isCancelled())
                        {
                            try
                            {
                                emitter.onNext(ctx.getScheduledFireTime());
                            }
                            catch (Exception e)
                            {
                                emitter.onError(e);
                            }
                        }
                    };
                }

            };

            scheduler.setJobFactory(jobFactory);
            scheduler.scheduleJob(job, trigger);

            log.info("Starting scheduler");
            scheduler.start();

            emitter.setCancellable(() ->
            {
                log.info("Shutting down scheduler");
                scheduler.shutdown();
            });

        }, BackpressureStrategy.ERROR);
    }

    private static class TheJob implements Job
    {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            // This is a Dummy job that does nothing. Still it is needed to create the
            // scheduler.
        }

    }

}
