package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to search users by name for copy access")
public class SearchForCopyRequestDto {

    @Schema(description = "Search text for user", example = "John")
    private String searchText;
}
