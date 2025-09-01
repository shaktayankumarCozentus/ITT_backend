package com.itt.service.dto.home;

import com.itt.service.dto.DataTableRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortShipmentsInfoRequestDTO {
	private MapViewRequestDTO globalFilterRequest;
	private DataTableRequest dataTableRequest;
}
