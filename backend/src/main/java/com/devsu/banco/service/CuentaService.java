package com.devsu.banco.service;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.dto.CuentaPatchRequest;
import com.devsu.banco.dto.CuentaRequest;
import com.devsu.banco.dto.CuentaResponse;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.CuentaMapper;
import com.devsu.banco.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;

    public CuentaService(CuentaRepository cuentaRepository, ClienteService clienteService) {
        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listar(String busqueda) {
        List<Cuenta> cuentas;
        if (busqueda == null || busqueda.isBlank()) {
            cuentas = cuentaRepository.findAll();
        } else {
            String termino = busqueda.trim();
            cuentas = cuentaRepository.findByNumeroCuentaContainingIgnoreCaseOrTipoCuentaContainingIgnoreCase(
                    termino, termino);
        }
        return cuentas.stream()
                .map(CuentaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CuentaResponse obtener(Long id) {
        return CuentaMapper.toResponse(buscarEntidad(id));
    }

    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new BusinessException("Ya existe una cuenta con ese número");
        }
        Cliente cliente = clienteService.buscarEntidad(request.getClienteId());

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setSaldoInicial(request.getSaldoInicial());
        cuenta.setSaldoActual(request.getSaldoInicial());
        cuenta.setEstado(request.getEstado());
        cuenta.setCliente(cliente);

        return CuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        Cuenta cuenta = buscarEntidad(id);
        Cliente cliente = clienteService.buscarEntidad(request.getClienteId());

        if (!cuenta.getNumeroCuenta().equals(request.getNumeroCuenta())
                && cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new BusinessException("Ya existe una cuenta con ese número");
        }

        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setSaldoInicial(request.getSaldoInicial());
        cuenta.setEstado(request.getEstado());
        cuenta.setCliente(cliente);

        return CuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    public CuentaResponse patch(Long id, CuentaPatchRequest request) {
        Cuenta cuenta = buscarEntidad(id);

        if (request.getTipoCuenta() != null) {
            cuenta.setTipoCuenta(request.getTipoCuenta());
        }
        if (request.getEstado() != null) {
            cuenta.setEstado(request.getEstado());
        }
        if (request.getSaldoInicial() != null) {
            cuenta.setSaldoInicial(request.getSaldoInicial());
        }

        return CuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    public void eliminar(Long id) {
        Cuenta cuenta = buscarEntidad(id);
        cuentaRepository.delete(cuenta);
    }

    public Cuenta buscarEntidad(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    public Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
    }
}
