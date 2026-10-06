# Instrucciones para la evidencia de ejecución

Este documento indica **qué pantallazos capturar** para evidenciar la ejecución del proyecto, organizado en tres bloques: **logs**, **Postman** y **consola de Kafka**.

> Requisito previo: tener el stack levantado con `docker compose up -d --build` y los datos cargados por el batch (estado `COMPLETED`).

---

## 1. Logs (consola de Docker)

Cada pantallazo debe mostrar el **nombre del contenedor** y el mensaje indicado.

### 1.1 Estado general del stack

```bash
docker compose ps
```

**Qué mostrar:** la tabla con los 10 contenedores (`mysql`, `zookeeper`, `kafka`, `auth-server`, `batch`, `backend-core`, los 3 BFFs y `notificaciones`) con estado `healthy` / `Up`.

### 1.2 Authorization Server (OAuth 2.0)

```bash
docker logs banco-bff-auth-server
```

**Qué mostrar:** la línea `Started AuthServerApplication` (evidencia de que el servidor OAuth 2.0 arrancó).

### 1.3 Carga de datos (Spring Batch)

```bash
docker logs banco-bff-batch
```

**Qué mostrar:** `Batch iniciado automaticamente al arrancar` y el estado `COMPLETED` con el resumen (`leidos`, `escritos`, `omitidos`).

### 1.4 Backend Core (resource server + productor Kafka)

```bash
docker logs banco-bff-core
```

**Qué mostrar:** `Started BackendCoreApplication` y que quedó escuchando en el puerto `8081` (HTTPS).

### 1.5 Un BFF (ej. ATM)

```bash
docker logs banco-bff-atm
```

**Qué mostrar:** `Started BffAtmApplication` en `8084` (HTTPS). (Opcional: capturar también `banco-bff-web` y `banco-bff-mobile`.)

### 1.6 Consumidor Kafka (notificaciones)

```bash
docker logs banco-bff-notificaciones
```

**Qué mostrar:** la línea `Evento consumido: RetiroEvent(cuentaId=..., monto=..., estado=APROBADO, fecha=...)` que aparece **después de ejecutar un retiro** (sección 2.7). Es la evidencia de la mensajería asíncrona.

---

## 2. Postman

En Postman, desactivar la verificación SSL (`Settings → General → SSL certificate verification → OFF`).

> Los tokens se capturan en la pestaña **Auth → Bearer Token** o rellenando la variable de la colección tras cada login.

### 2.1 Login de los 3 canales (OAuth 2.0)

Capturar el **cuerpo de la respuesta** de:

- `POST https://localhost:8082/api/auth/login` → token canal Web.
- `POST https://localhost:8083/api/auth/login` → token canal Mobile.
- `POST https://localhost:8084/api/auth/login` → token canal ATM.

**Qué mostrar:** el `accessToken` (JWT) y `channel` correspondiente.

### 2.2 Token desde el Authorization Server (opcional, evidencia OAuth)

```bash
curl -u bff-atm-client:atm-secret \
  -d "grant_type=client_credentials&scope=atm" \
  http://localhost:9000/oauth2/token
```

**Qué mostrar:** el JSON con `access_token`, `scope: atm` y `expires_in`. (También se puede capturar `http://localhost:9000/oauth2/jwks`.)

### 2.3 Backend Core (con Bearer token)

- `GET https://localhost:8081/api/cuentas` → listado de cuentas.
- `GET https://localhost:8081/api/cuentas/101` → detalle.

**Qué mostrar:** los datos limpios servidos por `backend-core`.

### 2.4 BFF Web

- `GET https://localhost:8082/api/web/cuentas` → datos completos.
- `GET https://localhost:8082/api/web/cuentas/101` → detalle + resumen.

### 2.5 BFF Mobile

- `GET https://localhost:8083/api/mobile/cuentas` → datos ligeros.
- `GET https://localhost:8083/api/mobile/cuentas/101` → últimos 5 movimientos.

### 2.6 BFF ATM

- `GET https://localhost:8084/api/atm/cuentas/101/saldo` → consulta de saldo.
- `POST https://localhost:8084/api/atm/cuentas/101/retiros` con body `{"monto": 500}` → retiro aprobado.

### 2.7 Notificaciones (evidencia Kafka)

- `GET http://localhost:8085/api/notificaciones` → lista de eventos de retiro consumidos.

**Qué mostrar:** el `RetiroEvent` del retiro realizado en 2.6.

### 2.8 Verificación de seguridad (401 / 403)

Capturar al menos dos casos:

- **401**: llamar un endpoint protegido **sin token**.
- **403**: usar el token Web para llamar un endpoint ATM (`/api/atm/**`), o el token Web para el retiro en core.

**Qué mostrar:** el código de estado y el mensaje de error.

---

## 3. Consola de Kafka

Todos los comandos se ejecutan **dentro del contenedor del broker**:

### 3.1 Listar tópicos

```bash
docker exec banco-xyz-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

**Qué mostrar:** la salida que incluye el tópico `retiros`.

### 3.2 Describir el tópico (particiones)

```bash
docker exec banco-xyz-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic retiros
```

**Qué mostrar:** `Topic: retiros  PartitionCount: 3  ReplicationFactor: 1`.

### 3.3 Consumir el tópico (consola)

```bash
docker exec banco-xyz-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic retiros --from-beginning
```

**Qué mostrar:** el evento `RetiroEvent` (JSON) publicado tras el retiro del punto 2.6.

### 3.4 Producir/verificar en Kafka UI (opcional)

Kafka UI en `http://localhost:8090` (si se habilita) → visualizar el tópico `retiros` y sus mensajes.

---

## Resumen de pantallazos mínimos

| # | Bloque | Pantallazo |
|---|---|---|
| 1 | Logs | `docker compose ps` (stack completo) |
| 2 | Logs | `docker logs banco-bff-batch` (COMPLETED) |
| 3 | Logs | `docker logs banco-bff-auth-server` (arrancado) |
| 4 | Logs | `docker logs banco-bff-notificaciones` (evento consumido) |
| 5 | Postman | Login de los 3 canales (tokens) |
| 6 | Postman | Backend Core + BFF Web/Mobile/ATM |
| 7 | Postman | Retiro ATM + `GET /api/notificaciones` |
| 8 | Postman | 401 y 403 (seguridad) |
| 9 | Kafka | `kafka-topics --list` y `--describe --topic retiros` |
| 10 | Kafka | `kafka-console-consumer --topic retiros --from-beginning` |
