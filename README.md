# backend_customers

Microservicio **customers** del Reto 004.

Permite registrar clientes, consultarlos (todos o filtrando por DNI / email) y obtener algunos indicadores sobre sus fechas de nacimiento.

## Tecnologías

- Java 17
- Spring Boot 3
- Maven
- H2 (base de datos en memoria)
- JUnit 5 y Mockito

El proyecto está organizado con arquitectura hexagonal:

- `domain`: modelos, excepciones y puertos (interfaces)
- `application`: servicio con la lógica de negocio
- `infrastructure`: controller REST, persistencia con JPA y configuración

## Cómo ejecutarlo

Se necesita JDK 17 o superior y Maven.

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080` y carga algunos clientes de prueba al iniciar.
Como la base de datos es en memoria, los datos se reinician cada vez que se levanta el servicio.

- Swagger: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (URL `jdbc:h2:mem:customersdb`, usuario `sa`, sin contraseña)

## Endpoints

**Crear cliente**

```
POST /api/v1/customers
```

```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@mail.com",
  "dni": "70123456",
  "birthDate": "1993-04-18"
}
```

La fecha de creación se asigna automáticamente. Se valida que los campos sean obligatorios, que el DNI tenga 8 dígitos, que el email sea válido y que no exista otro cliente con el mismo DNI o email.

**Consultar clientes**

```
GET /api/v1/customers
GET /api/v1/customers?dni=45871236
GET /api/v1/customers?email=maria.torres@mail.com
```

Sin parámetros devuelve todos los clientes.

**Consultar indicadores**

```
GET /api/v1/customers/indicators
```

Devuelve:

- cantidad de clientes nacidos por mes/año
- mes/año con mayor cantidad de nacidos
- mes/año con menor cantidad de nacidos
- tasa de natalidad de cada mes (nacidos en el mes / total de clientes × 100)

## Postman

La colección está en `postman/customers.postman_collection.json`.

## Pruebas

Las pruebas unitarias son del servicio `CustomerService`.

```bash
mvn test
```
