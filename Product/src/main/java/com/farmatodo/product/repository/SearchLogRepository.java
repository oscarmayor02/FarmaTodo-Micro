package com.farmatodo.product.repository;

import com.farmatodo.product.domain.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {}
