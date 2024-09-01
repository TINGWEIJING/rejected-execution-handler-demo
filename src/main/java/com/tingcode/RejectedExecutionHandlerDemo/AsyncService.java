package com.tingcode.RejectedExecutionHandlerDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {
    private final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Async
    public CompletableFuture<String> asyncGetNameByRank(int rank) throws InterruptedException {
        Thread.sleep(1000);
        logger.info(Thread.currentThread().getName());
        return CompletableFuture.completedFuture("Name" + rank);
    }
}
