| | |
|---|---|
| **Nombre** | Nicolás Cavieres |
| **Carrera** | Analista Programador |
| **Fecha** | 03/10/2026 |
| **Profesor** | Alonso Castillo |
| **Curso** | Backend III |

---

# Banco XYZ — Microservicios resilientes y seguros en la nube con Spring Cloud

Implementación del caso **"Desarrollando microservicios y resiliencia en la nube con Spring Cloud"** (Exp 3, Semana 8) sobre el sistema bancario simulado **Banco XYZ**, a partir de los datos legacy de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data).

El sistema evoluciona la arquitectura BFF de la Semana 6 añadiendo los componentes requeridos para un entorno **Cloud resiliente y seguro**:

1. **OAuth 2.0** (Spring Authorization Server) para proteger datos y servicios.
2. **Docker** + **Docker Compose** para la orquestación completa.
3. **Resilience4j** (Circuit Breaker, Retry y Fallback) para tolerancia a fallos.
4. **Mensajería asíncrona con Kafka** (arquitectura orientada a eventos).

---

## 1. Arquitectura

```
                    ┌───────────────────────────┐
                    │  auth-server (OAuth 2.0)  │  Authorization Server (port 9000)
                    └────────────┬──────────────┘
        client_credentials (token JWT) │  /oauth2/jwks
      ┌────────────────────────────────┼──────────────────────────────┐
      ▼                                ▼                              ▼
 Archivos CSV (bank_legacy_data)   backend-core  ─────────────►  Kafka (broker)
        │                          (HTTPS :8081)   publica "retiros"       │
        ▼                                ▲                               ▼
   banco-batch (Spring Batch)            │ RestClient + relay        notificaciones-service
        │                          ┌─────┴─────┬──────────┐          (consumer :8085)
        ▼                          ▼           ▼          ▼
      MySQL 8.4                 bff-web    bff-mobile   bff-atm
     (banco_xyz)                (8082)      (8083)       (8084)
```

- Los **BFFs no acceden a la base de datos**: consumen `backend-core` mediante HTTPS con *token relay*, protegidos por **Resilience4j** (Circuit Breaker + Retry + Fallback).
- La **seguridad** se delega al **Authorization Server** (OAuth 2.0): cada canal usa su propio *cliente* (`client_credentials`) y recibe un token con el rol del canal.
- **Kafka** habilita la arquitectura orientada a eventos: `backend-core` publica el evento `retiros` y `notificaciones-service` lo consume de forma asíncrona.

---

## 2. Responsabilidades

| Componente | Puerto | Protocolo | Responsabilidad |
|---|---|---|---|
| `auth-server` | 9000 | HTTP | Authorization Server OAuth 2.0: emite tokens JWT (clave RSA) y expone `/oauth2/jwks` |
| `banco-batch` | 8080 | HTTP (interno) | Lee los CSV, limpia datos y los persiste en MySQL (Spring Batch) |
| `backend-core` | 8081 | HTTPS | Reglas de negocio + JPA; resource server OAuth 2.0; productor Kafka del evento `retiros` |
| `bff-web` | 8082 | HTTPS | Datos completos + resumen (rol `ROLE_WEB`) |
| `bff-mobile` | 8083 | HTTPS | Respuestas ligeras + últimos 5 movimientos (rol `ROLE_MOBILE`) |
| `bff-atm` | 8084 | HTTPS | Consulta de saldo y retiro (rol `ROLE_ATM`) |
| `notificaciones-service` | 8085 | HTTP | Consumidor Kafka de eventos `retiros` |
| MySQL 8.4 | 3307 | — | Persistencia (`banco_xyz`) |
| Kafka + Zookeeper | 29092 / 2181 | — | Broker de mensajería asíncrona |

---

## 2.1 Detalle de cada componente

### `auth-server` (Authorization Server OAuth 2.0)
- Basado en **Spring Authorization Server** (1.3.4).
- Emite **access tokens JWT firmados con clave RSA** y expone los endpoints `/.well-known/openid-configuration`, `/oauth2/jwks` y `/oauth2/token`.
- Registra **un cliente por canal** (`client_credentials`): `bff-web-client`, `bff-mobile-client`, `bff-atm-client`.
- El token incluye los claims `authorities` (rol del canal) y `channel`, que los resource servers usan para autorizar.

### `common` (librería compartida)
- `OAuth2TokenClient`: cliente OAuth 2.0 que obtiene el token (HTTP Basic + `client_credentials`).
- `SecuritySupportConfig`: `JwtDecoder` que valida tokens contra el JWK Set del Authorization Server.
- `JwtAuthConverter`: mapea el claim `authorities` a roles de Spring Security.
- `SslRestClientFactory`: `RestClient` con TLS (confía en el certificado autofirmado) y timeouts explícitos.
- `TokenRelay`: reenvía el token JWT del contexto hacia `backend-core`.
- DTOs compartidos (login, token OAuth2, contratos con core).

