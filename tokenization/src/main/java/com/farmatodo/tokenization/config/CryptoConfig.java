package com.farmatodo.tokenization.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Configuro la clave simétrica para cifrado AES-GCM.
 */
@Configuration
public class CryptoConfig {

    @Value("${app.crypto.key-hex}")// Leo la clave en hex desde propiedades
    private String keyHex;

    @Bean
    public SecretKey secretKey() {
        if (keyHex == null || (keyHex.length() % 2) != 0) {
            throw new IllegalStateException("CRYPTO_KEY_HEX must be non-null even-length hex");
        }
        byte[] keyBytes = hexToBytes(keyHex);// Convierto hex → bytes
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalStateException("AES key size must be 16/24/32 bytes");
        }
        return new SecretKeySpec(keyBytes, "AES");// Creo SecretKey para AES
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];// Cada 2 chars → 1 byte
        for (int i = 0; i < len; i += 2) { // Recorro de 2 en 2
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
