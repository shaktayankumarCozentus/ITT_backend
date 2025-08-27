package com.itt.service.fw.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(name = "eventLoggerExecutor")
    public Executor asyncExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}

