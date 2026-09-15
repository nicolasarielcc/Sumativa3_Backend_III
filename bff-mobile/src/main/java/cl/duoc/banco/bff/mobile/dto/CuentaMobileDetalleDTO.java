package cl.duoc.banco.bff.mobile.dto;

import java.math.BigDecimal;
import java.util.List;

/** Detalle liviano de cuenta (saldo + ultimos movimientos). */
public record CuentaMobileDetalleDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        List<MovimientoMobileDTO> ultimosMovimientos) {
}
