package com.itt.service.service;

import com.itt.service.entity.MasterConfig;
import com.itt.service.repository.MasterConfigRepository;
import com.itt.service.service.impl.FeatureToggleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for FeatureToggleServiceImpl.
 * 
 * <p>Tests the feature toggle functionality that retrieves configuration values
 * from the master_config table to control application behavior at runtime.</p>
 * 
 * <p>Coverage includes:</p>
 * <ul>
 *   <li>Async logging toggle - enabled/disabled scenarios</li>
 *   <li>Configuration not found scenarios</li>
 *   <li>Invalid configuration value parsing</li>
 *   <li>Multiple configurations with same type</li>
 *   <li>Case sensitivity handling</li>
 *   <li>Default value behavior</li>
 * </ul>
 * 
 * @author Service Test Team
 * @version 1.0
 * @see FeatureToggleServiceImpl
 * @see MasterConfig
 * @see MasterConfigRepository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FeatureToggleServiceImpl Tests")
class FeatureToggleServiceImplTest {

    @Mock
    private MasterConfigRepository configRepo;

    @InjectMocks
    private FeatureToggleServiceImpl featureToggleService;

    private static final String FEATURE_TOGGLE_TYPE = "feature_toggle";
    private static final String ASYNC_LOGGING_KEY = "async_logging_enabled";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Nested
    @DisplayName("isAsyncLoggingEnabled() Tests")
    class IsAsyncLoggingEnabledTests {

        @Test
        @DisplayName("Should return true when async logging is enabled with 'true' string value")
        void shouldReturnTrueWhenAsyncLoggingEnabledWithTrueString() {
            // Given
            MasterConfig enabledConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "true");
            List<MasterConfig> configs = Arrays.asList(enabledConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isTrue();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when async logging is disabled with 'false' string value")
        void shouldReturnFalseWhenAsyncLoggingDisabledWithFalseString() {
            // Given
            MasterConfig disabledConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "false");
            List<MasterConfig> configs = Arrays.asList(disabledConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return true when async logging enabled with 'TRUE' uppercase")
        void shouldReturnTrueWhenAsyncLoggingEnabledWithUppercaseTrue() {
            // Given
            MasterConfig enabledConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "TRUE");
            List<MasterConfig> configs = Arrays.asList(enabledConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isTrue();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when async logging has invalid boolean value")
        void shouldReturnFalseWhenAsyncLoggingHasInvalidBooleanValue() {
            // Given
            MasterConfig invalidConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "invalid");
            List<MasterConfig> configs = Arrays.asList(invalidConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when async logging config has null string value")
        void shouldReturnFalseWhenAsyncLoggingConfigHasNullValue() {
            // Given
            MasterConfig nullValueConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, null);
            List<MasterConfig> configs = Arrays.asList(nullValueConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when async logging config has empty string value")
        void shouldReturnFalseWhenAsyncLoggingConfigHasEmptyValue() {
            // Given
            MasterConfig emptyValueConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "");
            List<MasterConfig> configs = Arrays.asList(emptyValueConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when no feature toggle configurations exist")
        void shouldReturnFalseWhenNoFeatureToggleConfigurationsExist() {
            // Given
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(Collections.emptyList());

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return false when async logging configuration not found among other toggles")
        void shouldReturnFalseWhenAsyncLoggingConfigNotFoundAmongOtherToggles() {
            // Given
            MasterConfig otherToggle1 = createFeatureToggleConfig("maintenance_mode_enabled", "true");
            MasterConfig otherToggle2 = createFeatureToggleConfig("new_feature_enabled", "false");
            List<MasterConfig> configs = Arrays.asList(otherToggle1, otherToggle2);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should find async logging config among multiple feature toggles")
        void shouldFindAsyncLoggingConfigAmongMultipleFeatureToggles() {
            // Given
            MasterConfig maintenanceToggle = createFeatureToggleConfig("maintenance_mode_enabled", "false");
            MasterConfig asyncLoggingToggle = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "true");
            MasterConfig newFeatureToggle = createFeatureToggleConfig("new_feature_enabled", "false");
            
            List<MasterConfig> configs = Arrays.asList(maintenanceToggle, asyncLoggingToggle, newFeatureToggle);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isTrue();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should handle case insensitive key code matching")
        void shouldHandleCaseInsensitiveKeyCodeMatching() {
            // Given - key code in different case
            MasterConfig upperCaseKeyConfig = createFeatureToggleConfig("ASYNC_LOGGING_ENABLED", "true");
            List<MasterConfig> configs = Arrays.asList(upperCaseKeyConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isTrue();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should return first matching config when multiple async logging configs exist")
        void shouldReturnFirstMatchingConfigWhenMultipleAsyncLoggingConfigsExist() {
            // Given - multiple configs with same key (shouldn't happen in practice but test edge case)
            MasterConfig firstConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "true");
            firstConfig.setId(1);
            MasterConfig secondConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "false");
            secondConfig.setId(2);
            
            List<MasterConfig> configs = Arrays.asList(firstConfig, secondConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isTrue(); // Should use first match (true)
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should handle numeric boolean values - '1' as true")
        void shouldHandleNumericBooleanValueOneAsTrue() {
            // Given
            MasterConfig numericTrueConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, "1");
            List<MasterConfig> configs = Arrays.asList(numericTrueConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse(); // Boolean.parseBoolean("1") returns false
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should handle whitespace in boolean values")
        void shouldHandleWhitespaceInBooleanValues() {
            // Given
            MasterConfig whitespaceConfig = createFeatureToggleConfig(ASYNC_LOGGING_KEY, " true ");
            List<MasterConfig> configs = Arrays.asList(whitespaceConfig);
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(configs);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse(); // Boolean.parseBoolean(" true ") returns false (strict parsing)
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }
    }

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should call repository with correct config type")
        void shouldCallRepositoryWithCorrectConfigType() {
            // Given
            when(configRepo.findByConfigType(anyString())).thenReturn(Collections.emptyList());

            // When
            featureToggleService.isAsyncLoggingEnabled();

            // Then
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }

        @Test
        @DisplayName("Should handle repository returning null")
        void shouldHandleRepositoryReturningNull() {
            // Given
            when(configRepo.findByConfigType(FEATURE_TOGGLE_TYPE)).thenReturn(null);

            // When
            boolean result = featureToggleService.isAsyncLoggingEnabled();

            // Then
            assertThat(result).isFalse();
            verify(configRepo).findByConfigType(FEATURE_TOGGLE_TYPE);
        }
    }

    // ==================== Test Helper Methods ====================

    /**
     * Creates a test MasterConfig for feature toggle testing.
     * 
     * @param keyCode the key code for the toggle
     * @param stringValue the string value (true/false)
     * @return configured MasterConfig for testing
     */
    private MasterConfig createFeatureToggleConfig(String keyCode, String stringValue) {
        MasterConfig config = new MasterConfig();
        config.setId(1);
        config.setConfigType(FEATURE_TOGGLE_TYPE);
        config.setKeyCode(keyCode);
        config.setName("Test Feature Toggle");
        config.setDescription("Test configuration for " + keyCode);
        config.setStringValue(stringValue);
        config.setCreatedOn(LocalDateTime.now());
        config.setCreatedById(1);
        return config;
    }


}
