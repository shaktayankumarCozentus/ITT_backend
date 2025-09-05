package com.itt.service.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itt.service.entity.ReleaseManual;

import java.util.Optional;


@Repository
public interface ReleaseManualRepository extends JpaRepository<ReleaseManual, Integer> {
	
	@Query("SELECT r FROM ReleaseManual r WHERE r.noteType = :docType AND r.isLatest = 1 AND r.isDeleted = 0")
    Page<ReleaseManual> findByDocTypeAndLatestUpdate(@Param("docType") String docType, Pageable pageable);
	
	@Query("SELECT r FROM ReleaseManual r WHERE r.folderName = :folderName AND r.isDeleted = 0 AND r.isLatest = 1")
	ReleaseManual findReleaseNotesWithFolderName(@Param("folderName") String folderName);
}
