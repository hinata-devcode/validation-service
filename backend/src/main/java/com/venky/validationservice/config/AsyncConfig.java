package com.venky.validationservice.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync 
public class AsyncConfig {

    @Bean(name = "providerTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 1. Core Pool: Number of threads kept alive constantly (Ready for immediate work)
        executor.setCorePoolSize(10);
        
        // 2. Max Pool: Maximum threads allowed if traffic spikes
        executor.setMaxPoolSize(50);
        
        // 3. Queue Capacity: How many requests can wait in line if all 50 threads are busy
        executor.setQueueCapacity(100);
        
        // 4. Thread Prefix: Makes debugging in your console logs incredibly easy
        executor.setThreadNamePrefix("ThirdPartyProvider-");
        
        // 5. Graceful Shutdown: Do not kill threads mid-flight if the server restarts!
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // Wait a maximum of 30 seconds for background tasks to finish before forcing a shutdown
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
}

