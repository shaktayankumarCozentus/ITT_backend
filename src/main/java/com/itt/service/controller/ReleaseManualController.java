package com.itt.service.controller;


import com.itt.service.dto.release_manual.ReleaseManualDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.enums.ErrorCode;
import com.itt.service.service.ReleaseManualNotesService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/releases")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Release Management", description = "APIs for Release Manual upload")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReleaseManualController {
	
	private final ReleaseManualNotesService releaseNotesService;
	
	@PostMapping(value = "/release-manual-notes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<PaginationResponse<ReleaseManualDTO>>> getReleaseManualNotes(@RequestParam String docType, @RequestBody DataTableRequest request) {

		PaginationResponse<ReleaseManualDTO> releaseNotesResponseDTOs = releaseNotesService.getReleaseNotesResponse(docType, request);

	    // Return the dynamic response built with the PaginationResponse
	    return ResponseBuilder.dynamicResponse(releaseNotesResponseDTOs);
	}


	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<String>> getReleaseNotesById(@PathVariable("id") Integer docId) {
	    log.info("Entering getReleaseNotesById for docId={}",docId);
		String documentUrl = releaseNotesService.getReleaseNotesById(docId);

	    // ResponseBuilder.dynamicResponse will return 204 if documentUrl is empty, else 200 with ApiResponse
	    return ResponseBuilder.dynamicResponse(documentUrl);
	}

	@Operation(summary = "Document uploading", description = "API for uploading a PDF document")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<ReleaseManualDTO>> releaseNotesUpload(
	        @RequestParam("file") MultipartFile file,
	        @RequestParam String docType,
	        @RequestParam String releaseUserManualName,
	        @RequestParam String releaseDate) {

		try {
			ApiResponse<ReleaseManualDTO> response =
					releaseNotesService.releaseNotesUpload(file, releaseUserManualName, docType, releaseDate);

			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (Exception e) {
			log.error("Error occurred during file upload", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "Unexpected error during upload"));
		}
	}

	@Operation(summary = "Document re-uploading", description = "API for re-uploading a PDF document based on ID")
	@PutMapping(value = "/{id}/re-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<ReleaseManualDTO>> reUploadReleaseNotes(
			@PathVariable("id") Integer id,
			@RequestParam("file") MultipartFile file) {

		try {
			ApiResponse<ReleaseManualDTO> response =
					releaseNotesService.reUploadReleaseNotes(id, file);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error occurred during document re-upload for id={}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "Unexpected error during re-upload"));
		}
	}


	@Operation(summary = "Delete release note", description = "API for deleting a release note by ID and docType")
	@DeleteMapping(value = "/{id}/{docType}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<Void>> deleteReleaseNoteByIdAndDocType(
			@PathVariable("id") Integer id,
			@PathVariable("docType") String docType) {

		log.info("Deleting release note for id={} and docType={}", id, docType);

		try {
			ApiResponse<Void> response = releaseNotesService.deleteReleaseNoteByIdAndDocType(id, docType);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error occurred while deleting release note for id={} and docType={}", id, docType, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "Unexpected error during delete"));
		}
	}

}
