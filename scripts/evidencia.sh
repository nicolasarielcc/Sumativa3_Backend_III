#!/usr/bin/env bash
# Evidencia de ejecucion: recorre los endpoints de los 3 canales (BFF) y del core.
# Requiere: core (:8081), bff-web (:8082), bff-mobile (:8083), bff-atm (:8084) levantados.
set -e

BASE_WEB="https://localhost:8082"
BASE_MOBILE="https://localhost:8083"
BASE_ATM="https://localhost:8084"

echo "=============================================="
echo " 1) LOGIN POR CANAL"
echo "=============================================="
WEB_TOKEN=$(curl -sk $BASE_WEB/api/auth/login -X POST -H 'Content-Type: application/json' \
  -d '{"username":"web.admin","password":"WebPass123!"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "[WEB] token obtenido"

MOBILE_TOKEN=$(curl -sk $BASE_MOBILE/api/auth/login -X POST -H 'Content-Type: application/json' \
  -d '{"username":"mobile.user","password":"MobilePass123!"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "[MOBILE] token obtenido"

ATM_TOKEN=$(curl -sk $BASE_ATM/api/auth/login -X POST -H 'Content-Type: application/json' \
  -d '{"username":"atm.terminal","password":"AtmPass123!"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "[ATM] token obtenido"

echo
echo "=============================================="
echo " 2) CANAL WEB (datos completos)"
echo "=============================================="
echo "-- Listar cuentas (resumen):"
curl -sk $BASE_WEB/api/web/cuentas -H "Authorization: Bearer $WEB_TOKEN" | python3 -m json.tool
echo "-- Detalle cuenta 101:"
curl -sk $BASE_WEB/api/web/cuentas/101 -H "Authorization: Bearer $WEB_TOKEN" | python3 -m json.tool

echo
echo "=============================================="
echo " 3) CANAL MOVIL (datos ligeros)"
echo "=============================================="
echo "-- Detalle cuenta 101 (ultimos 5 movimientos):"
curl -sk $BASE_MOBILE/api/mobile/cuentas/101 -H "Authorization: Bearer $MOBILE_TOKEN" | python3 -m json.tool

echo
echo "=============================================="
echo " 4) CANAL ATM (operaciones criticas)"
echo "=============================================="
echo "-- Consulta de saldo:"
curl -sk $BASE_ATM/api/atm/cuentas/101/saldo -H "Authorization: Bearer $ATM_TOKEN" | python3 -m json.tool
echo "-- Retiro de 500:"
curl -sk $BASE_ATM/api/atm/cuentas/101/retiros -X POST -H "Authorization: Bearer $ATM_TOKEN" \
  -H 'Content-Type: application/json' -d '{"monto":500}' | python3 -m json.tool

echo
echo "=============================================="
echo " 5) AUTORIZACION POR CANAL (debe ser 403)"
echo "=============================================="
echo -n "WEB token -> ATM saldo: "
curl -sk -o /dev/null -w "%{http_code}\n" $BASE_ATM/api/atm/cuentas/101/saldo -H "Authorization: Bearer $WEB_TOKEN"
echo -n "MOBILE token -> WEB cuentas: "
curl -sk -o /dev/null -w "%{http_code}\n" $BASE_WEB/api/web/cuentas -H "Authorization: Bearer $MOBILE_TOKEN"
echo -n "ATM token -> core retiro: "
curl -sk -o /dev/null -w "%{http_code}\n" https://localhost:8081/api/cuentas/101/retiros \
  -X POST -H "Authorization: Bearer $ATM_TOKEN" -H 'Content-Type: application/json' -d '{"monto":100}'

echo
echo "Evidencia generada correctamente."
