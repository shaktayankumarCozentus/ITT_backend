package com.itt.service.dto.user_management;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Search criteria for company access tree")
public class SearchCompanyRequestDto {

    @Schema(description = "Search keyword for companies", example = "john")
    private String searchText;

    @Schema(description = "Whether to filter PSA/BDP companies", example = "true")
    private Boolean isPsaBdp;
}
