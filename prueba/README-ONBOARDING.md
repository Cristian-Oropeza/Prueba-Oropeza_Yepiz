# Onboarding Clientes Personas Fisicas

API REST en Spring Boot 3.3 + Java 17 + PostgreSQL 16 + JPA/Hibernate + Flyway + JWT (opcional).

## Correr en local (Docker)

```bash
# 1. Postgres
docker compose up -d

# 2. App
./gradlew bootRun            # arranca en http://localhost:8081
```

Swagger: <http://localhost:8081/swagger-ui.html>
Health: <http://localhost:8081/actuator/health>

## Deploy en Railway

1. Push del repo a GitHub.
2. Railway → New Project → Deploy from GitHub → agregar plugin PostgreSQL.
3. Variables de entorno (Railway ya crea `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`):
   ```
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=<64+ chars>
   PORT=8080
   ```
4. Railway detecta el `Dockerfile` y compila.

## JWT

Por default `app.security.jwt.enabled=false` → todos los endpoints abiertos, el header `Authorization: Bearer <token>` se parsea si viene pero no bloquea. Cuando el profe lo pida obligatorio: `JWT_ENABLED=true`.

## Payload de prueba (alta de cliente)

```json
POST /api/v1/clientes
{
  "nombre": "Juan",
  "segundoNombre": "Carlos",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Lopez",
  "fechaNacimiento": "1995-04-12",
  "curp": "PELJ950412HDFRPN00",
  "rfc": "PELJ950412ABC",
  "sexo": "MASCULINO",
  "nacionalidad": "Mexicana",
  "estadoCivil": "SOLTERO",
  "correo": "juan.perez@example.com",
  "telefonoMovil": "5512345678",
  "telefonoAlterno": null,
  "ocupacion": "Ingeniero",
  "empresa": "Acme SA",
  "ingresoMensual": 25000.00,
  "password": "MiP@ssw0rd!",
  "domicilio": {
    "calle": "Av. Reforma",
    "numeroExterior": "123",
    "numeroInterior": "4B",
    "colonia": "Juarez",
    "municipio": "Cuauhtemoc",
    "estado": "CDMX",
    "codigoPostal": "06600",
    "pais": "Mexico"
  }
}
```

## Legacy GestoPago

El codigo original (`client/`, `document/`, `controller/Producto*`, etc.) se conserva intacto pero no arranca por default (`gestopago.enabled=false`). Para revivirlo: `GESTOPAGO_ENABLED=true` + `MONGODB_URI`.
