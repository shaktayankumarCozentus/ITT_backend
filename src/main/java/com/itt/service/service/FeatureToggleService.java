package com.itt.service.service;

/**
 * Service interface for managing feature toggle flags.
 * <p>
 * Feature toggles allow runtime control of application features without
 * code deployment. Flags are stored in the database (master_config table)
 * and can be modified by operations teams as needed.
 * <p>
 * This enables:
 * <ul>
 *   <li>Gradual feature rollouts</li>
 *   <li>Quick feature disabling during incidents</li>
 *   <li>A/B testing capabilities</li>
 *   <li>Environment-specific feature control</li>
 * </ul>
 * 
 * @see com.itt.service.service.impl.FeatureToggleServiceImpl
 * @see com.itt.service.entity.MasterConfig
 */
public interface FeatureToggleService {
    
    /**
     * Checks if asynchronous logging is currently enabled.
     * <p>
     * This flag controls whether the LoggingAspect performs async logging
     * of method entry, exit, and exceptions. Useful for:
     * <ul>
     *   <li>Disabling in production for performance</li>
     *   <li>Enabling during debugging sessions</li>
     *   <li>Temporary troubleshooting without redeployment</li>
     * </ul>
     * 
     * @return true if async logging should be active, false otherwise
     * @see com.itt.service.aspect.LoggingAspect
     */
    boolean isAsyncLoggingEnabled();
    
    // TODO: Add additional feature toggles as needed
    // boolean isNewFeatureEnabled();
    // boolean isMaintenanceModeEnabled();
    // boolean isMetricsCollectionEnabled();
}
