package com.itt.service.dto.user_management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyTreeNode {

    @Builder.Default
    private boolean checked = false;
    private String key;
    private CompanyDtoPOc data;
    private String icon;
    private String expandedIcon;
    private String collapsedIcon;
    @Builder.Default
    private boolean expanded = false;
    private Integer parentId;
    @Builder.Default
    private List<CompanyTreeNode> children = new ArrayList<>();

    // Getters and Setters
}
