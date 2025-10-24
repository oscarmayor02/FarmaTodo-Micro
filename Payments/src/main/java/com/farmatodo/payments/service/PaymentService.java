package com.farmatodo.payments.service;

import com.farmatodo.payments.domain.Payment;
import com.farmatodo.payments.domain.PaymentStatus;
import com.farmatodo.payments.dto.ChargeRequest;
import com.farmatodo.payments.dto.ChargeResponse;
import com.farmatodo.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repo;
    private final TokenizationClient tokenizationClient;

    private final NotificationPublisher publisher;
    private final AuditClient auditClient;

    @Value("${payment.rejection-prob:0.20}")
    private double rejectionProb;

    @Value("${payment.max-retries:2}")
    private int maxRetries;

    @Value("${payment.backoff-ms:200}")
    private long backoffMs;

    @Value("${payment.tokenization.max-retries:2}")
    private int tokenizationMaxRetries;

    @Value("${payment.tokenization.backoff-ms:150}")
    private long tokenizationBackoffMs;

    public ChargeResponse charge(ChargeRequest req) {
        if ((req.token() == null || req.token().isBlank()) && req.card() == null) {
            throw badRequest("Either 'token' or 'card' must be provided");
        }
        if (req.amount() <= 0) throw badRequest("Amount must be > 0");
        if (req.currency() == null || req.currency().isBlank()) throw badRequest("Currency is required");

        final String txId = MDC.get("txId");
        System.out.println(req + txId);
        auditClient.log(
                txId,
                "payments",
                "PAYMENT.REQUESTED",
                req.orderId(),
                null,
                """
                {
                  "amount": %d,
                  "currency": "%s",
                  "hasToken": %s,
                  "hasCard": %s
                }
                """.formatted(req.amount(), req.currency(), req.token()!=null && !req.token().isBlank(), req.card()!=null)
        );

        String token = req.token();
        String last4 = null;
        String brand = null;

        // Tokenización con reintentos cortos en 422
        if (token == null || token.isBlank()) {
            int tries = 0;
            while (true) {
                try {
                    var tRes = tokenizationClient.tokenize(req.card());
                    if (!"ISSUED".equalsIgnoreCase(tRes.status())) {
                        throw unprocessable("Tokenization did not issue a token");
                    }
                    token = tRes.token();
                    last4 = tRes.last4();
                    brand = tRes.brand();
                    break;
                } catch (WebClientResponseException ex) {
                    if (ex.getStatusCode().value() == 422 && tries++ < tokenizationMaxRetries) {
                        sleepQuiet(tokenizationBackoffMs);
                        continue;
                    }
                    throw ex;
                }
            }
        }

        // Pago probabilístico con reintentos
        int attempts = 0;
        boolean approved = false;
        String authCode = null;

        while (attempts <= maxRetries) {
            attempts++;
            double r = ThreadLocalRandom.current().nextDouble();
            if (r >= rejectionProb) {
                approved = true;
                authCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
                break;
            }
            if (attempts <= maxRetries) sleepQuiet(backoffMs);
        }

        Payment saved = repo.save(Payment.builder()
                .orderId(req.orderId())
                .amount(req.amount())
                .currency(req.currency().toUpperCase())
                .last4(last4)
                .brand(brand)
                .token(token)
                .authCode(authCode)
                .attempts(attempts)
                .status(approved ? PaymentStatus.APPROVED : PaymentStatus.REJECTED)
                .createdAt(Instant.now())
                .build());

        if (!approved) {
            safeNotify("PAYMENT_FAILED", req.orderId(), req.customerEmail(),
                    req.amount(), req.currency().toUpperCase(), attempts, "REJECTED");
            auditClient.log(
                    txId,
                    "payments",
                    "PAYMENT.REJECTED",
                    req.orderId(),
                    null,
                    """
                    {
                      "attempts": %d,
                      "amount": %d,
                      "currency": "%s"
                    }
                    """.formatted(attempts, req.amount(), req.currency().toUpperCase())
            );
            throw unprocessable("Payment rejected after " + attempts + " attempt(s)");
        }

        safeNotify("PAYMENT_SUCCEEDED", saved.getOrderId(), req.customerEmail(),
                saved.getAmount(), saved.getCurrency(), saved.getAttempts(), "APPROVED");
        auditClient.log(
                txId,
                "payments",
                "PAYMENT.APPROVED",
                saved.getOrderId(),
                null,
                """
                {
                  "attempts": %d,
                  "amount": %d,
                  "currency": "%s",
                  "authCode": "%s",
                  "brand": "%s",
                  "last4": "%s"
                }
                """.formatted(saved.getAttempts(), saved.getAmount(), saved.getCurrency(), saved.getAuthCode(), saved.getBrand(), saved.getLast4())
        );
        return new ChargeResponse("APPROVED", saved.getAttempts(), saved.getAuthCode(), saved.getOrderId());
    }

    public ChargeResponse getById(Long id) {
        var p = repo.findById(id).orElseThrow(() -> notFound("Payment not found"));
        return new ChargeResponse(p.getStatus().name(), p.getAttempts(), p.getAuthCode(), p.getOrderId());
    }

    /* ---------------- Helpers ---------------- */

    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw internal("Interrupted during backoff"); }
    }

    private void safeNotify(String type, String orderId, String email,
                            long amount, String currency, int attempts, String status) {
        try {
            publisher.notify(type, orderId, email, amount, currency, attempts, status);
        } catch (Exception e) {
            System.err.println(" No se pudo enviar notificación: " + e.getMessage());
        }
    }


    private ErrorResponseException unprocessable(String m) { var ex=new ErrorResponseException(HttpStatus.UNPROCESSABLE_ENTITY); ex.setDetail(m); return ex; }
    private ErrorResponseException notFound(String m) { var ex=new ErrorResponseException(HttpStatus.NOT_FOUND); ex.setDetail(m); return ex; }
    private ErrorResponseException badRequest(String m) { var ex=new ErrorResponseException(HttpStatus.BAD_REQUEST); ex.setDetail(m); return ex; }
    private ErrorResponseException internal(String m) { var ex=new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR); ex.setDetail(m); return ex; }
}
