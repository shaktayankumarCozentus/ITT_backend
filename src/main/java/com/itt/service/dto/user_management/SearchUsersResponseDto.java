package com.itt.service.dto.user_management;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User record in search result")
public class SearchUsersResponseDto {

	@Schema(description = "User ID", example = "usr_123")
	private Integer userId;

	@Schema(description = "Full name of the user", example = "John Doe")
	private String name;

	@Schema(description = "User type", example = "Customer")
	private String userType;

	@Schema(description = "User email address", example = "john.doe@example.com")
	private String email;

	@Schema(description = "Number of companies assigned to this user", example = "10000")
	private Long companyCount;

	@Schema(description = "Assigned role name", example = "Standard User")
	private String assignedRoleName;

	@Schema(description = "Assigned role ID", example = "role_456")
	private Integer assignedRoleId;

	@Schema(description = "Last user who updated this record", example = "admin_user")
	private String updatedBy;

	@Schema(description = "Timestamp of last update", example = "2023-10-27T10:00:00")
	private LocalDateTime updatedOn;

	@Schema(description = "Whether user is a BDP employee", example = "false")
	private Boolean isBdpEmployee;
}
