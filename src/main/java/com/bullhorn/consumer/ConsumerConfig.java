package com.bullhorn.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ConsumerConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerConfig.class);
	
    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
    	LOGGER.info("Starting Task Executor");
    	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    	executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("CONSUMER_T_");
        //executor.initialize();
        return executor;
    }
}