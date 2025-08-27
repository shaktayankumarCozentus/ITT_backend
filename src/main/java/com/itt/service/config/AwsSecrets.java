package com.itt.service.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * AWS Secrets Manager configuration POJO. Maps JSON structure from AWS Secrets
 * Manager to Java object. All fields are mandatory and must be present in the
 * AWS secret.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwsSecrets {

	// =============================================================================
	// DATABASE CONFIGURATION
	// =============================================================================

	// Write Database (Master) - For all write operations
	private String databaseWriteHost;
	private String databaseWritePort;
	private String databaseWriteUsername;
	private String databaseWritePassword;
	private String databaseWriteDatabase;

	// Read Database (Replica) - For read operations and load balancing
	private String databaseReadHost;
	private String databaseReadPort;
	private String databaseReadUsername;
	private String databaseReadPassword;
	private String databaseReadDatabase;

	// =============================================================================
	// AUTH0 CONFIGURATION
	// =============================================================================

	private String auth0ClientId;
	private String auth0ClientSecret;
	private String auth0Domain;

	// =============================================================================
	// OAUTH2 CONFIGURATION
	// =============================================================================

	private String oauth2JwtIssuerUri;

}