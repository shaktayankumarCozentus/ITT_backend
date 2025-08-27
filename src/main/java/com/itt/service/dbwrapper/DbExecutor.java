package com.itt.service.dbwrapper;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbExecutor {

    private final HikariDataSource dataSource;


    /**
     * Executes a database operation that returns a value, with built-in error handling and retry logic.
     * If the operation fails due to an authentication error, it will evict all connections and attempt a single retry.
     * For other exceptions, it logs the error and returns an empty Optional.
     *
     * @param supplier The database operation to execute, encapsulated in a DbSupplier lambda.
     * @param label    A descriptive label for the operation, used for logging.
     * @param <T>      The type of the result returned by the operation.
     * @return An Optional containing the result if successful, or an empty Optional if an error occurs.
     */
    public <T> Optional<T> execute(DbSupplier<T> supplier, String label) {
        try {
            T result = supplier.get();
            log.debug("DB operation [{}] successful", label);
            return Optional.ofNullable(result);
        } catch (SQLException e) {
            if (isAuthenticationError(e)) {
                log.warn("DB access denied for [{}]. Evicting connections and attempting a single retry.", label);
                return executeWithRetry(supplier, label);
            }
            log.error("SQL error during DB operation [{}]: {}", label, e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error in DB operation [{}]: {}", label, e.getMessage(), e);
            return Optional.empty();
        }
    }


    /**
     * Executes a database operation that does not return a value (e.g., INSERT, UPDATE, DELETE).
     * If the operation fails due to an authentication error, it will evict all connections and attempt a single retry.
     * For other exceptions, it logs the error and does not re-throw it.
     *
     * @param runnable The database operation to execute, encapsulated in a DbRunnable lambda.
     * @param label    A descriptive label for the operation, used for logging.
     */
    public void executeVoid(DbRunnable runnable, String label) {
        try {
            runnable.run();
            log.debug("Void DB operation [{}] successful", label);
        } catch (SQLException e) {
            if (isAuthenticationError(e)) {
                log.warn("DB access denied for void [{}]. Evicting connections and attempting a single retry.", label);
                executeVoidWithRetry(runnable, label);
            } else {
                log.error("SQL error in void DB operation [{}]: {}", label, e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error in void DB operation [{}]: {}", label, e.getMessage(), e);
        }
    }

    /**
     * Private helper method to perform a single retry of a database operation that returns a value.
     * This is called after an initial authentication failure. It first evicts connections, then re-attempts the operation.
     *
     * @param supplier The database operation to retry.
     * @param label    The label for logging.
     * @param <T>      The type of the result.
     * @return An Optional containing the result if the retry is successful, otherwise an empty Optional.
     */
    private <T> Optional<T> executeWithRetry(DbSupplier<T> supplier, String label) {
        evictAllConnections();
        try {
            log.info("Retrying DB operation [{}]...", label);
            T result = supplier.get();
            log.info("DB operation [{}] successful on retry.", label);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("DB operation [{}] failed permanently on retry.", label, e);
            return Optional.empty();
        }
    }

    /**
     * Private helper method to perform a single retry of a void database operation.
     * This is called after an initial authentication failure. It first evicts connections, then re-attempts the operation.
     *
     * @param runnable The void database operation to retry.
     * @param label    The label for logging.
     */
    private void executeVoidWithRetry(DbRunnable runnable, String label) {
        evictAllConnections();
        try {
            log.info("Retrying void DB operation [{}]...", label);
            runnable.run();
            log.info("Void DB operation [{}] successful on retry.", label);
        } catch (Exception e) {
            log.error("Void DB operation [{}] failed permanently on retry.", label, e);
        }
    }

    /**
     * Checks if a given SQLException is due to an authentication failure.
     *
     * @param e The SQLException to check.
     * @return true if the error is an authentication error (SQLState '28000' or message contains "Access denied"), false otherwise.
     */
    private boolean isAuthenticationError(SQLException e) {
        return "28000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("Access denied"));
    }

    /**
     * Soft-evicts all idle connections from the Hikari connection pool.
     * This is used to force the pool to acquire fresh connections, which is useful after credentials have been rotated.
     */
    private void evictAllConnections() {
        try {
            if (dataSource.getHikariPoolMXBean() != null) {
                dataSource.getHikariPoolMXBean().softEvictConnections();
                log.info("All idle HikariCP connections were soft-evicted.");
            }
        } catch (Exception e) {
            log.error("Failed to evict HikariCP connections", e);
        }
    }
}