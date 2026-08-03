package com.devsu.banco.service.strategy;

import com.devsu.banco.config.BancoProperties;
import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.domain.TipoMovimiento;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.repository.MovimientoRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reglas de negocio del débito exigidas por el enunciado:
 * saldo insuficiente -> "Saldo no disponible"; la suma de débitos del día
 * más el nuevo retiro no puede superar el límite diario (parametrizable
 * vía {@code banco.limite-diario-retiro}) -> "Cupo diario Excedido".
 */
@Component
public class DebitoStrategy implements MovimientoStrategy {

    private final MovimientoRepository movimientoRepository;
    private final BancoProperties bancoProperties;

    public DebitoStrategy(MovimientoRepository movimientoRepository, BancoProperties bancoProperties) {
        this.movimientoRepository = movimientoRepository;
        this.bancoProperties = bancoProperties;
    }

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.DEBITO;
    }

    @Override
    public BigDecimal calcularValorFirmado(BigDecimal valorAbsoluto) {
        return valorAbsoluto.abs().negate();
    }

    @Override
    public void validar(Cuenta cuenta, BigDecimal valorAbsoluto, LocalDateTime fecha) {
        BigDecimal saldo = cuenta.getSaldoActual();
        BigDecimal monto = valorAbsoluto.abs();

        if (saldo.compareTo(BigDecimal.ZERO) == 0 || saldo.compareTo(monto) < 0) {
            throw new BusinessException("Saldo no disponible");
        }

        LocalDate dia = fecha.toLocalDate();
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.plusDays(1).atStartOfDay();

        BigDecimal retiradoHoy = movimientoRepository.sumaDebitosDelDia(cuenta.getId(), inicio, fin);
        BigDecimal nuevoTotal = retiradoHoy.add(monto);
        BigDecimal limite = bancoProperties.getLimiteDiarioRetiro();

        if (nuevoTotal.compareTo(limite) > 0) {
            throw new BusinessException("Cupo diario Excedido");
        }
    }
}
