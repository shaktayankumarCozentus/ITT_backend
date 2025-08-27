package com.itt.service.fw.logger.storage.impl;

import com.itt.service.fw.logger.core.aspect.enums.LoggerLevelEnum;
import com.itt.service.fw.logger.storage.LoggingService;
import com.itt.service.entity.LogEntity;
import com.itt.service.repository.LogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "logging.mode", havingValue = "db")
@Service
public class DBLoggingService implements LoggingService {

    private final LogRepository logRepository;

    @Override
    @Transactional
    public void info(String message) {
        log.info("📝 {}", message);
        saveToDB(message, LoggerLevelEnum.INFO.name());
    }

    @Override
    @Transactional
    public void debug(String message) {
        log.debug("📝 {}", message);
        saveToDB(message, LoggerLevelEnum.DEBUG.name());
    }

    @Override
    @Transactional
    public void warn(String message) {
        log.warn("📝 {}", message);
        saveToDB(message, LoggerLevelEnum.WARN.name());
    }

    @Override
    @Transactional
    public void error(String message) {
        log.error("📝 {}", message);
        saveToDB(message, LoggerLevelEnum.ERROR.name());
    }

    @Override
    @Transactional
    public void trace(String message) {
        log.trace("📝 {}", message);
        saveToDB(message, LoggerLevelEnum.TRACE.name());
    }
    public void saveToDB(String message, String level) {

        LogEntity logEntry = LogEntity.builder()
                .uri("N/A")
                .httpMethod("N/A")
                .level(level)
                .message(message)
                .stackTrace(null)
                .timestamp(LocalDateTime.now())
                .build();

        logRepository.save(logEntry);
    }

    @Override
    public void logExceptionToDB(Exception e, String uri, String httpMethod) {

        LogEntity log = LogEntity.builder()
                .message(e.getMessage())
                .httpMethod(httpMethod)
                .level(LoggerLevelEnum.ERROR.name())
                .uri(uri)
                .timestamp(LocalDateTime.now())
                .stackTrace(getStackTraceAsString(e))
                .build();

        logRepository.save(log);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
