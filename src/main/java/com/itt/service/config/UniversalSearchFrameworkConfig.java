package com.itt.service.config;

import com.itt.service.fw.search.UniversalSortFieldValidator;
import com.itt.service.fw.search.SearchableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Configuration class for the Universal Search Framework.
 * Automatically registers all SearchableEntity implementations with the framework.
 */
@Configuration
@ComponentScan(basePackages = "com.itt.service.fw.search")
public class UniversalSearchFrameworkConfig {

    private static final Logger logger = LoggerFactory.getLogger(UniversalSearchFrameworkConfig.class);

    @Autowired
    @Qualifier("universalSortFieldValidator")
    private UniversalSortFieldValidator sortFieldValidator;

    @Autowired(required = false)
    private List<SearchableEntity<?>> searchableEntities;

    /**
     * Register all SearchableEntity implementations with the framework
     */
    @PostConstruct
    public void registerSearchableEntities() {
        if (searchableEntities != null) {
            searchableEntities.forEach(entity -> {
                sortFieldValidator.registerEntity(entity);
                logger.info("✅ SearchableEntity registered: {}", entity.getEntityClass().getSimpleName());
            });
            
            logger.info("🎉 Universal Search Framework initialized with {} entities", searchableEntities.size());
        } else {
            logger.warn("⚠️ No SearchableEntity implementations found. Create SearchableEntity configurations for your entities.");
        }
    }
}
