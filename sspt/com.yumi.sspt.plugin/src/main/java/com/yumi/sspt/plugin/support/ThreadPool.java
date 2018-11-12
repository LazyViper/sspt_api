package com.yumi.sspt.plugin.support;

import java.util.concurrent.*;

/**
 * ThreadPool
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */

public class ThreadPool extends ThreadPoolExecutor {

    private int timeout;

    private String poolName;

    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public int getTimeout() {
        return timeout;
    }

    public ThreadPool setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getPoolName() {
        return poolName;
    }

    public ThreadPool setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + "(poolName=" + poolName + ",timeout=" + timeout + ")";
    }
}
