package cl.duoc.banco.bff.common.dto.backend;

import java.math.BigDecimal;

/**
 * Contrato de respuesta de backend-core (POST /api/cuentas/{id}/retiros).
 */
public record RetiroResultBackendDTO(
        Long cuentaId,
        String estado,
        BigDecimal montoRetirado,
        BigDecimal saldo) {
}
