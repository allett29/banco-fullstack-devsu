package com.devsu.banco.repository;

import com.devsu.banco.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);

    boolean existsByIdentificacion(String identificacion);

    List<Cliente> findByNombreContainingIgnoreCaseOrIdentificacionContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
            String nombre, String identificacion, String telefono);
}
