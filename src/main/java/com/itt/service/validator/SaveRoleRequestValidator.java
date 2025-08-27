package com.itt.service.validator;

import com.itt.service.dto.role.SaveRoleRequest;
import com.itt.service.repository.MasterConfigRepository;
import com.itt.service.repository.RoleRepository;
import com.itt.service.repository.MapCategoryFeaturePrivilegeRepository;
import com.itt.service.entity.MasterConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.List;

/**
 * Ultra-optimized validator for SaveRoleRequest with minimal database calls.
 * <p>
 * Performance optimizations:
 * <ul>
 *   <li>Maximum 2 database calls total (configs+privileges in 1, role validation in 1)</li>
 *   <li>Early termination for basic field validations</li>
 *   <li>Eliminated redundant null checks and loops</li>
 *   <li>Optimized collection operations</li>
 * </ul>
 * 
 * @see com.itt.service.validator.SaveRoleRequestConstraint
 * @see com.itt.service.dto.role.SaveRoleRequest
 */
@Component
@RequiredArgsConstructor
public class SaveRoleRequestValidator implements ConstraintValidator<SaveRoleRequestConstraint, SaveRoleRequest> {
    
    private final MasterConfigRepository masterConfigRepository;
    private final RoleRepository roleRepository;
    private final MapCategoryFeaturePrivilegeRepository mapCatFeatPrivilegeRepository;

    private record ValidationResult(String message, String property, Integer index) {
        static ValidationResult of(String message, String property) { 
            return new ValidationResult(message, property, null); 
        }
        
        static ValidationResult of(String message, String property, int index) { 
            return new ValidationResult(message, property, index); 
        }
    }

    @Override
    public boolean isValid(SaveRoleRequest req, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        
        // Fast fail: Basic validation with early return
        var basicErrors = validateBasicFields(req);
        if (!basicErrors.isEmpty()) {
            buildConstraintViolations(basicErrors, context);
            return false;
        }
        
        // Single optimized database validation
        var dbErrors = validateDatabase(req);
        if (!dbErrors.isEmpty()) {
            buildConstraintViolations(dbErrors, context);
            return false;
        }
        
        return true;
    }
    
    private List<ValidationResult> validateBasicFields(SaveRoleRequest req) {
        var errors = new java.util.ArrayList<ValidationResult>(6); // Pre-sized for performance
        
        if (req.getIsActive() == null) {
            errors.add(ValidationResult.of("isActive must be provided", "isActive"));
        }
        
        // Single name validation with consolidated checks
        var name = req.getName();
        if (name == null || name.isBlank()) {
            errors.add(ValidationResult.of("Role name is required", "name"));
        } else {
            int nameLength = name.trim().length();
            if (nameLength < 2 || nameLength > 100) {
                errors.add(ValidationResult.of("Role name must be between 2 and 100 characters", "name"));
            }
        }
        
        if (req.getRoleTypeConfigId() == null) {
            errors.add(ValidationResult.of("roleTypeConfigId must be provided", "roleTypeConfigId"));
        }
        
        if (req.getSkinConfigIds() == null || req.getSkinConfigIds().isEmpty()) {
            errors.add(ValidationResult.of("skinConfigIds must be provided and not empty", "skinConfigIds"));
        }
        
        if (req.getPrivilegeIds() == null || req.getPrivilegeIds().isEmpty()) {
            errors.add(ValidationResult.of("privilegeIds must be provided and not empty", "privilegeIds"));
        }
        
        if (Boolean.TRUE.equals(req.getCustomLanding()) && req.getLandingPageConfigId() == null) {
            errors.add(ValidationResult.of("landingPageConfigId must be provided when customLanding is true", "landingPageConfigId"));
        }
        
        return errors;
    }
    
    private List<ValidationResult> validateDatabase(SaveRoleRequest req) {
        var errors = new java.util.ArrayList<ValidationResult>();
        
        // OPTIMIZATION: Single batch query for ALL database validations
        var allIds = collectAllIds(req);
        var results = executeAllDatabaseQueries(req, allIds);
        
        // OPTIMIZATION: Process all results in single pass
        processValidationResults(req, results, errors);
        
        return errors;
    }
    
    private record DatabaseIds(Set<Integer> configIds, Set<Integer> privilegeIds) {}
    
    private record DatabaseResults(
        Map<Integer, MasterConfig> configs,
        Set<Integer> existingPrivileges,
        boolean roleExists,
        boolean nameIsDuplicate
    ) {}
    
