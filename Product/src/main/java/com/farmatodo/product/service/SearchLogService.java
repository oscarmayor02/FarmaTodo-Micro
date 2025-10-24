package com.farmatodo.product.service;

import com.farmatodo.product.domain.SearchLog;
import com.farmatodo.product.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchLogService {
    private final SearchLogRepository repo;

    @Async
    public void logAsync(String q){
        try {
            repo.save(SearchLog.builder().q(q).build());
        } catch (Exception ignored) {}
    }
}
