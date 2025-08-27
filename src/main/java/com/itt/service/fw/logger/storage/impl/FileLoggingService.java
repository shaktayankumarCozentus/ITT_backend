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
@ConditionalOnProperty(name = "logging.mode", havingValue = "file")
@Service
public class FileLoggingService implements LoggingService {

    private final LogRepository logRepository;

    @Override
    @Transactional
    public void info(String message) {
        log.info("📝 {}", message);
    }

    @Override
    @Transactional
    public void debug(String message) {
        log.debug("📝 {}", message);
    }

    @Override
    @Transactional
    public void warn(String message) {
        log.warn("📝 {}", message);
    }

    @Override
    @Transactional
    public void error(String message) {
        log.error("📝 {}", message);
    }

    @Override
    @Transactional
    public void trace(String message) {
        log.trace("📝 {}", message);
    }

    @Override
    @Transactional
    public void logExceptionToDB(Exception e, String uri, String httpMethod) {
        log.error("❌ Exception during {} {}: {}", httpMethod, uri, e.getMessage(), e);

        LogEntity exceptionLog = LogEntity.builder()
                .uri(uri)
                .httpMethod(httpMethod)
                .level(LoggerLevelEnum.ERROR.name())
                .message(e.getMessage())
                .stackTrace(getStackTraceAsString(e))
                .timestamp(LocalDateTime.now())
                .build();

        logRepository.save(exceptionLog);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
