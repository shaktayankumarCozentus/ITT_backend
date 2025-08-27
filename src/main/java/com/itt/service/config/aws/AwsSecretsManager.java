//package com.itt.service.config.aws;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.StringUtils;
//
//import com.itt.service.exception.AwsSecretFetchException;
//import com.itt.service.exception.SecretNotFoundException;
//import com.itt.service.exception.SecretParsingException;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.itt.service.config.AwsSecrets;
//
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.core.exception.SdkClientException;
//import software.amazon.awssdk.core.exception.SdkException;
//import software.amazon.awssdk.core.exception.SdkServiceException;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
//import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
//
//@Configuration
//public class AwsSecretsManager {
//
//	@Value("${cloud.aws.secret-name}")
//	private String secretName;
//
//	@Value("${cloud.aws.region.static}")
//	private String region;
//
//	private final ObjectMapper objectMapper;
//
//	public AwsSecretsManager(ObjectMapper objectMapper) {
//		this.objectMapper = objectMapper;
//	}
//
//	@Bean
//	public SecretsManagerClient secretsManagerClient() {
//		validateConfiguration();
//		return SecretsManagerClient.builder().region(Region.of(region))
//				.credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
//	}
//
//	@Bean("awsSecrets")
//	public AwsSecrets getSecret(SecretsManagerClient secretsManagerClient) {
//		try {
//			GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretName).build();
//
//			GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
//			String secretString = response.secretString();
//
//			if (!StringUtils.hasText(secretString)) {
//				throw new SecretNotFoundException("Secret value is empty or null for secret: " + secretName);
//			}
//
//			return objectMapper.readValue(secretString, AwsSecrets.class);
//		} catch (ResourceNotFoundException e) {
//			throw new SecretNotFoundException("Secret not found: " + secretName, e);
//		} catch (JsonProcessingException e) {
//			throw new SecretParsingException("Error parsing AWS secret JSON for secret: " + secretName, e);
//		} catch (SdkClientException e) {
//			throw new AwsSecretFetchException("AWS client configuration error: " + e.getMessage(), e);
//		} catch (SdkServiceException e) {
//			throw new AwsSecretFetchException("AWS service error: " + e.getMessage(), e);
//		} catch (SdkException e) {
//			throw new AwsSecretFetchException("Error fetching AWS secret: " + secretName, e);
//		}
//	}
//
//	private void validateConfiguration() {
//		if (!StringUtils.hasText(secretName)) {
//			throw new IllegalArgumentException("AWS secret name is not configured.");
//		}
//		if (!StringUtils.hasText(region)) {
//			throw new IllegalArgumentException("AWS region is not configured.");
//		}
//	}
//}
