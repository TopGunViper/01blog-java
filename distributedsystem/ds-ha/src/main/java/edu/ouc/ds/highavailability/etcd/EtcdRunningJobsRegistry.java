package edu.ouc.ds.highavailability.etcd;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.highavailability.RunningJobsRegistry;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;

/**
 * etcd base registry for running jobs, highly available.
 */
public class EtcdRunningJobsRegistry implements RunningJobsRegistry {
    private static final Charset ENCODING = Charset.forName("utf-8");
    private final Client client;
    private final String runningJobPath;

    public EtcdRunningJobsRegistry(final Client client, final Configuration configuration) {
        this.client = checkNotNull(client, "client");
        this.runningJobPath = configuration.getString(EtcdHaOptions.ETCD_RUNNING_JOB_REGISTRY_PATH);
    }

    @Override
    public void setJobRunning(JobID jobID) throws IOException {
        checkNotNull(jobID);

        try {
            writeStatusToEtcd(jobID, JobSchedulingStatus.RUNNING);
        } catch (Exception e) {
            throw new IOException("Failed to set RUNNING state in etcd for job " + jobID, e);
        }
    }

    @Override
    public void setJobFinished(JobID jobID) throws IOException {
        checkNotNull(jobID);

        try {
            writeStatusToEtcd(jobID, JobSchedulingStatus.DONE);
        } catch (Exception e) {
            throw new IOException("Failed to set DONE state in etcd for job " + jobID, e);
        }
    }

    @Override
    public JobSchedulingStatus getJobSchedulingStatus(JobID jobID) throws IOException {
        checkNotNull(jobID);

        try {
            final String path = createPath(jobID);
            GetResponse resp = this.client.getKVClient().get(withDefaultEncoding(path)).get();
            if (resp.getKvs() != null) {
                checkArgument(resp.getKvs().size() == 1,
                        String.format("Detect multiple job status under path:%s, size:%s", path, resp.getKvs().size()));

                KeyValue keyValue = resp.getKvs().get(0);
                try {
                    return JobSchedulingStatus.valueOf(keyValue.getValue().toString(ENCODING));
                } catch (IllegalArgumentException e) {
                    throw new IOException(String.format("Detect data corruption:%s for job:%s",
                            keyValue.getValue().toString(ENCODING), jobID));
                }
            }

            // nothing found, yet, must be in status 'PENDING'
            return JobSchedulingStatus.PENDING;
        } catch (Exception e) {
            throw new IOException("Failed to get status from etcd for job:" + jobID);
        }
    }

    @Override
    public void clearJob(JobID jobID) throws IOException {
        checkNotNull(jobID);

        try {
            final String path = createPath(jobID);
            this.client.getKVClient().delete(withDefaultEncoding(path)).get();
        } catch (Exception e) {
            throw new IOException("Failed to clear status from etcd for job:" + jobID);
        }
    }

    private void writeStatusToEtcd(JobID jobID, JobSchedulingStatus status) throws Exception {
        final String path = createPath(jobID);
        this.client.getKVClient().put(withDefaultEncoding(path),
                withDefaultEncoding(status.name()));
    }

    private ByteSequence withDefaultEncoding(String src) {
        return ByteSequence.from(src, ENCODING);
    }

    private String createPath(JobID jobID) {
        return runningJobPath + jobID.toString();
    }

}
