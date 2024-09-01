package com.tingcode.RejectedExecutionHandlerDemo;

import com.tingcode.RejectedExecutionHandlerDemo.payload.PersonalizedProductCategories;
import com.tingcode.RejectedExecutionHandlerDemo.payload.PersonalizedTrendsResp;
import com.tingcode.RejectedExecutionHandlerDemo.payload.ThreadPoolTaskExecutorDetailDto;
import com.tingcode.RejectedExecutionHandlerDemo.payload.ThreadPoolTaskExecutorDetailsResp;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * http://localhost:8080/swagger-ui/index.html#/
 */
@Validated
@RestController
@RequestMapping("/")
public class MainController {
    private final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ThreadPoolTaskExecutor abortPolicyExecutor;
    private final ThreadPoolTaskExecutor callerRunsPolicyExecutor;
    private final ThreadPoolTaskExecutor discardPolicyExecutor;
    private final ThreadPoolTaskExecutor discardOldestPolicyExecutor;
    private final ExecutorService virtualThreadPerTaskExecutor;
    private final Executor threadPoolTaskExecutor;
    private final AsyncService asyncService;

    @Autowired
    public MainController(@Qualifier("abortPolicyExecutor") ThreadPoolTaskExecutor abortPolicyExecutor,
                          @Qualifier("callerRunsPolicyExecutor") ThreadPoolTaskExecutor callerRunsPolicyExecutor,
                          @Qualifier("discardPolicyExecutor") ThreadPoolTaskExecutor discardPolicyExecutor,
                          @Qualifier("discardOldestPolicyExecutor") ThreadPoolTaskExecutor discardOldestPolicyExecutor,
                          @Qualifier("virtualThreadPerTaskExecutor") ExecutorService virtualThreadPerTaskExecutor,
                          @Qualifier("threadPoolTaskExecutor") Executor threadPoolTaskExecutor,
                          AsyncService asyncService) {
        this.abortPolicyExecutor = abortPolicyExecutor;
        this.callerRunsPolicyExecutor = callerRunsPolicyExecutor;
        this.discardPolicyExecutor = discardPolicyExecutor;
        this.discardOldestPolicyExecutor = discardOldestPolicyExecutor;
        this.virtualThreadPerTaskExecutor = virtualThreadPerTaskExecutor;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.asyncService = asyncService;
    }

    @GetMapping("/log")
    public String index() {
        logger.info("An INFO Message");
        logger.info(Thread.currentThread().getName());

        return "Howdy! Check out the Logs to see the output...";
    }

    @GetMapping("/executors")
    public ResponseEntity<ThreadPoolTaskExecutorDetailsResp> getExecutors() {
        return ResponseEntity.ok(getThreadPoolTaskExecutorDetailsResp());
    }

//    @GetMapping("/executor")
//    public ResponseEntity<ThreadPoolTaskExecutorDetailDto> getExecutor(@RequestParam ExecutorOption executorOption) {
//        Executor threadPoolTaskExecutor = getExecutorFromOption(executorOption);
//        ThreadPoolTaskExecutorDetailDto resp = toThreadPoolTaskExecutorDetailDto(threadPoolTaskExecutor);
//        return ResponseEntity.ok(resp);
//    }

