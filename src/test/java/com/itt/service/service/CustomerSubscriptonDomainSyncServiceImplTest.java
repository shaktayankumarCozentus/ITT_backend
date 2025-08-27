package com.itt.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;
import com.itt.service.service.impl.CustomerSubscriptonDomainSyncServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerSubscriptonDomainSyncServiceImpl - Business Logic Tests")
class CustomerSubscriptonDomainSyncServiceImplTest {

    @InjectMocks
    private CustomerSubscriptonDomainSyncServiceImpl service;

    // ==================== Single Update Tests ====================

    @Test
    @DisplayName("Should successfully send single subscription update to all domains")
    void shouldSuccessfullySendSingleSubscriptionUpdateToAllDomains() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() when all domain calls succeed
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(101);
        request.setSubscriptionTierType(2);
        request.setFeatureIds(Arrays.asList(1, 2, 3));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with basic subscription tier")
    void shouldHandleSingleUpdateWithBasicSubscriptionTier() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with basic tier
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(102);
        request.setSubscriptionTierType(1); // Basic tier
        request.setFeatureIds(Arrays.asList(1));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with premium subscription tier")
    void shouldHandleSingleUpdateWithPremiumSubscriptionTier() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with premium tier
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(103);
        request.setSubscriptionTierType(2); // Premium tier
        request.setFeatureIds(Arrays.asList(1, 2, 3, 4, 5));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with enterprise subscription tier")
    void shouldHandleSingleUpdateWithEnterpriseSubscriptionTier() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with enterprise tier
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(104);
        request.setSubscriptionTierType(3); // Enterprise tier
        request.setFeatureIds(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with large feature set")
    void shouldHandleSingleUpdateWithLargeFeatureSet() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with many features
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(105);
        request.setSubscriptionTierType(3);
        
        // Large feature list
        List<Integer> featureIds = Arrays.asList(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        );
        request.setFeatureIds(featureIds);

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with zero subscription tier")
    void shouldHandleSingleUpdateWithZeroSubscriptionTier() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with zero tier
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(106);
        request.setSubscriptionTierType(0); // Zero tier
        request.setFeatureIds(Arrays.asList(1));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle single update with high company code")
    void shouldHandleSingleUpdateWithHighCompanyCode() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with large company code
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        request.setCompanyCode(999999);
        request.setSubscriptionTierType(2);
        request.setFeatureIds(Arrays.asList(1, 2));

        Boolean result = service.sendCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    // ==================== Bulk Update Tests ====================

    @Test
    @DisplayName("Should successfully send bulk subscription update to all domains")
    void shouldSuccessfullySendBulkSubscriptionUpdateToAllDomains() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() when all domain calls succeed
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(101, 102, 103));
        request.setSubscriptionTierType(2);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with single company")
    void shouldHandleBulkUpdateWithSingleCompany() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with single company
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(201));
        request.setSubscriptionTierType(1);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update when all companies selected")
    void shouldHandleBulkUpdateWhenAllCompaniesSelected() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with isAllSelected=true
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(301, 302, 303));
        request.setSubscriptionTierType(3);
        request.setIsAllSelected(true);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with basic tier for multiple companies")
    void shouldHandleBulkUpdateWithBasicTierForMultipleCompanies() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with basic tier
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(401, 402, 403, 404));
        request.setSubscriptionTierType(1); // Basic tier
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with large company list")
    void shouldHandleBulkUpdateWithLargeCompanyList() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with many companies
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        
        // Large company list
        List<Integer> companyCodes = Arrays.asList(
            501, 502, 503, 504, 505, 506, 507, 508, 509, 510,
            601, 602, 603, 604, 605, 606, 607, 608, 609, 610,
            701, 702, 703, 704, 705
        );
        request.setCompanyCodes(companyCodes);
        request.setSubscriptionTierType(2);
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with null isAllSelected flag")
    void shouldHandleBulkUpdateWithNullIsAllSelectedFlag() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with null isAllSelected
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(801, 802));
        request.setSubscriptionTierType(3);
        request.setIsAllSelected(null);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle bulk update with high tier values")
    void shouldHandleBulkUpdateWithHighTierValues() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with high tier numbers
        SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
        request.setCompanyCodes(Arrays.asList(901, 902, 903));
        request.setSubscriptionTierType(999); // High tier value
        request.setIsAllSelected(false);

        Boolean result = service.sendBulkCustomerSubscriptionUpdateToDomains(request);

        assertTrue(result);
    }

    // ==================== Copy Subscription Tests ====================

    @Test
    @DisplayName("Should successfully send copy subscription to all domains")
    void shouldSuccessfullySendCopySubscriptionToAllDomains() {
        // Code Fix: Testing sendCopyCustomerSubscriptionToDomains() when all domain calls succeed
        SubscriptionCopyRequest request = new SubscriptionCopyRequest();
        request.setSourceCompanyId(1001);
        request.setTargetCompanyIds(Arrays.asList(1002, 1003, 1004));

        Boolean result = service.sendCopyCustomerSubscriptionToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle copy subscription with single target company")
    void shouldHandleCopySubscriptionWithSingleTargetCompany() {
        // Code Fix: Testing sendCopyCustomerSubscriptionToDomains() with single target
        SubscriptionCopyRequest request = new SubscriptionCopyRequest();
        request.setSourceCompanyId(2001);
        request.setTargetCompanyIds(Arrays.asList(2002));

        Boolean result = service.sendCopyCustomerSubscriptionToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle copy subscription with many target companies")
    void shouldHandleCopySubscriptionWithManyTargetCompanies() {
        // Code Fix: Testing sendCopyCustomerSubscriptionToDomains() with multiple targets
        SubscriptionCopyRequest request = new SubscriptionCopyRequest();
        request.setSourceCompanyId(3001);
        
        // Large target list
        List<Integer> targetIds = Arrays.asList(
            3002, 3003, 3004, 3005, 3006, 3007, 3008, 3009, 3010,
            3011, 3012, 3013, 3014, 3015, 3016, 3017, 3018, 3019, 3020
        );
        request.setTargetCompanyIds(targetIds);

        Boolean result = service.sendCopyCustomerSubscriptionToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle copy subscription with high company IDs")
    void shouldHandleCopySubscriptionWithHighCompanyIds() {
        // Code Fix: Testing sendCopyCustomerSubscriptionToDomains() with large company IDs
        SubscriptionCopyRequest request = new SubscriptionCopyRequest();
        request.setSourceCompanyId(999001);
        request.setTargetCompanyIds(Arrays.asList(999002, 999003, 999004));

        Boolean result = service.sendCopyCustomerSubscriptionToDomains(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle copy subscription between different company ranges")
    void shouldHandleCopySubscriptionBetweenDifferentCompanyRanges() {
        // Code Fix: Testing sendCopyCustomerSubscriptionToDomains() with varied company IDs
        SubscriptionCopyRequest request = new SubscriptionCopyRequest();
        request.setSourceCompanyId(100);
        request.setTargetCompanyIds(Arrays.asList(50000, 75000, 100000, 125000));

        Boolean result = service.sendCopyCustomerSubscriptionToDomains(request);

        assertTrue(result);
    }

    // ==================== Edge Cases and Additional Coverage ====================

    @Test
    @DisplayName("Should handle all three operations with consistent results")
    void shouldHandleAllThreeOperationsWithConsistentResults() {
        // Code Fix: Testing all three main methods in sequence for consistency
        
        // Test single update
        SubscriptionUpdateRequest updateRequest = new SubscriptionUpdateRequest();
        updateRequest.setCompanyCode(5001);
        updateRequest.setSubscriptionTierType(2);
        updateRequest.setFeatureIds(Arrays.asList(1, 2, 3));
        
        Boolean updateResult = service.sendCustomerSubscriptionUpdateToDomains(updateRequest);
        assertTrue(updateResult);
        
        // Test bulk update
        SubscriptionBulkUpdateRequest bulkRequest = new SubscriptionBulkUpdateRequest();
        bulkRequest.setCompanyCodes(Arrays.asList(5002, 5003, 5004));
        bulkRequest.setSubscriptionTierType(3);
        bulkRequest.setIsAllSelected(false);
        
        Boolean bulkResult = service.sendBulkCustomerSubscriptionUpdateToDomains(bulkRequest);
        assertTrue(bulkResult);
        
        // Test copy operation
        SubscriptionCopyRequest copyRequest = new SubscriptionCopyRequest();
        copyRequest.setSourceCompanyId(5005);
        copyRequest.setTargetCompanyIds(Arrays.asList(5006, 5007));
        
        Boolean copyResult = service.sendCopyCustomerSubscriptionToDomains(copyRequest);
        assertTrue(copyResult);
        
        // All operations should succeed
        assertThat(updateResult).isTrue();
        assertThat(bulkResult).isTrue();
        assertThat(copyResult).isTrue();
    }

    @Test
    @DisplayName("Should handle mixed subscription tiers across operations")
    void shouldHandleMixedSubscriptionTiersAcrossOperations() {
        // Code Fix: Testing different subscription tiers across different operations
        
        // Test with basic tier (1)
        SubscriptionUpdateRequest basicRequest = new SubscriptionUpdateRequest();
        basicRequest.setCompanyCode(6001);
        basicRequest.setSubscriptionTierType(1);
        basicRequest.setFeatureIds(Arrays.asList(1));
        
        Boolean basicResult = service.sendCustomerSubscriptionUpdateToDomains(basicRequest);
        assertTrue(basicResult);
        
        // Test with premium tier (2)
        SubscriptionBulkUpdateRequest premiumRequest = new SubscriptionBulkUpdateRequest();
        premiumRequest.setCompanyCodes(Arrays.asList(6002, 6003));
        premiumRequest.setSubscriptionTierType(2);
        premiumRequest.setIsAllSelected(false);
        
        Boolean premiumResult = service.sendBulkCustomerSubscriptionUpdateToDomains(premiumRequest);
        assertTrue(premiumResult);
        
        // Test with enterprise tier (3) via copy
        SubscriptionCopyRequest enterpriseRequest = new SubscriptionCopyRequest();
        enterpriseRequest.setSourceCompanyId(6004);
        enterpriseRequest.setTargetCompanyIds(Arrays.asList(6005, 6006, 6007));
        
        Boolean enterpriseResult = service.sendCopyCustomerSubscriptionToDomains(enterpriseRequest);
        assertTrue(enterpriseResult);
        
        assertThat(basicResult).isTrue();
        assertThat(premiumResult).isTrue();
        assertThat(enterpriseResult).isTrue();
    }

    @Test
    @DisplayName("Should handle edge case feature IDs and company codes")
    void shouldHandleEdgeCaseFeatureIdsAndCompanyCodes() {
        // Code Fix: Testing sendCustomerSubscriptionUpdateToDomains() with edge case values
        
        // Test with minimum values
        SubscriptionUpdateRequest minRequest = new SubscriptionUpdateRequest();
        minRequest.setCompanyCode(1);
        minRequest.setSubscriptionTierType(0);
        minRequest.setFeatureIds(Arrays.asList(1));
        
        Boolean minResult = service.sendCustomerSubscriptionUpdateToDomains(minRequest);
        assertTrue(minResult);
        
        // Test with maximum-like values
        SubscriptionUpdateRequest maxRequest = new SubscriptionUpdateRequest();
        maxRequest.setCompanyCode(Integer.MAX_VALUE);
        maxRequest.setSubscriptionTierType(Integer.MAX_VALUE);
        maxRequest.setFeatureIds(Arrays.asList(Integer.MAX_VALUE));
        
        Boolean maxResult = service.sendCustomerSubscriptionUpdateToDomains(maxRequest);
        assertTrue(maxResult);
        
        assertThat(minResult).isTrue();
        assertThat(maxResult).isTrue();
    }

    @Test
    @DisplayName("Should handle bulk operations with varied configurations")
    void shouldHandleBulkOperationsWithVariedConfigurations() {
        // Code Fix: Testing sendBulkCustomerSubscriptionUpdateToDomains() with different configurations
        
        // Configuration 1: Small batch, isAllSelected=false
        SubscriptionBulkUpdateRequest config1 = new SubscriptionBulkUpdateRequest();
        config1.setCompanyCodes(Arrays.asList(7001, 7002));
        config1.setSubscriptionTierType(1);
        config1.setIsAllSelected(false);
        
        Boolean result1 = service.sendBulkCustomerSubscriptionUpdateToDomains(config1);
        assertTrue(result1);
        
        // Configuration 2: Large batch, isAllSelected=true
        SubscriptionBulkUpdateRequest config2 = new SubscriptionBulkUpdateRequest();
        config2.setCompanyCodes(Arrays.asList(7003, 7004, 7005, 7006, 7007, 7008));
        config2.setSubscriptionTierType(2);
        config2.setIsAllSelected(true);
        
        Boolean result2 = service.sendBulkCustomerSubscriptionUpdateToDomains(config2);
        assertTrue(result2);
        
        // Configuration 3: Medium batch, null isAllSelected
        SubscriptionBulkUpdateRequest config3 = new SubscriptionBulkUpdateRequest();
        config3.setCompanyCodes(Arrays.asList(7009, 7010, 7011));
        config3.setSubscriptionTierType(3);
        config3.setIsAllSelected(null);
        
        Boolean result3 = service.sendBulkCustomerSubscriptionUpdateToDomains(config3);
        assertTrue(result3);
        
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isTrue();
    }
}
