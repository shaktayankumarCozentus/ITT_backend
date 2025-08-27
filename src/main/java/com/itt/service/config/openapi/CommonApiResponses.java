package com.itt.service.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common API response annotations for reusability across controllers.
 * These annotations help maintain consistency in API documentation.
 */
public class CommonApiResponses {

    /**
     * Standard success response for data retrieval operations.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved data",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                    {
                      "success": false,
                      "message": "Internal server error occurred",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    public @interface StandardGetResponse {}

    /**
     * Standard response for operations with validation.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "success": false,
                      "message": "Validation failed",
                      "data": null,
                      "errors": [
                        {
                          "field": "fieldName",
                          "message": "Field validation message"
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public @interface ValidationResponse {}

    /**
     * Standard response for create operations.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Resource created successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Resource already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    value = """
                    {
                      "success": false,
                      "message": "Resource already exists",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    public @interface CreateResponse {}

    /**
     * Standard response for update operations.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Resource updated successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Resource not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found Error",
                    value = """
                    {
                      "success": false,
                      "message": "Resource not found",
                      "data": null
                    }
                    """
                )
            )
        )
    })
    public @interface UpdateResponse {}
}
