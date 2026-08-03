package com.devsu.banco.controller;

import com.devsu.banco.dto.MovimientoPatchRequest;
import com.devsu.banco.dto.MovimientoRequest;
import com.devsu.banco.dto.MovimientoResponse;
import com.devsu.banco.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public List<MovimientoResponse> listar(@RequestParam(required = false) String buscar) {
        return movimientoService.listar(buscar);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return movimientoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse crear(@Valid @RequestBody MovimientoRequest request) {
        return movimientoService.crear(request);
    }

    @PutMapping("/{id}")
    public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
        return movimientoService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public MovimientoResponse patch(@PathVariable Long id, @RequestBody MovimientoPatchRequest request) {
        return movimientoService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
    }
}
