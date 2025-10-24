package com.farmatodo.tokenization.domain;


/**
 * Estado del token emitido.
 */
public enum TokenStatus {
    ISSUED,// Token emitido correctamente
    REJECTED// Petición rechazada por probabilidad
}