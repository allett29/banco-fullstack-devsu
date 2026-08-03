package com.devsu.banco.config;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.domain.Movimiento;
import com.devsu.banco.domain.TipoMovimiento;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Carga los datos de los casos de uso del enunciado solo si la base está vacía.
 * Excluido en Docker (allí la semilla proviene de BaseDatos.sql) y en tests.
 */
@Component
@Profile("!test & !docker")
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    public DataLoader(ClienteRepository clienteRepository,
                      CuentaRepository cuentaRepository,
                      MovimientoRepository movimientoRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            return;
        }

        Cliente jose = guardarCliente("Jose Lema", "Masculino", 30, "1001001001",
                "Otavalo sn y principal", "098254785", "1234");
        Cliente marianela = guardarCliente("Marianela Montalvo", "Femenino", 28, "1001001002",
                "Amazonas y NNUU", "097548965", "5678");
        Cliente juan = guardarCliente("Juan Osorio", "Masculino", 32, "1001001003",
                "13 junio y Equinoccial", "098874587", "1245");

        Cuenta c478758 = guardarCuenta("478758", "Ahorro", "2000", jose);
        Cuenta c225487 = guardarCuenta("225487", "Corriente", "100", marianela);
        Cuenta c495878 = guardarCuenta("495878", "Ahorros", "0", juan);
        Cuenta c496825 = guardarCuenta("496825", "Ahorros", "540", marianela);
        guardarCuenta("585545", "Corriente", "1000", jose);

        aplicarMovimiento(c478758, TipoMovimiento.DEBITO, "575", LocalDateTime.of(2022, 2, 8, 10, 0));
        aplicarMovimiento(c225487, TipoMovimiento.CREDITO, "600", LocalDateTime.of(2022, 2, 10, 10, 0));
        aplicarMovimiento(c495878, TipoMovimiento.CREDITO, "150", LocalDateTime.of(2022, 2, 9, 10, 0));
        aplicarMovimiento(c496825, TipoMovimiento.DEBITO, "540", LocalDateTime.of(2022, 2, 8, 11, 0));
    }

    private Cliente guardarCliente(String nombre, String genero, int edad, String identificacion,
                                   String direccion, String telefono, String contrasena) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setGenero(genero);
        cliente.setEdad(edad);
        cliente.setIdentificacion(identificacion);
        cliente.setDireccion(direccion);
        cliente.setTelefono(telefono);
        cliente.setContrasena(contrasena);
        cliente.setEstado(true);
        return clienteRepository.save(cliente);
    }

    private Cuenta guardarCuenta(String numero, String tipo, String saldo, Cliente cliente) {
        BigDecimal valor = new BigDecimal(saldo);
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(numero);
        cuenta.setTipoCuenta(tipo);
        cuenta.setSaldoInicial(valor);
        cuenta.setSaldoActual(valor);
        cuenta.setEstado(true);
        cuenta.setCliente(cliente);
        return cuentaRepository.save(cuenta);
    }

    private void aplicarMovimiento(Cuenta cuenta, TipoMovimiento tipo, String monto, LocalDateTime fecha) {
        BigDecimal absoluto = new BigDecimal(monto).abs();
        BigDecimal firmado = tipo == TipoMovimiento.CREDITO ? absoluto : absoluto.negate();
        BigDecimal nuevoSaldo = cuenta.getSaldoActual().add(firmado);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(fecha);
        movimiento.setTipoMovimiento(tipo.name());
        movimiento.setValor(firmado);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuenta(cuenta);

        cuenta.setSaldoActual(nuevoSaldo);
        movimientoRepository.save(movimiento);
        cuentaRepository.save(cuenta);
    }
}
