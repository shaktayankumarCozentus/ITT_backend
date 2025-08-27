package com.itt.service.fw.logger.storage.impl;

import com.itt.service.entity.LogEntity;
import com.itt.service.fw.logger.storage.persistence.LogPersistenceService;
import com.itt.service.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbLogPersistenceService implements LogPersistenceService {

    private final LogRepository logRepository;

    @Override
    public void save(LogEntity logEntity) {
        logRepository.save(logEntity);
    }
}
