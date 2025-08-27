package com.itt.service.fw.logger;

import com.itt.service.fw.logger.api.config.LoggerConfigurationProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

//@SpringBootApplication
//@EnableConfigurationProperties(LoggerConfigurationProperties.class)
public class FrameworkLoggingApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(FrameworkLoggingApplication.class, args);
//	}
//
//	@Bean
//	public ApplicationRunner loggingModeInitializer(LoggerConfigurationProperties props) {
//		return args -> {
//			if ("db".equalsIgnoreCase(props.getMode())) {
//				ch.qos.logback.classic.Logger rootLogger =
//						(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
//				rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
//			}
//		};
//	}
}
