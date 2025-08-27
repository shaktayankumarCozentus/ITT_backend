package com.itt.service.fw.audit.service;

import com.itt.service.fw.audit.dto.AuditDetail;

public interface IEventLoggingService {
    void log(AuditDetail log);
}
