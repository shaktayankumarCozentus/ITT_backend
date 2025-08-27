package com.itt.service.constants;


public final class ErrorMessages {
    
    // General Messages
    public static final String INTERNAL_SERVER_ERROR = "We're experiencing technical difficulties. Please try again later.";
    public static final String INVALID_REQUEST = "The request contains invalid data. Please check your input.";
    public static final String RESOURCE_NOT_FOUND = "The requested resource could not be found.";
    public static final String ACCESS_DENIED = "You don't have permission to access this resource.";
    public static final String UNAUTHORIZED = "Authentication is required to access this resource.";
    
    // Validation Messages
    public static final String VALIDATION_FAILED = "Please correct the highlighted fields and try again.";
    public static final String CONSTRAINT_VIOLATION = "The provided data violates system constraints.";
    public static final String MISSING_REQUIRED_FIELD = "Required field is missing: {0}";
    public static final String INVALID_DATA_FORMAT = "Invalid data format for field: {0}";
    public static final String INVALID_EMAIL_FORMAT = "Please enter a valid email address.";
    public static final String INVALID_PHONE_FORMAT = "Please enter a valid phone number.";
    public static final String PASSWORD_TOO_WEAK = "Password must be at least 8 characters with uppercase, lowercase, and numbers.";
    
    // Authentication Messages
    public static final String INVALID_CREDENTIALS = "Invalid email or password. Please try again.";
    public static final String TOKEN_EXPIRED = "Your session has expired. Please log in again.";
    public static final String TOKEN_INVALID = "Invalid authentication token. Please log in again.";
    public static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact support.";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. Please contact support.";
    
    // Business Logic Messages
    public static final String USER_ALREADY_EXISTS = "A user with this information already exists.";
    public static final String USER_NOT_FOUND = "User not found. Please check the details and try again.";
    public static final String EMAIL_ALREADY_REGISTERED = "This email address is already registered.";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance to complete this transaction.";
    public static final String OPERATION_NOT_ALLOWED = "This operation is not allowed at this time.";
    public static final String DUPLICATE_ENTRY = "This entry already exists in the system.";
    
    // Database Messages
    public static final String DATABASE_CONNECTION_ERROR = "Unable to connect to the database. Please try again later.";
    public static final String DATA_INTEGRITY_VIOLATION = "The operation conflicts with existing data.";
    public static final String ENTITY_NOT_FOUND = "The requested item could not be found.";
    public static final String OPTIMISTIC_LOCK_ERROR = "The record was modified by another user. Please refresh and try again.";
    
    // File Operation Messages
    public static final String FILE_NOT_FOUND = "The requested file could not be found.";
    public static final String FILE_SIZE_EXCEEDED = "File size exceeds the maximum allowed limit of {0}MB.";
    public static final String INVALID_FILE_FORMAT = "Invalid file format. Supported formats: {0}";
    public static final String FILE_UPLOAD_FAILED = "File upload failed. Please try again.";
    
    // HTTP Messages
    public static final String METHOD_NOT_ALLOWED = "This HTTP method is not supported for this endpoint.";
    public static final String MEDIA_TYPE_NOT_SUPPORTED = "The content type is not supported.";
    public static final String ENDPOINT_NOT_FOUND = "The requested endpoint does not exist.";
    public static final String REQUEST_TIMEOUT = "Request timed out. Please try again.";
    
    // External Service Messages
    public static final String EXTERNAL_SERVICE_UNAVAILABLE = "External service is temporarily unavailable.";
    public static final String EXTERNAL_SERVICE_TIMEOUT = "External service request timed out.";
    public static final String THIRD_PARTY_API_ERROR = "Third-party service error. Please try again later.";

    // Customer Subscription Messages
    public static final String SUBSCRIPTION_UPDATE_FAILURE = "Unable to update subscription. Please try again.";
    public static final String BULK_SUBSCRIPTION_UPDATE_FAILURE = "Unable to update subscriptions. Please try again.";
    public static final String SUBSCRIPTION_FEATURES_COPY_FAILURE = "Unable to copy features. Please try again.";
    
    // PETA-PETD Messages
    public static final String PETA_PETD_UPDATE_FAILURE = "Unable to update PETA/PETD calling and frequency. Please try again.";
    public static final String BULK_PETA_PETD_UPDATE_FAILURE = "Unable to update PETA/PETD calling for customers. Please try again.";
    
    private ErrorMessages() {
        // Utility class
    }
}
