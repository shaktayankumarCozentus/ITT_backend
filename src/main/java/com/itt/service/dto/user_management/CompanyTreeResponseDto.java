package com.itt.service.dto.user_management;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyTreeResponseDto {
	List<CompanyTreeNode> companyTreeNodes;
	Integer totalSize;
}