### `banco-batch` (Spring Batch)
- Lee los CSV de `bank_legacy_data` (`semana_1/2/3`), los **limpia** (fechas, montos, edades, tipos, duplicados) y los **persiste** en MySQL.
- Escritura idempotente (`INSERT IGNORE`), con `retry` para fallas transitorias y `skip` para registros inválidos.
- Se ejecuta automáticamente al arrancar (`BATCH_RUN_ON_STARTUP=true`).

### `backend-core` (lógica de negocio)
- Expone `GET /api/cuentas`, `GET /api/cuentas/{id}`, `GET /api/transacciones/cuenta/{id}` y `POST /api/cuentas/{id}/retiros` (solo `ROLE_ATM`).
- **Resource server** OAuth 2.0 (valida el token vía JWK Set).
- **Productor Kafka**: publica `RetiroEvent` en el topic `retiros` tras cada retiro aprobado.

### `bff-web`, `bff-mobile`, `bff-atm` (Backend for Frontend)
- Cada canal expone su API especializada y consume `backend-core` por HTTPS con *token relay*.
- Son **resource servers** y, a la vez, **clientes OAuth 2.0** (obtienen el token del canal en `/api/auth/login`).
- Aplican **Resilience4j** (Circuit Breaker + Retry + Fallback) sobre las llamadas a core.
- `bff-web`: datos completos + resumen. `bff-mobile`: datos ligeros + últimos 5 movimientos. `bff-atm`: saldo y retiro (fallback seguro = `RECHAZADO`).

### `notificaciones-service` (consumidor Kafka)
- Consume el topic `retiros` (ack manual, `earliest`) y acumula los eventos en memoria.
- Expone `GET /api/notificaciones` para evidenciar el flujo asíncrono productor → consumidor.

### Infraestructura (Docker Compose)
- **MySQL 8.4** (persistencia), **Kafka + Zookeeper** (mensajería), **Authorization Server**, **banco-batch**, **backend-core**, **3 BFFs** y **notificaciones-service**, todo orquestado en `docker-compose.yml`.

---

## 3. Seguridad (OAuth 2.0)

- **Authorization Server** (`auth-server`) basado en **Spring Authorization Server**, que centraliza la emisión de *access tokens* (JWT firmados con clave RSA).
- **Resource Server** en `backend-core` y los 3 BFFs: validan el token contra el JWK Set (`/oauth2/jwks`) antes de servir cada solicitud.
- **Flujo `client_credentials`**: cada BFF es un cliente OAuth 2.0 con credenciales propias y obtiene un token cuyo claim `authorities` contiene el rol del canal.

### Clientes registrados (por canal)

| Canal | client_id | client_secret | Rol emitido |
|---|---|---|---|
| Web | `bff-web-client` | `web-secret` | `ROLE_WEB` |
| Mobile | `bff-mobile-client` | `mobile-secret` | `ROLE_MOBILE` |
| ATM | `bff-atm-client` | `atm-secret` | `ROLE_ATM` |

### Verificación de la autorización

| Prueba | Resultado esperado |
|---|---|
| Sin token → endpoint protegido | `401` |
| Token Web → endpoint Mobile/ATM | `403` |
| Token Mobile → endpoint ATM | `403` |
| Token Web → retiro en core | `403` (requiere `ROLE_ATM`) |
| client_secret incorrecto en el token endpoint | `401` |

---

## 4. Tolerancia a fallos (Resilience4j)

Cada BFF aplica **Circuit Breaker + Retry + Fallback** sobre las llamadas a `backend-core` (el consumidor es quien asegura el mecanismo):

- **Circuit Breaker**: abre el circuito cuando `backend-core` falla repetidamente, evitando cascadas.
- **Retry**: reintenta fallas transitorias (hasta 3 intentos).
- **Fallback**: respuestas alternativas:
  - Web/Mobile → listado vacío o detalle "No disponible".
  - ATM `retiro` → estado `RECHAZADO` (un cajero nunca dispensa sin confirmar el saldo).

Configuración en `application.properties` (`resilience4j.circuitbreaker.instances.backendCore.*` y `resilience4j.retry.instances.backendCore.*`).

---

## 5. Mensajería asíncrona (Kafka)

- **Productor**: `backend-core` publica el evento `RetiroEvent {cuentaId, monto, estado, fecha}` en el topic `retiros` tras cada retiro aprobado.
- **Consumidor**: `notificaciones-service` consume el topic `retiros` (ack manual, `earliest`) y expone `GET /api/notificaciones` con los eventos recibidos.
- **Broker**: Kafka + Zookeeper (imágenes Confluent) orquestados en el `docker-compose.yml`.

---

## 6. Estructura de proyecto

