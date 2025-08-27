package com.itt.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;
import com.itt.service.service.impl.PetaPetdDomainSyncServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetaPetdDomainSyncServiceImpl - Business Logic Tests")
class PetaPetdDomainSyncServiceImplTest {

    @InjectMocks
    private PetaPetdDomainSyncServiceImpl service;

    // ==================== Single Update Tests ====================

    @Test
    @DisplayName("Should successfully send single update to all domains with PETA calling")
    void shouldSuccessfullySendSingleUpdateToAllDomainsWithPetaCalling() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() when all domain calls succeed with PETA calling
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(101);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(1);
        request.setAirFrequencyType(2);
        request.setRailRoadFrequencyType(3);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should successfully send single update to all domains with PETD calling")
    void shouldSuccessfullySendSingleUpdateToAllDomainsWithPetdCalling() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() when all domain calls succeed with PETD calling
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(102);
        request.setPetaCalling(false);
        request.setOceanFrequencyType(2);
        request.setAirFrequencyType(1);
        request.setRailRoadFrequencyType(1);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with null frequency types")
    void shouldHandleSingleUpdateWithNullFrequencyTypes() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with null frequency types
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(103);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(null);
        request.setAirFrequencyType(null);
        request.setRailRoadFrequencyType(null);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with zero frequency types")
    void shouldHandleSingleUpdateWithZeroFrequencyTypes() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with zero frequency types
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(104);
        request.setPetaCalling(false);
        request.setOceanFrequencyType(0);
        request.setAirFrequencyType(0);
        request.setRailRoadFrequencyType(0);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with mixed frequency types")
    void shouldHandleSingleUpdateWithMixedFrequencyTypes() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with mixed frequency types
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(105);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(5);
        request.setAirFrequencyType(null);
        request.setRailRoadFrequencyType(10);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with null company code")
    void shouldHandleSingleUpdateWithNullCompanyCode() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with null company code
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(null);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(1);
        request.setAirFrequencyType(2);
        request.setRailRoadFrequencyType(3);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with null peta calling")
    void shouldHandleSingleUpdateWithNullPetaCalling() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with null peta calling
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(106);
        request.setPetaCalling(null);
        request.setOceanFrequencyType(1);
        request.setAirFrequencyType(2);
        request.setRailRoadFrequencyType(3);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with large frequency values")
    void shouldHandleSingleUpdateWithLargeFrequencyValues() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with large frequency values
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(107);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(9999);
        request.setAirFrequencyType(8888);
        request.setRailRoadFrequencyType(7777);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    // ==================== Bulk Update Tests ====================

