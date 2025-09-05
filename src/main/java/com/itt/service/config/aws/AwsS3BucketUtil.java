package com.itt.service.config.aws;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

@Slf4j
@Component
public class AwsS3BucketUtil {

	/**
	 * Method to fetch the uploaded CSV file in a bucket.
	 * 
	 * @param bucketName
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private final S3Client s3Client;

	// Initialize S3Client once during construction
	public AwsS3BucketUtil() {
		this.s3Client = S3Client.builder().region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.builder().build()).build();

		log.info("AWSS3BucketUtil :: S3Client initialized");
	}

	public InputStream loadFileFromAWSS3(String bucketName, String fileName) throws Exception {
		log.info("AWSS3BucketUtil :: loadFileFromS3 :: Start() filename: {} and bucket name: {}", fileName, bucketName);
		try {
			log.info("AWSS3BucketUtil :: loadFileFromS3 :: extracting data");
			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

			InputStream csvData = s3Client.getObject(getObjectRequest);
			if (ObjectUtils.isEmpty(csvData)) {
				log.info("AWSS3BucketUtil :: loadFileFromS3 :: Empty/Null data from CSV.");
			}
			return csvData;
		} catch (Exception e) {
			log.error("Error while fetching file from S3", e);
			throw new Exception("Failed to load file from S3: " + e.getMessage());
		}
	}

	/*
	 * public boolean fileExistsInS3(String bucketName, String key) { try {
	 * HeadObjectRequest headObjectRequest =
	 * HeadObjectRequest.builder().bucket(bucketName).key(key).build();
	 * 
	 * s3Client.headObject(headObjectRequest); // returns metadata if object exists
	 * return true; } catch (S3Exception e) { if (e.statusCode() == 404) { return
	 * false; // object not found } else { throw e; // rethrow other errors } } }
	 */
	
	public boolean fileExistsInS3(String bucketName, String key) {
	    try {
	       InputStream s3Object = getS3Object(bucketName, key);
	       return true;
	    } catch (S3Exception e) {
	       if (e.statusCode() == 404) {
	          return false; // object not found
	       } else {
	          throw e; // rethrow other errors
	       }
	    }
	}

	public InputStream getS3Object(String bucketName, String keyName) {
	    try {
	       GetObjectRequest objectRequest = GetObjectRequest.builder().key(keyName).bucket(bucketName).build();
	       return s3Client.getObject(objectRequest, ResponseTransformer.toInputStream());
	    } catch (Exception e) {
	       log.error("Error downloading file from S3: {}", e.getMessage());
	       throw e;
	    }
	}

