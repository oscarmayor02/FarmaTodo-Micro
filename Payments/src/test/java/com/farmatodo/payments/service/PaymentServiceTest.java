package com.farmatodo.payments.service;

import com.farmatodo.payments.domain.Payment;
import com.farmatodo.payments.dto.CardData;
import com.farmatodo.payments.dto.ChargeRequest;
import com.farmatodo.payments.dto.TokenizationModels;
import com.farmatodo.payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.ErrorResponseException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Tests unitarios de PaymentService
 */
class PaymentServiceTest {

    private PaymentRepository repo;
    private TokenizationClient tokenClient;
    private NotificationPublisher publisher;
    private AuditClient auditClient;

    private PaymentService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(PaymentRepository.class);
        tokenClient = Mockito.mock(TokenizationClient.class);
        publisher = Mockito.mock(NotificationPublisher.class);
        auditClient = Mockito.mock(AuditClient.class);

        service = new PaymentService(repo, tokenClient, publisher, auditClient);

        setField(service, "rejectionProb", 0.0d); // siempre aprueba
        setField(service, "maxRetries", 0);       // sin reintentos
        setField(service, "backoffMs", 0L);       // sin sleep
        setField(service, "tokenizationMaxRetries", 0); // sin reintentos de tokenización
        setField(service, "tokenizationBackoffMs", 0L);
    }

    private static void setField(Object target, String name, Object val) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void charge_with_card_calls_tokenization_and_approves() {
        var card = new CardData("4111111111111111", "123", 12, 2030, "John");
        var req = new ChargeRequest("order-1", 1000, "COP", null, card, "user@mail.com");

        var tRes = new TokenizationModels.TokenizeResponse("tok-123", "1111", "VISA", "ISSUED");
        Mockito.when(tokenClient.tokenize(any())).thenReturn(tRes);

        Mockito.when(repo.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var res = service.charge(req);

        assertEquals("APPROVED", res.status());
        Mockito.verify(tokenClient).tokenize(any());
        Mockito.verify(repo).save(any(Payment.class));

        // Se notifica éxito
        Mockito.verify(publisher).notify(
                Mockito.eq("PAYMENT_SUCCEEDED"),
                Mockito.eq("order-1"),
                Mockito.eq("user@mail.com"),
                Mockito.eq(1000L),
                Mockito.eq("COP"),
                anyInt(),
                Mockito.eq("APPROVED")
        );

        // Se auditó al menos una vez
        Mockito.verify(auditClient, Mockito.atLeastOnce()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void charge_with_token_does_not_call_tokenization() {
        var req = new ChargeRequest("order-2", 2000, "COP", "tok-abc", null, "u@mail.com");

        Mockito.when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = service.charge(req);

        assertEquals("APPROVED", res.status());
        Mockito.verify(tokenClient, Mockito.never()).tokenize(any());

        Mockito.verify(publisher).notify(
                Mockito.eq("PAYMENT_SUCCEEDED"),
                Mockito.eq("order-2"),
                Mockito.eq("u@mail.com"),
                Mockito.eq(2000L),
                Mockito.eq("COP"),
                anyInt(),
                Mockito.eq("APPROVED")
        );

        Mockito.verify(auditClient, Mockito.atLeastOnce()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void charge_missing_token_and_card_bad_request() {
        var req = new ChargeRequest("order-3", 1000, "COP", null, null, "a@mail.com");
        var ex = assertThrows(ErrorResponseException.class, () -> service.charge(req));
        assertEquals(400, ex.getStatusCode().value());
        Mockito.verifyNoInteractions(repo, tokenClient, publisher, auditClient);
    }

    @Test
    void charge_rejected_after_retries_throws_422() {
        // Fuerza rechazo: prob=1.0 y con reintentos
        setField(service, "rejectionProb", 1.0d);
        setField(service, "maxRetries", 2);

        var req = new ChargeRequest("order-4", 500, "COP", "tok", null, "x@mail.com");

        Mockito.when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var ex = assertThrows(ErrorResponseException.class, () -> service.charge(req));
        // Tu servicio actualmente lanza 422 (Unprocessable) en el rechazo final
        assertEquals(422, ex.getStatusCode().value());

        // Debe notificar fallo
        Mockito.verify(publisher).notify(
                Mockito.eq("PAYMENT_FAILED"),
                Mockito.eq("order-4"),
                Mockito.eq("x@mail.com"),
                Mockito.eq(500L),
                Mockito.eq("COP"),
                anyInt(),
                Mockito.eq("REJECTED")
        );

        // También debe auditar
        Mockito.verify(auditClient, Mockito.atLeastOnce()).log(any(), any(), any(), any(), any(), any());
    }
}
