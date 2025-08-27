package com.itt.service.dto.user_management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDtoPOc {
    private Integer id;
    private String companyName;
    private Integer parentId;
    private LocalDateTime onboardedByDate;
    private String updatedBy;
    private String subscriptionType;

    // Getters and Setters
}