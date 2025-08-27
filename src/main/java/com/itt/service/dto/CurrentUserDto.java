package com.itt.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the currently authenticated user information.
 * Used across all APIs to provide consistent access to user context.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserDto {
    
    /**
     * The unique identifier of the current user
     */
    private Integer userId;
    
    /**
     * The email address of the current user
     */
    private String email;
    
    /**
     * The full name of the current user
     */
    private String name;
}
