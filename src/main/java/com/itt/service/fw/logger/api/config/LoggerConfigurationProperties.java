package com.itt.service.fw.logger.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "logging")
public class LoggerConfigurationProperties {

    private boolean logMethodStart = true;
    private boolean logMethodEnd = true;
    private boolean logParameters = true;
    private boolean logReturnValue = true;
    private String mode = "file";
}

