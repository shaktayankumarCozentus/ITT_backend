package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to get child companies by parent list")
public class ChildCompaniesRequestDto {

    @Schema(description = "List of parent company codes", example = "[\"comp_001\", \"comp_002\"]")
    private List<String> parentCompanyList;
}
