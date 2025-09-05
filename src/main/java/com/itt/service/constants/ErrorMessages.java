package com.itt.service.constants;

/**
 * Error messages for API responses - contains all constants used throughout the
 * application.
 * 
 * <p>
 * This class contains error messages that are used across the application.
 * These messages are designed to be user-friendly while maintaining
 * consistency.
 * </p>
 */
public final class ErrorMessages {

	// ==================== VALIDATION MESSAGES ====================
	public static final String VALIDATION_FAILED = "Validation failed for object";
	public static final String CONSTRAINT_VIOLATION = "Constraint violation";
	public static final String MISSING_REQUIRED_FIELD = "Missing required field: {0}";
	public static final String INVALID_DATA_FORMAT = "Invalid data format for field: {0}";
	public static final String INVALID_EMAIL = "Please enter a valid email address";
	public static final String INVALID_PHONE = "Please enter a valid phone number";
	public static final String PASSWORD_TOO_WEAK = "Password must be at least 8 characters with letters and numbers";

	// ==================== AUTHENTICATION MESSAGES ====================
	public static final String INVALID_CREDENTIALS = "Invalid credentials";
	public static final String TOKEN_EXPIRED = "Token has expired";
	public static final String ACCESS_DENIED = "Access denied";
	public static final String UNAUTHORIZED = "Unauthorized access";
	public static final String SESSION_EXPIRED = "Session has expired";
	public static final String ACCOUNT_LOCKED = "Account has been locked";

	// ==================== BUSINESS LOGIC MESSAGES ====================
	public static final String USER_ALREADY_EXISTS = "User already exists";
	public static final String USER_NOT_FOUND = "User not found";
	public static final String EMAIL_ALREADY_REGISTERED = "Email address is already registered";
	public static final String INSUFFICIENT_BALANCE = "Insufficient balance";
	public static final String DUPLICATE_ENTRY = "Duplicate entry found";
	public static final String DATA_INTEGRITY_VIOLATION = "Data integrity violation";
	public static final String RESOURCE_NOT_FOUND = "Resource not found";
	public static final String ENTITY_NOT_FOUND = "Entity not found";
	public static final String OPERATION_NOT_ALLOWED = "Operation not allowed";

	// ==================== SUBSCRIPTION MESSAGES ====================
	public static final String BULK_SUBSCRIPTION_UPDATE_FAILURE = "Failed to update subscription for selected companies";
	public static final String SUBSCRIPTION_UPDATE_FAILURE = "Failed to update customer subscription";
	public static final String SUBSCRIPTION_FEATURES_COPY_FAILURE = "Failed to copy subscription features";

	// ==================== PETA/PETD MESSAGES ====================
	public static final String BULK_PETA_PETD_UPDATE_FAILURE = "Failed to update PETA/PETD for selected companies";
	public static final String PETA_PETD_UPDATE_FAILURE = "Failed to update PETA/PETD settings";

	// ==================== SYSTEM MESSAGES ====================
	public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred";
	public static final String DATABASE_CONNECTION_ERROR = "Database connection error";
	public static final String EXTERNAL_SERVICE_ERROR = "External service error";
	public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";

	// ==================== FILE OPERATION MESSAGES ====================
	public static final String FILE_NOT_FOUND = "File not found";
	public static final String INVALID_FILE_FORMAT = "Invalid file format. Supported formats: {0}";
	public static final String FILE_SIZE_EXCEEDED = "File size exceeds maximum limit of {0} MB";
	public static final String INVALID_FILE_TYPE = "Invalid file type";
	public static final String FILE_UPLOAD_FAILED = "File upload failed";

	// ==================== HTTP/REQUEST MESSAGES ====================
	public static final String METHOD_NOT_ALLOWED = "HTTP method not allowed";
	public static final String MEDIA_TYPE_NOT_SUPPORTED = "Media type not supported";
	public static final String ENDPOINT_NOT_FOUND = "Endpoint not found";
	public static final String INVALID_REQUEST = "Invalid request";
	public static final String REQUEST_TIMEOUT = "Request timeout";

	// ==================== ROLE MANAGEMENT MESSAGES ====================
	public static final String ROLE_NOT_FOUND = "Role not found";
	public static final String ROLE_ALREADY_EXISTS = "Role already exists";
	public static final String INVALID_ROLE_CONFIGURATION = "Invalid role configuration";

	// ==================== COMPANY MANAGEMENT MESSAGES ====================
	public static final String COMPANY_NOT_FOUND = "Company not found";
	public static final String INVALID_COMPANY_DATA = "Invalid company data";

	// ==================== CONFIGURATION MESSAGES ====================
	public static final String CONFIG_NOT_FOUND = "Configuration not found";
	public static final String INVALID_CONFIGURATION = "Invalid configuration";

	private ErrorMessages() {
		// Utility class - prevent instantiation
	}
}