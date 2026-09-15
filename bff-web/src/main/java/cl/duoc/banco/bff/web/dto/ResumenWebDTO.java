package cl.duoc.banco.bff.web.dto;

import java.math.BigDecimal;

/** Resumen calculado de movimientos (totales por tipo). */
public record ResumenWebDTO(
        BigDecimal totalDepositos,
        BigDecimal totalRetiros,
        int totalMovimientos) {
}
