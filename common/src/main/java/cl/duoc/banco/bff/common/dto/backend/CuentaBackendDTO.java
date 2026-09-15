package cl.duoc.banco.bff.common.dto.backend;

import java.math.BigDecimal;

/**
 * Contrato de respuesta de backend-core (GET /api/cuentas y /api/cuentas/{id}).
 */
public record CuentaBackendDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {
}
