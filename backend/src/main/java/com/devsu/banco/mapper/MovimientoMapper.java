package com.devsu.banco.mapper;

import com.devsu.banco.domain.Movimiento;
import com.devsu.banco.dto.MovimientoResponse;

public final class MovimientoMapper {

    private MovimientoMapper() {
    }

    public static MovimientoResponse toResponse(Movimiento movimiento) {
        MovimientoResponse response = new MovimientoResponse();
        response.setId(movimiento.getId());
        response.setFecha(movimiento.getFecha());
        response.setTipoMovimiento(movimiento.getTipoMovimiento());
        response.setValor(movimiento.getValor());
        response.setSaldo(movimiento.getSaldo());
        response.setCuentaId(movimiento.getCuenta().getId());
        response.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        return response;
    }
}
