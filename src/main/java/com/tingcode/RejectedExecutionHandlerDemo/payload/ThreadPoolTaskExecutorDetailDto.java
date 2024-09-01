package com.tingcode.RejectedExecutionHandlerDemo.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadPoolTaskExecutorDetailDto {
    private int activeCount;
    private int poolSize;
    private int corePoolSize;
    private int largestPoolSize;
    private int maxPoolSize;
    private long taskCount;
    private int queueCapacity;
    private int queueSize;
    private int keepAliveSeconds;
    private int phase;
    private int threadPriority;
}
