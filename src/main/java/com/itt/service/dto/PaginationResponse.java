package com.itt.service.dto;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
	private Integer page;
	private Integer size;
	private Long totalElements;
	private Integer totalPages;
	private Boolean last;
	private List<T> content;
	public PaginationResponse(Page<T> page) {
		this.content = page.getContent();
		this.page = page.getNumber();
		this.size = page.getSize();
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.last = page.isLast();
	}
}