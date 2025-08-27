package com.itt.service.dto.user_management;

import com.itt.service.dto.DataTableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CompanyTreeRequestDto",
        description = "Request to fetch paginated company-tree, with optional PSA/NON-PSA/ALL filter"
)
public class CompanyTreeRequestDto {

    @Schema(
            description = "Pagination, filter and sort settings",
            required    = true
    )
    private DataTableRequest dataTableRequest;

    @Schema(
            description = "Subscription type filter: PSA, NON-PSA",
            example     = "PSA",
            required    = true
    )
    private String type;
}
