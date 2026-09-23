# Integración GestoPago – Catálogo de productos

Integración con el servicio externo de GestoPago para obtener el catálogo de productos
(`GET /sistema/service/getProductList.do`), guardarlo en MongoDB y exponerlo por REST.
El catálogo se sincroniza todos los días a las 6:00 a.m. (hora de Ciudad de México) y también
se puede sincronizar a mano desde un endpoint.

## Flujo

```
ProductoSyncScheduler (cron 6:00)      ProductoController (REST)
            │                                   │
            └──────────────┬────────────────────┘
                           ▼
                  ProductoServiceImpl ──────────► ProductoRepository ──► MongoDB
                           │                      (gestopago_productos)
                           ▼
              GestoPagoProductIntegration  (logs de inicio/fin, traducción de errores)
                   │                 │
                   ▼                 ▼
     GestoPagoProductClient     GestoPagoProductXmlParser (JAXB)
     (Feign + Bearer Token)
                   │
                   ▼
        GestoPago getProductList.do (XML)
```

## Estructura por capas

| Capa | Clases |
|---|---|
| Configuración | `application.properties`, `GestoPagoProductFeignConfig`, `ClockConfig` |
| Client | `GestoPagoProductClient` (`@FeignClient`), `GestoPagoProductErrorDecoder` |
| Integration | `GestoPagoProductIntegration`, `GestoPagoProductXmlParser` |
| DTOs XML (JAXB) | `GestoPagoProductListResponse`, `GestoPagoMessage`, `GestoPagoProduct`, `TrimmedStringAdapter` |
| Service | `ProductoService`, `ProductoServiceImpl` |
| Persistencia | `ProductoDocument`, `ProductoRepository` (MongoDB), `ProductoMapper` (MapStruct) |
| Controller | `ProductoController`, `GestoPagoExceptionHandler` |
| Scheduler | `ProductoSyncScheduler` |
| Errores | `GestoPagoErrorCode` (enum), `GestoPagoIntegrationException` |

## Configuración

| Propiedad | Descripción |
|---|---|
| `gestopago.product.url` | URL base del servicio de GestoPago |
| `gestopago.product.token` | Bearer Token. Se lee de la variable de entorno `GESTOPAGO_PRODUCT_TOKEN` |
| `spring.cloud.openfeign.client.config.gestoPagoProduct.connect-timeout` | Timeout de conexión (ms) |
| `spring.cloud.openfeign.client.config.gestoPagoProduct.read-timeout` | Timeout de lectura (ms) |
| `gestopago.product.sync-cron` | Cron de la sincronización (por defecto `0 0 6 * * *`) |
| `gestopago.product.sync-zone` | Zona horaria del cron (por defecto `America/Mexico_City`) |
| `spring.data.mongodb.uri` | Conexión a MongoDB. Se puede cambiar con la variable `MONGODB_URI` |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/productos/sincronizacion` | Consulta GestoPago y actualiza el catálogo en MongoDB |
| `GET` | `/productos` | Regresa el catálogo guardado |
| `GET` | `/productos?servicio=Telcel` | Filtra el catálogo por servicio (sin distinguir mayúsculas) |

Swagger: `http://localhost:8081/swagger-ui.html`

## Manejo de errores

Todos los errores de la integración se convierten en una `GestoPagoIntegrationException`
con un código del enum `GestoPagoErrorCode`. El `GestoPagoExceptionHandler` responde con
`{ "codigo": "...", "mensaje": "..." }` sin exponer detalles internos.

| Código | Enum | Cuándo ocurre | HTTP |
|---|---|---|---|
| GP-001 | `AUTHENTICATION_ERROR` | GestoPago responde 401/403 (token faltante, inválido o vencido) | 502 |
| GP-002 | `CLIENT_ERROR` | GestoPago responde otro 4xx | 502 |
| GP-003 | `SERVICE_UNAVAILABLE` | GestoPago responde 5xx | 503 |
| GP-004 | `TIMEOUT` | Se agota el timeout de conexión o de lectura | 504 |
| GP-005 | `COMMUNICATION_ERROR` | Error de red (conexión rechazada, DNS, etc.) | 502 |
| GP-006 | `INVALID_RESPONSE` | El cuerpo no es XML válido o no trae `MENSAJE` | 502 |
| GP-007 | `BUSINESS_ERROR` | El XML trae `CODIGO` distinto de `01` | 502 |
| GP-008 | `EMPTY_CATALOG` | GestoPago responde sin productos | 502 |

Cuando la sincronización programada falla, el scheduler registra el código de error en el log y
el catálogo guardado en MongoDB se queda como estaba.

## Registro y monitoreo

- `GestoPagoProductIntegration` registra el inicio y el fin de cada invocación, con la duración
  y el número de productos, o con el código de error si falló.
- Los logs nunca incluyen el token ni el cuerpo de la respuesta.
- El scheduler registra el inicio y el fin de cada sincronización programada.

## Decisiones técnicas

- **OpenFeign en lugar de RestTemplate**: es la convención del proyecto (`GestoPagoAuthClient`).
- **Configuración de Feign exclusiva del cliente**: `GestoPagoProductFeignConfig` no lleva
  `@Configuration`. Así el interceptor del Bearer Token y el `ErrorDecoder` solo aplican a este
  cliente y no a `GestoPagoAuthClient`.
