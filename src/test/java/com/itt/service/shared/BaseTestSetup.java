package com.itt.service.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base test setup class providing common configuration for all test classes.
 * 
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>Pure Mock Testing:</strong> Uses only Mockito for dependency mocking</li>
 *   <li><strong>Database Isolation:</strong> No Spring context loading or database connections</li>
 *   <li><strong>Fast Execution:</strong> Lightweight testing framework for quick test runs</li>
 *   <li><strong>Common Setup:</strong> Shared initialization logic for all tests</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <pre>
 * public class MyServiceTest extends BaseTestSetup {
 *     &#64;Mock
 *     private MyDependency mockDependency;
 *     
 *     &#64;InjectMocks
 *     private MyService serviceUnderTest;
 *     
 *     &#64;Test
 *     void testMyMethod() {
 *         // Test implementation with pure mocks
 *     }
 * }
 * </pre>
 * 
 * <h3>Database Independence:</h3>
 * <p>This base class ensures complete database isolation by:</p>
 * <ul>
 *   <li>Using only {@code @Mock} annotations for dependencies</li>
 *   <li>Avoiding Spring context loading</li>
 *   <li>Preventing any database connections during testing</li>
 *   <li>Enabling fast, reliable unit tests</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseTestSetup {

    /**
     * Common setup method executed before each test method.
     * Override this method in subclasses to add specific setup logic.
     */
    @BeforeEach
    void setUpBase() {
        // Common setup logic can be added here
        // For example: clearing static caches, resetting counters, etc.
    }

    /**
     * Utility method to create a test transaction ID for audit logging.
     * 
     * @return formatted transaction ID for testing
     */
    protected String getTestTransactionId() {
        return "TEST-TXN-" + System.currentTimeMillis();
    }

    /**
     * Utility method to create a test user ID for testing.
     * 
     * @return test user ID
     */
    protected Long getTestUserId() {
        return 12345L;
    }

    /**
     * Utility method to create a test company ID for multi-tenant testing.
     * 
     * @return test company ID
     */
    protected Long getTestCompanyId() {
        return 100L;
    }

    /**
     * Utility method to create a test role ID.
     * 
     * @return test role ID
     */
    protected Long getTestRoleId() {
        return 1L;
    }
}
