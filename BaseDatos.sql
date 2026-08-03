-- BaseDatos.sql
-- Script de esquema y datos - Banco Full-Stack (Devsu)

DROP TABLE IF EXISTS movimientos CASCADE;
DROP TABLE IF EXISTS cuentas CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;

CREATE TABLE clientes (
    cliente_id      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    genero          VARCHAR(50)  NOT NULL,
    edad            INTEGER      NOT NULL,
    identificacion  VARCHAR(50)  NOT NULL UNIQUE,
    direccion       VARCHAR(255) NOT NULL,
    telefono        VARCHAR(50)  NOT NULL,
    contrasena      VARCHAR(100) NOT NULL,
    estado          BOOLEAN      NOT NULL
);

CREATE TABLE cuentas (
    id              BIGSERIAL PRIMARY KEY,
    numero_cuenta   VARCHAR(50)    NOT NULL UNIQUE,
    tipo_cuenta     VARCHAR(50)    NOT NULL,
    saldo_inicial   NUMERIC(19, 2) NOT NULL,
    saldo_actual    NUMERIC(19, 2) NOT NULL,
    estado          BOOLEAN        NOT NULL,
    cliente_id      BIGINT         NOT NULL REFERENCES clientes (cliente_id)
);

CREATE TABLE movimientos (
    id               BIGSERIAL PRIMARY KEY,
    fecha            TIMESTAMP     NOT NULL,
    tipo_movimiento  VARCHAR(20)   NOT NULL,
    valor            NUMERIC(19, 2) NOT NULL,
    saldo            NUMERIC(19, 2) NOT NULL,
    cuenta_id        BIGINT        NOT NULL REFERENCES cuentas (id)
);

-- 1. Creación de Usuarios (Clientes)
INSERT INTO clientes (nombre, genero, edad, identificacion, direccion, telefono, contrasena, estado)
VALUES
    ('Jose Lema', 'Masculino', 30, '1001001001', 'Otavalo sn y principal', '098254785', '1234', TRUE),
    ('Marianela Montalvo', 'Femenino', 28, '1001001002', 'Amazonas y NNUU', '097548965', '5678', TRUE),
    ('Juan Osorio', 'Masculino', 32, '1001001003', '13 junio y Equinoccial', '098874587', '1245', TRUE);

-- 2 y 3. Creación de Cuentas
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id)
VALUES
    ('478758', 'Ahorro', 2000, 2000, TRUE, (SELECT cliente_id FROM clientes WHERE nombre = 'Jose Lema')),
    ('225487', 'Corriente', 100, 100, TRUE, (SELECT cliente_id FROM clientes WHERE nombre = 'Marianela Montalvo')),
    ('495878', 'Ahorros', 0, 0, TRUE, (SELECT cliente_id FROM clientes WHERE nombre = 'Juan Osorio')),
    ('496825', 'Ahorros', 540, 540, TRUE, (SELECT cliente_id FROM clientes WHERE nombre = 'Marianela Montalvo')),
    ('585545', 'Corriente', 1000, 1000, TRUE, (SELECT cliente_id FROM clientes WHERE nombre = 'Jose Lema'));

-- 4. Movimientos de ejemplo (valores firmados: crédito +, débito -)
-- Retiro 575 de 478758 -> saldo 1425
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-08 10:00:00', 'DEBITO', -575, 1425,
        (SELECT id FROM cuentas WHERE numero_cuenta = '478758'));
UPDATE cuentas SET saldo_actual = 1425 WHERE numero_cuenta = '478758';

-- Depósito 600 en 225487 -> saldo 700
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-10 10:00:00', 'CREDITO', 600, 700,
        (SELECT id FROM cuentas WHERE numero_cuenta = '225487'));
UPDATE cuentas SET saldo_actual = 700 WHERE numero_cuenta = '225487';

-- Depósito 150 en 495878 -> saldo 150
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-09 10:00:00', 'CREDITO', 150, 150,
        (SELECT id FROM cuentas WHERE numero_cuenta = '495878'));
UPDATE cuentas SET saldo_actual = 150 WHERE numero_cuenta = '495878';

-- Retiro 540 de 496825 -> saldo 0
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-08 11:00:00', 'DEBITO', -540, 0,
        (SELECT id FROM cuentas WHERE numero_cuenta = '496825'));
UPDATE cuentas SET saldo_actual = 0 WHERE numero_cuenta = '496825';
