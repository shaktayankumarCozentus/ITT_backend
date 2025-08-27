package com.itt.service.dto.peta_petd;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetaPetdDTO {
    private String companyName;
    private Integer companyCode;
    private Boolean petaCalling;
    private Integer oceanFrequencyTypeId;
    private String oceanFrequencyTypeCode;
    private String oceanFrequencyTypeName;
    private Integer airFrequencyTypeId;
    private String airFrequencyTypeCode;
    private String airFrequencyTypeName;
    private Integer railRoadFrequencyTypeId;
    private String railRoadFrequencyTypeCode;
    private String railRoadFrequencyTypeName;
    private String updatedBy;
    private LocalDateTime updatedOn;
}