package com.itt.service.dto.release_manual;

import com.itt.service.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseManualDTO extends BaseDTO {
	private Integer docId;
	private Integer id;
	private String releaseUserManualName;
	private String noteType;
	private String fileName;
	private String fileUrl;
	private LocalDateTime uploadedOn;
	private LocalDateTime dateOfReleaseNote;
}
