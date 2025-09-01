package com.itt.service.dto.user_management;

import com.itt.service.dto.DataTableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to search/filter users with role status")
public class SearchUsersRequestDto {

    @Schema(description = "Paginated table request with filters and sorts")
    private DataTableRequest dataTableRequest;

    @Schema(description = "User role filter type", example = "ACTIVE_OR_NO_ROLE", allowableValues = {"ACTIVE_OR_NO_ROLE", "INACTIVE_ROLE"})
    private UserRoleFilter userRoleFilter;

    /**
     * Enum to define user role filtering options
     */
    public enum UserRoleFilter {
        @Schema(description = "Users with active role or no role assigned")
        ACTIVE_OR_NO_ROLE,
        
        @Schema(description = "Users with inactive role only")
        INACTIVE_ROLE
    }
}
