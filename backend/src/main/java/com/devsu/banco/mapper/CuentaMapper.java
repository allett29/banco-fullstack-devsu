package com.devsu.banco.mapper;

import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.dto.CuentaResponse;

public final class CuentaMapper {

    private CuentaMapper() {
    }

    public static CuentaResponse toResponse(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setId(cuenta.getId());
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setTipoCuenta(cuenta.getTipoCuenta());
        response.setSaldoInicial(cuenta.getSaldoInicial());
        response.setSaldoActual(cuenta.getSaldoActual());
        response.setEstado(cuenta.getEstado());
        response.setClienteId(cuenta.getCliente().getClienteId());
        response.setClienteNombre(cuenta.getCliente().getNombre());
        return response;
    }
}
