package edu.ouc.ds.highavailability.etcd;

import static org.apache.flink.configuration.ConfigOptions.key;

import org.apache.flink.configuration.ConfigOption;

public class EtcdHaOptions {

    public static final ConfigOption<String> ETCD_RUNNING_JOB_REGISTRY_PATH =
            key("high-availability.etcd.path.running-registry")
                    .defaultValue("/running_job_registry/");

}
