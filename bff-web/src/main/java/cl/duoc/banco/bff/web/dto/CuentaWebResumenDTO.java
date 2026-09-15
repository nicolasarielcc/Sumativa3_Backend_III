package cl.duoc.banco.bff.web.dto;

import java.math.BigDecimal;

/** Resumen completo de cuenta para el canal Web (listado). */
public record CuentaWebResumenDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {
}