- **Token desde la configuración**: se lee de `gestopago.product.token`, que toma su valor de
  la variable de entorno `GESTOPAGO_PRODUCT_TOKEN`. No queda escrito en el código ni en el repositorio.
- **El cliente regresa `byte[]`**: JAXB lee el `encoding` que declara el propio XML y los
  caracteres como `¡` o `“` no se corrompen si el servidor no manda el charset.
- **JAXB seguro**: el parser deshabilita DTDs y entidades externas para prevenir ataques XXE.
  El `JAXBContext` se crea una sola vez porque es costoso y se puede compartir entre hilos.
- **Detección de respuestas que no son XML**: si GestoPago responde con JSON (como en el error de
  autenticación), se reporta `INVALID_RESPONSE` en lugar de un error confuso de JAXB.
- **Sincronización sin dejar el catálogo vacío**: los productos se guardan con `idProducto` como
  `_id` (upsert) y con la fecha de sincronización. Después se eliminan solo los productos que no
  vinieron en la nueva respuesta (`fechaSincronizacion` anterior). Si GestoPago falla o responde
  vacío, MongoDB no se toca.
- **Fecha truncada a milisegundos**: MongoDB guarda las fechas con precisión de milisegundos. Sin
  truncar, los productos recién guardados se verían "anteriores" a la fecha de sincronización y se
  borrarían.
- **`Clock` inyectado**: permite probar la lógica de fechas con un reloj fijo.
- **Inyección por constructor** en todas las clases nuevas y en las existentes que se tocaron.

## Migración de PostgreSQL a MongoDB

El proyecto ya no usa PostgreSQL: MongoDB es la única base de datos.

| Antes (PostgreSQL / JPA) | Ahora (MongoDB) |
|---|---|
| Tabla `personas` (`entity/sf/Personas`) | Colección `personas` (`document/sf/Personas`) |
| Tabla `gestopago_tokens` (`entity/gestopago/GestoPagoToken`) | Colección `gestopago_tokens` (`document/gestopago/GestoPagoToken`) |
| `JpaRepository` | `MongoRepository` (mismos métodos de consulta) |
| Llave `Integer` autoincremental | `_id` de tipo `String` (ObjectId de MongoDB) |
| `@PrePersist` / `@PreUpdate` para las fechas | `@CreatedDate` / `@LastModifiedDate` con `@EnableMongoAuditing` (`MongoConfig`) |
| Restricción `UNIQUE (id_distribuidor, codigo_dispositivo)` | `@CompoundIndex(unique = true)` + `spring.data.mongodb.auto-index-creation=true` |
| `ConfigDB`, `FlywayConfig`, `V1__create_gestopago_tokens.sql` | Eliminados: Spring Boot configura MongoDB con `spring.data.mongodb.uri` |

Dependencias eliminadas de `build.gradle`: `spring-boot-starter-data-jpa`, `postgresql`, `flyway-core`,
`flyway-database-postgresql`, `hibernate-core`, `ojdbc8`, `ucp` y `h2`.

## Correcciones al proyecto existente

- `build.gradle`: Lombok 1.18.26 → 1.18.44. La versión anterior falla al compilar con JDK 21 o
  superior (`TypeTag :: UNKNOWN`).
- `build.gradle` + `settings.gradle`: toolchain de Java 17 con el plugin `foojay-resolver-convention`.
  Gradle compila, prueba y ejecuta con Java 17 aunque IntelliJ use otro JDK, y lo descarga solo si
  no está instalado.
- `build.gradle`: se eliminó la dependencia de Lombok duplicada como `implementation`, se
  unificaron las versiones de `jjwt` (había 0.12.6 y 0.11.5 mezcladas), se corrigió el bloque
  `jacocoTestReport` y se fijó la codificación UTF-8 al compilar.
- `OpenApi`: le faltaba `@Configuration`, así que su `@Bean` nunca se registraba.
- `PersonaController` y `PersonasServiceImpl`: inyección por constructor, se quitó un import de
  `java.awt` que no se usaba y se renombraron los métodos duplicados `actualizUser`.

## Cómo compilar y probar

Requisitos: solo MongoDB corriendo en `localhost:27017`. No hace falta instalar Java 17: Gradle lo
descarga automáticamente la primera vez (toolchain). Solo se necesita internet en esa primera compilación.
Las colecciones (`personas`, `gestopago_tokens`, `gestopago_productos`) se crean solas.

```bash
# Compilar
gradlew.bat compileJava

# Pruebas unitarias
gradlew.bat test

# Levantar la aplicación (Windows, CMD)
set GESTOPAGO_PRODUCT_TOKEN=<token>
gradlew.bat bootRun
```

Pruebas unitarias incluidas (37 casos):

- `ProductoServiceImplTest`: sincronización exitosa, truncado de fecha, descarte de productos sin
  id, catálogo vacío, cada error de integración (MongoDB no se toca) y consultas.
- `GestoPagoProductIntegrationTest`: respuesta exitosa, código de negocio distinto de `01`,
  respuesta sin `MENSAJE`, error de autenticación, timeout y conexión rechazada.
- `GestoPagoProductXmlParserTest`: mapeo de atributos y CDATA, JSON, XML malformado, cuerpo vacío y XXE.
- `GestoPagoProductErrorDecoderTest`: mapeo de estatus HTTP a código de error.
- `GestoPagoExceptionHandlerTest`: estatus HTTP y cuerpo de respuesta por código de error.
