package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated response for user listing")
public class PaginatedUserResponseDto {

    @Schema(description = "Current page number", example = "0")
    private int page;

    @Schema(description = "Number of records per page", example = "20")
    private int size;

    @Schema(description = "Total number of records", example = "198")
    private long totalElements;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "List of users for this page")
    private List<SearchUsersResponseDto> content;
}
