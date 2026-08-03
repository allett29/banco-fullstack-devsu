package com.devsu.banco.service.strategy;

import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Patrón Strategy: encapsula las reglas de negocio propias de cada tipo de
 * movimiento (crédito/débito), de modo que agregar un nuevo tipo no requiera
 * modificar {@code MovimientoService} (Open/Closed).
 */
public interface MovimientoStrategy {

    TipoMovimiento getTipo();

    BigDecimal calcularValorFirmado(BigDecimal valorAbsoluto);

    void validar(Cuenta cuenta, BigDecimal valorAbsoluto, LocalDateTime fecha);
}
