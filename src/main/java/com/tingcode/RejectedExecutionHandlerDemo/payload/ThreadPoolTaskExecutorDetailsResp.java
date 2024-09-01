package com.tingcode.RejectedExecutionHandlerDemo.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadPoolTaskExecutorDetailsResp {
    private ThreadPoolTaskExecutorDetailDto abortPolicyExecutorDetail;
    private ThreadPoolTaskExecutorDetailDto callerRunsPolicyExecutorDetail;
    private ThreadPoolTaskExecutorDetailDto discardPolicyExecutorDetail;
    private ThreadPoolTaskExecutorDetailDto discardOldestPolicyExecutorDetail;
}
