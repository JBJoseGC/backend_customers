# backend_customers

Microservicio **customers** del Reto 004.
Permite registrar clientes, consultarlos con o sin filtros y obtener indicadores de nacimientos.

## Stack

| Componente | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.5 (Web, Data JPA, Validation) |
| Base de datos | H2 en memoria |
| Build | Maven |
| Documentación API | springdoc-openapi (Swagger UI) |
| Pruebas | JUnit 5 + Mockito + AssertJ |

## Arquitectura hexagonal

```
src/main/java/com/indigital/customers
├── domain                      # Núcleo: sin dependencias de frameworks
│   ├── model                   # Customer, CustomerFilter, CustomerIndicators, ...
│   ├── exception               # BusinessException, CustomerAlreadyExistsException
│   └── port
│       ├── in                  # Casos de uso (CreateCustomer, FindCustomers, GetCustomerIndicators)
│       └── out                 # CustomerRepositoryPort
├── application
│   └── service                 # CustomerService: implementa los casos de uso
└── infrastructure
    ├── adapter
    │   ├── in/rest             # Controller, DTOs, mapper, manejo global de errores
    │   └── out/persistence     # Entidad JPA, repositorio Spring Data, adaptador del puerto
    └── config                  # CORS, Clock
```

El controller solo conoce los puertos de entrada; el servicio solo conoce el puerto de salida.
La persistencia (H2/JPA) puede reemplazarse por otra base de datos implementando `CustomerRepositoryPort`.

## Requisitos

- JDK 17 o superior
- Maven 3.8+

## Instalación y ejecución

```bash
git clone <url-del-repositorio>
cd backend_customers
mvn clean spring-boot:run
```

El servicio queda disponible en `http://localhost:8080`.
Al iniciar se cargan 12 clientes de ejemplo (`src/main/resources/data.sql`).

Otras opciones:

```bash
mvn clean package
java -jar target/backend_customers-1.0.0.jar
```

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Consola H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:customersdb`, usuario `sa`, sin clave) |

> Al ser una base de datos en memoria, la información se reinicia con cada arranque.

## Endpoints

Base: `http://localhost:8080/api/v1/customers`

### 1. Crear cliente — `POST /api/v1/customers`

```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@mail.com",
  "dni": "70123456",
  "birthDate": "1993-04-18"
}
```

La **fecha de creación** la asigna el servicio. Respuesta `201 Created`:

```json
{
  "id": 13,
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@mail.com",
  "dni": "70123456",
  "birthDate": "1993-04-18",
  "createdAt": "2026-10-14T18:30:00"
}
```

Reglas de validación:

| Regla | Código |
|---|---|
| Campos obligatorios, email válido, DNI de 8 dígitos, fecha de nacimiento pasada | `400` |
| DNI o email ya registrado | `409` |
| Fecha de nacimiento futura (regla de negocio) | `422` |

### 2. Consultar clientes — `GET /api/v1/customers`

| Parámetro | Descripción |
|---|---|
| `dni` (opcional) | Coincidencia exacta |
| `email` (opcional) | Coincidencia exacta, sin distinguir mayúsculas |

Sin parámetros devuelve todos los clientes (los más recientes primero). Si se envían ambos, se combinan con AND.

```
GET /api/v1/customers
GET /api/v1/customers?dni=45871236
GET /api/v1/customers?email=maria.torres@mail.com
```

### 3. Consultar indicadores — `GET /api/v1/customers/indicators`

```json
{
  "totalCustomers": 12,
  "birthsByMonthYear": [
    { "year": 1990, "month": 1, "period": "Enero 1990", "total": 3 }
  ],
  "highestBirthMonthYear": [
    { "year": 1990, "month": 1, "period": "Enero 1990", "total": 3 }
  ],
  "lowestBirthMonthYear": [
    { "year": 1985, "month": 12, "period": "Diciembre 1985", "total": 1 }
  ],
  "birthRateByMonth": [
    { "month": 1, "monthName": "Enero", "total": 3, "rate": 25.00 }
  ]
}
```

| Indicador | Cálculo |
|---|---|
| `birthsByMonthYear` | Cantidad de clientes nacidos por cada mes/año, en orden cronológico |
| `highestBirthMonthYear` | Mes/año con mayor cantidad de nacidos (lista: incluye todos los empates) |
| `lowestBirthMonthYear` | Mes/año con menor cantidad de nacidos (lista: incluye todos los empates) |
| `birthRateByMonth` | Tasa de natalidad de cada mes calendario (Enero–Diciembre): `nacidos en el mes / total de clientes × 100`, con 2 decimales |

## Postman

Importar `postman/customers.postman_collection.json`. La variable `baseUrl` apunta a `http://localhost:8080/api/v1`.

## Pruebas unitarias

Las pruebas cubren la capa de servicio (`CustomerService`), con el puerto de persistencia simulado con Mockito:

- creación válida (normalización de datos y fecha de creación)
- DNI duplicado, email duplicado y fecha de nacimiento futura
- consulta sin filtros y con filtros
- cálculo de indicadores y caso sin clientes

```bash
mvn test
```

## CORS

Por defecto se permite el origen `http://localhost:4200` (frontend Angular). Se puede cambiar en `application.yml` (`app.cors.allowed-origins`).
