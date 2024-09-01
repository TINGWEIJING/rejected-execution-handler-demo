package com.tingcode.RejectedExecutionHandlerDemo;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ExecutorOption {
    ABORT_POLICY("AbortPolicyExecutor"),
    CALLER_RUNS_POLICY("CallerRunsPolicyExecutor"),
    DISCARD_POLICY("DiscardPolicyExecutor"),
    DISCARD_OLDEST_POLICY("DiscardOldestPolicyExecutor"),
    VIRTUAL_THREAD_EXECUTOR("VirtualThreadExecutor"),
    THREAD_POOL_TASK_EXECUTOR("ThreadPoolTaskExecutor"),
    ;

    private final String executorName;
    private static final Map<String, ExecutorOption> ENUM_MAP = Stream.of(values())
            .collect(Collectors.toMap(ExecutorOption::getValue, e -> e));

    ExecutorOption(String executorName) {
        this.executorName = executorName;
    }

    public String getValue() {
        return executorName;
    }

    @JsonCreator
    public static ExecutorOption fromValue(String value) {
        return ENUM_MAP.getOrDefault(value, ABORT_POLICY);
    }
}
