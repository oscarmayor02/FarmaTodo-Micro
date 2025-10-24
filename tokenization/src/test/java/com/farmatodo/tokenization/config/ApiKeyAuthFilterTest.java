package com.farmatodo.tokenization.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba rápida del filtro de API Key.
 */
class ApiKeyAuthFilterTest {

    @Test
    void rejects_when_missing_api_key() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter() {
            {   // truco para setear la key vía reflexión mínima
                try {
                    var f = ApiKeyAuthFilter.class.getDeclaredField("configuredKey");
                    f.setAccessible(true);
                    f.set(this, "dev-secret");
                } catch (Exception ignored) {}
            }
        };

        MockHttpServletRequest req = new MockHttpServletRequest("POST","/api/v1/tokenize");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = (request, response) -> fail("No debería llegar al siguiente filtro sin API Key");

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
    }
}
