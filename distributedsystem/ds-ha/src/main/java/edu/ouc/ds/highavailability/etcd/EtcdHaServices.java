package edu.ouc.ds.highavailability.etcd;

import java.io.IOException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.blob.BlobStore;
import org.apache.flink.runtime.checkpoint.CheckpointRecoveryFactory;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.highavailability.RunningJobsRegistry;
import org.apache.flink.runtime.jobmanager.SubmittedJobGraphStore;
import org.apache.flink.runtime.leaderelection.LeaderElectionService;
import org.apache.flink.runtime.leaderretrieval.LeaderRetrievalService;

public class EtcdHaServices implements HighAvailabilityServices {

    @Override
    public LeaderRetrievalService getResourceManagerLeaderRetriever() {
        return null;
    }

    @Override
    public LeaderRetrievalService getDispatcherLeaderRetriever() {
        return null;
    }

    @Override
    public LeaderRetrievalService getJobManagerLeaderRetriever(JobID jobID) {
        return null;
    }

    @Override
    public LeaderRetrievalService getJobManagerLeaderRetriever(JobID jobID, String defaultJobManagerAddress) {
        return null;
    }

    @Override
    public LeaderRetrievalService getWebMonitorLeaderRetriever() {
        return null;
    }

    @Override
    public LeaderElectionService getResourceManagerLeaderElectionService() {
        return null;
    }

    @Override
    public LeaderElectionService getDispatcherLeaderElectionService() {
        return null;
    }

    @Override
    public LeaderElectionService getJobManagerLeaderElectionService(JobID jobID) {
        return null;
    }

    @Override
    public LeaderElectionService getWebMonitorLeaderElectionService() {
        return null;
    }

    @Override
    public CheckpointRecoveryFactory getCheckpointRecoveryFactory() {
        return null;
    }

    @Override
    public SubmittedJobGraphStore getSubmittedJobGraphStore() throws Exception {
        return null;
    }

    @Override
    public RunningJobsRegistry getRunningJobsRegistry() throws Exception {
        return null;
    }

    @Override
    public BlobStore createBlobStore() throws IOException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void closeAndCleanupAllData() throws Exception {

    }
}
