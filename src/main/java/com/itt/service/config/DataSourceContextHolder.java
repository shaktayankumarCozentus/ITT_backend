package com.itt.service.config;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local context holder for managing datasource routing.
 * 
 * This class maintains the current datasource type in a thread-local variable,
 * allowing the routing datasource to determine which datasource to use
 * for the current transaction.
 * 
 * @author ITT Team
 * @version 1.0
 */
@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * Set the datasource type for the current thread.
     * 
     * @param dataSourceType the datasource type to set
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            log.warn("Setting null datasource type, will use default");
        } else {
            log.debug("Switching to {} datasource", dataSourceType);
        }
        CONTEXT_HOLDER.set(dataSourceType);
    }

    /**
     * Get the current datasource type for the current thread.
     * 
     * @return the current datasource type, or WRITE if not set
     */
    public static DataSourceType getDataSourceType() {
        DataSourceType dataSourceType = CONTEXT_HOLDER.get();
        // Default to WRITE if not explicitly set
        return dataSourceType != null ? dataSourceType : DataSourceType.WRITE;
    }

    /**
     * Clear the datasource type for the current thread.
     * Should be called after transaction completion to prevent memory leaks.
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
        log.debug("Cleared datasource context");
    }

    /**
     * Check if read datasource is currently selected.
     * 
     * @return true if read datasource is selected
     */
    public static boolean isReadDataSource() {
        return DataSourceType.READ.equals(getDataSourceType());
    }

    /**
     * Check if write datasource is currently selected.
     * 
     * @return true if write datasource is selected
     */
    public static boolean isWriteDataSource() {
        return DataSourceType.WRITE.equals(getDataSourceType());
    }
}
