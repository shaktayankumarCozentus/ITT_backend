package com.itt.service.specification;

import org.springframework.data.jpa.domain.Specification;

// UserSpecification.java

import com.itt.service.entity.MasterUser;
import com.itt.service.entity.Role;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class UserSpecification {

    /**
     * Global search across name, email, and assignedRole.name
     */
    public static Specification<MasterUser> searchByNameEmailRole(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + text.toLowerCase() + "%";
            Join<MasterUser, Role> role = root.join("assignedRole", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")),    pattern),
                    cb.like(cb.lower(role.get("name")),     pattern)
            );
        };
    }

    /**
     * Filter by active/inactive role flag.
     */
    public static Specification<MasterUser> byActiveRole(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return null;
            Join<MasterUser, Role> role = root.join("assignedRole", JoinType.INNER);
            return isActive
                    ? cb.equal(role.get("isActive"), 1)
                    : cb.equal(role.get("isActive"), 0);
        };
    }

    /**
     * Column-specific filter: WHERE lower(column) LIKE %filter%
     */
    public static Specification<MasterUser> columnFilter(String column, String filter) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get(column)), "%" + filter.toLowerCase() + "%");
    }

    /**
     * Users whose assigned Role.isActive = 1
     */
    public static Specification<MasterUser> hasActiveRole() {
        return (root, query, cb) -> {
            // ensure we only get users who actually have a role
            Join<MasterUser, Role> role = root.join("assignedRole", JoinType.INNER);
            return cb.equal(role.get("isActive"), 1);
        };
    }

    /**
     * Users whose assigned Role.isActive = 0
     */
    public static Specification<MasterUser> hasInactiveRole() {
        return (root, query, cb) -> {
            Join<MasterUser, Role> role = root.join("assignedRole", JoinType.INNER);
            return cb.equal(role.get("isActive"), 0);
        };
    }

    /**
     * NEW: Users with active role OR no role assigned
     * This uses LEFT JOIN to include users without roles
     */
    public static Specification<MasterUser> hasActiveRoleOrNoRole() {
        return (root, query, cb) -> {
            Join<MasterUser, Role> role = root.join("assignedRole", JoinType.LEFT);
            return cb.or(
                cb.isNull(role.get("id")), // No role assigned
                cb.equal(role.get("isActive"), 1) // Or has active role
            );
        };
    }
}
