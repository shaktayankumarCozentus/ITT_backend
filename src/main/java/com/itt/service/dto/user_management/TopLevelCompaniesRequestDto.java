package com.itt.service.dto.user_management;


import com.itt.service.dto.DataTableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for loading top-level companies with search and pagination")
public class TopLevelCompaniesRequestDto {

    @Schema(description = "Pagination and search parameters")
    private DataTableRequest dataTableRequest;
}
