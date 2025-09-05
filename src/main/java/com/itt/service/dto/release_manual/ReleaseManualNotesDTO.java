package com.itt.service.dto.release_manual;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.itt.service.entity.ReleaseManual;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReleaseManualNotesDTO {

	private Integer id;
	private String noteType;
	private String fileName;
	private String releaseUserManualName;
	private LocalDateTime dateOfReleaseNote;
	private LocalDateTime uploadedOn;
	private String uploadedBy;
	private LocalDateTime updatedOn;
	private String updatedBy;
	
	
	 public ReleaseManualNotesDTO(ReleaseManual releaseManual) {
	        this.id = releaseManual.getId();
	        this.noteType = releaseManual.getNoteType();
	        this.releaseUserManualName = releaseManual.getReleaseUserManualName();
	        this.fileName = releaseManual.getFileName();
	        this.uploadedOn = releaseManual.getUploadedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	        this.dateOfReleaseNote = releaseManual.getDateOfReleaseNote().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	        this.updatedOn = releaseManual.getUpdatedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	    }

}
