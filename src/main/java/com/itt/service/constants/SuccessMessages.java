package com.itt.service.constants;

public final class SuccessMessages {

    // General Success Messages
    public static final String OPERATION_SUCCESSFUL = "Operation completed successfully.";
    public static final String DATA_RETRIEVED = "Data retrieved successfully.";
    public static final String DATA_SAVED = "Data saved successfully.";
    public static final String DATA_UPDATED = "Data updated successfully.";
    public static final String DATA_DELETED = "Data deleted successfully.";

    // User Management
    public static final String USER_CREATED = "User account created successfully.";
    public static final String USER_UPDATED = "User profile updated successfully.";
    public static final String USER_DELETED = "User account deleted successfully.";
    public static final String PASSWORD_CHANGED = "Password changed successfully.";
    public static final String EMAIL_VERIFIED = "Email address verified successfully.";

    // Authentication
    public static final String LOGIN_SUCCESSFUL = "Login successful. Welcome back!";
    public static final String LOGOUT_SUCCESSFUL = "You have been logged out successfully.";
    public static final String TOKEN_REFRESHED = "Authentication token refreshed successfully.";

    // File Operations
    public static final String FILE_UPLOADED = "File uploaded successfully.";
    public static final String FILE_DELETED = "File deleted successfully.";
    public static final String FILE_DOWNLOADED = "File downloaded successfully.";

    // Customer Subscription Messages
    public static final String SUBSCRIPTION_UPDATE_SUCCESSFUL = "Subscription for '%s' updated successfully";
    public static final String BULK_SUBSCRIPTION_UPDATE_SUCCESSFUL = "Subscription tier updated for %d customer%s";
    public static final String SUBSCRIPTION_FEATURES_COPY_SUCCESSFUL = "Features copied to '%d' customer%s successfully";
    
    // PETA-PETD Messages
    public static final String PETA_PETD_UPDATE_SUCCESSFUL = "PETA/PETD calling and frequency have been updated for '%s'";
    public static final String BULK_PETA_PETD_UPDATE_SUCCESSFUL = "PETA/PETD calling updated for %d customer%s";

    private SuccessMessages() {
        // Utility class
    }
}
