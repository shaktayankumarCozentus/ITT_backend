package com.itt.service.fw.logger.starter;

import com.itt.service.fw.logger.api.config.LoggerConfigurationProperties;
import com.itt.service.fw.logger.core.aspect.MethodLoggerAspect;
import com.itt.service.fw.logger.exception.GlobalExceptionLogger;
import com.itt.service.fw.logger.storage.LoggingService;
import com.itt.service.fw.logger.storage.impl.DBLoggingService;
import com.itt.service.fw.logger.storage.impl.DbLogPersistenceService;
import com.itt.service.fw.logger.storage.impl.FileLogPersistenceService;
import com.itt.service.fw.logger.storage.impl.FileLoggingService;
import com.itt.service.fw.logger.storage.persistence.LogPersistenceService;
import com.itt.service.repository.LogRepository;
import com.itt.service.exception.CustomException;
import com.itt.service.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(LoggerConfigurationProperties.class)
@EnableJpaRepositories(basePackages = "com.itt.service.fw.logger.storage.repository")
@EntityScan(basePackages = "com.itt.service.fw.logger.storage.entity")
@ConditionalOnProperty(prefix = "logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({MethodLoggerAspect.class, GlobalExceptionLogger.class})
public class LoggerAutoConfiguration {

    // IMPORTANT: Ensure these @Bean annotations are REMOVED entirely.
    // They are no longer needed as DBLoggingService and FileLoggingService
    // will be instantiated directly within the 'loggingService' @Bean method.

    // public DBLoggingService dbLoggingService(LogRepository logRepository) {
    //     return new DBLoggingService(logRepository);
    // }

    // public FileLoggingService fileLoggingService(LogRepository logRepository) {
    //     return new FileLoggingService(logRepository);
    // }

    @Bean
    @ConditionalOnMissingBean(LogPersistenceService.class)
    @Primary
    public LogPersistenceService logPersistenceService(LoggerConfigurationProperties props,
                                                       DbLogPersistenceService dbLogger,
                                                       FileLogPersistenceService fileLogger) {
        return switch (props.getMode() == null ? "" : props.getMode().toLowerCase()) {
            case "db" -> {
                log.info("Getting DB LogPersistenceService");
                yield dbLogger;
            }
            case "file" -> {
                log.info("Getting File LogPersistenceService");
                yield fileLogger;
            }
            default ->
                    throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "❌ Logger mode is missing or unsupported. Please set 'logging.mode' to either 'db' or 'file'. Found: " + props.getMode());
        };
    }

    @Bean
    @ConditionalOnMissingBean(LoggingService.class)
    @Primary // This ensures this is the SINGLE primary LoggingService bean
    public LoggingService loggingService(LoggerConfigurationProperties props,
                                         LogRepository logRepository) {
        String mode = props.getMode() == null ? "" : props.getMode().toLowerCase();

        return switch (mode) {
            case "db" -> {
                log.info("Getting DB logger service");
                yield new DBLoggingService(logRepository);
            }
            case "file" -> {
                log.info("Getting File logger service");
                yield new FileLoggingService(logRepository);
            }
            default -> throw new CustomException(ErrorCode.INVALID_DATA_FORMAT,
                    "❌ Logger mode is missing or unsupported. Please set 'logging.mode' to either 'db' or 'file'. Found: " + props.getMode()
            );
        };
    }

}