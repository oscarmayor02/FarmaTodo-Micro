package com.farmatodo.tokenization.service;

import com.farmatodo.tokenization.domain.CardToken;
import com.farmatodo.tokenization.domain.TokenStatus;
import com.farmatodo.tokenization.dto.TokenizeRequest;
import com.farmatodo.tokenization.dto.TokenizeResponse;
import com.farmatodo.tokenization.repository.CardTokenRepository;
import com.farmatodo.tokenization.util.CardBrandDetector;
import com.farmatodo.tokenization.util.CardValidators;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lógica principal de tokenización con rechazo probabilístico y cifrado AES-GCM.
 */
@Service
public class TokenService {

    private final CardTokenRepository repo;
    private final SecretKey secretKey;

    @Value("${app.token.rejection-probability:0.15}")
    private double rejectionProb;

    // si usas HMAC determinista:
    @Value("${app.token.hmac-secret:}")
    private String hmacSecret;

    public TokenService(CardTokenRepository repo, SecretKey secretKey) {
        this.repo = repo;
        this.secretKey = secretKey;
    }

    public TokenizeResponse tokenize(TokenizeRequest req) {
        // 1) Validaciones (Luhn/exp/cvv por marca) — si ya las tienes, mantenlas

        // 2) Rechazo probabilístico
        if (ThreadLocalRandom.current().nextDouble() < rejectionProb) {
            return new TokenizeResponse(null, last4(req.pan()),
                    CardBrandDetector.detect(req.pan()), TokenStatus.REJECTED.name());
        }

        // 3) Calcular token (HMAC recomendado). Si sigues con UUID, sáltate el existsByToken
        final String brand = CardBrandDetector.detect(req.pan());
        final String token = (hmacSecret != null && !hmacSecret.isBlank())
                ? HmacToken.token(hmacSecret, req.pan(), req.expMonth(), req.expYear())
                : java.util.UUID.randomUUID().toString();

        // 4) Idempotencia: si ya existe, devolverlo (200)
        var existing = repo.findByToken(token);
        if (existing.isPresent()) {
            CardToken ct = existing.get();
            return new TokenizeResponse(ct.getToken(), ct.getLast4(), ct.getBrand(), ct.getStatus().name());
        }

        // 5) Cifrar payload (SIN CVV)
        byte[] iv = randomIv();
        byte[] cipher = encrypt(payloadWithoutCvv(req), iv);

        // 6) Intentar insertar. Si hay carrera → capturar 409 y leer existente.
        try {
            CardToken saved = repo.save(CardToken.builder()
                    .token(token)
                    .last4(last4(req.pan()))
                    .brand(brand)
                    .createdAt(java.time.Instant.now())
                    .status(TokenStatus.ISSUED)
                    .encryptedPayload(cipher)
                    .payloadIvHex(bytesToHex(iv))
                    .build());

            return new TokenizeResponse(saved.getToken(), saved.getLast4(), saved.getBrand(), saved.getStatus().name());

        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // Otro hilo lo insertó primero → leer y devolver 200
            CardToken ct = repo.findByToken(token)
                    .orElseThrow(() -> new IllegalStateException("Token duplicate but not found after insert"));
            return new TokenizeResponse(ct.getToken(), ct.getLast4(), ct.getBrand(), ct.getStatus().name());
        }
    }

    private String payloadWithoutCvv(TokenizeRequest r) {
        // PAN|MM/AAAA|NAME — nunca persistir CVV
        return r.pan() + "|" + r.expMonth() + "/" + r.expYear() + "|" + r.name();
    }

    private String last4(String pan) {
        int len = pan.length();
        return pan.substring(Math.max(0, len - 4));
    }

    private byte[] encrypt(String payload, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            return cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("encryption_error", e);
        }
    }


    private byte[] randomIv() {
        byte[] iv = new byte[12];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }


    private String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte v : b) sb.append(String.format("%02X", v));
        return sb.toString();
    }

    public final class HmacToken {
        private HmacToken() {
        }

        public static String token(String secret, String pan, int expMonth, int expYear) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] raw = mac.doFinal((pan + "|" + expMonth + "|" + expYear).getBytes(StandardCharsets.UTF_8));
                // Base64url sin padding, trunc a 22-24 chars
                return Base64.getUrlEncoder().withoutPadding().encodeToString(raw).substring(0, 24);
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("hmac_error", e);
            }
        }
    }
}
