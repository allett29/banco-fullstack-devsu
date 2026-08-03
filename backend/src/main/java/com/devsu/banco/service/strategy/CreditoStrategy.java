package com.devsu.banco.service.strategy;

import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.domain.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CreditoStrategy implements MovimientoStrategy {

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.CREDITO;
    }

    @Override
    public BigDecimal calcularValorFirmado(BigDecimal valorAbsoluto) {
        return valorAbsoluto.abs();
    }

    @Override
    public void validar(Cuenta cuenta, BigDecimal valorAbsoluto, LocalDateTime fecha) {
        // Los créditos no tienen restricciones de saldo ni cupo diario
    }
}
