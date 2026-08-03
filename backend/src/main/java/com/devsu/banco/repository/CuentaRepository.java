package com.devsu.banco.repository;

import com.devsu.banco.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteClienteId(Long clienteId);

    List<Cuenta> findByNumeroCuentaContainingIgnoreCaseOrTipoCuentaContainingIgnoreCase(
            String numeroCuenta, String tipoCuenta);
}
