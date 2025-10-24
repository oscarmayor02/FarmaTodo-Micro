//package com.farmatodo.payments.service;
//
//
//import com.farmatodo.payments.domain.Payment;
//import com.farmatodo.payments.dto.CardData;
//import com.farmatodo.payments.dto.ChargeRequest;
//import com.farmatodo.payments.dto.TokenizationModels;
//import com.farmatodo.payments.repository.PaymentRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.web.ErrorResponseException;
//
//import java.lang.reflect.Field;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//// Tests unitarios del servicio PaymentService
//class PaymentServiceTest {
//
//    private PaymentRepository repo;
//    private TokenizationClient tokenClient;
//    private PaymentService service;
//
//    // Antes de cada test, inicializamos mocks y el servicio
//    @BeforeEach
//    void setup() {
//        repo = Mockito.mock(PaymentRepository.class);
//        tokenClient = Mockito.mock(TokenizationClient.class);
//        service = new PaymentService(repo, tokenClient, null);
//
//        // Inyectamos valores de configuración vía reflexión (para el test)
//        setField(service, "rejectionProb", 0.0d);   // Aprobación garantizada
//        setField(service, "maxRetries", 0);         // Sin reintentos
//        setField(service, "backoffMs", 0L);         // Sin espera
//    }
//
//    // Helper para setear campos privados
//    private static void setField(Object target, String name, Object val) {
//        try {
//          Field f = target.getClass().getDeclaredField(name);
//            f.setAccessible(true);
//            f.set(target, val);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // Caso: viene card (sin token) -> llamamos a tokenization y aprobamos
//    @Test
//    void charge_with_card_calls_tokenization_and_approves() {
//        // Preparamos request con card
//        var card = new CardData("4111111111111111", "123", 12, 2030, "John");
//        var req = new ChargeRequest("order-1", 1000, "COP", null, card);
//
//        // Simulamos respuesta exitosa de tokenization
//        var tRes = new TokenizationModels.TokenizeResponse("tok-123", "1111", "VISA", "ISSUED");
//        Mockito.when(tokenClient.tokenize(Mockito.any())).thenReturn(tRes);
//
//        // Simulamos que repo.save devuelve la entidad con id
//        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> {
//            var p = (Payment) inv.getArgument(0);
//            p.setId(1L);
//            return p;
//        });
//
//        // Ejecutamos el servicio
//        var res = service.charge(req);
//
//        // Validamos que está aprobado
//        assertEquals("APPROVED", res.status());
//        // Validamos que se llamó a tokenization
//       Mockito.verify(tokenClient).tokenize(Mockito.any());
//        // Validamos que se persistió
//       Mockito.verify(repo).save(Mockito.any());
//    }
//
//    // Caso: viene token directo (no llamamos a tokenization)
//    @Test
//    void charge_with_token_does_not_call_tokenization() {
//        // Request con token presente
//        var req = new ChargeRequest("order-2", 2000, "COP", "tok-abc", null);
//
//        // Mock repo.save
//        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
//
//        // Ejecutamos
//        var res = service.charge(req);
//
//        // Aprobado
//        assertEquals("APPROVED", res.status());
//        // No se llama tokenization
//        Mockito.verify(tokenClient, Mockito.never()).tokenize(Mockito.any());
//    }
//
//    // Caso: no viene token ni card -> bad request
//    @Test
//    void charge_missing_token_and_card_bad_request() {
//        var req = new ChargeRequest("order-3", 1000, "COP", null, null);
//        var ex = assertThrows(ErrorResponseException.class, () -> service.charge(req));
//        assertTrue(ex.getStatusCode().value() == 400);
//    }
//
//    // Caso: rechazo por probabilidad con reintentos -> terminamos lanzando 402
//    @Test
//    void charge_rejected_after_retries_throws_402() {
//        // Rechazo garantizado (prob=1.0) y 2 reintentos
//        setField(service, "rejectionProb", 1.0d);
//        setField(service, "maxRetries", 2);
//
//        var req = new ChargeRequest("order-4", 500, "COP", "tok", null);
//        // Mock repo.save para registrar el pago (aunque sea rechazado)
//       Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
//
//        var ex = assertThrows(ErrorResponseException.class, () -> service.charge(req));
//        assertEquals(402, ex.getStatusCode().value());
//    }
//}
