package com.itt.service.repository;

import com.itt.service.entity.UserEventAuditConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventAuditConfigRepository extends JpaRepository<UserEventAuditConfig, Long> {
    List<UserEventAuditConfig> findByEnabledTrue();
}
