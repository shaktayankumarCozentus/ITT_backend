package com.itt.service.repository;

import com.itt.service.entity.UserEventAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventAuditRepository extends JpaRepository<UserEventAuditLog, Long> {
}