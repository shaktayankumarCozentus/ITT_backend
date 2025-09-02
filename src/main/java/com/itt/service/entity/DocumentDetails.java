package com.itt.service.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "document_download_config")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "bucket_name")
	private String bucketName;
	
	@Column(name = "document_type")
	private String documentType;
	
	@Column(name = "version")
	private String version;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "created_on")
	private Date createdOn;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "is_active")
	private boolean isActive;
	
	
}