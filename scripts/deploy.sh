#!/usr/bin/env bash
# Despliegue completo del proyecto Banco XYZ (BFF):
# construye y levanta MySQL + proceso batch + backend-core + los 3 BFFs.
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
echo "    Backend Core  : https://localhost:8081"
echo "    BFF Web       : https://localhost:8082"
echo "    BFF Mobile    : https://localhost:8083"
echo "    BFF ATM       : https://localhost:8084"
echo "    Proceso batch : http://localhost:8080"