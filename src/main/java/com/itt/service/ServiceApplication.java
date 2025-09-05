package com.itt.service;

import com.itt.service.fw.audit.config.AuditConfigProperties;
import com.itt.service.fw.lit.cache.CacheProperties;
import com.itt.service.fw.logger.api.config.LoggerConfigurationProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Service Spring Boot application entry point.
 * <p>
 * Bootstraps the application context, sets up web MVC, JPA, security, and all
 * configured components (controllers, services, aspects, filters).
 * Activate profiles via `spring.profiles.active` for dev, stage, prod, etc.
 */
@SpringBootApplication
@EnableConfigurationProperties({
		CacheProperties.class,
		LoggerConfigurationProperties.class,
		AuditConfigProperties.class   // ⬅️ add this
})
@EnableScheduling
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	/*
	 * Added by @Chinmaya for disabling logging to file system when mode selected as DB
	 */
	@Bean
	public ApplicationRunner loggingModeInitializer(LoggerConfigurationProperties props) {
		return args -> {
			if ("db".equalsIgnoreCase(props.getMode())) {
				ch.qos.logback.classic.Logger rootLogger =
						(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
				rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
			}
		};
	}
}
