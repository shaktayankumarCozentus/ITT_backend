package com.itt.service.fw.logger.starter.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import com.itt.service.exception.CustomException;
import com.itt.service.enums.ErrorCode;

public class DefaultLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            PropertySource<?> defaultProps = new ResourcePropertySource("classpath:application-default.yml");
            environment.getPropertySources().addLast(defaultProps);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to load default logging properties from application-default.yml", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Allow app-defined props to override
    }
}
