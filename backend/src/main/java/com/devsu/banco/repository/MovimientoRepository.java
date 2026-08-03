package com.devsu.banco.repository;

import com.devsu.banco.domain.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaId(Long cuentaId);

    @Query("""
            SELECT COALESCE(SUM(ABS(m.valor)), 0)
            FROM Movimiento m
            WHERE m.cuenta.id = :cuentaId
              AND m.tipoMovimiento = 'DEBITO'
              AND m.fecha >= :inicio
              AND m.fecha < :fin
            """)
    BigDecimal sumaDebitosDelDia(@Param("cuentaId") Long cuentaId,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT m FROM Movimiento m
            JOIN FETCH m.cuenta c
            JOIN FETCH c.cliente cl
            WHERE cl.clienteId = :clienteId
              AND m.fecha >= :fechaInicio
              AND m.fecha < :fechaFin
            ORDER BY m.fecha ASC
            """)
    List<Movimiento> findByClienteAndFechaBetween(@Param("clienteId") Long clienteId,
                                                  @Param("fechaInicio") LocalDateTime fechaInicio,
                                                  @Param("fechaFin") LocalDateTime fechaFin);
}
