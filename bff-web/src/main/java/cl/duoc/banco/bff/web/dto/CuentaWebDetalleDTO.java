package cl.duoc.banco.bff.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Detalle completo de cuenta para el canal Web. */
public record CuentaWebDetalleDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo,
        List<TransaccionWebDTO> movimientos,
        ResumenWebDTO resumen) {
}
