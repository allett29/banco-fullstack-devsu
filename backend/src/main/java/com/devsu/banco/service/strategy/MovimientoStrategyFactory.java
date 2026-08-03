package com.devsu.banco.service.strategy;

import com.devsu.banco.domain.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resuelve la estrategia según el tipo de movimiento. Spring inyecta todas las
 * implementaciones de {@link MovimientoStrategy}, por lo que registrar una
 * nueva estrategia solo requiere declarar el componente.
 */
@Component
public class MovimientoStrategyFactory {

    private final Map<TipoMovimiento, MovimientoStrategy> strategies;

    public MovimientoStrategyFactory(List<MovimientoStrategy> strategyList) {
        this.strategies = new EnumMap<>(TipoMovimiento.class);
        strategyList.forEach(strategy -> strategies.put(strategy.getTipo(), strategy));
    }

    public MovimientoStrategy resolve(TipoMovimiento tipo) {
        MovimientoStrategy strategy = strategies.get(tipo);
        if (strategy == null) {
            throw new IllegalArgumentException("No existe estrategia para el tipo: " + tipo);
        }
        return strategy;
    }
}
