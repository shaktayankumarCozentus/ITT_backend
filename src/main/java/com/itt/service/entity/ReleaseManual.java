package com.itt.service.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

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
	public Integer id;

	@Column(name = "note_type")
	public String noteType;

	@Column(name = "folder_name")
	public String folderName;

	@Column(name = "file_name")
	public String fileName;

	@Column(name = "file_size")
	public Double fileSize;

	@Lob
	@Column(name = "file_url")
	public String fileUrl;

	@Lob
	@Column(name = "bucket_name")
	public String bucketName;

	@Column(name = "date_of_release_note")
	public Timestamp dateOfReleaseNote;

	@Column(name = "is_latest")
	public Integer isLatest;

	@Column(name = "is_deleted")
	public Integer isDeleted;

	@Column(name = "created_by_id")
	public Integer createdById;

	@Column(name = "uploaded_on")
	public Timestamp uploadedOn;

	@Column(name = "updated_by_id")
	public Integer updatedById;

	@Column(name = "updated_on")
	public Timestamp updatedOn;
}