    @Test
    @DisplayName("Should successfully send bulk update to all domains with PETA calling")
    void shouldSuccessfullySendBulkUpdateToAllDomainsWithPetaCalling() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() when all domain calls succeed with PETA calling
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(101, 102, 103));
        request.setPetaCalling(true);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should successfully send bulk update to all domains with PETD calling")
    void shouldSuccessfullySendBulkUpdateToAllDomainsWithPetdCalling() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() when all domain calls succeed with PETD calling
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(201, 202));
        request.setPetaCalling(false);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with single company")
    void shouldHandleBulkUpdateWithSingleCompany() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with single company
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(301));
        request.setPetaCalling(true);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with empty company codes")
    void shouldHandleBulkUpdateWithEmptyCompanyCodes() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with empty company codes
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Collections.emptyList());
        request.setPetaCalling(false);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with null company codes")
    void shouldHandleBulkUpdateWithNullCompanyCodes() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with null company codes
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(null);
        request.setPetaCalling(true);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update when all selected is true")
    void shouldHandleBulkUpdateWhenAllSelectedIsTrue() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() when isAllSelected is true
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(401, 402, 403));
        request.setPetaCalling(true);
        request.setIsAllSelected(true);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with null peta calling")
    void shouldHandleBulkUpdateWithNullPetaCalling() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with null peta calling
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(501, 502));
        request.setPetaCalling(null);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with null all selected flag")
    void shouldHandleBulkUpdateWithNullAllSelectedFlag() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with null isAllSelected
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(601, 602, 603));
        request.setPetaCalling(false);
        request.setIsAllSelected(null);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle large bulk update request")
    void shouldHandleLargeBulkUpdateRequest() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with large data sets
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        
        // Large list of company codes
        List<Integer> companyCodes = Arrays.asList(
            101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
            201, 202, 203, 204, 205, 206, 207, 208, 209, 210,
            301, 302, 303, 304, 305, 306, 307, 308, 309, 310
        );
        request.setCompanyCodes(companyCodes);
        request.setPetaCalling(true);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    // ==================== Edge Cases and Additional Coverage ====================

    @Test
    @DisplayName("Should handle mixed PETA and PETD calls consistently")
    void shouldHandleMixedPetaAndPetdCallsConsistently() {
        // Code Fix: Testing both request types with different PETA/PETD calling values
        
        // Test single update with PETA calling
        PetaPetdUpdateRequest singleRequest1 = new PetaPetdUpdateRequest();
        singleRequest1.setCompanyCode(701);
        singleRequest1.setPetaCalling(true);
        singleRequest1.setOceanFrequencyType(1);
        singleRequest1.setAirFrequencyType(2);
        singleRequest1.setRailRoadFrequencyType(3);
        
        Boolean singleResult1 = service.sendPetaPetdUpdateToDomains(singleRequest1);
        assertTrue(singleResult1);
        
        // Test single update with PETD calling
        PetaPetdUpdateRequest singleRequest2 = new PetaPetdUpdateRequest();
        singleRequest2.setCompanyCode(702);
        singleRequest2.setPetaCalling(false);
        singleRequest2.setOceanFrequencyType(3);
        singleRequest2.setAirFrequencyType(4);
        singleRequest2.setRailRoadFrequencyType(5);
        
        Boolean singleResult2 = service.sendPetaPetdUpdateToDomains(singleRequest2);
        assertTrue(singleResult2);
        
        // Test bulk update with PETA calling
        PetaPetdBulkUpdateRequest bulkRequest1 = new PetaPetdBulkUpdateRequest();
        bulkRequest1.setCompanyCodes(Arrays.asList(801, 802));
        bulkRequest1.setPetaCalling(true);
        bulkRequest1.setIsAllSelected(false);
        
        Boolean bulkResult1 = service.sendBulkPetaPetdUpdateToDomains(bulkRequest1);
        assertTrue(bulkResult1);
        
        // Test bulk update with PETD calling
        PetaPetdBulkUpdateRequest bulkRequest2 = new PetaPetdBulkUpdateRequest();
        bulkRequest2.setCompanyCodes(Arrays.asList(803, 804));
        bulkRequest2.setPetaCalling(false);
        bulkRequest2.setIsAllSelected(true);
        
        Boolean bulkResult2 = service.sendBulkPetaPetdUpdateToDomains(bulkRequest2);
        assertTrue(bulkResult2);
        
        // All operations should succeed
        assertThat(singleResult1).isTrue();
        assertThat(singleResult2).isTrue();
        assertThat(bulkResult1).isTrue();
        assertThat(bulkResult2).isTrue();
    }

    @Test
    @DisplayName("Should handle completely null request objects")
    void shouldHandleCompletelyNullRequestObjects() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() and sendBulkPetaPetdUpdateToDomains() with all null fields
        
        // Test single update with all nulls
        PetaPetdUpdateRequest singleRequest = new PetaPetdUpdateRequest();
        singleRequest.setCompanyCode(null);
        singleRequest.setPetaCalling(null);
        singleRequest.setOceanFrequencyType(null);
        singleRequest.setAirFrequencyType(null);
        singleRequest.setRailRoadFrequencyType(null);
        
        Boolean singleResult = service.sendPetaPetdUpdateToDomains(singleRequest);
        assertTrue(singleResult);
        
        // Test bulk update with all nulls
        PetaPetdBulkUpdateRequest bulkRequest = new PetaPetdBulkUpdateRequest();
        bulkRequest.setCompanyCodes(null);
        bulkRequest.setPetaCalling(null);
        bulkRequest.setIsAllSelected(null);
        
        Boolean bulkResult = service.sendBulkPetaPetdUpdateToDomains(bulkRequest);
        assertTrue(bulkResult);
        
        assertThat(singleResult).isTrue();
        assertThat(bulkResult).isTrue();
    }

    @Test
    @DisplayName("Should handle negative frequency values")
    void shouldHandleNegativeFrequencyValues() {
        // Code Fix: Testing sendPetaPetdUpdateToDomains() with negative frequency values
        PetaPetdUpdateRequest request = new PetaPetdUpdateRequest();
        request.setCompanyCode(901);
        request.setPetaCalling(true);
        request.setOceanFrequencyType(-1);
        request.setAirFrequencyType(-5);
        request.setRailRoadFrequencyType(-10);

        Boolean result = service.sendPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle duplicate company codes in bulk update")
    void shouldHandleDuplicateCompanyCodesInBulkUpdate() {
        // Code Fix: Testing sendBulkPetaPetdUpdateToDomains() with duplicate company codes
        PetaPetdBulkUpdateRequest request = new PetaPetdBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(1001, 1002, 1001, 1003, 1002));
        request.setPetaCalling(false);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkPetaPetdUpdateToDomains(request);

        assertTrue(result);
    }
}
