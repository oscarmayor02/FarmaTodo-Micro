package com.farmatodo.orders.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class TxFilter extends OncePerRequestFilter {
    public static final String HDR = "X-TX-ID";
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String tx = Optional.ofNullable(req.getHeader(HDR)).orElse(UUID.randomUUID().toString());
        MDC.put("txId", tx);
        res.setHeader(HDR, tx);
        try { chain.doFilter(req, res); }
        finally { MDC.remove("txId"); }
    }
}
