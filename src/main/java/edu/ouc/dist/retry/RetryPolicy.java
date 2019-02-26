package edu.ouc.dist.retry;

public interface RetryPolicy {

    /**
     * @param retryCount the number of times retried so far
     * @param t          The exception that causes the method fail
     *
     * @return <code>true</code> if the method should be retried,
     * <code>false</code> otherwise.
     */
    boolean shouldRetry(int retryCount, Throwable t);
}
