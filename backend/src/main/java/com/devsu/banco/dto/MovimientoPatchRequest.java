package com.devsu.banco.dto;

import java.time.LocalDateTime;

public class MovimientoPatchRequest {

    private LocalDateTime fecha;
    private String tipoMovimiento;

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
}
