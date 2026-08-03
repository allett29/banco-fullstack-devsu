package com.devsu.banco.service;

import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.domain.Movimiento;
import com.devsu.banco.domain.TipoMovimiento;
import com.devsu.banco.dto.MovimientoPatchRequest;
import com.devsu.banco.dto.MovimientoRequest;
import com.devsu.banco.dto.MovimientoResponse;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.MovimientoMapper;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.strategy.MovimientoStrategy;
import com.devsu.banco.service.strategy.MovimientoStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orquesta el registro de movimientos: valida contra la estrategia del tipo,
 * almacena el valor firmado (crédito +, débito -) junto con el saldo resultante
 * y mantiene sincronizado el saldo actual de la cuenta en la misma transacción.
 */
@Service
@Transactional
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaService cuentaService;
    private final MovimientoStrategyFactory strategyFactory;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             CuentaService cuentaService,
                             MovimientoStrategyFactory strategyFactory) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaService = cuentaService;
        this.strategyFactory = strategyFactory;
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar(String busqueda) {
        return movimientoRepository.findAll().stream()
                .filter(movimiento -> coincideBusqueda(movimiento, busqueda))
                .map(MovimientoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Long id) {
        return MovimientoMapper.toResponse(buscarEntidad(id));
    }

    public MovimientoResponse crear(MovimientoRequest request) {
        Cuenta cuenta = cuentaService.buscarEntidad(request.getCuentaId());
        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessException("La cuenta está inactiva");
        }

        TipoMovimiento tipo = TipoMovimiento.from(request.getTipoMovimiento());
        MovimientoStrategy strategy = strategyFactory.resolve(tipo);
        BigDecimal valorAbsoluto = request.getValor().abs();
        LocalDateTime fecha = request.getFecha() != null ? request.getFecha() : LocalDateTime.now();

        strategy.validar(cuenta, valorAbsoluto, fecha);

        BigDecimal valorFirmado = strategy.calcularValorFirmado(valorAbsoluto);
        BigDecimal nuevoSaldo = cuenta.getSaldoActual().add(valorFirmado);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(fecha);
        movimiento.setTipoMovimiento(tipo.name());
        movimiento.setValor(valorFirmado);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuenta(cuenta);

        cuenta.setSaldoActual(nuevoSaldo);
        return MovimientoMapper.toResponse(movimientoRepository.save(movimiento));
    }

    public MovimientoResponse actualizar(Long id, MovimientoRequest request) {
        Movimiento actual = buscarEntidad(id);
        Cuenta cuentaAnterior = actual.getCuenta();
        cuentaAnterior.setSaldoActual(cuentaAnterior.getSaldoActual().subtract(actual.getValor()));
        movimientoRepository.delete(actual);
        movimientoRepository.flush();
        return crear(request);
    }

    public MovimientoResponse patch(Long id, MovimientoPatchRequest request) {
        Movimiento movimiento = buscarEntidad(id);
        if (request.getFecha() != null) {
            movimiento.setFecha(request.getFecha());
        }
        if (request.getTipoMovimiento() != null) {
            // Solo etiqueta visible; el valor firmado ya quedó registrado
            TipoMovimiento tipo = TipoMovimiento.from(request.getTipoMovimiento());
            movimiento.setTipoMovimiento(tipo.name());
        }
        return MovimientoMapper.toResponse(movimientoRepository.save(movimiento));
    }

    public void eliminar(Long id) {
        Movimiento movimiento = buscarEntidad(id);
        Cuenta cuenta = movimiento.getCuenta();
        cuenta.setSaldoActual(cuenta.getSaldoActual().subtract(movimiento.getValor()));
        movimientoRepository.delete(movimiento);
    }

    public Movimiento buscarEntidad(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    private boolean coincideBusqueda(Movimiento movimiento, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }
        String termino = busqueda.trim().toLowerCase();
        return movimiento.getTipoMovimiento().toLowerCase().contains(termino)
                || movimiento.getCuenta().getNumeroCuenta().toLowerCase().contains(termino)
                || String.valueOf(movimiento.getValor()).contains(termino);
    }
}
