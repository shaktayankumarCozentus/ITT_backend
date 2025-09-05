package com.itt.service.service.impl;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.config.aws.AwsS3BucketUtil;
import com.itt.service.constants.DocumentRelatedConstants;
import com.itt.service.constants.ErrorMessages;
import com.itt.service.constants.SuccessMessages;
import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.release_manual.ReleaseManualNotesDTO;
import com.itt.service.entity.DocumentDetails;
import com.itt.service.entity.ReleaseManual;
import com.itt.service.enums.ErrorCode;
import com.itt.service.repository.DocumentDetailsRepository;
import com.itt.service.repository.ReleaseManualRepository;
import com.itt.service.service.ReleaseManualNotesService;
import com.itt.service.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseManualServiceImpl implements ReleaseManualNotesService {

    private final ReleaseManualRepository releaseManualRepository;
    private final AwsS3BucketUtil awsS3BucketUtil;
    private final DocumentDetailsRepository detailsRepository;

    //	@Override
//	public PaginationResponse<ReleaseManualNotesDTO> getReleaseNotesResponse(String userId, String docType) {
//		
//		log.info("enter into ReleaseNotesResponse");
//		List<ReleaseManualNotesDTO> releaseNotesResponse = new ArrayList<>();
//		
//		try {
//			//ReleaseNotesUserEntity releasenoteusers = releaseNotesUserRepository.findByUserEmail(userId);
//			ReleaseManualNotesDTO responseDTO = new ReleaseManualNotesDTO();
//			//responseDTO.setIsSuperUser(!ObjectUtils.isEmpty(releasenoteusers) ? 1 : 0);
//
//			//GetDocumentDetailsEntity docDetails = null;
//			List<ReleaseManual> releasenotesresponse = releaseManualRepository.findLatestUpdateReleaseNotes();
//			responseDTO.set(releasenotesresponse);
//
//			//docDetails = documentDetails.getDocumentDetails(docType);
//			releaseNotesResponse.add(responseDTO);
//		} catch (Exception e) {
//			log.error("An error occurred while fetching release notes response: " + e.getMessage(), e);
//		}
//
//		return releaseNotesResponse;
//	}

    @Override
    @ReadOnlyDataSource("Get release notes based on document type")
    @Transactional(readOnly = true)
    public PaginationResponse<ReleaseManualNotesDTO> getReleaseNotesResponse(String docType, DataTableRequest request) {
        log.info("Entering ReleaseManualServiceImpl.getReleaseNotesResponse docType={}", docType);


        // Optionally, validate docType if needed (e.g., "USER MANUAL" or "RELEASE NOTE")
        if (!"USER_MANUAL".equalsIgnoreCase(docType) && !"RELEASE_NOTE".equalsIgnoreCase(docType)) {
            throw new IllegalArgumentException("Invalid docType. Allowed values are 'USER_MANUAL' or 'RELEASE_NOTE'");
        }

        // Assuming userId is retrieved from the authenticated session/context
        String userId = "";  // Retrieve userId based on your authentication mechanism

        // Call service method to fetch paginated data
        // Convert DataTableRequest into Pageable for pagination and sorting
        Pageable pageable = request.toPageable();

        // Fetch paginated release notes from the repository using the updated query method
        Page<ReleaseManual> page = releaseManualRepository.findByDocTypeAndLatestUpdate(docType, pageable);

        if (page.isEmpty()) {
            return new PaginationResponse<>(
                    page.getPageable().getPageNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast(),
                    List.of()
            );
        }

        List<ReleaseManualNotesDTO> content = page.getContent().stream()
                .map(releaseManual -> ReleaseManualNotesDTO.builder()
                        .id(releaseManual.getId())
                        .noteType(releaseManual.getNoteType())
                        .fileName(releaseManual.getFileName())
                        .releaseUserManualName(releaseManual.getReleaseUserManualName())
                        .dateOfReleaseNote(toLocalDateTimeOrNull(releaseManual.getDateOfReleaseNote()))
                        .uploadedOn(toLocalDateTimeOrNull(releaseManual.getUploadedOn()))
                        .updatedOn(toLocalDateTimeOrNull(releaseManual.getUpdatedOn()))
                        .build())
                .toList();

        return new PaginationResponse<>(
                page.getPageable().getPageNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                content
        );
    }

    private boolean checkIfExistsInS3(String fileName, DocumentDetails docDetails, String folderName) {
        if (docDetails != null && fileName != null && !fileName.isBlank()) {
            try {
                String key = folderName + "/" + fileName;
                return awsS3BucketUtil.fileExistsInS3(docDetails.getBucketName(), key);
            } catch (S3Exception e) {
                if (e.awsErrorDetails() != null && "ExpiredToken".equals(e.awsErrorDetails().errorCode())) {
                    log.info("S3 token expired while checking existence; treating as exists to allow flow.");
                    return true;
                }
                log.error("S3Exception while checking file existence in S3: {}", e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
                throw e; // Replace with your custom storage exception
            }
        }
        return false;
    }

    @Override
    @ReadOnlyDataSource("Get presigned url from S3 based on file name")
    @Transactional(readOnly = true)
    public String getReleaseNotesById(Integer docId) {

        String documentUrl = "";
        var releaseNotesResponse = releaseManualRepository.findById(docId)
                .orElseThrow(() -> new NoSuchElementException("ReleaseManual not found with id: " + docId));

        if (releaseNotesResponse != null) {
            var docDetails = detailsRepository.getDocumentDetails(releaseNotesResponse.getNoteType())//Sanskar need to clarify
                    .orElseThrow(() -> new NoSuchElementException("Document config not found for docType: " + releaseNotesResponse.getNoteType()));

            String bucketBase = docDetails.getBucketName();
            String bucketPath = bucketBase + "/" + "release-documents";
//            String bucketPath = "https://apps-itt-docs-bucket-dev.s3.us-east-1.amazonaws.com/release-documents";

            boolean existsInS3 = checkIfExistsInS3(releaseNotesResponse.getFileName(), docDetails, "release-documents");
            if (existsInS3) {
                Date expiration = Date.from(Instant.now().plus(Duration.ofHours(1))); // 1 hour
                try (S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build()) {
                    try {
                        documentUrl = awsS3BucketUtil.getPresignedURL(
                                s3Client,
                                bucketPath,
                                releaseNotesResponse.getFileName(),
                                expiration
                        );
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to generate presigned URL", ex); // Replace with your custom exception
                    }
                }
            }
        }
        return documentUrl;
    }

    @Override
    @ReadOnlyDataSource("Get presigned url from S3 based on file name")
    @Transactional(readOnly = true)
    public String getReleaseNotesWithFileName(String userId, String docType, String releaseUserManualName) {
        log.info("Entering ReleaseManualServiceImpl.getReleaseNotesWithFileName userId={}, docType={}, releaseUserManualName={}", userId, docType, releaseUserManualName);

        String documentUrl = "";
        ReleaseManual releasenotesresponse = releaseManualRepository.findReleaseNotesWithReleaseUserManualName(releaseUserManualName);

        if (releasenotesresponse != null) {
            var docDetails = detailsRepository.getDocumentDetails(docType)//Sanskar need to clarify
                    .orElseThrow(() -> new NoSuchElementException("Document config not found for docType: " + docType));
            String bucketBase = docDetails.getBucketName();
            String bucketPath = bucketBase + "/" + docDetails.getDocumentType() + "/" + releaseUserManualName;

            boolean existsInS3 = checkIfExistsInS3(releasenotesresponse.getFileName(), docDetails, releaseUserManualName);
            if (existsInS3) {
                Date expiration = Date.from(Instant.now().plus(Duration.ofHours(1))); // 1 hour
                try (S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build()) {
                    try {
                        documentUrl = awsS3BucketUtil.getPresignedURL(
                                s3Client,
                                bucketPath,
                                releasenotesresponse.getFileName(),
                                expiration
                        );
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to generate presigned URL", ex); // Replace with your custom exception
                    }
                }
            }
        }
        return documentUrl;
    }

    /**
     * Helper method to validate PDF file size and file type
     *
     * @param file the uploaded file
     * @return boolean value
     */
    public boolean isValidFileForPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        if (!DocumentRelatedConstants.PERMITED_FILE_TYPES_PDF.contains(file.getContentType())) {
            return false;
        }
        double fileSizeMb = file.getSize() / DocumentRelatedConstants.BYTES_PER_MB; // Convert size to MB
        return fileSizeMb <= DocumentRelatedConstants.MAX_FILE_SIZE_MB;
    }

    @Override
    public ApiResponse<ReleaseManualNotesDTO> releaseNotesUpload(
            MultipartFile file, String releaseUserManualName, String docType, String releaseDate) {

        log.info("Uploading release notes: release name={}, docType={}", releaseUserManualName, docType);

        // 1. Validate inputs
        ApiResponse<?> validationError = RequestValidator.validateRequiredFields(Map.of(
                "file", file,
                "releaseUserManualName", releaseUserManualName,
                "docType", docType,
                "releaseDate", releaseDate
        ));

        if (validationError != null) {
            return (ApiResponse<ReleaseManualNotesDTO>) validationError;
        }

        // 2. Parse release date (moved from controller)
        Date parsedDate;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            parsedDate = dateFormat.parse(releaseDate);
        } catch (ParseException e) {
            return ApiResponse.error(ErrorCode.INVALID_DATE_FORMAT, "Invalid release date format. Expected yyyy-MM-dd.");
        }

        // 3. Validate file format
        if (!isValidFileForPdf(file)) {
            String supported = String.join(", ", DocumentRelatedConstants.PERMITED_FILE_TYPES_PDF);
            String message = ErrorMessages.INVALID_FILE_FORMAT + " Supported formats: " + supported;
            return ApiResponse.error(ErrorCode.INVALID_FILE_FORMAT, message);
        }

        // 4. Fetch document details
        DocumentDetails docDetails = detailsRepository.getDocumentDetails(docType)
                .orElse(null);
        if (docDetails == null) {
            return ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, ErrorMessages.ENTITY_NOT_FOUND);
        }

        // 5. Upload to S3
        String awsS3UploadedURL;
        try {
            awsS3UploadedURL = awsS3BucketUtil.uploadPDFToS3Bucket(
                    docDetails.getBucketName(),
                    file,
                    DocumentRelatedConstants.RELEASE_NOTES_PREFIX + "/" + releaseUserManualName);
        } catch (Exception ex) {
            log.error("S3 Upload failed : {}", ex);
            return ApiResponse.error(ErrorCode.FILE_UPLOAD_FAILED, ErrorMessages.FILE_UPLOAD_FAILED);
        }

        // 6. Persist entity
        ReleaseManual releaseManual = buildReleaseManual(file, releaseUserManualName, parsedDate, docDetails, awsS3UploadedURL);

        releaseManualRepository.save(releaseManual);

        // 7. Return response
        return ApiResponse.success(SuccessMessages.FILE_UPLOADED, new ReleaseManualNotesDTO(releaseManual));
    }

    private ReleaseManual buildReleaseManual(MultipartFile file, String releaseUserManualName,
                                             Date releaseDate, DocumentDetails docDetails,
                                             String fileUrl) {

        Instant now = Instant.now();
        ReleaseManual releaseManual = new ReleaseManual();

        releaseManual.setFileName(file.getOriginalFilename());
        releaseManual.setFileSize((double) file.getSize());
        releaseManual.setReleaseUserManualName(releaseUserManualName);
        releaseManual.setBucketName(docDetails.getBucketName());
        releaseManual.setFileUrl(fileUrl);
        releaseManual.setIsDeleted(0);
        releaseManual.setIsLatest(1);
        releaseManual.setUploadedOn(Timestamp.from(now));
        releaseManual.setUpdatedOn(Timestamp.from(now));
        releaseManual.setDateOfReleaseNote(new Timestamp(releaseDate.getTime()));
        // TODO: userId from security context
        releaseManual.setUpdatedById(1);

        return releaseManual;
    }

    @Override
    public ApiResponse<ReleaseManualNotesDTO> reUploadReleaseNotes(Integer id, MultipartFile file) {
        log.info("Re-uploading release notes for id={}", id);

        // 1. Validate required fields
        ApiResponse<?> validationError = RequestValidator.validateRequiredFields(Map.of(
                "file", file,
                "id", id
        ));
        if (validationError != null) {
            return (ApiResponse<ReleaseManualNotesDTO>) validationError;
        }

        // 2. Validate file format
        if (!isValidFileForPdf(file)) {
            String supported = String.join(", ", DocumentRelatedConstants.PERMITED_FILE_TYPES_PDF);
            String message = ErrorMessages.INVALID_FILE_FORMAT + " Supported formats: " + supported;
            return ApiResponse.error(ErrorCode.INVALID_FILE_FORMAT, message);
        }

        // 3. Fetch existing release manual
        ReleaseManual existing = releaseManualRepository.findByIdAndIsDeleted(id,0)
                .filter(releaseManual -> releaseManual.getNoteType().equalsIgnoreCase("RELEASE_NOTE"))
                .orElse(null);

        if (existing == null) {
            return ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND,
                    "No document found");
        }

        // 4. Fetch document details using noteType
        DocumentDetails docDetails = detailsRepository.getDocumentDetails(existing.getNoteType())
                .orElse(null);
        if (docDetails == null) {
            return ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, ErrorMessages.ENTITY_NOT_FOUND);
        }

        // 5. Upload new file to S3
        String awsS3UploadedURL;
        try {
            awsS3UploadedURL = awsS3BucketUtil.uploadPDFToS3Bucket(
                    docDetails.getBucketName(),
                    file,
                    DocumentRelatedConstants.RELEASE_NOTES_PREFIX + "/release-documents");
        } catch (Exception ex) {
            log.error("S3 Re-Upload failed : {}", ex);
            return ApiResponse.error(ErrorCode.FILE_UPLOAD_FAILED, ErrorMessages.FILE_UPLOAD_FAILED);
        }

        // 6. Update existing entity
        Instant now = Instant.now();
        existing.setFileName(file.getOriginalFilename());
        existing.setFileSize((double) file.getSize());
        existing.setFileUrl(awsS3UploadedURL);
        existing.setUpdatedOn(Timestamp.from(now));
        // TODO: replace with actual logged-in userId
        existing.setUpdatedById(1);

        releaseManualRepository.save(existing);

        // 7. Return response
        return ApiResponse.success("File re-uploaded successfully", new ReleaseManualNotesDTO(existing));
    }


    @Override
    public ApiResponse<Void> deleteReleaseNoteById(Integer id) {
        log.info("Deleting release note for id={}", id);

        // 1. Validate input
        if (id == null) {
            return ApiResponse.error(ErrorCode.MISSING_REQUIRED_FIELD,
                    ErrorMessages.MISSING_REQUIRED_FIELD.formatted("id"));
        }

        // 2. Fetch existing release manual
        ReleaseManual existing = releaseManualRepository.findById(id)
                .orElse(null);
        if (existing == null) {
            return ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND,
                    "No such document available.");
        }

        // 3. Mark as deleted (soft delete)
        Instant now = Instant.now();
        existing.setIsDeleted(1);
        existing.setUpdatedOn(Timestamp.from(now));
        // TODO: replace with actual logged-in userId
        existing.setUpdatedById(1);

        releaseManualRepository.save(existing);

        // 4. Return response
        return ApiResponse.success("Release note deleted successfully");
    }

    @Override
    public ApiResponse<Void> deleteReleaseNoteByIdAndDocType(Integer id, String docType) {
        log.info("Deleting release note for id={} and docType={}", id, docType);

        // 1. Validate inputs
        if (id == null || docType == null || docType.isBlank()) {
            return ApiResponse.error(ErrorCode.MISSING_REQUIRED_FIELD,
                    "id and docType are required");
        }

        // 2. Fetch release manual by id + docType
        Optional<ReleaseManual> optionalReleaseManual =
                releaseManualRepository.findByIdAndNoteTypeAndIsDeleted(id, docType, 0);

        if (optionalReleaseManual.isEmpty()) {
            return ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND,
                    "No document found");
        }

        ReleaseManual existing = optionalReleaseManual.get();

        // 3. Soft delete
        existing.setIsDeleted(1);
        existing.setUpdatedOn(Timestamp.from(Instant.now()));
        // TODO: fetch actual logged-in user
        existing.setUpdatedById(1);

        releaseManualRepository.save(existing);
        log.debug("Release note deleted successfully for id= {} and docType= {}", id, docType);
        // 4. Response
        return ApiResponse.success(String.format("%s deleted successfully",existing.getReleaseUserManualName()));
    }


    private static LocalDateTime toLocalDateTimeOrNull(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
