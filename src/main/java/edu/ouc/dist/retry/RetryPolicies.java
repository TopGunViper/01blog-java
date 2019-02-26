package edu.ouc.dist.retry;

import java.util.concurrent.TimeUnit;

/**
 * Utility class that provides useful implementations of {@link RetryPolicy}.
 */
public class RetryPolicies {

    public static final RetryPolicy NEVER_RETRY_POLICY = new NeverRetryPolicy();

    public static final RetryPolicy FOREVER_RETRY_POLICY = new ForeverRetryPolicy();

    public static final RetryPolicy maximumCountPolicy(int maxRetries) {
        return new MaximumCountRetryPolicy(maxRetries);
    }

    public static final RetryPolicy maximumCountWithFixedSleepPolicy(int maxRetries, long sleepTime, TimeUnit
            timeUnit) {
        return new MaximumCountWithFixedSleepRetryPolicy(maxRetries, sleepTime, timeUnit);
    }
    // never retry
    static class NeverRetryPolicy implements RetryPolicy {
        @Override
        public boolean shouldRetry(int retryCount, Throwable t) {
            return false;
        }
    }

    // forever retry
    static class ForeverRetryPolicy implements RetryPolicy {
        @Override
        public boolean shouldRetry(int retryCount, Throwable t) {
            return true;
        }
    }

    // retry until maximum count
    static class MaximumCountRetryPolicy implements RetryPolicy {
        protected int maxRetries;

        public MaximumCountRetryPolicy(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public boolean shouldRetry(int retryCount, Throwable t) {
            return retryCount < maxRetries;
        }
    }

    // maximum count with sleep sleepTime.
    static class MaximumCountWithFixedSleepRetryPolicy implements RetryPolicy {
        private int maxRetries;
        private long sleepTime;
        private TimeUnit unit;

        public MaximumCountWithFixedSleepRetryPolicy(int maxRetries, long sleepTime, TimeUnit unit) {
            this.maxRetries = maxRetries;
            this.sleepTime = sleepTime;
            this.unit = unit;
        }

        @Override
        public boolean shouldRetry(int retryCount, Throwable t) {
            try {
                unit.sleep(sleepTime);
            } catch (InterruptedException ignore) {
                // Do nothing
            }
            return retryCount <= maxRetries;
        }
    }

}
