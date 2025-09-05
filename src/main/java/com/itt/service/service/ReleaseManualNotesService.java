package com.itt.service.service;

import com.itt.service.dto.release_manual.ReleaseManualDTO;
import org.springframework.web.multipart.MultipartFile;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;

public interface ReleaseManualNotesService {

	PaginationResponse<ReleaseManualDTO> getReleaseNotesResponse(String docType, DataTableRequest request);

	String getReleaseNotesWithFileName(String userId, String docType, String foldername);

	String getReleaseNotesById(Integer docId);

	ApiResponse<ReleaseManualDTO> releaseNotesUpload(MultipartFile file, String releaseUserManualName, String docType, String releaseDate);

	ApiResponse<ReleaseManualDTO> reUploadReleaseNotes(Integer id, MultipartFile file);

	ApiResponse<Void> deleteReleaseNoteById(Integer id);

	ApiResponse<Void> deleteReleaseNoteByIdAndDocType(Integer id, String docType);
}
