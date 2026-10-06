# Guía de ejecución y pruebas con Postman

Este documento explica cómo **levantar** los servicios del proyecto **Banco XYZ** y cómo **probarlos con la colección de Postman** incluida.

---

## 1. Requisitos previos

- Java 21
- Docker + Docker Compose
- Maven
- [Postman](https://www.postman.com/downloads/)

---

## 2. Levantar todo con Docker (opción recomendada)

Un solo comando construye y levanta **MySQL + Kafka + Authorization Server + 6 servicios**:

```bash
docker compose up -d --build
```

Esto inicia:

| Contenedor | Servicio | Puertos |
|---|---|---|
| `banco-xyz-mysql` | MySQL 8.4 | 3307 |
| `banco-xyz-zookeeper` | Zookeeper | 2181 |
| `banco-xyz-kafka` | Kafka | 29092 |
| `banco-bff-auth-server` | Authorization Server (OAuth 2.0) | 9000 |
| `banco-bff-batch` | Proceso batch | 8080 |
| `banco-bff-core` | Backend Core | 8081 (HTTPS) |
| `banco-bff-web` | BFF Web | 8082 (HTTPS) |
| `banco-bff-mobile` | BFF Mobile | 8083 (HTTPS) |
| `banco-bff-atm` | BFF ATM | 8084 (HTTPS) |
| `banco-bff-notificaciones` | Notificaciones (Kafka consumer) | 8085 |

El batch **carga los datos automáticamente al arrancar** (`BATCH_RUN_ON_STARTUP=true`).

> La red interna de Docker resuelve `auth-server`, `backend-core`, `kafka` y `mysql` por nombre, y el certificado autofirmado incluye el SAN `backend-core` para el HTTPS interno BFF→core.

### Detener los contenedores

```bash
docker compose down        # detiene (los datos se conservan en el volumen)
docker compose down -v     # detiene y borra el volumen de datos
```

---

## 3. Levantar sin Docker (opción manual)

Requiere MySQL en `localhost:3307`, Kafka en `localhost:29092` y compilar localmente.

```bash
mvn clean package -DskipTests
```

Levantar en orden:

```bash
java -jar auth-server/target/banco-bff-auth-server-1.0.0.jar                # :9000
java -jar banco-batch/target/banco-bff-batch-1.0.0.jar                      # :8080
java -jar backend-core/target/banco-bff-core-1.0.0.jar                      # :8081 (HTTPS)
java -jar bff-web/target/banco-bff-web-1.0.0.jar                            # :8082 (HTTPS)
java -jar bff-mobile/target/banco-bff-mobile-1.0.0.jar                      # :8083 (HTTPS)
java -jar bff-atm/target/banco-bff-atm-1.0.0.jar                            # :8084 (HTTPS)
java -jar notificaciones-service/target/banco-bff-notificaciones-1.0.0.jar  # :8085
```

---

## 4. Flujo OAuth 2.0 (client credentials)

El login de cada canal **delega en el Authorization Server**. Internamente el BFF solicita un token con sus credenciales de cliente:

```bash
curl -u bff-atm-client:atm-secret \
  -d "grant_type=client_credentials&scope=atm" \
  http://localhost:9000/oauth2/token
```

Respuesta (el access token lleva el claim `authorities=["ROLE_ATM"]`):

```json
{ "access_token": "...", "token_type": "Bearer", "expires_in": 299, "scope": "atm" }
```

| Canal | client_id | client_secret | Rol |
|---|---|---|---|
| Web | `bff-web-client` | `web-secret` | `ROLE_WEB` |
| Mobile | `bff-mobile-client` | `mobile-secret` | `ROLE_MOBILE` |
| ATM | `bff-atm-client` | `atm-secret` | `ROLE_ATM` |

---

## 5. Configurar Postman

Como los servicios usan HTTPS con certificado autofirmado, desactiva la verificación SSL:

1. Postman → **Settings** → **General**.
2. **SSL certificate verification** → **OFF**.

---

## 6. Importar la colección

Importa `postman/Banco_XYZ_BFF.postman_collection.json`. Incluye una variable `cuentaId` (por defecto `101`) y tres variables de token (`web_token`, `mobile_token`, `atm_token`) que se rellenan al ejecutar los login.

---

## 7. Orden de prueba sugerido

| # | Carpeta | Acción |
|---|---|---|
| 1 | `0 - Batch` | `Procesar datos` → limpia y carga los CSV en MySQL |
| 2 | `1 - Auth` | Ejecutar los **3 login** (obtienen token OAuth 2.0 por canal) |
| 3 | `2 - Backend Core` | Listar cuentas, transacciones, movimientos y retiro |
| 4 | `3 - BFF Web` | Datos completos + resumen |
| 5 | `4 - BFF Mobile` | Datos ligeros (últimos 5 movimientos) |
| 6 | `5 - BFF ATM` | Consulta de saldo y retiro |
| 7 | `6 - Notificaciones` | Eventos de retiro consumidos desde Kafka |

---

## 8. Verificación de la seguridad por canal

| Prueba | Esperado |
|---|---|
| Sin token → cualquier endpoint protegido | `401 Unauthorized` |
| Token Web → endpoint Mobile (`/api/mobile/**`) | `403 Forbidden` |
| Token Mobile → endpoint ATM (`/api/atm/**`) | `403 Forbidden` |
| Token Web → retiro en core (`POST /api/cuentas/{id}/retiros`) | `403 Forbidden` (requiere `ROLE_ATM`) |
| client_secret incorrecto en el token endpoint | `401 Unauthorized` |

---

## 9. Verificación de la mensajería asíncrona (Kafka)

1. Ejecuta un **retiro** en `5 - BFF ATM`.
2. Consulta `GET http://localhost:8085/api/notificaciones`: debería aparecer el evento de retiro consumido de Kafka.

---

## 10. Script alternativo por línea de comandos

```bash
./scripts/evidencia.sh
```

---

## 11. Detener los servicios

- **Con Docker:** `docker compose down` (conserva datos) o `docker compose down -v` (borra datos).
- **Sin Docker:** detén los procesos Java:

```bash
ps -eo pid,args | grep "banco-bff-\|banco-xyz" | grep -v grep | awk '{print $1}' | xargs -r kill
```