    @PostMapping("/submit-task")
    public ResponseEntity<String> submitTask(@RequestParam ExecutorOption executorOption,
                                             @RequestParam(defaultValue = "5") @Min(1) @Max(100) long sleepSeconds,
                                             @RequestParam(defaultValue = "1") @Min(1) @Max(200) int taskCount) {
        Executor threadPoolTaskExecutor = getExecutorFromOption(executorOption);
        for (int i = 0; i < taskCount; i++) {
            try {
                CompletableFuture.supplyAsync(() -> {
                            try {
                                Thread.sleep(sleepSeconds * 1000L);
                                logger.info(Thread.currentThread().getName());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }, threadPoolTaskExecutor)
                        .exceptionally(throwable -> {
                            // TaskRejectedException will NOT be caught here
                            logger.error("Error occurred while executing task", throwable);
                            return null;
                        });
            } catch (TaskRejectedException e) {
                logger.error("Error occurred while submitting task", e);
            }
        }
        return ResponseEntity.ok("Task submitted to " + executorOption.getValue());
    }

    @PostMapping("/submit-async-task")
    public ResponseEntity<String> submitAsyncTask(@RequestParam ExecutorOption executorOption,
                                                  @RequestParam(defaultValue = "5") @Min(1) @Max(10) long sleepSeconds,
                                                  @RequestParam(defaultValue = "1") @Min(1) @Max(100) int taskCount) {
        Executor threadPoolTaskExecutor = getExecutorFromOption(executorOption);
        for (int i = 0; i < taskCount; i++) {
            try {
                asyncService.asyncGetNameByRank(i)
                        .exceptionally(throwable -> {
                            // TaskRejectedException will NOT be caught here
                            logger.error("Error occurred while executing task", throwable);
                            return null;
                        });
            } catch (TaskRejectedException e) {
                logger.error("Error occurred while submitting task", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok("Task submitted to " + executorOption.getValue());
    }

    public ResponseEntity<String> submitTaskDumb(@RequestParam ExecutorOption executorOption,
                                                 @RequestParam(defaultValue = "5") @Min(1) @Max(6) long sleepSeconds,
                                                 @RequestParam(defaultValue = "1") @Min(1) @Max(20) int taskCount) {
        Executor abortPolicyExecutor = getAbortPolicyExecutor();
        CompletableFuture.supplyAsync(this::asyncTask, abortPolicyExecutor)
                .exceptionally(throwable -> {
                    // TaskRejectedException will NOT be caught here
                    logger.error("Error occurred while executing task", throwable);
                    return null;
                });

        return ResponseEntity.ok("Task submitted to " + executorOption.getValue());
    }

    private Executor getAbortPolicyExecutor() {
        return abortPolicyExecutor;
    }

    private Executor getCallerRunsPolicyExecutor() {
        return callerRunsPolicyExecutor;
    }

    private String getNameByRank(int rank) {
        return "Name" + rank;
    }

    private Supplier asyncTask() {
        return () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    private Executor getExecutorFromOption(ExecutorOption executorOption) {
        switch (executorOption) {
            case ExecutorOption.ABORT_POLICY:
                return abortPolicyExecutor;
            case ExecutorOption.CALLER_RUNS_POLICY:
                return callerRunsPolicyExecutor;
            case ExecutorOption.DISCARD_POLICY:
                return discardPolicyExecutor;
            case ExecutorOption.DISCARD_OLDEST_POLICY:
                return discardOldestPolicyExecutor;
            case ExecutorOption.VIRTUAL_THREAD_EXECUTOR:
                return virtualThreadPerTaskExecutor;
            case ExecutorOption.THREAD_POOL_TASK_EXECUTOR:
                return threadPoolTaskExecutor;
            default:
                return null;
        }
    }

    private ThreadPoolTaskExecutorDetailsResp getThreadPoolTaskExecutorDetailsResp() {
        return ThreadPoolTaskExecutorDetailsResp.builder()
                .abortPolicyExecutorDetail(toThreadPoolTaskExecutorDetailDto(abortPolicyExecutor))
                .callerRunsPolicyExecutorDetail(toThreadPoolTaskExecutorDetailDto(callerRunsPolicyExecutor))
                .discardPolicyExecutorDetail(toThreadPoolTaskExecutorDetailDto(discardPolicyExecutor))
                .discardOldestPolicyExecutorDetail(toThreadPoolTaskExecutorDetailDto(discardOldestPolicyExecutor))
                .build();
    }

    private ThreadPoolTaskExecutorDetailDto toThreadPoolTaskExecutorDetailDto(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolTaskExecutor.getThreadPoolExecutor();
        return ThreadPoolTaskExecutorDetailDto.builder()
                .activeCount(threadPoolTaskExecutor.getActiveCount())
                .poolSize(threadPoolTaskExecutor.getPoolSize())
                .corePoolSize(threadPoolTaskExecutor.getCorePoolSize())
                .largestPoolSize(threadPoolExecutor.getLargestPoolSize())
                .maxPoolSize(threadPoolTaskExecutor.getMaxPoolSize())
                .taskCount(threadPoolExecutor.getTaskCount())
                .queueCapacity(threadPoolTaskExecutor.getQueueCapacity())
                .queueSize(threadPoolTaskExecutor.getQueueSize())
                .keepAliveSeconds(threadPoolTaskExecutor.getKeepAliveSeconds())
                .phase(threadPoolTaskExecutor.getPhase())
                .threadPriority(threadPoolTaskExecutor.getThreadPriority())
                .build();
    }

    /**
     * Sample code
     */
    @GetMapping("/{userId}/personalized-trends")
    public PersonalizedTrendsResp getPersonalizedTrends(@PathVariable String userId) {
        // fetch user personalized product categories
        PersonalizedProductCategories personalizedProductCategories
                = fetchPersonalizedProductCategories(userId);
        // fetch trending products for each category
        List<CompletableFuture<List<String>>> futures = personalizedProductCategories
                .getCategories()
                .stream()
                .map(category -> CompletableFuture.supplyAsync(
                        () -> fetchTrendingProductsByCategory(category),
                        virtualThreadPerTaskExecutor
                ))
                .collect(Collectors.toList());
        List<List<String>> nestCategoryProductList = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return mapToPersonalizedTrendsResp(nestCategoryProductList);
    }

    private PersonalizedProductCategories fetchPersonalizedProductCategories(String userId) {
        return new PersonalizedProductCategories();
    }

    private List<String> fetchTrendingProductsByCategory(String category) {
        return List.of("Product1", "Product2");
    }

    private PersonalizedTrendsResp mapToPersonalizedTrendsResp(List<List<String>> nestedCategoryProductList) {
        return new PersonalizedTrendsResp();
    }
}
