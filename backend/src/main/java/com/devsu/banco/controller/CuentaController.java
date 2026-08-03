package com.devsu.banco.controller;

import com.devsu.banco.dto.CuentaPatchRequest;
import com.devsu.banco.dto.CuentaRequest;
import com.devsu.banco.dto.CuentaResponse;
import com.devsu.banco.service.CuentaService;
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
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public List<CuentaResponse> listar(@RequestParam(required = false) String buscar) {
        return cuentaService.listar(buscar);
    }

    @GetMapping("/{id}")
    public CuentaResponse obtener(@PathVariable Long id) {
        return cuentaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crear(@Valid @RequestBody CuentaRequest request) {
        return cuentaService.crear(request);
    }

    @PutMapping("/{id}")
    public CuentaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CuentaRequest request) {
        return cuentaService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public CuentaResponse patch(@PathVariable Long id, @RequestBody CuentaPatchRequest request) {
        return cuentaService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
    }
}
