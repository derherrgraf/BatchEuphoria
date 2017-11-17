/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.jobs.direct.synchronousexecution

import com.google.common.collect.LinkedHashMultimap
import de.dkfz.roddy.config.ResourceSet
import de.dkfz.roddy.execution.BEExecutionService
import de.dkfz.roddy.execution.io.ExecutionResult
import de.dkfz.roddy.execution.jobs.*
import de.dkfz.roddy.tools.LoggerWrapper
import groovy.transform.CompileStatic

/**
 */
@CompileStatic
class DirectSynchronousExecutionJobManager extends BatchEuphoriaJobManager<DirectCommand> {

    public static final LoggerWrapper logger = LoggerWrapper.getLogger(DirectSynchronousExecutionJobManager.class.getName())

    DirectSynchronousExecutionJobManager(BEExecutionService executionService, JobManagerCreationParameters parms) {
        super(executionService, parms)
    }

    @Override
    void createUpdateDaemonThread(int interval) {
        //Not necessary, a command / job knows its state in local execution
    }

    @Override
    DirectCommand createCommand(BEJob job, String jobName, List<ProcessingParameters> processingCommands, File tool, Map<String, String> parameters, List<String> dependencies) {
        return new DirectCommand(this, job, tool.getName(), null, job.getParameters(), null, null, dependencies, tool.getAbsolutePath())
    }

    @Override
    BEJob parseToJob(String commandString) {
        return null
    }

    @Override
    GenericJobInfo parseGenericJobInfo(String command) {
        return null
    }

    @Override
    BEJobResult convertToArrayResult(BEJob arrayChildJob, BEJobResult parentJobsResult, int arrayIndex) {
        throw new RuntimeException("Not implemented yet! " + this.getClass().getName() + ".convertToArrayResult()")
    }

    @Override
    void updateJobStatus() {

    }

    @Override
    void queryJobAbortion(List<BEJob> executedJobs) {

    }

    @Override
    void addJobStatusChangeListener(BEJob job) {

    }

    @Override
    String getLogFileWildcard(BEJob job) {
        return "*"
    }

    @Override
    String getStringForQueuedJob() {
        return null
    }

    @Override
    String getStringForJobOnHold() {
        return null
    }

    @Override
    String getStringForRunningJob() {
        return null
    }

    @Override
    String getJobIdVariable() {
        return ""
    }

    String getSpecificJobIDIdentifier() {
        logger.severe("BEJob id for " + getClass().getName() + " should be configurable")
        return '"$$"'
    }

    String getSpecificJobArrayIndexIdentifier() {
        logger.severe("BEJob arrays are not supported in " + getClass().getName())
        return "0"
    }

    String getSpecificJobScratchIdentifier() {
        logger.severe("BEJob scratch for " + getClass().getName() + " should be configurable")
        return '/data/roddyScratch/$$'
    }

    String getJobNameVariable() {
        return '$'
    }

    @Override
    String getQueueVariable() {
        return ''
    }

    @Override
    String getNodeFileVariable() {
        return null
    }

    @Override
    String getSubmitHostVariable() {
        return null
    }

    @Override
    String getSubmitDirectoryVariable() {
        return null
    }

    @Override
    Map<BEJob, JobState> queryJobStatus(List<BEJob> jobs) {
        (jobs?.collectEntries { BEJob job -> [job, JobState.UNKNOWN] } ?: [:]) as Map<BEJob, JobState>
    }

    @Override
    BEJobResult runJob(BEJob job) {
        // Some of the parent jobs are in a bad state!
        Command command = createCommand(job, job.tool, [], [:])
        BEJobResult jobResult
        BEJobID jobID
        ExecutionResult res
        boolean successful = false

        /** For direct execution, there might be parent jobs, which  failed or were aborted. Don't start, if this is the case.  **/
        if (job.parentJobs.findAll {
            BEJob pJob = it as BEJob
            !(pJob.getJobState() == JobState.COMPLETED_SUCCESSFUL || pJob.getJobState() == JobState.UNKNOWN)
        }
        ) {
            jobID = new BEFakeJobID(BEFakeJobID.FakeJobReason.NOT_EXECUTED)
            command.setExecutionID(jobID)
        } else {
            res = executionService.execute(command)
            jobID = new BEJobID(parseJobID(res.processID))
            successful = res.successful
            if (!successful)
                logger.sometimes("Execution of Job ${jobID} failed with exit code ${res.exitCode} and message ${res.resultLines}")
        }

        command.setExecutionID(jobID)
        jobResult = new BEJobResult(command, job, res, job.tool, job.parameters, job.parentJobs as List<BEJob>)
        job.setRunResult(jobResult)

        return jobResult
    }

    @Override
    ProcessingParameters convertResourceSet(BEJob job, ResourceSet resourceSet) {
        return new ProcessingParameters(LinkedHashMultimap.create())
    }

    @Override
    ProcessingParameters extractProcessingParametersFromToolScript(File file) {
        return null
    }

    @Override
    boolean executesWithoutJobSystem() {
        return true
    }

    @Override
    String parseJobID(String commandOutput) {
        return commandOutput
    }

    @Override
    String getSubmissionCommand() {
        return null
    }

    @Override
    Map<BEJob, JobState> queryJobStatus(List<BEJob> jobs, boolean forceUpdate) {
        return null
    }

    @Override
    Map<BEJobID, JobState> queryJobStatusAll(boolean forceUpdate) {
        return null;
    }

    @Override
    Map<BEJobID, JobState> queryJobStatusById(List<BEJobID> jobIds, boolean forceUpdate) {
        return null;
    }

    @Override
    Map<BEJobID, GenericJobInfo> queryExtendedJobState(List<BEJob> jobs, boolean forceUpdate) {
        return null;
    }

    @Override
    Map<BEJobID, GenericJobInfo> queryExtendedJobStateById(List<BEJobID> jobIds, boolean forceUpdate) {
        return null;
    }

    @Override
    JobState parseJobState(String stateString) {
        return null
    }

}
