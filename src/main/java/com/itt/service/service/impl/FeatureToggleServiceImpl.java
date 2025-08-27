package com.itt.service.service.impl;

import com.itt.service.entity.MasterConfig;
import com.itt.service.service.FeatureToggleService;
import com.itt.service.repository.MasterConfigRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Implementation of FeatureToggleService that retrieves toggle values from database.
 * <p>
 * This service reads feature toggle flags from the master_config table where:
 * <ul>
 *   <li>config_type = 'feature_toggle'</li>
 *   <li>key_code = the specific toggle name</li>
 *   <li>string_value = 'true' or 'false' for boolean toggles</li>
 * </ul>
 * <p>
 * Database setup example:
 * <pre>
 * INSERT INTO master_config (config_type, key_code, name, string_value, created_on, created_by_id)
 * VALUES ('feature_toggle', 'async_logging_enabled', 'Async Logging Toggle', 'false', NOW(), 'SYSTEM');
 * </pre>
 * 
 * @see com.itt.service.service.FeatureToggleService
 * @see com.itt.service.entity.MasterConfig
 */
@Service
@RequiredArgsConstructor
public class FeatureToggleServiceImpl implements FeatureToggleService {
    
    /** Repository for accessing master configuration data */
    private final MasterConfigRepository configRepo;
    
    /** Configuration type for feature toggles in master_config table */
    private static final String CONFIG_TYPE = "feature_toggle";
    
    /** Key code for async logging toggle */
    private static final String ASYNC_LOGGING_KEY = "async_logging_enabled";

    /**
     * {@inheritDoc}
     * <p>
     * Queries the master_config table for the async_logging_enabled toggle.
     * Returns false by default if the configuration is not found or cannot
     * be parsed as a boolean.
     * <p>
     * Database query equivalent:
     * <pre>
     * SELECT string_value FROM master_config 
     * WHERE config_type = 'feature_toggle' 
     * AND key_code = 'async_logging_enabled'
     * </pre>
     * 
     * @return true if async logging is enabled, false otherwise or if config not found
     */
    @Override
    public boolean isAsyncLoggingEnabled() {
        List<MasterConfig> configs = configRepo.findByConfigType(CONFIG_TYPE);
        if (configs == null) {
            return false; // Default to disabled if repository returns null
        }
        
        return configs.stream()
                .filter(config -> ASYNC_LOGGING_KEY.equalsIgnoreCase(config.getKeyCode()))
                .findFirst()
                .map(config -> Boolean.parseBoolean(config.getStringValue()))
                .orElse(false); // Default to disabled if not configured
    }
    
    // TODO: Implement additional feature toggles
    // Example implementation pattern:
    /*
    @Override
    public boolean isMaintenanceModeEnabled() {
        return getBooleanToggle("maintenance_mode_enabled", false);
    }
    
    private boolean getBooleanToggle(String keyCode, boolean defaultValue) {
        return configRepo.findByConfigType(CONFIG_TYPE).stream()
                .filter(config -> keyCode.equalsIgnoreCase(config.getKeyCode()))
                .findFirst()
                .map(config -> Boolean.parseBoolean(config.getStringValue()))
                .orElse(defaultValue);
    }
    */
}
