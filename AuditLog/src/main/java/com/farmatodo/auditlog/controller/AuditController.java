package com.farmatodo.auditlog.controller;

import com.farmatodo.auditlog.domain.AuditEvent;
import com.farmatodo.auditlog.dto.AuditEventRequest;
import com.farmatodo.auditlog.repository.AuditEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuditController {

    private final AuditEventRepository repo;

    @PostMapping("/logs")
    public ResponseEntity<Void> log(@RequestHeader(value="X-TX-ID", required=false) String txId,
                                    @Valid @RequestBody AuditEventRequest req) {
        String tx = (txId==null || txId.isBlank()) ? java.util.UUID.randomUUID().toString() : txId;
        AuditEvent ev = AuditEvent.builder()
                .txId(tx)
                .service(req.service())
                .eventType(req.eventType())
                .orderId(req.orderId())
                .entityId(req.entityId())
                .payloadJson(req.payloadJson())
                .createdAt(Instant.now())
                .build();
        repo.save(ev);
        return ResponseEntity.noContent().build();
    }
}