package com.farmatodo.tokenization.service;                         // Paquete de tests

import com.farmatodo.tokenization.domain.CardToken;                // Entidad
import com.farmatodo.tokenization.dto.TokenizeRequest;            // DTO request
import com.farmatodo.tokenization.repository.CardTokenRepository; // Repo
import org.junit.jupiter.api.BeforeEach;                           // JUnit lifecycle
import org.junit.jupiter.api.Test;                                 // Test annotation
import org.mockito.InjectMocks;                                    // Mockito: inyección
import org.mockito.Mock;                                           // Mockito: mock
import org.mockito.MockitoAnnotations;                             // Init mocks
import javax.crypto.SecretKey;                                     // Clave simétrica
import javax.crypto.spec.SecretKeySpec;                            // SecretKey impl
import java.time.Instant;                                          // Timestamp
import static org.junit.jupiter.api.Assertions.*;                  // Asserts
import static org.mockito.ArgumentMatchers.any;                    // Matchers
import static org.mockito.Mockito.when;                            // Stubbing

/**
 * Prueba simple de tokenización (camino feliz).
 */
class TokenServiceTest {

    @Mock
    CardTokenRepository repo;

    @InjectMocks
    TokenService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        SecretKey key = new SecretKeySpec(new byte[32], "AES");// Clave fake de 256 bits
        service = new TokenService(repo, key);                     // Inyecto dependencias

        var field = TokenService.class.getDeclaredField("rejectionProb");
        field.setAccessible(true);
        field.set(service, 0.0d);
    }

    @Test
    void tokenize_success_persists_and_returns_token() {
        // Simulo que el repo devuelve el mismo objeto guardado con campos poblados
        when(repo.save(any())).thenAnswer(inv -> {
            CardToken t = inv.getArgument(0);
            t.setCreatedAt(Instant.now());
            return t;
        });

        var req = new TokenizeRequest("4111111111111111","123",12,2030,"John");
        var res = service.tokenize(req);

        assertEquals("ISSUED", res.status());
        assertNotNull(res.token());
        assertEquals("1111", res.last4());
    }
}