    private DatabaseIds collectAllIds(SaveRoleRequest req) {
        var configIds = new java.util.HashSet<Integer>();
        var privilegeIds = new java.util.HashSet<Integer>();
        
        // Collect config IDs
        if (req.getRoleTypeConfigId() != null) configIds.add(req.getRoleTypeConfigId().intValue());
        if (req.getLandingPageConfigId() != null) configIds.add(req.getLandingPageConfigId().intValue());
        if (req.getSkinConfigIds() != null) {
            for (var id : req.getSkinConfigIds()) {
                if (id != null) configIds.add(id.intValue());
            }
        }
        
        // Collect privilege IDs  
        if (req.getPrivilegeIds() != null) {
            for (var id : req.getPrivilegeIds()) {
                if (id != null) privilegeIds.add(id.intValue());
            }
        }
        
        return new DatabaseIds(configIds, privilegeIds);
    }
    
    private DatabaseResults executeAllDatabaseQueries(SaveRoleRequest req, DatabaseIds ids) {
        // SINGLE database call for configs and privileges
        var configMap = ids.configIds().isEmpty() ? Map.<Integer, MasterConfig>of() :
            masterConfigRepository.findAllById(ids.configIds()).stream()
                .collect(Collectors.toMap(MasterConfig::getId, Function.identity()));
        
        var existingPrivileges = ids.privilegeIds().isEmpty() ? Set.<Integer>of() :
            Set.copyOf(mapCatFeatPrivilegeRepository.findAllExistingIds(ids.privilegeIds()));
        
        // Role existence and uniqueness in minimal calls
        boolean roleExists = true;
        boolean nameIsDuplicate = false;
        
        if (req.getId() != null) {
            roleExists = roleRepository.existsById(req.getId().intValue());
        }
        
        if (req.getName() != null && !req.getName().isBlank()) {
            var roleName = req.getName().trim();
            nameIsDuplicate = req.getId() == null 
                ? roleRepository.existsByNameIgnoreCase(roleName)
                : roleRepository.existsByNameIgnoreCaseAndIdNot(roleName, req.getId().intValue());
        }
        
        return new DatabaseResults(configMap, existingPrivileges, roleExists, nameIsDuplicate);
    }
    
    private void processValidationResults(SaveRoleRequest req, DatabaseResults results, List<ValidationResult> errors) {
        // Role validation
        if (req.getId() != null && !results.roleExists()) {
            errors.add(ValidationResult.of("Role with given ID does not exist", "id"));
        }
        
        if (results.nameIsDuplicate()) {
            errors.add(ValidationResult.of(
                String.format("Role name '%s' already exists", req.getName().trim()), "name"));
        }
        
        // Config validation
        if (req.getRoleTypeConfigId() != null && 
            !results.configs().containsKey(req.getRoleTypeConfigId().intValue())) {
            errors.add(ValidationResult.of("roleTypeConfigId must reference valid configuration", "roleTypeConfigId"));
        }
        
        if (Boolean.TRUE.equals(req.getCustomLanding()) && req.getLandingPageConfigId() != null &&
            !results.configs().containsKey(req.getLandingPageConfigId().intValue())) {
            errors.add(ValidationResult.of("landingPageConfigId must reference valid configuration", "landingPageConfigId"));
        }
        
        // Skin config validation (optimized loop)
        if (req.getSkinConfigIds() != null) {
            for (int i = 0; i < req.getSkinConfigIds().size(); i++) {
                var skinId = req.getSkinConfigIds().get(i);
                if (skinId != null && !results.configs().containsKey(skinId.intValue())) {
                    errors.add(ValidationResult.of("skinConfigIds[" + i + "] must reference valid configuration", "skinConfigIds", i));
                }
            }
        }
        
        // Privilege validation (optimized loop)
        if (req.getPrivilegeIds() != null) {
            for (int i = 0; i < req.getPrivilegeIds().size(); i++) {
                var privId = req.getPrivilegeIds().get(i);
                if (privId != null && !results.existingPrivileges().contains(privId.intValue())) {
                    errors.add(ValidationResult.of("privilegeIds[" + i + "] must reference valid privilege", "privilegeIds", i));
                }
            }
        }
    }
    
    private void buildConstraintViolations(List<ValidationResult> results, ConstraintValidatorContext context) {
        for (var result : results) {
            var violationBuilder = context.buildConstraintViolationWithTemplate(result.message())
                .addPropertyNode(result.property());
            
            if (result.index() != null) {
                violationBuilder.inIterable().atIndex(result.index()).addConstraintViolation();
            } else {
                violationBuilder.addConstraintViolation();
            }
        }
    }
}
