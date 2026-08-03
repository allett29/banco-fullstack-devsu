package com.devsu.banco.domain;

public enum TipoMovimiento {
    CREDITO,
    DEBITO;

    /**
     * Normaliza la entrada del cliente aceptando sinónimos de uso común
     * (Depósito/Retiro, con o sin tilde) para una API más tolerante.
     */
    public static TipoMovimiento from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "CREDITO", "CRÉDITO", "DEPOSITO", "DEPÓSITO" -> CREDITO;
            case "DEBITO", "DÉBITO", "RETIRO" -> DEBITO;
            default -> throw new IllegalArgumentException("Tipo de movimiento no válido: " + value);
        };
    }
}
