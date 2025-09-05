package com.itt.service.fw.audit.aspect;

import com.itt.service.fw.audit.annotation.EventAuditLogger;
import com.itt.service.fw.audit.context.EventContextHolder;
import com.itt.service.fw.audit.context.TraceIdContextHolder;
import com.itt.service.fw.audit.dto.AuditDetail;
import com.itt.service.fw.audit.dto.EffectiveAuditSettings;
import com.itt.service.fw.audit.service.IEventLoggingService;
import com.itt.service.fw.audit.service.impl.EventAuditConfigService;
import com.itt.service.fw.audit.util.JsonUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class EventLoggingAspect {

    private final IEventLoggingService loggingService;
    private final EventAuditConfigService auditConfigService;

    // Intercept any public method in @RestController classes
    @Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public Object handleLogging(ProceedingJoinPoint joinPoint) throws Throwable {

        Instant start = Instant.now();
        Object result;
        Throwable error = null;

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest  request  = attrs != null ? attrs.getRequest()  : null;
        HttpServletResponse response = attrs != null ? attrs.getResponse() : null;

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 1) DB-config first
        Optional<EffectiveAuditSettings> cfgOpt = auditConfigService.resolve(request, method);

        // 2) Fallback to annotation (backward compatibility) IF no DB config
        EffectiveAuditSettings cfg = cfgOpt.orElseGet(() -> {
            EventAuditLogger ann = AnnotationUtils.findAnnotation(method, EventAuditLogger.class);
            if (ann == null) {
                ann = AnnotationUtils.findAnnotation(method.getDeclaringClass(), EventAuditLogger.class);
            }
            if (ann == null) {
                // no DB config, no annotation -> auditing disabled
                return EffectiveAuditSettings.builder().enabled(false).build();
            }
            return EffectiveAuditSettings.builder()
                    .enabled(true)
                    .logRequest(ann.logRequest())
                    .logResponse(ann.logResponse())
                    .logError(ann.logError())
                    .onlyOnError(ann.onlyOnError())
                    .maskFields(Set.of(ann.maskFields()))
                    .build();
        });

        if (!cfg.isEnabled()) {
            return joinPoint.proceed(); // auditing disabled
        }

        String requestBody  = null;
        String responseBody = null;

        try {
            if (cfg.isLogRequest()) {
                requestBody = JsonUtility.maskPayload(joinPoint.getArgs(), cfg.getMaskFields());
            }
            result = joinPoint.proceed();
            if (cfg.isLogResponse()) {
                responseBody = JsonUtility.maskPayload(result, cfg.getMaskFields());
            }
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            long duration = java.time.Duration.between(start, Instant.now()).toMillis();

            boolean shouldLog = !cfg.isOnlyOnError() || error != null;
            if (shouldLog && (error == null || cfg.isLogError())) {
                String traceId = TraceIdContextHolder.get();
                if (traceId == null || traceId.isBlank()) {
                    traceId = UUID.randomUUID().toString();
                }

                AuditDetail auditDetail = new AuditDetail(
                        UUID.fromString(traceId),
                        EventContextHolder.getCurrentUser(),
                        EventContextHolder.getCurrentUserRoles(),
                        request != null ? request.getMethod() : null,
                        request != null ? request.getRequestURI() : null,
                        requestBody,
                        responseBody,
                        error != null ? stringifyThrowable(error) : null,
                        start,
                        duration,
                        (response != null ? response.getStatus() : 0),
                        request != null ? request.getRemoteAddr() : null
                );
                loggingService.log(auditDetail);
            }
        }
    }

    private String stringifyThrowable(Throwable t) {
        // keep it short; or use a stacktrace utility if you prefer
        return t.toString();
    }
}
