package com.itt.service.service;

import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.home.KpiIndicatorDTO;
import com.itt.service.dto.home.KpiIndicatorRequestDTO;
import com.itt.service.dto.home.LocationStatsSummaryDTO;
import com.itt.service.dto.home.MapViewRequestDTO;
import com.itt.service.dto.home.PortShipmentsInfoRequestDTO;
import com.itt.service.dto.home.ShipmentSummaryDTO;

public interface HomeService {
	public KpiIndicatorDTO getKpiIndicator(KpiIndicatorRequestDTO request, CurrentUserDto currentUser);

	public LocationStatsSummaryDTO getMapData(MapViewRequestDTO request, CurrentUserDto currentUser);
	
	public PaginationResponse<ShipmentSummaryDTO> getShipmentsInfoByPort(PortShipmentsInfoRequestDTO request,
			CurrentUserDto currentUser);
}