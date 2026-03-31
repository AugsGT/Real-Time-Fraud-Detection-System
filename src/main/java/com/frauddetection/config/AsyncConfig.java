package com.frauddetection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${fraud.detection.thread-pool-size:5}")
    private int threadPoolSize;

    @Bean(name = "fraudDetectionExecutor")
    public Executor fraudDetectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("fraud-detect-");
        executor.initialize();
        return executor;
    }
}
