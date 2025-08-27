package com.itt.service.dto.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterConfigDTO {
    private Integer id;
    private String configType;
    private String keyCode;
    private String name;
    private String description;
    private Integer intValue;
    private String stringValue;
}
