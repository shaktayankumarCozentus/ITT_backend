package com.itt.service.dto.user_management;

import com.itt.service.dto.DataTableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to search/filter users with active role flag")
public class SearchUsersRequestDto {

    @Schema(description = "Paginated table request with filters and sorts")
    private DataTableRequest dataTableRequest;

    @Schema(description = "Whether to include only active roles", example = "true")
    private Boolean isActiveRole;
}
