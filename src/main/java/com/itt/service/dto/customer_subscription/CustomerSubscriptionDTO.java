package com.itt.service.dto.customer_subscription;

import java.time.LocalDateTime;
import java.util.List;

import com.itt.service.dto.BaseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSubscriptionDTO extends BaseDTO {
	private Integer customerId;
	private String customerName;
	private String customerCode;
	private Integer subscriptionTypeId;
	private String subscriptionTypeKeyCode;
	private String subscriptionTypeName;
	private Integer onboardedSourceId;
	private String onboardedSourceKeyCode;
	private String onboardedSourceName;
	private LocalDateTime onboardedOn;
	private List<Integer> featureIds;
}
