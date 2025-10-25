package com.farmatodo.tokenization.util;

/**
 * Detección muy simple de marca por prefijo.
 * (Para el reto es suficiente; en producción usaríamos tablas BIN/IIN).
 */
public final class CardBrandDetector {

    private CardBrandDetector() {}

    public static String detect(String pan) {
        if (pan == null || pan.isEmpty()) return "UNKNOWN";        // Null safety básico
        if (pan.startsWith("4")) return "VISA";                    // Visa: 4
        if (pan.matches("5[1-5].*")) return "MASTERCARD";          // MasterCard: 51-55
        if (pan.matches("3[47].*")) return "AMEX";                 // Amex: 34 o 37
        return "UNKNOWN";                                          // Cualquier otro prefijo
    }
}