	/**
	 * Method to upload a file to S3 Bucket.
	 * 
	 * @param file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String uploadCSVToS3Bucket(String uploadLocation, MultipartFile file, String fileName) throws Exception {
		log.info("AWSS3BucketUtil :: uploadCSVToS3Bucket :: Start() filename = {} and bucket name = {}", fileName,
				uploadLocation);

		Path tempFilePath = Files.createTempFile("upload-", file.getOriginalFilename());
		file.transferTo(tempFilePath.toFile());

		S3TransferManager transferManager = null;
		try {
			transferManager = S3TransferManager.create();

			UploadFileRequest uploadRequest = UploadFileRequest.builder()
					.putObjectRequest(builder -> builder.bucket(uploadLocation).key(fileName).contentType("text/csv"))
					.source(tempFilePath).build();

			uploadWithRetries(transferManager, uploadRequest);

			log.info("AWSS3BucketUtil :: uploadCSVToS3Bucket :: File uploaded successfully.");
			return String.format("https://%s.s3.%s.amazonaws.com/%s", uploadLocation, Region.US_EAST_1.id(), fileName);
		} catch (Exception e) {
			log.error("AWSS3BucketUtil :: uploadCSVToS3Bucket :: Unexpected error during file upload", e);
			throw e;
		} finally {

			if (transferManager != null) {
				transferManager.close();
			}

			Files.deleteIfExists(tempFilePath);
		}

	}

	private void uploadWithRetries(S3TransferManager transferManager, UploadFileRequest uploadRequest)
			throws InterruptedException {
		int maxRetries = 3;
		int attempt = 0;
		boolean uploadSuccess = false;

		while (!uploadSuccess && attempt < maxRetries) {
			try {
				attempt++;
				log.info("AWSS3BucketUtil :: uploadCSVToS3Bucket :: Attempt #{} to upload file", attempt);

				transferManager.uploadFile(uploadRequest).completionFuture().join();

				uploadSuccess = true;
				log.info("AWSS3BucketUtil :: uploadCSVToS3Bucket :: File uploaded successfully on attempt #{}",
						attempt);
			} catch (SdkClientException e) {
				if (e.getMessage().contains("connection was closed") && attempt < maxRetries) {
					int backoffTime = (int) Math.pow(2, attempt) * 1000;
					log.warn("AWSS3BucketUtil :: uploadCSVToS3Bucket :: Connection closed during upload. "
							+ "Retrying in {} ms. Attempt {}/{}", backoffTime, attempt, maxRetries, e);
					Thread.sleep(backoffTime);
				} else {
					log.error("AWSS3BucketUtil :: uploadCSVToS3Bucket :: Failed to upload file after {} attempts",
							attempt, e);
					throw e;
				}
			}
		}

	}

	/**
	 * Method to get the presigned URL from AWS S3 bucket.
	 * 
	 * @param s3Client
	 */
	public String getPresignedURL(S3Client s3Client, String bucketName, String fileName, Date expiration)
			throws Exception {
		log.info("AWSS3BucketUtil :: getPresignedURL :: Start() bucketName: {}, fileName: {}", bucketName, fileName);

		try (S3Presigner presigner = S3Presigner.builder().region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.builder().build()).build()) {

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(java.time.Duration.ofMillis(expiration.getTime() - System.currentTimeMillis()))
					.getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key(fileName).build()).build();

			URL presignedUrl = presigner.presignGetObject(presignRequest).url();
			log.info("AWSS3BucketUtil :: getPresignedURL :: Presigned URL generated successfully.");
			return presignedUrl.toString();
		} catch (Exception e) {
			log.error("AWSS3BucketUtil :: getPresignedURL :: Error occurred: {}", e.getMessage());
			throw e;
		}
	}

	public String uploadFilebyCreatingFolder(String bucketName, String folderName, MultipartFile file, String fileName)
			throws Exception {
		log.info("AWSS3BucketUtil :: uploadFileByCreatingFolder :: Start() filename = {} and bucket name = {}",
				fileName, bucketName);
		S3Client s3Client = S3Client.builder().region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.builder().build()).build();

		String folderKey = folderName + "/";
		PutObjectRequest folderRequest = PutObjectRequest.builder().bucket(bucketName).key(folderKey).build();

		s3Client.putObject(folderRequest, RequestBody.empty());

		PutObjectRequest fileRequest = PutObjectRequest.builder().bucket(bucketName).key(folderName + "/" + fileName)
				.contentType("application/pdf").build();

		s3Client.putObject(fileRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		log.info("AWSS3BucketUtil :: uploadFileByCreatingFolder :: File uploaded successfully.");
		return String.format("https://%s.s3.%s.amazonaws.com/%s/%s", bucketName, Region.US_EAST_1.id(), folderName,
				fileName);
	}

	/**
	 * Method to upload a file to S3 Bucket with content-type as application/pdf.
	 * 
	 * @param file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String uploadPDFToS3Bucket(String bucketName, MultipartFile file, String fileName) throws Exception {
		log.info("AWSS3BucketUtil :: uploadPDFToS3Bucket :: Start() filename = {} and bucket name = {}", fileName,
				bucketName);

		S3Client s3Client = S3Client.builder().region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.builder().build()).build();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileName)
				.contentType("application/pdf").build();

		s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		log.info("AWSS3BucketUtil :: uploadPDFToS3Bucket :: File uploaded successfully.");
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.US_EAST_1.id(), fileName);
	}
}