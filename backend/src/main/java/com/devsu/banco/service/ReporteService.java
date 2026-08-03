package com.devsu.banco.service;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.domain.Movimiento;
import com.devsu.banco.domain.TipoMovimiento;
import com.devsu.banco.dto.ReporteMovimientoResponse;
import com.devsu.banco.dto.ReporteResponse;
import com.devsu.banco.repository.MovimientoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Estado de cuenta por cliente y rango de fechas. Devuelve el detalle en JSON
 * y el mismo reporte renderizado como PDF codificado en base64, según pide el
 * enunciado. Los totales se calculan con streams sobre los valores firmados.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final MovimientoRepository movimientoRepository;
    private final ClienteService clienteService;

    public ReporteService(MovimientoRepository movimientoRepository, ClienteService clienteService) {
        this.movimientoRepository = movimientoRepository;
        this.clienteService = clienteService;
    }

    public ReporteResponse generar(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        Cliente cliente = clienteService.buscarEntidad(clienteId);
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

        List<Movimiento> movimientos = movimientoRepository.findByClienteAndFechaBetween(clienteId, inicio, fin);

        List<ReporteMovimientoResponse> detalle = movimientos.stream()
                .map(this::mapearMovimiento)
                .collect(Collectors.toList());

        BigDecimal totalCreditos = movimientos.stream()
                .filter(m -> TipoMovimiento.CREDITO.name().equals(m.getTipoMovimiento()))
                .map(Movimiento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = movimientos.stream()
                .filter(m -> TipoMovimiento.DEBITO.name().equals(m.getTipoMovimiento()))
                .map(m -> m.getValor().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReporteResponse response = new ReporteResponse();
        response.setClienteId(cliente.getClienteId());
        response.setClienteNombre(cliente.getNombre());
        response.setTotalCreditos(totalCreditos);
        response.setTotalDebitos(totalDebitos);
        response.setMovimientos(detalle);
        response.setPdfBase64(generarPdfBase64(cliente, fechaInicio, fechaFin, detalle, totalCreditos, totalDebitos));
        return response;
    }

    private ReporteMovimientoResponse mapearMovimiento(Movimiento movimiento) {
        ReporteMovimientoResponse item = new ReporteMovimientoResponse();
        item.setFecha(movimiento.getFecha().format(FECHA_FORMATO));
        item.setCliente(movimiento.getCuenta().getCliente().getNombre());
        item.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        item.setTipo(movimiento.getCuenta().getTipoCuenta());
        item.setSaldoInicial(movimiento.getCuenta().getSaldoInicial());
        item.setEstado(movimiento.getCuenta().getEstado());
        item.setMovimiento(movimiento.getValor());
        item.setSaldoDisponible(movimiento.getSaldo());
        return item;
    }

    private String generarPdfBase64(Cliente cliente,
                                    LocalDate fechaInicio,
                                    LocalDate fechaFin,
                                    List<ReporteMovimientoResponse> movimientos,
                                    BigDecimal totalCreditos,
                                    BigDecimal totalDebitos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Estado de Cuenta - BANCO", titleFont));
            document.add(new Paragraph("Cliente: " + cliente.getNombre(), normal));
            document.add(new Paragraph(
                    "Periodo: " + fechaInicio.format(FECHA_FORMATO) + " - " + fechaFin.format(FECHA_FORMATO),
                    normal));
            document.add(new Paragraph("Total créditos: " + totalCreditos, normal));
            document.add(new Paragraph("Total débitos: " + totalDebitos, normal));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            String[] headers = {
                    "Fecha", "Cliente", "Numero Cuenta", "Tipo",
                    "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible"
            };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, normal));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            movimientos.forEach(item -> {
                table.addCell(new Phrase(item.getFecha(), normal));
                table.addCell(new Phrase(item.getCliente(), normal));
                table.addCell(new Phrase(item.getNumeroCuenta(), normal));
                table.addCell(new Phrase(item.getTipo(), normal));
                table.addCell(new Phrase(String.valueOf(item.getSaldoInicial()), normal));
                table.addCell(new Phrase(String.valueOf(item.getEstado()), normal));
                table.addCell(new Phrase(String.valueOf(item.getMovimiento()), normal));
                table.addCell(new Phrase(String.valueOf(item.getSaldoDisponible()), normal));
            });

            document.add(table);
            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        }
    }
}
