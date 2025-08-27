package com.itt.service.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.exception.ValidationException;
import com.itt.service.fw.search.SearchableEntity;

/**
 * Comprehensive test suite for DataTableRequestValidator.
 * 
 * Tests all validation scenarios including:
 * - Valid requests (should pass)
 * - Invalid filter fields (should throw ValidationException)
 * - Invalid sort fields (should throw ValidationException)
 * - Invalid search columns (should throw ValidationException)
 * - Mixed valid/invalid fields (should throw ValidationException)
 * - Edge cases (null values, empty strings, etc.)
 * 
 * @author ITT Development Team
 * @since 2025.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataTableRequestValidator Tests")
class DataTableRequestValidatorTest {

    private DataTableRequestValidator validator;
    
    @Mock
    private SearchableEntity<Object> mockEntity;

    @BeforeEach
    void setUp() {
        validator = new DataTableRequestValidator();
        
        // Mock entity configuration
        when(mockEntity.getEntityClass()).thenReturn(Object.class);
        when(mockEntity.getSearchableFields()).thenReturn(Set.of("name", "email", "status", "createdOn"));
        when(mockEntity.getSortableFields()).thenReturn(Set.of("name", "email", "createdOn", "id"));
        when(mockEntity.getFieldAliases()).thenReturn(java.util.Map.of(
            "userName", "name",
            "userEmail", "email"
        ));
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation for valid filter fields")
        void shouldPassValidationForValidFilterFields() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("name", "cnt:admin", null),
                createColumn("email", "sw:test@", null),
                createColumn("status", "eq:active", null)
            ));

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }

        @Test
        @DisplayName("Should pass validation for valid sort fields")
        void shouldPassValidationForValidSortFields() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("name", null, "asc"),
                createColumn("email", null, "desc"),
                createColumn("id", null, "asc")
            ));

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }

        @Test
        @DisplayName("Should pass validation for valid search columns")
        void shouldPassValidationForValidSearchColumns() {
            // Given
            DataTableRequest request = createDataTableRequest();
            DataTableRequest.SearchFilter searchFilter = new DataTableRequest.SearchFilter();
            searchFilter.setSearchText("test");
            searchFilter.setColumns(List.of("name", "email", "status"));
            request.setSearchFilter(searchFilter);

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }

        @Test
        @DisplayName("Should pass validation for DTO field name aliases")
        void shouldPassValidationForDtoFieldNameAliases() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("userName", "cnt:admin", "asc"), // Should resolve to "name"
                createColumn("userEmail", "sw:test@", "desc") // Should resolve to "email"
            ));

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }

        @Test
        @DisplayName("Should pass validation for empty/null optional fields")
        void shouldPassValidationForEmptyOptionalFields() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(null);
            request.setSearchFilter(null);

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }
    }

    @Nested
    @DisplayName("Invalid Filter Field Tests")
    class InvalidFilterFieldTests {

        @Test
        @DisplayName("Should throw ValidationException for invalid filter field")
        void shouldThrowValidationExceptionForInvalidFilterField() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidField", "eq:value", null)
            ));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(1, errors.size());
            assertEquals("columns[0].columnName", errors.get(0).getField());
            assertEquals("invalidField", errors.get(0).getRejectedValue());
            assertEquals("INVALID_FILTER_FIELD", errors.get(0).getCode());
        }

        @Test
        @DisplayName("Should throw ValidationException for multiple invalid filter fields")
        void shouldThrowValidationExceptionForMultipleInvalidFilterFields() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidField1", "eq:value1", null),
                createColumn("name", "cnt:valid", null), // Valid field
                createColumn("invalidField2", "eq:value2", null)
            ));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(2, errors.size());
            
            // Check first error
            ValidationError error1 = errors.stream()
                .filter(e -> "columns[0].columnName".equals(e.getField()))
                .findFirst().orElse(null);
            assertNotNull(error1);
            assertEquals("invalidField1", error1.getRejectedValue());
            assertEquals("INVALID_FILTER_FIELD", error1.getCode());
            
            // Check second error
            ValidationError error2 = errors.stream()
                .filter(e -> "columns[2].columnName".equals(e.getField()))
                .findFirst().orElse(null);
            assertNotNull(error2);
            assertEquals("invalidField2", error2.getRejectedValue());
            assertEquals("INVALID_FILTER_FIELD", error2.getCode());
        }
    }

    @Nested
    @DisplayName("Invalid Sort Field Tests")
    class InvalidSortFieldTests {

        @Test
        @DisplayName("Should throw ValidationException for invalid sort field")
        void shouldThrowValidationExceptionForInvalidSortField() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidSortField", null, "asc")
            ));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(1, errors.size());
            assertEquals("columns[0].columnName", errors.get(0).getField());
            assertEquals("invalidSortField", errors.get(0).getRejectedValue());
            assertEquals("INVALID_SORT_FIELD", errors.get(0).getCode());
        }

        @Test
        @DisplayName("Should throw ValidationException for sort field not in sortable fields")
        void shouldThrowValidationExceptionForSortFieldNotInSortableFields() {
            // Given - "status" is searchable but not sortable
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("status", null, "asc")
            ));

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(1, errors.size());
            assertEquals("INVALID_SORT_FIELD", errors.get(0).getCode());
            assertTrue(errors.get(0).getMessage().contains("status"));
        }
    }

    @Nested
    @DisplayName("Invalid Search Column Tests")
    class InvalidSearchColumnTests {

        @Test
        @DisplayName("Should throw ValidationException for invalid search column")
        void shouldThrowValidationExceptionForInvalidSearchColumn() {
            // Given
            DataTableRequest request = createDataTableRequest();
            DataTableRequest.SearchFilter searchFilter = new DataTableRequest.SearchFilter();
            searchFilter.setSearchText("test");
            searchFilter.setColumns(List.of("name", "invalidSearchColumn", "email"));
            request.setSearchFilter(searchFilter);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(1, errors.size());
            assertEquals("searchFilter.columns[1]", errors.get(0).getField());
            assertEquals("invalidSearchColumn", errors.get(0).getRejectedValue());
            assertEquals("INVALID_SEARCH_COLUMN", errors.get(0).getCode());
        }
    }

    @Nested
    @DisplayName("Mixed Validation Tests")
    class MixedValidationTests {

        @Test
        @DisplayName("Should throw ValidationException with multiple error types")
        void shouldThrowValidationExceptionWithMultipleErrorTypes() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidFilter", "eq:value", null),
                createColumn("invalidSort", null, "asc")
            ));
            
            DataTableRequest.SearchFilter searchFilter = new DataTableRequest.SearchFilter();
            searchFilter.setSearchText("test");
            searchFilter.setColumns(List.of("invalidSearch"));
            request.setSearchFilter(searchFilter);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateRequest(request, mockEntity));
            
            List<ValidationError> errors = exception.getValidationErrors();
            assertEquals(3, errors.size());
            
            // Check we have all three error types
            assertTrue(errors.stream().anyMatch(e -> "INVALID_FILTER_FIELD".equals(e.getCode())));
            assertTrue(errors.stream().anyMatch(e -> "INVALID_SORT_FIELD".equals(e.getCode())));
            assertTrue(errors.stream().anyMatch(e -> "INVALID_SEARCH_COLUMN".equals(e.getCode())));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException for null request")
        void shouldThrowIllegalArgumentExceptionForNullRequest() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> validator.validateRequest(null, mockEntity));
            
            assertEquals("DataTableRequest cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null entity")
        void shouldThrowIllegalArgumentExceptionForNullEntity() {
            // Given
            DataTableRequest request = createDataTableRequest();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> validator.validateRequest(request, null));
            
            assertEquals("SearchableEntity cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty filter and sort values gracefully")
        void shouldHandleEmptyFilterAndSortValuesGracefully() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("name", "", ""), // Empty filter and sort
                createColumn("email", "   ", "   ") // Whitespace only
            ));

            // When & Then
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }

        @Test
        @DisplayName("Should handle null column names gracefully")
        void shouldHandleNullColumnNamesGracefully() {
            // Given
            DataTableRequest request = createDataTableRequest();
            DataTableRequest.Column nullNameColumn = new DataTableRequest.Column();
            nullNameColumn.setColumnName(null);
            nullNameColumn.setFilter("eq:value");
            request.setColumns(List.of(nullNameColumn));

            // When & Then - Should not throw exception for null column name
            assertDoesNotThrow(() -> validator.validateRequest(request, mockEntity));
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("validateField should throw ValidationException for invalid field")
        void validateFieldShouldThrowValidationExceptionForInvalidField() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, 
                () -> validator.validateField("invalidField", mockEntity, "filter"));
            
            assertEquals("Invalid filter field 'invalidField'", exception.getMessage());
        }

        @Test
        @DisplayName("validateField should return actual field name for valid field")
        void validateFieldShouldReturnActualFieldNameForValidField() {
            // When
            String actualField = validator.validateField("userName", mockEntity, "filter");

            // Then
            assertEquals("name", actualField);
        }

        @Test
        @DisplayName("isValidRequest should return false for invalid request")
        void isValidRequestShouldReturnFalseForInvalidRequest() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidField", "eq:value", null)
            ));

            // When
            boolean isValid = validator.isValidRequest(request, mockEntity);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("isValidRequest should return true for valid request")
        void isValidRequestShouldReturnTrueForValidRequest() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("name", "cnt:admin", "asc")
            ));

            // When
            boolean isValid = validator.isValidRequest(request, mockEntity);

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("getValidationErrors should return error list without throwing")
        void getValidationErrorsShouldReturnErrorListWithoutThrowing() {
            // Given
            DataTableRequest request = createDataTableRequest();
            request.setColumns(List.of(
                createColumn("invalidField", "eq:value", null)
            ));

            // When
            List<ValidationError> errors = validator.getValidationErrors(request, mockEntity);

            // Then
            assertEquals(1, errors.size());
            assertEquals("INVALID_FILTER_FIELD", errors.get(0).getCode());
        }
    }

    // Helper methods
    
    private DataTableRequest createDataTableRequest() {
        DataTableRequest request = new DataTableRequest();
        DataTableRequest.Pagination pagination = new DataTableRequest.Pagination();
        pagination.setPage(0);
        pagination.setSize(10);
        request.setPagination(pagination);
        return request;
    }

    private DataTableRequest.Column createColumn(String columnName, String filter, String sort) {
        DataTableRequest.Column column = new DataTableRequest.Column();
        column.setColumnName(columnName);
        column.setFilter(filter);
        column.setSort(sort);
        return column;
    }
}