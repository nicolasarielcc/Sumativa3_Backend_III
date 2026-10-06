#!/usr/bin/env bash
# Despliegue completo del proyecto Banco XYZ (microservicios + OAuth 2.0 + Kafka):
# construye y levanta MySQL, Kafka, Authorization Server, proceso batch,
# backend-core, los 3 BFFs y el servicio de notificaciones.
set -e

cd "$(dirname "$0")/.."

echo "==> Compilando imagenes y levantando todos los contenedores ..."
docker compose up -d --build

echo
echo "==> Estado de los contenedores:"
docker compose ps

echo
echo "==> Log del batch (carga automatica de datos):"
docker logs banco-bff-batch 2>&1 | grep -E "Batch iniciado|COMPLETED" | tail -3 || true

echo
echo "==> Despliegue completado. Endpoints:"
echo "    Authorization Server : http://localhost:9000"
echo "    Backend Core         : https://localhost:8081"
echo "    BFF Web              : https://localhost:8082"
echo "    BFF Mobile           : https://localhost:8083"
echo "    BFF ATM              : https://localhost:8084"
echo "    Notificaciones       : http://localhost:8085"
echo "    Proceso batch        : http://localhost:8080"
echo "    Kafka (host)         : localhost:29092"
