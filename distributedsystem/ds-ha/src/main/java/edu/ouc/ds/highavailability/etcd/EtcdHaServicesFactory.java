package edu.ouc.ds.highavailability.etcd;

import java.util.concurrent.Executor;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServicesFactory;

public class EtcdHaServicesFactory implements HighAvailabilityServicesFactory {

    @Override
    public HighAvailabilityServices createHAServices(Configuration configuration, Executor executor) throws Exception {
        return null;
    }

}
