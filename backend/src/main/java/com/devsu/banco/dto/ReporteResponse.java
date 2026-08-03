package com.devsu.banco.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteResponse {

    private Long clienteId;
    private String clienteNombre;
    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private List<ReporteMovimientoResponse> movimientos;
    private String pdfBase64;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public BigDecimal getTotalDebitos() {
        return totalDebitos;
    }

    public void setTotalDebitos(BigDecimal totalDebitos) {
        this.totalDebitos = totalDebitos;
    }

    public BigDecimal getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(BigDecimal totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public List<ReporteMovimientoResponse> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<ReporteMovimientoResponse> movimientos) {
        this.movimientos = movimientos;
    }

    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }
}
