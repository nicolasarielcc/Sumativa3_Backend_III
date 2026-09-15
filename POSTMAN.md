# Guía de ejecución y pruebas con Postman

Este documento explica cómo **levantar** los servicios del proyecto **Banco XYZ (BFF)** y cómo **probarlos con la colección de Postman** incluida.

---

## 1. Requisitos previos

- Java 21
- Docker + Docker Compose
- Maven
- [Postman](https://www.postman.com/downloads/)

---

## 2. Levantar todo con Docker (opción recomendada)

Un solo comando construye y levanta **MySQL + los 5 servicios**:

```bash
docker compose up -d --build
```

Esto inicia:

| Contenedor | Servicio | Puertos |
|---|---|---|
| `banco-xyz-mysql` | MySQL 8.4 | 3307 |
| `banco-bff-batch` | Proceso batch | 8080 |
| `banco-bff-core` | Backend Core | 8081 (HTTPS) |
| `banco-bff-web` | BFF Web | 8082 (HTTPS) |
| `banco-bff-mobile` | BFF Mobile | 8083 (HTTPS) |
| `banco-bff-atm` | BFF ATM | 8084 (HTTPS) |

El batch **carga los datos automáticamente al arrancar** (`BATCH_RUN_ON_STARTUP=true`), así que no es necesario el paso manual. En los logs puedes ver:

```bash
docker logs banco-bff-batch
# ... Batch iniciado automaticamente al arrancar. Estado: COMPLETED
```

> Además, la red interna de Docker resuelve `backend-core` y `mysql` por nombre, y el certificado autofirmado incluye el SAN `backend-core` para el HTTPS interno BFF→core.

### Detener los contenedores

```bash
docker compose down        # detiene (los datos se conservan en el volumen)
docker compose down -v     # detiene y borra el volumen de datos
```

---

## 3. Levantar sin Docker (opción manual)

Requiere MySQL en `localhost:3307` (p. ej. con `docker run` o un `docker compose` con solo MySQL) y compilar localmente.

### 3.1 Compilar el proyecto

```bash
mvn clean package -DskipTests
```

### 3.2 Levantar los servicios (en orden)

Abre una terminal por servicio (o ejecútalos en segundo plano).

### 3.3 Proceso batch (carga de datos)

```bash
java -jar banco-batch/target/banco-bff-batch-1.0.0.jar
```

Una vez iniciado, disparar la carga de datos:

```bash
curl http://localhost:8080/api/batch/procesar
```

Respuesta esperada (los números pueden variar según el dataset):

```json
{ "jobId": 1, "estado": "COMPLETED", "leidos": 3000, "escritos": 1711, "omitidos": 1289 }
```

### 3.4 Backend Core

```bash
java -jar backend-core/target/banco-bff-core-1.0.0.jar
```

Disponible en `https://localhost:8081`.

### 3.5 BFFs (uno por canal)

```bash
java -jar bff-web/target/banco-bff-web-1.0.0.jar       # https://localhost:8082
java -jar bff-mobile/target/banco-bff-mobile-1.0.0.jar # https://localhost:8083
java -jar bff-atm/target/banco-bff-atm-1.0.0.jar       # https://localhost:8084
```

> Todos los servicios (excepto el batch) usan **HTTPS** con un certificado autofirmado.

---

## 5. Configurar Postman

Como los servicios usan HTTPS con certificado autofirmado, debes **desactivar la verificación SSL**:

1. Abre Postman → **Settings** (Configuración).
2. Pestaña **General**.
3. **SSL certificate verification** → **OFF**.

---

## 6. Importar la colección

1. En Postman, haz clic en **Import**.
2. Selecciona el archivo:

```
postman/Banco_XYZ_BFF.postman_collection.json
```

3. La colección incluye una variable `cuentaId` (por defecto `101`) y tres variables de token (`web_token`, `mobile_token`, `atm_token`) que se rellenan automáticamente al ejecutar los login.

---

## 7. Orden de prueba sugerido

Ejecuta las carpetas en este orden:

| # | Carpeta | Acción |
|---|---|---|
| 1 | `0 - Batch` | `Procesar datos` → carga y limpia los CSV en MySQL |
| 2 | `1 - Auth` | Ejecutar los **3 login** (guardan los tokens automáticamente) |
| 3 | `2 - Backend Core` | Listar cuentas, transacciones, movimientos y retiro |
| 4 | `3 - BFF Web` | Datos completos + resumen |
| 5 | `4 - BFF Mobile` | Datos ligeros (últimos 5 movimientos) |
| 6 | `5 - BFF ATM` | Consulta de saldo y retiro |

### Credenciales de prueba

| Canal | Usuario | Contraseña | Rol |
|---|---|---|---|
| Web | `web.admin` | `WebPass123!` | `ROLE_WEB` |
| Mobile | `mobile.user` | `MobilePass123!` | `ROLE_MOBILE` |
| ATM | `atm.terminal` | `AtmPass123!` | `ROLE_ATM` |

---

## 8. Verificación de la seguridad por canal

Puedes comprobar la autorización usando la variable de token equivocada:

| Prueba | Esperado |
|---|---|
| Sin token → cualquier endpoint protegido | `401 Unauthorized` |
| Token Web → endpoint Mobile (`/api/mobile/**`) | `403 Forbidden` |
| Token Mobile → endpoint ATM (`/api/atm/**`) | `403 Forbidden` |
| Token Web → retiro en core (`POST /api/cuentas/{id}/retiros`) | `403 Forbidden` (requiere `ROLE_ATM`) |
| Login con contraseña incorrecta | `401 Unauthorized` |

---

## 9. Script alternativo por línea de comandos

Si no usas Postman, puedes generar la evidencia de ejecución con:

```bash
./scripts/evidencia.sh
```

Este script recorre todos los endpoints con `curl -k` e imprime las respuestas.

---

## 10. Detener los servicios

- **Con Docker:** `docker compose down` (conserva datos) o `docker compose down -v` (borra datos).
- **Sin Docker:** detén los procesos Java:

```bash
ps -eo pid,args | grep "banco-bff-" | grep -v grep | awk '{print $1}' | xargs -r kill
```
