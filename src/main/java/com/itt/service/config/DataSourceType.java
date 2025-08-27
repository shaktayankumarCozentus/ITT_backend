package com.itt.service.config;

/**
 * Enumeration for datasource types in the hybrid database approach.
 * 
 * @author ITT Team
 * @version 1.0
 */
public enum DataSourceType {
    /**
     * Write datasource for INSERT, UPDATE, DELETE operations
     */
    WRITE,
    
    /**
     * Read datasource for SELECT operations (can be read replica)
     */
    READ
}
