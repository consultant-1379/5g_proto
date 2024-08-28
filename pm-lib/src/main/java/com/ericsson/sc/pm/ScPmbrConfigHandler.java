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
 * Created on: May 27, 2021
 *     Author: eotkkni
 */

package com.ericsson.sc.pm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.sc.pm.model.pmbr.Group;
import com.ericsson.sc.pm.model.pmbr.Job;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;

/**
 * @param <T>
 * @param <T>
 * 
 */
public class ScPmbrConfigHandler
{
    private static final Logger log = LoggerFactory.getLogger(ScPmbrConfigHandler.class);
    private static final String PMBR_CONFIG = "/cm/api/v1/configurations/adp-gs-pm-br";

    private static final String PM_GROUPS = "/ericsson-pm:pm/group/";
    private static final String PM_JOBS = "/ericsson-pm:pm/job/";
    private static final String PM_ERICSSON_PM_KEY = "ericsson-pm:pm";
    private static final String PM_JOB_KEY = "job";
    private static final String PM_GROUP_KEY = "group";

    private static final String FAILED_PATCH_OPERATION = "Could not update PM Bulk Reporter configuration with PATCH operation. Cause: {}";

    private static final long DELAY = 5 * 1000L;
    private static final long RETRIES_TIMEOUT = 60;

    private final CmmPatch cmPatch;
    private final ScPmbrConfigGenerator pmbrCfgGen;

    public ScPmbrConfigHandler(CmmPatch cmPatch)
    {
        this.cmPatch = cmPatch;
        this.pmbrCfgGen = new ScPmbrConfigGenerator();
    }

    public Completable createPmbrJobPatches()
    {

        List<PatchItem> patches = this.pmbrCfgGen.getPmbrJobPatches();

        return this.cmPatch.get(PMBR_CONFIG).flatMapCompletable(resp ->
        {
            // Extract jobs from CMM response
            var jobs = getJobsFromCmmResponse(resp);
            log.debug("PMBR Configuration Jobs: {}", jobs);

            // Add new jobs and replace any existing ones
            final var indexedPatches = patches.stream().map(patch -> jobModification(patch, jobs)).toList();

            // Send job patch to CMM
            return this.cmPatch.patch(PMBR_CONFIG, indexedPatches)
                               .doOnError(e -> log.warn(FAILED_PATCH_OPERATION, e.toString()))
                               .retryWhen(new RetryFunction().withDelay(DELAY) // retry after 5 seconds
                                                             .withRetries(RETRIES_TIMEOUT) // give up after 5 minutes
                                                             .withRetryAction((error,
                                                                               retry) -> log.warn("Could not deploy default PM Bulk reporter job configuration, retrying: {}",
                                                                                                  retry,
                                                                                                  error))
                                                             .create())
                               .doOnSubscribe(disp -> log.info("Deploying PM Bulk reporter job configuration"))
                               .doOnComplete(() -> log.info("PM Bulk Reporter job configuration updated with PATCH operation."));
        });

    }

    public Set<Job> getJobsFromCmmResponse(String resp)
    {
        Set<Job> jobs = new LinkedHashSet<>();
        var jsonBody = new JsonObject(resp);

        if (!jsonBody.containsKey("data") || !jsonBody.getJsonObject("data").containsKey(PM_ERICSSON_PM_KEY)
            || !jsonBody.getJsonObject("data").getJsonObject(PM_ERICSSON_PM_KEY).containsKey(PM_JOB_KEY))
        {
            log.warn("CMM Json response does not contain key: data/" + PM_GROUP_KEY + "/" + PM_JOB_KEY);
            return jobs;
        }

        JsonObject jsonData = (JsonObject) jsonBody.getValue("data");

        var jobArray = jsonData.getJsonObject(PM_ERICSSON_PM_KEY).getJsonArray(PM_JOB_KEY);

        for (int i = 0; i < jobArray.size(); i++)
        {
            jobs.add(Jackson.om().convertValue(jobArray.getJsonObject(i).getMap(), Job.class));
        }

        return jobs;
    }

