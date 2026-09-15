package cl.duoc.banco.bff.mobile.dto;

import java.math.BigDecimal;

/** Resumen liviano de cuenta para el canal Mobile. */
public record CuentaMobileResumenDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo) {
}
