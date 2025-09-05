package com.itt.service.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.release_manual.ReleaseManualNotesDTO;
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
	public ResponseEntity<ApiResponse<PaginationResponse<ReleaseManualNotesDTO>>> getReleaseManualNotes(@RequestParam String docType, @RequestBody DataTableRequest request) {

	    PaginationResponse<ReleaseManualNotesDTO> releaseNotesResponseDTOs = releaseNotesService.getReleaseNotesResponse(docType, request);

	    // Return the dynamic response built with the PaginationResponse
	    return ResponseBuilder.dynamicResponse(releaseNotesResponseDTOs);
	}


	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<String>> getReleaseNotesById(@PathVariable("id") Integer docId) {
	    log.info("Entering getReleaseNotesById for docId={}",docId);

	    // retrieve userId from auth context if available, otherwise keep blank as before
	    String userId = "";

	    // service returns the presigned URL or an empty/null value if not found

//	    String documentUrl = releaseNotesService.getReleaseNotesWithFileName(
//	            userId,
//	            releaseNotesRequest.getNoteType(),
//	            releaseNotesRequest.getFileName()
//	    );

		String documentUrl = releaseNotesService.getReleaseNotesById(docId);

	    // ResponseBuilder.dynamicResponse will return 204 if documentUrl is empty, else 200 with ApiResponse
	    return ResponseBuilder.dynamicResponse(documentUrl);
	}
 

	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<ReleaseManualNotesDTO>> releaseNotesUpload(
	        @RequestParam MultipartFile file,
	        @RequestParam String docType,
	        @RequestParam String releaseDate) {

		try {
			ApiResponse<ReleaseManualNotesDTO> response =
					releaseNotesService.releaseNotesUpload(file, "release-documents", docType, releaseDate);

			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (Exception e) {
			log.error("Error occurred during file upload", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "Unexpected error during upload"));
		}
	}


	
	
	

}
