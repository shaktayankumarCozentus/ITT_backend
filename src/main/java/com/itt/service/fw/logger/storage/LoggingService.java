package com.itt.service.fw.logger.storage;

public interface LoggingService {
    void info(String message);
    void debug(String message);
    void warn(String message);
    void error(String message);
    void trace(String message);
    void logExceptionToDB(Exception e, String uri, String httpMethod);
}

