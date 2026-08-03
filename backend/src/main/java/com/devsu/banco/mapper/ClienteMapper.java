package com.devsu.banco.mapper;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.dto.ClienteRequest;
import com.devsu.banco.dto.ClienteResponse;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();
        apply(request, cliente);
        return cliente;
    }

    public static void apply(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.getNombre());
        cliente.setGenero(request.getGenero());
        cliente.setEdad(request.getEdad());
        cliente.setIdentificacion(request.getIdentificacion());
        cliente.setDireccion(request.getDireccion());
        cliente.setTelefono(request.getTelefono());
        cliente.setContrasena(request.getContrasena());
        cliente.setEstado(request.getEstado());
    }

    public static ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setClienteId(cliente.getClienteId());
        response.setNombre(cliente.getNombre());
        response.setGenero(cliente.getGenero());
        response.setEdad(cliente.getEdad());
        response.setIdentificacion(cliente.getIdentificacion());
        response.setDireccion(cliente.getDireccion());
        response.setTelefono(cliente.getTelefono());
        response.setEstado(cliente.getEstado());
        return response;
    }
}
