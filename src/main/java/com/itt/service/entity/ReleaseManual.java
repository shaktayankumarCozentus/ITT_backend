package com.itt.service.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "release_notes")
public class ReleaseManual {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "note_type")
	private String noteType;

	@Column(name = "release_user_manual_name")
	private String releaseUserManualName;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "file_size")
	private Double fileSize;

	@Lob
	@Column(name = "file_url")
	public String fileUrl;

	@Lob
	@Column(name = "bucket_name")
	public String bucketName;

	@Column(name = "date_of_release_note")
	private LocalDateTime dateOfReleaseNote;

	@Column(name = "is_latest")
	private Integer isLatest;

	@Column(name = "is_deleted")
	private Integer isDeleted;

	@Column(name = "created_by_id")
	private Integer createdById;

	@Column(name = "uploaded_on")
	private LocalDateTime uploadedOn;

	@Column(name = "updated_by_id")
	private Integer updatedById;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;
}
