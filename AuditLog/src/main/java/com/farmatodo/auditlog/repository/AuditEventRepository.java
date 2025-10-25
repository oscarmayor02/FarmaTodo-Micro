package com.farmatodo.auditlog.repository;

import com.farmatodo.auditlog.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> { }