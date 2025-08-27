package com.itt.service.dto.user_management;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Assigned company info for a user")
public class AssignedCompanyDto {

    @Schema(description = "Company code", example = "comp_001")
    private String companyCode;


    @Schema(description = "Company name", example = "ABC Corporation")
    private String companyName;
}
