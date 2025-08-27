package com.itt.service.fw.logger.storage.impl;

import com.itt.service.entity.LogEntity;
import com.itt.service.fw.logger.storage.persistence.LogPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileLogPersistenceService implements LogPersistenceService {

    @Override
    public void save(LogEntity logEntity) {
        String formatted = String.format("[%s] %s - %s",
                logEntity.getLevel(), logEntity.getTimestamp(), logEntity.getMessage());

        log.info(formatted);

        if (logEntity.getStackTrace() != null) {
            log.error(logEntity.getStackTrace());
        }
    }
}

