package com.itt.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.itt.service.entity.DocumentDetails;

import java.util.Optional;

public interface DocumentDetailsRepository  extends JpaRepository<DocumentDetails, Integer>{
	
	@Query(value = "select * from document_download_config ddc where ddc.document_type = :docType "
			+ "and ddc.is_active = 1", nativeQuery = true)
	Optional<DocumentDetails> getDocumentDetails(String docType);
}
