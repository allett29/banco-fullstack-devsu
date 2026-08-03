# Banco - API y Frontend Full Stack

Aplicacion bancaria con API REST en Java Spring Boot y frontend en Angular. Permite la gestion de clientes, cuentas, movimientos y reportes de estado de cuenta.

## Stack

- Backend: Java 17, Spring Boot 3, JPA, PostgreSQL
- Frontend: Angular 18, Jest, CSS propio
- Contenedores: Docker Compose
- Validacion de API: coleccion Postman incluida

## Requisitos

- Docker Desktop
- Postman (recomendado 9.13.2 o superior)

## Despliegue con Docker

Desde la raiz del repositorio:

```bash
docker compose up --build
```

Servicios disponibles:

| Servicio  | URL |
|-----------|-----|
| Frontend  | http://localhost:4200 |
| Backend   | http://localhost:8080/api |
| PostgreSQL| localhost:5432 |

Credenciales de base de datos:

- Base de datos: `banco`
- Usuario: `banco`
- Password: `banco`

Para detener:

```bash
docker compose down
```

Para reiniciar limpiando volumenes (incluye datos):

```bash
docker compose down -v
docker compose up --build
```

## Endpoints principales

| Metodo | Ruta |
|--------|------|
| GET, POST, PUT, PATCH, DELETE | `/api/clientes` |
| GET, POST, PUT, PATCH, DELETE | `/api/cuentas` |
| GET, POST, PUT, PATCH, DELETE | `/api/movimientos` |
| GET | `/api/reportes?clienteId={id}&fechaInicio=yyyy-MM-dd&fechaFin=yyyy-MM-dd` |

El reporte retorna JSON con totales de creditos/debitos, detalle de movimientos y el PDF codificado en base64 (`pdfBase64`).

## Validacion con Postman

1. Importar `postman/Banco_API.postman_collection.json`
2. Configurar la variable `baseUrl` en `http://localhost:8080/api`
3. Ejecutar las peticiones de la coleccion

## Script de base de datos

El archivo `BaseDatos.sql` contiene el esquema y datos de ejemplo. En Docker se carga automaticamente al inicializar PostgreSQL.

## Ejecucion local (opcional, sin Docker)

### Backend

```bash
cd backend
mvn spring-boot:run
```

Requiere PostgreSQL disponible en `localhost:5432` con usuario/clave/db `banco`.

### Frontend

```bash
cd frontend
npm install
npm start
```

La aplicacion queda en `http://localhost:4200`.

## Pruebas

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

## Estructura del proyecto

```
.
├── BaseDatos.sql
├── docker-compose.yml
├── backend/
├── frontend/
└── postman/
```

## Reglas de negocio relevantes

- Creditos se almacenan con valor positivo y debitos con valor negativo.
- Cada movimiento guarda el saldo disponible resultante.
- Debito con saldo insuficiente o cero retorna: `Saldo no disponible`
- Limite diario de retiro: `1000`
- Si se excede el cupo diario retorna: `Cupo diario Excedido`
