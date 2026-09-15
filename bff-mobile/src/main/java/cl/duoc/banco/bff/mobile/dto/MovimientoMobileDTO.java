package cl.duoc.banco.bff.mobile.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimiento liviano para el canal Mobile. */
public record MovimientoMobileDTO(
        LocalDate fecha,
        BigDecimal monto,
        String tipo) {
}
