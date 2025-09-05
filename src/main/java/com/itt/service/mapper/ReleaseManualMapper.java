package com.itt.service.mapper;

import com.itt.service.dto.release_manual.ReleaseManualDTO;
import com.itt.service.entity.ReleaseManual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReleaseManualMapper {

	@Mapping(source = "uploadedOn", target = "uploadedOn")
	@Mapping(source = "id", target = "docId")
	@Mapping(source = "id", target = "id")
	@Mapping(source = "releaseUserManualName", target = "releaseUserManualName")
	@Mapping(source = "dateOfReleaseNote", target = "dateOfReleaseNote")
	@Mapping(source = "noteType", target = "noteType")
	@Mapping(source = "fileName", target = "fileName")
	@Mapping(source = "fileUrl", target = "fileUrl")
	@Mapping(source = "updatedById", target = "updatedBy")
	@Mapping(source = "createdById", target = "createdBy")
	ReleaseManualDTO toDto(ReleaseManual manual);
}