```
S3_Bank_BFF/
├── pom.xml                     # Módulo padre (agregador Maven)
├── docker-compose.yml          # Orquestación completa (BD + Kafka + 7 servicios)
├── keystore.p12                # Certificado autofirmado (HTTPS en desarrollo)
├── common/                     # Librería compartida: OAuth2 client, JWT decoder, SSL, Resilience4j, DTOs
├── auth-server/                # Authorization Server OAuth 2.0
├── backend-core/               # Reglas de negocio + JPA + resource server + productor Kafka
├── banco-batch/                # Spring Batch (limpieza y carga de datos)
├── bff-web/                    # BFF canal Web
├── bff-mobile/                 # BFF canal Móvil
├── bff-atm/                    # BFF canal Cajeros Automáticos
├── notificaciones-service/     # Consumidor Kafka de eventos de retiro
├── postman/                    # Colección Postman para pruebas
├── imgs/                       # Evidencias de ejecución (informe)
└── scripts/
    ├── deploy.sh               # Despliegue completo con Docker
    ├── evidencia.sh            # Generación de evidencia por línea de comandos
    └── generar_imagenes.py     # Generador de imágenes del informe
```

---

## 7. Tecnologías y requisitos previos

### Tecnologías

- **Java 21**, **Spring Boot 3.3.5**, Maven.
- **Spring Authorization Server 1.3.4** (OAuth 2.0), **Spring Security OAuth2 Resource Server (JWT)**.
- **Resilience4j 2.2.0** (Circuit Breaker, Retry, Fallback).
- **Spring Kafka** (productor/consumidor), **Kafka + Zookeeper (Confluent 7.4.4)**.
- **Spring Data JPA**, **Spring Batch**, **MySQL 8.4**, `RestClient`, **HTTPS/TLS** (certificado autofirmado).

### Requisitos previos

- **Docker** + **Docker Compose**.
- Postman (para las pruebas — ver sección 9).

> Java y Maven solo son necesarios si se desea compilar fuera de Docker; `docker compose` compila dentro de los contenedores.

---

## 8. Puesta en marcha

```bash
./scripts/deploy.sh
# equivale a: docker compose up -d --build
```

Al terminar:

```bash
docker compose ps
docker logs banco-bff-batch       # carga de datos (estado COMPLETED)
```

| Contenedor | Servicio | Puerto |
|---|---|---|
| `banco-xyz-mysql` | MySQL 8.4 | 3307 |
| `banco-xyz-zookeeper` | Zookeeper | 2181 |
| `banco-xyz-kafka` | Kafka | 29092 |
| `banco-bff-auth-server` | Authorization Server | 9000 |
| `banco-bff-batch` | Proceso batch | 8080 |
| `banco-bff-core` | Backend Core | 8081 (HTTPS) |
| `banco-bff-web` | BFF Web | 8082 (HTTPS) |
| `banco-bff-mobile` | BFF Mobile | 8083 (HTTPS) |
| `banco-bff-atm` | BFF ATM | 8084 (HTTPS) |
| `banco-bff-notificaciones` | Notificaciones (Kafka consumer) | 8085 |

Detener el entorno:

```bash
docker compose down       # conserva los datos en el volumen
docker compose down -v    # también borra el volumen de datos
```

---

## 9. Uso de la colección Postman

La colección está en `postman/Banco_XYZ_BFF.postman_collection.json`.

1. **Importar** la colección en Postman.
2. **Desactivar SSL** (servicios HTTPS con certificado autofirmado): *Settings → General → SSL certificate verification → OFF*.
3. **Orden de prueba**:
   - `0 - Batch` → dispara la carga de datos.
   - `1 - Auth` → ejecutar los **3 login** (obtienen el token OAuth 2.0 por canal).
   - `2 - Backend Core`, `3 - BFF Web`, `4 - BFF Mobile`, `5 - BFF ATM`.
   - `6 - Notificaciones` → listar los eventos de retiro consumidos desde Kafka.

---

## 10. Informe con la evidencia del proceso corriendo

A continuación se muestran las evidencias de la ejecución del sistema (capturas en la carpeta [`imgs/`](imgs/)).

### 10.1 Contenedores corriendo

El stack completo levantado con `docker compose up -d --build` (MySQL, Kafka, Authorization Server, batch, backend-core, los 3 BFFs y notificaciones).

![Contenedores corriendo](imgs/contenedores.png)

### 10.2 Autenticación por canal (OAuth 2.0)

Cada BFF obtiene su access token desde el Authorization Server.

![Login canal Web](imgs/loginWEB.png)

![Login canal Mobile](imgs/loginMOBILE.png)

![Login canal ATM](imgs/loginATM.png)

### 10.3 Carga de datos (Spring Batch)

![Carga de datos batch](imgs/postman-batch.png)

### 10.4 Backend Core

![Backend Core - listar cuentas](imgs/BackendCoreListar.png)

### 10.5 BFF Web

![Web - listar cuentas](imgs/WebListarCuentas.png)

![Web - detalle de cuenta](imgs/WebDetalleCuenta.png)

### 10.6 BFF Mobile

![Mobile - listar cuentas](imgs/MobileListarCuentas.png)

### 10.7 BFF ATM

![ATM - consulta de saldo](imgs/ATMConsultaSaldo.png)

![ATM - retiro](imgs/ATMRetiro.png)
