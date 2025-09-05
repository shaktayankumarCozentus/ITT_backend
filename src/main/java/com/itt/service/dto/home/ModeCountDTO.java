package com.itt.service.dto.home;

import com.itt.service.enums.ModeOfTransport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeCountDTO {
	private ModeOfTransport mode;
	private Long count;
}
