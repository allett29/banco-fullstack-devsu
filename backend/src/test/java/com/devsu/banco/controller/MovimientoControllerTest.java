package com.devsu.banco.controller;

import com.devsu.banco.domain.Cliente;
import com.devsu.banco.domain.Cuenta;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    private Long cuentaConSaldoId;
    private Long cuentaSinSaldoId;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Test");
        cliente.setGenero("Masculino");
        cliente.setEdad(30);
        cliente.setIdentificacion("9999999999");
        cliente.setDireccion("Calle 1");
        cliente.setTelefono("0999999999");
        cliente.setContrasena("1234");
        cliente.setEstado(true);
        cliente = clienteRepository.save(cliente);

        Cuenta conSaldo = new Cuenta();
        conSaldo.setNumeroCuenta("100001");
        conSaldo.setTipoCuenta("Ahorros");
        conSaldo.setSaldoInicial(new BigDecimal("5000"));
        conSaldo.setSaldoActual(new BigDecimal("5000"));
        conSaldo.setEstado(true);
        conSaldo.setCliente(cliente);
        cuentaConSaldoId = cuentaRepository.save(conSaldo).getId();

        Cuenta sinSaldo = new Cuenta();
        sinSaldo.setNumeroCuenta("100002");
        sinSaldo.setTipoCuenta("Corriente");
        sinSaldo.setSaldoInicial(BigDecimal.ZERO);
        sinSaldo.setSaldoActual(BigDecimal.ZERO);
        sinSaldo.setEstado(true);
        sinSaldo.setCliente(cliente);
        cuentaSinSaldoId = cuentaRepository.save(sinSaldo).getId();
    }

    @Test
    void crearCredito_ok() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("tipoMovimiento", "CREDITO");
        body.put("valor", 600);
        body.put("cuentaId", cuentaConSaldoId);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(600))
                .andExpect(jsonPath("$.saldo").value(5600))
                .andExpect(jsonPath("$.tipoMovimiento").value("CREDITO"));
    }

    @Test
    void crearDebitoSinSaldo_retornaSaldoNoDisponible() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("tipoMovimiento", "DEBITO");
        body.put("valor", 100);
        body.put("cuentaId", cuentaSinSaldoId);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void crearDebitoExcedeCupoDiario_retornaCupoExcedido() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("tipoMovimiento", "DEBITO");
        body.put("valor", 1001);
        body.put("cuentaId", cuentaConSaldoId);

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cupo diario Excedido"));
    }
}
