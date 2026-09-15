package cl.duoc.banco.bff.common.dto.backend;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de respuesta de backend-core (GET /api/transacciones/cuenta/{id}).
 */
public record TransaccionBackendDTO(
        Long id,
        Long cuentaId,
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion) {
}
