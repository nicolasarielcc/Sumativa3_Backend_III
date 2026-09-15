package cl.duoc.banco.bff.common.dto.backend;

import java.math.BigDecimal;

/**
 * Cuerpo de peticion de retiro hacia backend-core.
 */
public record RetiroRequestDTO(BigDecimal monto) {
}
