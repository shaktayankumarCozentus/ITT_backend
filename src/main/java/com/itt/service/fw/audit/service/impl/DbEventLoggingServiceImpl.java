package com.itt.service.fw.audit.service.impl;

import com.itt.service.annotation.WriteDataSource;
import com.itt.service.entity.UserEventAuditLog;
import com.itt.service.entity.AuditPayload;
import com.itt.service.fw.audit.dto.AuditDetail;
import com.itt.service.fw.audit.service.IEventLoggingService;
import com.itt.service.repository.UserEventAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DbEventLoggingServiceImpl implements IEventLoggingService {

    private final UserEventAuditRepository userEventAuditRepository;

    @Async("eventLoggerExecutor")
    @Override
    @WriteDataSource("Persisting user event logs to the database")
    @Transactional(rollbackFor = Exception.class)
    public void log(AuditDetail detail) {
        AuditPayload payload = AuditPayload.builder()
                .requestPayload(detail.requestBody())
                .responsePayload(detail.responseBody())
                .errorDetails(detail.error())
                .userRoles(detail.roles())
                .build();

        UserEventAuditLog userEventAuditLog = UserEventAuditLog.builder()
                .correlationId(detail.traceId().toString())
                .eventTimestamp(detail.start())
                .username(detail.username())
                .httpMethod(detail.httpMethod())
                .endpoint(detail.endpoint())
                .statusCode(detail.statusCode())
                .durationMs(detail.durationMs())
                .clientIp(detail.clientIp())
                .build();
        userEventAuditLog.setPayload(payload);

        userEventAuditRepository.save(userEventAuditLog);
    }
}
