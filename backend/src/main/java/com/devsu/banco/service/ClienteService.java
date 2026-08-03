package com.devsu.banco.service;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.dto.ClientePatchRequest;
import com.devsu.banco.dto.ClienteRequest;
import com.devsu.banco.dto.ClienteResponse;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.ClienteMapper;
import com.devsu.banco.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String busqueda) {
        List<Cliente> clientes;
        if (busqueda == null || busqueda.isBlank()) {
            clientes = clienteRepository.findAll();
        } else {
            String termino = busqueda.trim();
            clientes = clienteRepository
                    .findByNombreContainingIgnoreCaseOrIdentificacionContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
                            termino, termino, termino);
        }
        return clientes.stream()
                .map(ClienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteMapper.toResponse(buscarEntidad(id));
    }

    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new BusinessException("Ya existe un cliente con esa identificación");
        }
        Cliente cliente = ClienteMapper.toEntity(request);
        return ClienteMapper.toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidad(id);
        validarIdentificacionUnica(request.getIdentificacion(), id);
        ClienteMapper.apply(request, cliente);
        return ClienteMapper.toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse patch(Long id, ClientePatchRequest request) {
        Cliente cliente = buscarEntidad(id);

        if (request.getNombre() != null) {
            cliente.setNombre(request.getNombre());
        }
        if (request.getGenero() != null) {
            cliente.setGenero(request.getGenero());
        }
        if (request.getEdad() != null) {
            cliente.setEdad(request.getEdad());
        }
        if (request.getIdentificacion() != null) {
            validarIdentificacionUnica(request.getIdentificacion(), id);
            cliente.setIdentificacion(request.getIdentificacion());
        }
        if (request.getDireccion() != null) {
            cliente.setDireccion(request.getDireccion());
        }
        if (request.getTelefono() != null) {
            cliente.setTelefono(request.getTelefono());
        }
        if (request.getContrasena() != null) {
            cliente.setContrasena(request.getContrasena());
        }
        if (request.getEstado() != null) {
            cliente.setEstado(request.getEstado());
        }

        return ClienteMapper.toResponse(clienteRepository.save(cliente));
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarEntidad(id);
        clienteRepository.delete(cliente);
    }

    public Cliente buscarEntidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    private void validarIdentificacionUnica(String identificacion, Long clienteId) {
        clienteRepository.findByIdentificacion(identificacion)
                .filter(existente -> !existente.getClienteId().equals(clienteId))
                .ifPresent(existente -> {
                    throw new BusinessException("Ya existe un cliente con esa identificación");
                });
    }
}