    public PatchItem jobModification(PatchItem patchObject,
                                     Set<Job> jobsSet)
    {
        var index = 0;
        var ipatch = new PatchItem(PatchOperation.ADD, PM_JOBS + "-", "", patchObject.getValue());
        var jobJsonFromPatch = new JsonObject(patchObject.getValue().toString());
        log.debug("The system-defined-jobs are: {}", patchObject);

        for (var job : jobsSet)
        {
            if (job.getName().equals(jobJsonFromPatch.getValue("name").toString()))
            {
                ipatch.setOp(PatchOperation.REPLACE);
                var jobRequestedJobState = job.getRequestedJobState();

                ipatch.setValue(jobJsonFromPatch.put("requested-job-state", jobRequestedJobState.getValue()));
                ipatch.setPath(PM_JOBS + index);
                log.debug("The patch {} will be replaced by the job {}.", ipatch.getValue(), job);
                log.debug("{} action was excecuted for the patch {}.", ipatch.getOp(), ipatch.getValue());

                break;
            }

            index++;
        }

        log.debug("Patch Operation: {}, path: {}, from: {}", ipatch.getOp(), ipatch.getPath(), ipatch.getFrom());
        log.debug("Patch Value: {}", ipatch.getValue());

        return ipatch;
    }

    public Completable createPmbrGroupPatches()
    {

        List<PatchItem> patches = this.pmbrCfgGen.getPmbrGroupPatches();

        return this.cmPatch.get(PMBR_CONFIG).flatMapCompletable(resp ->
        {
            // Extract groups from CMM response
            var groups = getGroupsFromCmmResponse(resp);
            log.debug("PMBR Configuration Groups: {}", groups);

            // Add new groups and replace any existing ones
            final var indexedPatches = patches.stream().map(patch -> groupModification(patch, groups)).toList();

            // Send group patch to CMM
            return this.cmPatch.patch(PMBR_CONFIG, indexedPatches)
                               .doOnError(e -> log.warn(FAILED_PATCH_OPERATION, e.toString()))
                               .retryWhen(new RetryFunction().withDelay(DELAY) // retry after 5 seconds
                                                             .withRetries(RETRIES_TIMEOUT) // give up after 5 minutes
                                                             .withRetryAction((error,
                                                                               retry) -> log.warn("Could not deploy default PM Bulk reporter group configuration, retrying: {}",
                                                                                                  retry,
                                                                                                  error))
                                                             .create())
                               .doOnSubscribe(disp -> log.info("Deploying PM Bulk reporter group configuration"))
                               .doOnComplete(() -> log.info("PM Bulk Reporter group configuration updated with PATCH operation."));
        });
    }

    public Set<Group> getGroupsFromCmmResponse(String resp)
    {
        Set<Group> groups = new LinkedHashSet<>();
        var jsonBody = new JsonObject(resp);

        if (!jsonBody.containsKey("data") || !jsonBody.getJsonObject("data").containsKey(PM_ERICSSON_PM_KEY)
            || !jsonBody.getJsonObject("data").getJsonObject(PM_ERICSSON_PM_KEY).containsKey(PM_GROUP_KEY))
        {
            log.warn("CMM Json response does not contain key: data/" + PM_GROUP_KEY + "/" + PM_GROUP_KEY);
            return groups;
        }

        JsonObject jsonData = (JsonObject) jsonBody.getValue("data");

        var groupArray = jsonData.getJsonObject(PM_ERICSSON_PM_KEY).getJsonArray(PM_GROUP_KEY);

        for (int i = 0; i < groupArray.size(); i++)
        {
            groups.add(Jackson.om().convertValue(groupArray.getJsonObject(i).getMap(), Group.class));
        }

        return groups;
    }

    public PatchItem groupModification(PatchItem patchObject,
                                       Set<Group> groupSet)
    {
        var index = 0;
        var ipatch = new PatchItem(PatchOperation.ADD, PM_GROUPS + "-", "", patchObject.getValue());

        for (var group : groupSet)
        {
            if (group.getName().matches(((JsonObject) patchObject.getValue()).getValue("name").toString()))
            {
                ipatch.setOp(PatchOperation.REPLACE);
                ipatch.setPath(PM_GROUPS + index);

                break;
            }

            index++;
        }

        log.debug("Patch Operation: {}, path: {}, from: {}", ipatch.getOp(), ipatch.getPath(), ipatch.getFrom());
        log.debug("Patch Value: {}", ipatch.getValue());

        return ipatch;
    }
}
