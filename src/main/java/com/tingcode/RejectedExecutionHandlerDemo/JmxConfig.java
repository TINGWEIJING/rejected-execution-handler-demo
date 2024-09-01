package com.tingcode.RejectedExecutionHandlerDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
public class JmxConfig {

    //    @Autowired
//    private ThreadPoolTaskExecutor abortPolicyExecutor;
//    @Autowired
//    private ExecutorService virtualThreadPerTaskExecutor;
    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private Executor threadPoolTaskExecutor;

    @Bean
    public MBeanExporter exporter() {
        MBeanExporter exporter = new MBeanExporter();
        Map<String, Object> beans = new HashMap<>();
        beans.put("bean:name=threadPoolTaskExecutor", threadPoolTaskExecutor);
//        beans.put("bean:name=abortPolicyExecutor", abortPolicyExecutor);
//        beans.put("bean:name=virtualThreadPerTaskExecutor", virtualThreadPerTaskExecutor);
        exporter.setBeans(beans);
        return exporter;
    }
}

