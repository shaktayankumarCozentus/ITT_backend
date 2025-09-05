package com.itt.service.service;

import org.springframework.web.multipart.MultipartFile;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.release_manual.ReleaseManualNotesDTO;

public interface ReleaseManualNotesService {

	PaginationResponse<ReleaseManualNotesDTO> getReleaseNotesResponse(String docType, DataTableRequest request);

	String getReleaseNotesWithFileName(String userId, String docType, String foldername);

	String getReleaseNotesById(Integer docId);

	ApiResponse<ReleaseManualNotesDTO> releaseNotesUpload(MultipartFile file, String releaseUserManualName, String docType, String releaseDate);

	ApiResponse<ReleaseManualNotesDTO> reUploadReleaseNotes(Integer id, MultipartFile file);

	ApiResponse<Void> deleteReleaseNoteById(Integer id);

	ApiResponse<Void> deleteReleaseNoteByIdAndDocType(Integer id, String docType);
}
