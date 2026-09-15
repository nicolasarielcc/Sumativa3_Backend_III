package cl.duoc.banco.bff.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimiento de cuenta en formato Web. */
public record TransaccionWebDTO(
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion) {
}
