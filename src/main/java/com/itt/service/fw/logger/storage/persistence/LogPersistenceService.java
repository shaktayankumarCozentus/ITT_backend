package com.itt.service.fw.logger.storage.persistence;

import com.itt.service.entity.LogEntity;

public interface LogPersistenceService {
    void save(LogEntity logEntity);
}
