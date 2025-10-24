package com.farmatodo.payments.repository;

import com.farmatodo.payments.domain.PaymentEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventLogRepository extends JpaRepository<PaymentEventLog, Long> {}
