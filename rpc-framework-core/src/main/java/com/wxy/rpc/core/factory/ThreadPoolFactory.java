package com.wxy.rpc.core.factory;

import com.wxy.rpc.core.config.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工厂类
 *
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} ThreadPoolFactory
 * {@code Date} 2025/1/12 12:16
 */
public class ThreadPoolFactory {

    private static final int AVAILABLE_PROCESSOR_NUMBER = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolConfig threadPoolConfig;

    public ThreadPoolFactory() {
        threadPoolConfig = new ThreadPoolConfig();
    }

    public static ThreadPoolExecutor getDefaultThreadPool() {
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                threadPoolConfig.getWorkQueue());
    }

}
